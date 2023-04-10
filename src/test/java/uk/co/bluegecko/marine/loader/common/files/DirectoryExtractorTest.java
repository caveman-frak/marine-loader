package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.test.jassert.Conditions.extracted;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DirectoryExtractorTest extends AbstractExtractorTest {

	@Test
	void testExtractCsvFile() throws IOException, URISyntaxException {
		var resultMap = new DirectoryExtractor().extract(getDirPath(), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(ParseResult::fileName, "file name", "dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractCsvFileFromResource() throws IOException, URISyntaxException {
		var resultMap = new DirectoryExtractor().extract(getDirUrl(), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(ParseResult::fileName, "file name", "dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractJsonFile() throws IOException, URISyntaxException {
		var resultMap = new DirectoryExtractor().extract(getDirPath(), jsonParser());
		List<ParseResult> results = resultMap.get(Dummy.JSON);
		assertThat(results)
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(ParseResult::fileName, "file name", "dummy-data.json"),
						extracted(r -> r.values().size(), "value", 200)));
	}

	@Test
	void testExtractTxtFile() throws IOException, URISyntaxException {
		var resultMap = new DirectoryExtractor().extract(getDirPath(), textParser());
		List<ParseResult> results = resultMap.get(Dummy.TEXT);
		assertThat(results)
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllFile() throws IOException, URISyntaxException {
		var resultMap = new DirectoryExtractor().extract(getDirPath(), csvParser(), jsonParser(), textParser());
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