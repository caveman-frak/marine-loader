package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import static uk.co.bluegecko.marine.loader.downloader.back4app.city.CityCsv.beanWriter;
import static uk.co.bluegecko.marine.shared.utility.function.QuietFunctions.quietFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.beanio.BeanWriter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uk.co.bluegecko.marine.loader.downloader.back4app.core.Back4AppProperties;
import uk.co.bluegecko.marine.loader.downloader.back4app.core.Back4AppProperties.Application;
import uk.co.bluegecko.marine.loader.downloader.back4app.core.Back4AppProperties.Connection;

/**
 * Download missing city data from Back4App.
 */
@Slf4j
@Value
@Component
@ConditionalOnProperty(prefix = "marine.back4app", name = "enabled", havingValue = "true")
public class CityDownloader implements ApplicationRunner {

	Back4AppProperties properties;

	@SuppressWarnings("SpellCheckingInspection")
	@Override
	public void run(ApplicationArguments args) {
		final Connection connection = properties.connection();
		final Application application = properties.application();
		log.info("Connecting to {}://{} with limit {}, using ID: {} and token: {}",
				connection.scheme(), connection.host(),
				properties.limit(), application.id(), application.key());

		ObjectMapper mapper = new ObjectMapper();
		List<String> missingEntries =
				List.of(
						"GB:London",
						"FK:Stanley",
						"GS:King Edward Point",
						"IO:Diego Garcia",
						"MM:Naypyidaw",
						"PA:Panama City",
						"PW:Ngerulmud",
						"SH:Jamestown",
						"TK:Fakaofo");

		try (BeanWriter writer = beanWriter(new FileWriter(properties.output()))) {
			for (String missing : missingEntries) {
				var s = missing.split(":");
				String missingCountry = s[0];
				String missingCity = s[1];

				URL url = buildCityUrl(connection, missingCity);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestProperty("X-Parse-Application-Id", application.id());
				urlConnection.setRequestProperty("X-Parse-REST-API-Key", application.key());

				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream()))) {
					List<City> cities = extractCities(mapper, reader, missingCity, missingCountry);

					processResults(cities, missingCity, missingCountry).ifPresent(writer::write);
				}
			}
		} catch (IOException ex) {
			log.error("Failed to process cities", ex);
		}
	}

	List<City> extractCities(ObjectMapper mapper, Reader reader, String missingCity, String missingCountry)
			throws IOException {
		JsonNode node = mapper.readTree(reader);

		int count = node.get("count").asInt();
		if (count >= properties.limit()) {
			log.warn("Correct city ({}) for {} may be missed, as only {}/{} results returned",
					missingCity, missingCountry, properties.limit(), count);
		}

		return StreamSupport.stream(node.get("results").spliterator(), false)
				.map(quietFunction(n -> mapper.treeToValue(n, City.class))).toList();
	}

	Optional<City> processResults(List<City> cities, String missingCity, String missingCountry) throws IOException {
		boolean found = false;
		City result = null;
		for (City city : cities) {
			if (missingCity.equals(city.name()) && missingCountry.equals(city.country().code())) {
				if (!found) {
					log.info("Writing {} / {}", city.name(), city.country().code());
					result = city;
					found = true;
				} else {
					log.debug("Skipping {} / {}, already found a matching entry", city.name(), city.country().code());
				}
			} else if (missingCity.equals(city.name())) {
				log.debug("Skipping {} / {}, country should be {}", city.name(), city.country().code(), missingCountry);
			} else if (missingCountry.equals(city.country().code())) {
				log.debug("Skipping {} / {}, city should be {}", city.name(), city.country().code(), missingCity);
			} else {
				log.debug("Skipping {} / {}, looking for {} / {}",
						city.name(), city.country().code(), missingCity, missingCountry);
			}

			if (!found) {
				log.warn("No city found for {} / {}", missingCity, missingCountry);
			}
		}
		return Optional.ofNullable(result);
	}

	URL buildCityUrl(Connection connection, String missingCity) throws MalformedURLException {
		return UriComponentsBuilder.newInstance()
				.scheme(connection.scheme())
				.host(connection.host())
				.path(connection.path("city"))
				.queryParam("skip", 0)
				.queryParam("limit", properties.limit())
				.queryParam("include", "country")
				.queryParam("order", "-population")
				.queryParam("count", 1)
				.queryParam("keys",
						"name,country,country.code,population,location,cityId,adminCode")
				.queryParam("where", String.format("{ \"name\": \"%s\" }", missingCity))
				.build()
				.encode(StandardCharsets.UTF_8)
				.toUri()
				.toURL();
	}

}