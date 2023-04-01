package uk.co.bluegecko.marine.loader.back4app.core;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "back4app")
public record Back4AppProperties(Connection connection, Application application) {

	//	@ConfigurationProperties(prefix = "connection")
	public record Connection(String scheme, String host, Map<String, String> path) {

		public String path(String type) {
			return path.get(type);
		}

	}

	//	@ConfigurationProperties(prefix = "back4app.application")
	public record Application(String id, String key) {

	}

}