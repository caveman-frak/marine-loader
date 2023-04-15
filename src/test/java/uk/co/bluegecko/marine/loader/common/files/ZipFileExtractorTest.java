package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.test.jassert.Conditions.extracted;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ZipFileExtractorTest extends AbstractExtractorTest {

	@Test
	void testExtractCsvFile() throws IOException, URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().toString(), "file name", "/dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractCsvFileFromResource() throws IOException, URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().toString(), "file name", "/dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractJsonFile() throws IOException, URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), jsonParser());
		List<ParseResult> results = resultMap.get(Dummy.JSON);
		assertThat(results)
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().toString(), "file name", "/dummy-data.json"),
						extracted(r -> r.values().size(), "value", 200)));
	}

	@Test
	void testExtractTxtFile() throws IOException, URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()), textParser());
		List<ParseResult> results = resultMap.get(Dummy.TEXT);
		assertThat(results)
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllFile() throws IOException, URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(data(), zip()),
				csvParser(), jsonParser(), textParser());
		assertThat(resultMap.get(Dummy.CSV))
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(Dummy.JSON))
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(Dummy.TEXT))
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllNestedFile() throws IOException, URISyntaxException {
		var resultMap = new ZipFileExtractor().extract(path(nested(), zip()),
				csvParser(), jsonParser(), textParser());
		assertThat(resultMap.get(Dummy.CSV))
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(Dummy.JSON))
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);
		assertThat(resultMap.get(Dummy.TEXT))
				.as("Text parser")
				.isNull();
	}

}