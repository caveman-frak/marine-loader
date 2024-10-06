package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record City(@JsonProperty("cityId") long id, String name, String adminCode, Location location, long population,
                   Country country) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Location(double latitude, double longitude) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Country(String code) {

	}

}