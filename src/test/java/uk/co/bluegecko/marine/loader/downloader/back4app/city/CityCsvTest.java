package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.loader.downloader.back4app.city.CityCsv.beanWriter;

import java.io.StringWriter;
import org.beanio.BeanWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.bluegecko.marine.loader.downloader.back4app.city.City.Country;
import uk.co.bluegecko.marine.loader.downloader.back4app.city.City.Location;

class CityCsvTest {

	City city;

	@BeforeEach
	void set() {
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
	void export() {
		StringWriter writer = new StringWriter();

		try (BeanWriter out = beanWriter(writer)) {
			out.write(CityCsv.export(city));
		}

		assertThat(writer.toString()).contains("""
				id,name,adminCode,latitude,longitude,population,country
				3040132,la Massana,04,42.54499,1.51483,7211,AD
				""");

	}

}