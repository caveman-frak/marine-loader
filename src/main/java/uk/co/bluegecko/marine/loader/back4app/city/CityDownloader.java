package uk.co.bluegecko.marine.loader.back4app.city;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uk.co.bluegecko.marine.loader.back4app.core.Back4AppProperties;
import uk.co.bluegecko.marine.loader.back4app.core.Back4AppProperties.Connection;

/**
 * Download missing city data from Back4App.
 */
@Slf4j
@Value
@Component
public class CityDownloader implements ApplicationRunner {

	Back4AppProperties properties;

	@SuppressWarnings("SpellCheckingInspection")
	@Override
	public void run(ApplicationArguments args) {
		final Connection connection = properties.connection();
		log.debug("Connecting to {}://{} using ID: {} and token: {}",
				connection.scheme(), connection.host(),
				properties.application().id(), properties.application().key());

		ObjectMapper mapper = new ObjectMapper();
		List<String> missingCities =
				List.of(
						"FK:Stanley",
						"GS:King Edward Point",
						"IO:Diego Garcia",
						"MM:Naypyidaw",
						"PA:Panama City",
						"PW:Ngerulmud",
						"SH:Jamestown",
						"TK:Fakaofo");

		try (ICSVWriter writer =
				new CSVWriterBuilder(new FileWriter("build/missing-cities.csv"))
						.withQuoteChar('"')
						.withSeparator(',')
						.build()) {

			writer.writeNext(new String[]{"id", "name", "adminCode", "latitude", "longitude", "population", "country"});

			for (String missing : missingCities) {
				var s = missing.split(":");
				String missingCountry = s[0];
				String missingCity = s[1];

				URL url = UriComponentsBuilder.newInstance()
						.scheme(connection.scheme())
						.host(connection.host())
						.path(connection.path("city"))
						.queryParam("skip", 0)
						.queryParam("limit", 10)
						.queryParam("include", "country")
						.queryParam(
								"keys",
								"name,country,country.code,population,location,cityId,adminCode")
						.queryParam("where", String.format("{ \"name\": \"%s\" }", missingCity))
						.build()
						.encode(StandardCharsets.UTF_8)
						.toUri()
						.toURL();

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestProperty("X-Parse-Application-Id", properties.application().id());
				urlConnection.setRequestProperty("X-Parse-REST-API-Key", properties.application().key());
				try (BufferedReader reader =
						new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
					JsonNode node = mapper.readTree(reader);

					boolean found = false;
					Iterator<JsonNode> results = node.get("results").elements();
					while (results.hasNext()) {
						JsonNode city = results.next();

						long id = city.get("cityId").asLong();
						String name = city.get("name").asText();
						String adminCode = city.get("adminCode").asText();
						JsonNode location = city.get("location");
						double latitude = location.get("latitude").asDouble();
						double longitude = location.get("longitude").asDouble();
						long population = city.get("population").asLong();
						String country = city.get("country").get("code").asText();

						if (!found && missingCountry.equals(country)) {
							log.info("Writing {} / {}", name, country);
							writer.writeNext(
									new String[]{
											String.valueOf(id),
											name,
											adminCode,
											String.valueOf(latitude),
											String.valueOf(longitude),
											String.valueOf(population),
											country
									},
									false);
							found = true;
						} else if (found) {
							log.info("Skipping {} / {}, already found a matching city", name,
									country);
						} else {
							log.info("Skipping {} / {}, wanted country {}", name, country,
									missingCountry);
						}
					}
					if (!found) {
						log.warn("No city found for {} / {}", missingCity, missingCountry);
					}
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}