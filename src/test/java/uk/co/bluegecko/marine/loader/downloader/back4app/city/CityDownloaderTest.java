package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.co.bluegecko.marine.loader.downloader.back4app.city.City.Country;
import uk.co.bluegecko.marine.loader.downloader.back4app.city.City.Location;
import uk.co.bluegecko.marine.loader.downloader.back4app.core.Back4AppProperties;
import uk.co.bluegecko.marine.loader.downloader.back4app.core.Back4AppProperties.Connection;
import uk.co.bluegecko.marine.shared.test.base.MapperTest;

@SpringJUnitConfig
@TestPropertySource(properties = "marine.back4app.enabled=true")
@Import({CityDownloader.class})
class CityDownloaderTest extends MapperTest {

	private static final String CITIES = """
			  {
			       "count": 1,
			       "results": [
			         {
			           "objectId": "USzGpPyQYE",
			           "adminCode": "04",
			           "cityId": 3040132,
			           "country": {
			             "objectId": "sv7fjDVISU",
			             "code": "AD",
			             "__type": "Object",
			             "className": "Country"
			           },
			           "location": {
			             "__type": "GeoPoint",
			             "latitude": 42.54499,
			             "longitude": 1.51483
			           },
			           "name": "la Massana",
			           "population": 7211,
			           "createdAt": "2019-12-09T21:04:56.736Z",
			           "updatedAt": "2019-12-09T21:04:56.736Z"
			         }
			       ]
			  }
			""";

	@MockitoBean
	Back4AppProperties properties;
	@Autowired
	CityDownloader cityDownloader;
	City city;

	@BeforeEach
	void set() {
		Connection connection = new Connection("http", "localhost", Map.of("city", "/city"));
		when(properties.connection()).thenReturn(connection);

		city = City.builder()
				.id(3040132)
				.adminCode("04")
				.country(new Country("AD"))
				.location(new Location(42.54499, 1.51483))
				.name("la Massana")
				.population(7211)
				.build();
	}

	@Test
	void urlBuilder() throws MalformedURLException {
		assertThat(cityDownloader.buildCityUrl(properties.connection(), "London").toExternalForm())
				.startsWith("http://localhost/city?skip=0&limit=0&include=country&order=-population&count=1")
				.contains("&keys=name,country,country.code,population,location,cityId,adminCode")
				.endsWith("&where=%7B%20%22name%22:%20%22London%22%20%7D");
	}

	@Test
	void extractCities() throws IOException {
		StringReader reader = new StringReader(CITIES);
		assertThat(cityDownloader.extractCities(jsonMapper(), reader, "London", "GB"))
				.hasSize(1).contains(city);
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			la Massana, AD, true
			la Massana, AB, false
			London,     AD, false
			""")
	void processResult(String missingCity, String missingCountry, boolean expected) throws IOException {
		assertThat(cityDownloader.processResults(List.of(city), missingCity, missingCountry).isPresent())
				.isEqualTo(expected);
	}

}