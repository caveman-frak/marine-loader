package uk.co.bluegecko.marine.loader.common.files;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.test.jassert.Conditions.extracted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class ZipFileExtractorTest {

	@Test
	void testCsvFileContents() throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getSystemResourceAsStream("data/dummy-data.csv")))) {

			assertThat(reader.readLine())
					.startsWith("\"id\",")
					.endsWith(",\"country\"");
		}
	}

	@Test
	void testJsonFileContents() throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getSystemResourceAsStream("data/dummy-data.json")))) {

			assertThat(reader.readLine())
					.isEqualTo("{");
		}
	}

	@Test
	void testRawZipFileContents() throws IOException {
		try (InputStream in = getSystemResourceAsStream("data/dummy-data.zip")) {

			assertThat(in.readNBytes(4)).as("Header 'PK♥♦'")
					.isEqualTo(new byte[]{80, 75, 3, 4});
			assertThat(in.readNBytes(2)).as("Version .0")
					.isEqualTo(new byte[]{20, 0});
			assertThat(in.readNBytes(2)).as("Bit flag 2 0")
					.isEqualTo(new byte[]{2, 0});
			assertThat(in.readNBytes(2)).as("Compression 8 0")
					.isEqualTo(new byte[]{8, 0});
		}
	}

	@Test
	void testExtractCsvFile() throws IOException {
		try (ZipInputStream zin = new ZipInputStream(
				getSystemResourceAsStream("data/dummy-data.zip"))) {

			Optional<InputStream> extracted = new ZipFileExtractor()
					.extract(zin, Pattern.compile("(.+)\\.csv"));
			assertThat(extracted)
					.as("CSV mask")
					.isPresent();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(extracted.orElseThrow()))) {
				assertThat(reader.lines().toList())
						.hasSize(4);
			}
		}
	}

	@Test
	void testExtractJsonFile() throws IOException {
		try (ZipInputStream zin = new ZipInputStream(
				getSystemResourceAsStream("data/dummy-data.zip"))) {

			Optional<InputStream> extracted = new ZipFileExtractor()
					.extract(zin, Pattern.compile("(.+)\\.json"));
			assertThat(extracted)
					.as("JSON mask")
					.isPresent();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(extracted.orElseThrow()))) {
				assertThat(reader.lines().toList())
						.hasSize(200);
			}
		}
	}

	@Test
	void testExtractTxtFile() throws IOException {
		try (ZipInputStream zin = new ZipInputStream(
				getSystemResourceAsStream("data/dummy-data.zip"))) {

			Optional<InputStream> extracted = new ZipFileExtractor()
					.extract(zin, Pattern.compile("(.+)\\.txt"));
			assertThat(extracted)
					.as("TXT mask")
					.isEmpty();
		}
	}

	@Test
	void testZipFileContents() throws IOException {
		try (ZipInputStream zin = new ZipInputStream(
				getSystemResourceAsStream("data/dummy-data.zip"))) {

			// CSV file
			assertThat(zin.getNextEntry())
					.has(extracted(ZipEntry::getName, "file name of", "dummy-data.csv"));
			BufferedReader csvReader = new BufferedReader(new InputStreamReader(zin));
			assertThat(csvReader.lines().toList())
					.hasSize(4);
			zin.closeEntry();

			// JSON file
			assertThat(zin.getNextEntry())
					.has(extracted(ZipEntry::getName, "file name of", "dummy-data.json"));
			BufferedReader jsonReader = new BufferedReader(new InputStreamReader(zin));
			assertThat(jsonReader.lines().toList())
					.hasSize(200);
			zin.closeEntry();
		}
	}
}