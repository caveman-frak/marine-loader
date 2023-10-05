package uk.co.bluegecko.marine.loader.downloader.back4app.core;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;


@ConfigurationProperties(prefix = "back4app")
public record Back4AppProperties(boolean enabled,
                                 Connection connection,
                                 Application application,
                                 @DefaultValue("10") int limit) {

	public record Connection(String scheme, String host, Map<String, String> path) {

		public String path(String type) {
			return path.get(type);
		}
	}

	public record Application(String id, String key) {

	}

}