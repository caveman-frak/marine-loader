package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.shared.test.jassert.Conditions.extracted;

import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ZipFileExtractorTest extends AbstractExtractorTest {

	@Test
	void testExtractCsvFile() throws URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), csvParser());
		List<ParseResult> results = resultMap.get(DummyType.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.getFirst())
				.is(allOf(extracted(r -> r.file().toString(), "file name", "/dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractCsvFileFromResource() throws URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), csvParser());
		List<ParseResult> results = resultMap.get(DummyType.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.getFirst())
				.is(allOf(extracted(r -> r.file().toString(), "file name", "/dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractJsonFile() throws URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), jsonParser());
		List<ParseResult> results = resultMap.get(DummyType.JSON);
		assertThat(results)
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.getFirst())
				.is(allOf(extracted(r -> r.file().toString(), "file name", "/dummy-data.json"),
						extracted(r -> r.values().size(), "value", 200)));
	}

	@Test
	void testExtractTxtFile() throws URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), textParser());
		List<ParseResult> results = resultMap.get(DummyType.TEXT);
		assertThat(results)
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllFile() throws URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()),
				csvParser(), jsonParser(), textParser());
		assertThat(resultMap.get(DummyType.CSV))
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(DummyType.JSON))
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(DummyType.TEXT))
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllNestedFile() throws URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(nested(), zip()),
				csvParser(), jsonParser(), textParser());
		assertThat(resultMap.get(DummyType.CSV))
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(DummyType.JSON))
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(DummyType.TEXT))
				.as("Text parser")
				.isNull();
	}

}