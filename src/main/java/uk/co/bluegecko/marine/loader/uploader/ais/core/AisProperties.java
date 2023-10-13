package uk.co.bluegecko.marine.loader.uploader.ais.core;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ais")
public record AisProperties(boolean enabled, List<Feed> feeds) {

	public record Feed(String id, @DefaultValue("true") boolean enabled, Connection connection, List<String> tags) {

	}

	public enum Protocol {TCP, UDP}

	public record Connection(String host, int port, @DefaultValue("TCP") Protocol protocol,
	                         @DefaultValue("false") boolean listen) {

	}
}