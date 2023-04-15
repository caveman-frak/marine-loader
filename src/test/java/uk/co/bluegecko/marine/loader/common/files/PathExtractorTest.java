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
class PathExtractorTest extends AbstractExtractorTest {

	@Test
	void testExtractCsvFileFromDirectory() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(data()), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().getFileName().toString(), "file name", "dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractCsvFileFromDirectoryResource() throws IOException, URISyntaxException {
		var resultMap = new PathExtractor().extract(url(data()), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().getFileName().toString(), "file name", "dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractJsonFileFromDirectory() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(data()), jsonParser());
		List<ParseResult> results = resultMap.get(Dummy.JSON);
		assertThat(results)
				.as("JSON parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().getFileName().toString(), "file name", "dummy-data.json"),
						extracted(r -> r.values().size(), "value", 200)));
	}

	@Test
	void testExtractTxtFileFromDirectory() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(data()), textParser());
		List<ParseResult> results = resultMap.get(Dummy.TEXT);
		assertThat(results)
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllFilesFromDirectory() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(data()),
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
	void testExtractCsvFileFromPath() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(data(), csv()), csvParser());
		List<ParseResult> results = resultMap.get(Dummy.CSV);
		assertThat(results)
				.as("CSV parser")
				.isNotNull()
				.hasSize(1);

		assertThat(results.get(0))
				.is(allOf(extracted(r -> r.file().getFileName().toString(), "file name", "dummy-data.csv"),
						extracted(r -> r.values().size(), "value", 4)));
	}

	@Test
	void testExtractTxtFileFromPath() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(data(), csv()), textParser());
		List<ParseResult> results = resultMap.get(Dummy.TEXT);
		assertThat(results)
				.as("Text parser")
				.isNull();
	}

	@Test
	void testExtractAllFilesFromNestedDirectory() throws URISyntaxException {
		var resultMap = new PathExtractor().extract(path(nested()),
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