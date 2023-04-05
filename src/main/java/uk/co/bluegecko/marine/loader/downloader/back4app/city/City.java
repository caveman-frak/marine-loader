package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record City(@JsonProperty("cityId") long id, String name, String adminCode, Location location, long population,
                   Country country) {

	public record Location(double latitude, double longitude) {

	}

	public record Country(String code) {

	}

	public String[] asStrings() {
		return new String[]{
				String.valueOf(id),
				name,
				adminCode,
				String.valueOf(location.latitude),
				String.valueOf(location.longitude),
				String.valueOf(population),
				country.code};
	}

	public static String[] asHeaders() {
		return new String[]{"id", "name", "adminCode", "latitude", "longitude", "population", "country"};
	}


}