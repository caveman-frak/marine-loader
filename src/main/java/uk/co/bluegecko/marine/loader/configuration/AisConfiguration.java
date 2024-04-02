package uk.co.bluegecko.marine.loader.configuration;

import dk.dma.ais.sentence.Vdm;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AisConfiguration {

	@Bean
	public Vdm vdm() {
		return new Vdm();
	}

	@Bean
	public SpatialContextFactory spatialContextFactory() {
		return new SpatialContextFactory();
	}

	@Bean
	public SpatialContext spatialContext(SpatialContextFactory factory) {
		return factory.newSpatialContext();
	}

}