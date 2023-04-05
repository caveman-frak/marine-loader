package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import uk.co.bluegecko.marine.loader.downloader.back4app.city.City.Country;
import uk.co.bluegecko.marine.loader.downloader.back4app.city.City.Location;

class CityTest {

	private static final String CITIES = """
			  {
			       "results": [
			         {
			           "adminCode": "04",
			           "cityId": 3040132,
			           "country": {
			             "code": "AD"
			           },
			           "location": {
			             "latitude": 42.54499,
			             "longitude": 1.51483
			           },
			           "name": "la Massana",
			           "population": 7211
			         }
			       ]
			  }
			""";

	@Test
	void testDeserialise() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(CITIES);

		Iterator<JsonNode> results = node.get("results").elements();
		City city = mapper.treeToValue(results.next(), City.class);

		assertThat(city)
				.isEqualTo(City.builder()
						.id(3040132)
						.adminCode("04")
						.country(new Country("AD"))
						.location(new Location(42.54499, 1.51483))
						.name("la Massana")
						.population(7211)
						.build());
	}

	@Test
	void testAsStrings() {
		assertThat(City.builder()
				.id(3040132)
				.adminCode("04")
				.country(new Country("AD"))
				.location(new Location(42.54499, 1.51483))
				.name("la Massana")
				.population(7211)
				.build().asStrings())
				.isEqualTo(new String[]{"3040132", "la Massana", "04", "42.54499", "1.51483", "7211", "AD"});
	}

}