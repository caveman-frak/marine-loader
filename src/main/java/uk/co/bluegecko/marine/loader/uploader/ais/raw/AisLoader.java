package uk.co.bluegecko.marine.loader.uploader.ais.raw;

import static java.net.StandardProtocolFamily.INET;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Connection;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Feed;

@Slf4j
@Data
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "ais", name = "enabled", havingValue = "true")
public class AisLoader implements ApplicationRunner {

	private static final int CAPACITY = 1024;

	private final AisProperties properties;
	private final TaskScheduler scheduler;
	private short running = 0;

	public void run(ApplicationArguments args) {
		log.info("AIS Properties: {}", properties);

		SelectorProvider provider = SelectorProvider.provider();
		try (final Selector selector = provider.openSelector()) {
			for (Feed feed : properties.feeds()) {
				if (feed.enabled()) {
					connectToFeed(feed, selector);
					running++;
				}
			}

			while (isRunning()) {
				if (selector.select() > 0) {
					selector.selectedKeys().removeIf(this::processChannel);
				}
			}
		} catch (IOException ex) {
			log.error("Error while processing selector: {}", ex.getMessage());
		}
	}

	private void connectToFeed(Feed feed, Selector selector) throws IOException {
		Connection connection = feed.connection();
		log.info("Connecting to {}", connection);

		SelectableChannel channel = open(selector.provider(), connection)
				.configureBlocking(false);
		connect(channel, new InetSocketAddress(connection.host(), connection.port()))
				.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ, feed);
	}

	private SelectableChannel open(SelectorProvider provider, Connection connection) throws IOException {
		return switch (connection.protocol()) {
			case TCP -> provider.openSocketChannel(INET);
			case UDP -> provider.openDatagramChannel(INET);
		};
	}

	private SelectableChannel connect(SelectableChannel channel, InetSocketAddress address) throws IOException {
		if (channel instanceof SocketChannel socket) {
			socket.connect(address);
		} else if (channel instanceof DatagramChannel datagram) {
			datagram.connect(address);
		} else if (channel instanceof ServerSocketChannel server) {
			server.bind(address);
		}
		return channel;
	}

	private boolean processChannel(SelectionKey key) {
		try {
			if (key.isConnectable()) {
				SocketChannel channel = (SocketChannel) key.channel();
				if (channel.isConnectionPending()) {
					log.info("Channel waiting to connect to {}", channel.getRemoteAddress());
					channel.finishConnect();
				} else if (channel.isConnected()) {
					log.info("Channel connected to {}", channel.getRemoteAddress());
				} else {
					log.info("Channel disconnected from {}", channel.getRemoteAddress());
				}
			} else if (key.isReadable()) {
				processMessage((Feed) key.attachment(), (SocketChannel) key.channel());
			}
			return true;
		} catch (IOException ex) {
			log.error("Error while processing channels: {}", ex.getMessage());
			return false;
		}
	}

	private void processMessage(Feed feed, ByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
		StringBuilder builder = new StringBuilder(CAPACITY);

		while (isRunning() && channel.read(buffer) > 0) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				char ch = (char) buffer.get();
				if (Character.isWhitespace(ch)) {
					if (!builder.isEmpty()) {
						writeMessage(feed, builder.toString());
						builder.setLength(0);
					}
				} else {
					builder.append(ch);
				}
			}
			buffer.clear();
		}
	}

	private void writeMessage(Feed feed, String message) {
		log.info("{} Read: '{}'", feed.id(), message);
	}

	private boolean isRunning() {
		return running > 0;
	}

	@Bean
	public ApplicationListener<ContextClosedEvent> registerFeedMonitor() {
		return e -> running = 0;
	}

}