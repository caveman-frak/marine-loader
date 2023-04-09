package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.test.jassert.Conditions.extracted;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class ZipStreamExtractorTest extends AbstractExtractorTest {

	@Test
	void testExtractCsvFile() throws IOException {
		try (ZipInputStream zin = getZipInputStream()) {

			var resultMap = new ZipStreamExtractor()
					.extract(zin, csvParser());
			List<ParseResult> results = resultMap.get(Dummy.CSV);
			assertThat(results)
					.as("CSV parser")
					.isNotNull()
					.hasSize(1);

			assertThat(results.get(0))
					.is(allOf(extracted(ParseResult::fileName, "file name", "dummy-data.csv"),
							extracted(r -> r.values().size(), "value", 4)));
		}
	}

	@Test
	void testExtractJsonFile() throws IOException {
		try (ZipInputStream zin = getZipInputStream()) {

			var resultMap = new ZipStreamExtractor()
					.extract(zin, jsonParser());
			List<ParseResult> results = resultMap.get(Dummy.JSON);
			assertThat(results)
					.as("JSON parser")
					.isNotNull()
					.hasSize(1);

			assertThat(results.get(0))
					.is(allOf(extracted(ParseResult::fileName, "file name", "dummy-data.json"),
							extracted(r -> r.values().size(), "value", 200)));
		}
	}

	@Test
	void testExtractTxtFile() throws IOException {
		try (ZipInputStream zin = getZipInputStream()) {

			var resultMap = new ZipStreamExtractor()
					.extract(zin, textParser());
			List<ParseResult> results = resultMap.get(Dummy.TEXT);
			assertThat(results)
					.as("Text parser")
					.isNull();
		}
	}

	@Test
	void testExtractAllFile() throws IOException {
		try (ZipInputStream zin = getZipInputStream()) {

			var resultMap = new ZipStreamExtractor()
					.extract(zin, csvParser(), jsonParser(), textParser());
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

}