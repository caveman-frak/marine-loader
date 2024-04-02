package uk.co.bluegecko.marine.loader.uploader.ais.raw;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.sentence.SentenceException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Connection;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Feed;
import uk.co.bluegecko.marine.loader.uploader.ais.processed.AisMessageHandler;
import uk.co.bluegecko.marine.shared.configuration.ExecutorConfiguration;

/**
 * Load AIS data from a TCP or UDP source.
 * <p/>
 * {@code TCP: netcat -v --send-only -l 127.0.0.1 10000 < loader/src/test/resources/ais/feed/norway-ais.txt}<p/>
 * {@code UDP: netcat -vu -p 10001 ::1 10000 < loader/src/test/resources/ais/feed/norway-ais.txt}
 */
@Slf4j
@Data
@Component
@Import({ExecutorConfiguration.class})
@ConditionalOnProperty(prefix = "marine.ais", name = "enabled", havingValue = "true")
public class AisLoader implements ApplicationRunner {

	private static final int CAPACITY = 1024;

	private final RawMessageHandler rawMessageHandler;
	private final AisMessageHandler aisMessageHandler;
	private final AisProperties properties;
	private final ExecutorService executor;
	private final Queue<Future<Optional<ChannelFeed>>> pending;
	private final SelectorProvider provider;
	private final AtomicInteger running;
	private final int maxRetries;
	private final int retryInterval;

	public AisLoader(
			final RawMessageHandler rawMessageHandler,
			final AisMessageHandler aisMessageHandler,
			final AisProperties properties,
			final ExecutorService executor,
			@Value("${marine.loader.retry.max:3}") final int maxRetries,
			@Value("${marine.loader.retry.interval:10}") final int retryInterval) {
		this.rawMessageHandler = rawMessageHandler;
		this.aisMessageHandler = aisMessageHandler;
		this.properties = properties;
		this.executor = executor;
		this.maxRetries = maxRetries;
		this.retryInterval = retryInterval;
		pending = new ConcurrentLinkedQueue<>();
		provider = SelectorProvider.provider();
		running = new AtomicInteger();

		log.info("Retry interval = {}s, max attempts = {}", retryInterval, maxRetries);
	}

	public void run(ApplicationArguments args) {
		try (final Selector selector = provider.openSelector()) {
			for (Feed feed : properties.feeds()) {
				if (feed.enabled()) {
					tryConnect(feed);
					running.getAndIncrement();
				}
			}
			while (isRunning()) {
				processPending(selector);
				selector.selectNow(this::processChannel);
			}
		} catch (IOException ex) {
			log.error("Error while processing selector: {}", ex.getMessage());
		}
		log.info("Exiting ...");
	}

	private void processPending(Selector selector) {
		var iterator = pending.iterator();
		while (iterator.hasNext()) {
			var future = iterator.next();
			if (future.isDone()) {
				future.resultNow().ifPresent(channelFeed -> registerChannel(channelFeed, selector));
				iterator.remove();
			}
		}
	}

	private void tryConnect(Feed feed) {
		pending.add(getExecutor().submit(() ->
		{
			log.info("Attempting to connect to {} at {}", feed.id(), feed.connection());
			int count = 0;
			while (count < maxRetries) {
				Optional<ChannelFeed> result = retryConnect(feed, ++count);
				if (result.isPresent()) {
					return result;
				}
				log.info("Attempt {} failed to connect", count);
			}
			log.warn("Max retries ({}) exceeded, aborting connection for {}", maxRetries, feed.id());
			running.getAndDecrement();
			return Optional.empty();
		}));
	}

	private Optional<ChannelFeed> retryConnect(Feed feed, int count) throws InterruptedException {
		try {
			Duration duration = Duration.ofSeconds((long) Math.pow(retryInterval, count));
			log.debug("Waiting {} before connection attempt {}", duration, count);
			Thread.sleep(duration);
			SelectableChannel channel = openChannel(feed.connection());
			channel.configureBlocking(false);
			return Optional.of(new ChannelFeed(channel, feed));
		} catch (IOException ex) {
			return Optional.empty();
		}
	}

	private SelectableChannel openChannel(Connection connection) throws IOException {
		switch (connection.protocol()) {
			case TCP -> {
				SocketChannel channel = provider.openSocketChannel();
				boolean result = channel.connect(new InetSocketAddress(connection.host(), connection.port()));
				if (result) {
					log.info("TCP channel connected to {}", channel.getRemoteAddress());
				}
				return channel;
			}
			case UDP -> {
				DatagramChannel channel = provider.openDatagramChannel();
				channel.bind(new InetSocketAddress(connection.port()));
				channel.connect(new InetSocketAddress(connection.port() + 1));
				if (channel.isConnected()) {
					log.info("UDP channel listening on {} / {}", channel.getLocalAddress(),
							channel.getRemoteAddress());
				}
				return channel;
			}
		}
		throw new IllegalArgumentException("Connection protocol {} not supported");
	}

	private void registerChannel(ChannelFeed channelFeed, Selector selector) {
		try {
			channelFeed.channel().register(selector, SelectionKey.OP_READ, channelFeed.feed());
			log.debug("Registered channel for {}", channelFeed.feed().connection());
		} catch (ClosedChannelException ex) {
			log.error("Unable to register channel: {}", ex.getMessage());
		}
	}


	private void processChannel(SelectionKey key) {
		try {
			if (key.isReadable()) {
				processMessage((Feed) key.attachment(), (ByteChannel) key.channel());
			}
		} catch (IOException ex) {
			log.debug("Error while processing channels: {}", ex.getMessage());
		}
	}

	private void processMessage(Feed feed, ByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
		StringBuilder builder = new StringBuilder(CAPACITY);

		while (isRunning()) {
			int result = channel.read(buffer);
			if (result > 0) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					char ch = (char) buffer.get();
					if (Character.isWhitespace(ch)) {
						if (!builder.isEmpty()) {
							readMessage(feed, builder.toString());
							builder.setLength(0);
						}
					} else {
						builder.append(ch);
					}
				}
				buffer.clear();
			} else if (result == -1) {
				log.info("Connection to {} was lost", feed.id());
				channel.close();
				tryConnect(feed);
			}
		}
	}

	private void readMessage(Feed feed, String message) {
		try {
			getRawMessageHandler().handleMessage(feed.id(), message).ifPresent(m ->
					getAisMessageHandler().handleMessage(feed, m));
		} catch (SentenceException | AisMessageException | SixbitException ex) {
			log.info("Unhandled message '{}' due to {}", message, ex.getMessage());
		}
	}

	private boolean isRunning() {
		return running.get() > 0;
	}

	@Bean
	public ApplicationListener<ContextClosedEvent> registerFeedMonitor() {
		return e -> running.set(0);
	}

	private record ChannelFeed(SelectableChannel channel, Feed feed) {

	}

}