package uk.co.bluegecko.marine.loader.downloader.back4app.city;

import java.io.Writer;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.beanio.BeanWriter;
import org.beanio.StreamFactory;
import org.beanio.builder.FieldBuilder;
import org.beanio.builder.RecordBuilder;
import org.beanio.builder.StreamBuilder;

public class CityCsv {

	static final String HEADER = "Header";
	static final String STREAM = "Cities";
	static final String RECORD = "City";
	static final String FORMAT = "csv";

	static BeanWriter beanWriter(Writer writer) {
		BeanWriter beanWriter = streamFactory().createWriter(STREAM, writer);
		beanWriter.write(HEADER, null);
		return beanWriter;
	}

	static StreamFactory streamFactory() {
		StreamFactory factory = StreamFactory.newInstance();
		factory.define(streamBuilder());
		return factory;
	}

	static StreamBuilder streamBuilder() {
		return new StreamBuilder(STREAM, FORMAT)
				.addRecord(recordBuilder())
				.addRecord(recordHeader())
				.writeOnly();
	}

	static RecordBuilder recordBuilder() {
		RecordBuilder builder = new RecordBuilder(RECORD, Csv.class);
		Arrays.stream(Csv.Fields.values()).map(Enum::name)
				.forEach(f -> builder.addField(new FieldBuilder(f)));
		return builder;
	}

	static RecordBuilder recordHeader() {
		RecordBuilder builder = new RecordBuilder(HEADER);
		Arrays.stream(Csv.Fields.values()).map(Enum::name)
				.forEach(f -> builder.addField(new FieldBuilder(f).defaultValue(f)));
		return builder;
	}

	@Data
	@Builder
	@FieldNameConstants(asEnum = true, level = AccessLevel.PROTECTED)
	public static class Csv {

		long id;
		String name;
		String adminCode;
		double latitude;
		double longitude;
		long population;
		String country;

	}

	public static Csv export(City city) {
		return Csv.builder()
				.id(city.id())
				.name(city.name())
				.adminCode(city.adminCode())
				.latitude(city.location().latitude())
				.longitude(city.location().longitude())
				.population(city.population())
				.country(city.country().code())
				.build();
	}


}