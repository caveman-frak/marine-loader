package uk.co.bluegecko.marine.loader.uploader.ais.raw;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
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
import uk.co.bluegecko.marine.shared.configuration.ExecutorConfiguration;

@Slf4j
@Data
@Component
@Import({ExecutorConfiguration.class})
@ConditionalOnProperty(prefix = "ais", name = "enabled", havingValue = "true")
public class AisLoader implements ApplicationRunner {

	private static final int CAPACITY = 1024;

	private final AisProperties properties;
	private final ExecutorService executor;
	private final Queue<Future<Optional<ChannelFeed>>> pending;
	private final SelectorProvider provider;
	private final AtomicInteger running;
	private final int maxRetries;
	private final int retryInterval;

	public AisLoader(
			AisProperties properties,
			ExecutorService executor,
			@Value("${marine.loader.retry.max:3}")
			int maxRetries,
			@Value("${marine.loader.retry.interval:10}")
			int retryInterval) {
		this.properties = properties;
		this.executor = executor;
		this.maxRetries = maxRetries;
		this.retryInterval = retryInterval;
		pending = new ConcurrentLinkedQueue<>();
		provider = SelectorProvider.provider();
		running = new AtomicInteger();
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

	private static void registerChannel(ChannelFeed channelFeed, Selector selector) {
		try {
			channelFeed.channel().register(selector, SelectionKey.OP_READ, channelFeed.feed());
			log.debug("Registered channel for {}", channelFeed.feed().connection());
		} catch (ClosedChannelException ex) {
			log.error("Unable to register channel: {}", ex.getMessage());
		}
	}


	private void tryConnect(Feed feed) {
		pending.add(getExecutor().submit(() ->
		{
			log.info("Attempting to connect to {} at {}", feed.id(), feed.connection());
			int count = 0;
			while (count < maxRetries) {
				Optional<ChannelFeed> result = retryConnect(feed, count++);
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
			Connection connection = feed.connection();
			SocketChannel channel = provider.openSocketChannel();
			channel.connect(new InetSocketAddress(connection.host(), connection.port()));
			channel.configureBlocking(false);
			log.info("Channel connected to {}", channel.getRemoteAddress());
			return Optional.of(new ChannelFeed(channel, feed));
		} catch (IOException ex) {
			return Optional.empty();
		}
	}

	private void processChannel(SelectionKey key) {
		try {
			if (key.isReadable()) {
				processMessage((Feed) key.attachment(), (SocketChannel) key.channel());
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
		log.info("{} Read: '{}'", feed.id(), message);
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