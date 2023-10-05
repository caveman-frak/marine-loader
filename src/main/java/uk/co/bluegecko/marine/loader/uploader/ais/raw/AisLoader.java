package uk.co.bluegecko.marine.loader.uploader.ais.raw;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Connection;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Feed;

@Slf4j
@Value
@Component
@ConditionalOnProperty(prefix = "ais", name = "enabled", havingValue = "true")
public class AisLoader implements ApplicationRunner {

	AisProperties properties;

	public void run(ApplicationArguments args) {
		log.info("AIS Properties: {}", properties);

		properties.feeds().forEach(AisLoader::connectToFeed);
	}

	private static void connectToFeed(Feed feed) {
		Connection connection = feed.connection();
		log.info("Connecting to {}", connection);

		try (SocketChannel channel = SocketChannel.open()) {
			channel.configureBlocking(true);
			channel.connect(new InetSocketAddress(connection.host(), connection.port()));
			log.info(channel.isConnected() ? "Connected" : "Failed to connect");
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			StringBuilder builder = new StringBuilder(1024);
			int count = 0;

			while (channel.read(buffer) != -1) {
				if (buffer.position() > 0) {
					buffer.flip();
					while (buffer.hasRemaining()) {
						char ch = (char) buffer.get();
						if (Character.isWhitespace(ch)) {
							if (!builder.isEmpty()) {
								log.info("{} {} Read: '{}'", feed.id(), String.format("%03d", count++),
										builder);
								builder.setLength(0);
							}
						} else {
							builder.append(ch);
						}
					}
					buffer.clear();
					if (count > 100) {
						break;
					}
				}
			}
		} catch (IOException ex) {
			log.error("Unable to connect to {} because {}", connection, ex.getMessage());
		}
	}

}