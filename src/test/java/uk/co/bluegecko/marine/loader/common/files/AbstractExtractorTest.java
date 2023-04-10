package uk.co.bluegecko.marine.loader.common.files;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.bluegecko.marine.test.jassert.Conditions.extracted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.co.bluegecko.marine.wire.batch.Batchable;

public class AbstractExtractorTest {

	@Test
	@Tag("sanity-check")
	@DisplayName("Sanity check the CSV file")
	void checkCsvFileContents() throws IOException {
		try (BufferedReader reader = getBufferedReader("csv")) {

			assertThat(reader.readLine())
					.startsWith("\"id\",")
					.endsWith(",\"country\"");
		}
	}

	@Test
	@Tag("sanity-check")
	@DisplayName("Sanity check the JSON file")
	void checkJsonFileContents() throws IOException {
		try (BufferedReader reader = getBufferedReader("json")) {

			assertThat(reader.readLine())
					.isEqualTo("{");
		}
	}

	@Test
	@Tag("sanity-check")
	@DisplayName("Sanity check the ZIP file")
	void checkRawZipFileContents() throws IOException {
		try (InputStream in = getInputStream("zip")) {

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
	@Tag("sanity-check")
	@DisplayName("Sanity check the ZIP file contents")
	void checkZipFileContents() throws IOException {
		try (ZipInputStream zin = getZipInputStream()) {

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

	protected ZipInputStream getZipInputStream() {
		return new ZipInputStream(getInputStream("zip"));
	}

	protected static InputStream getInputStream(String suffix) {
		return Objects.requireNonNull(getSystemResourceAsStream("data/dummy-data." + suffix));
	}

	protected static BufferedReader getBufferedReader(String suffix) {
		return new BufferedReader(new InputStreamReader(getInputStream(suffix)));
	}

	protected URL getZipUrl() {
		return getSystemResource("data/dummy-data.zip");
	}

	protected Path getZipPath() throws URISyntaxException {
		return Paths.get(getZipUrl().toURI());
	}

	protected Path getDirPath() throws URISyntaxException {
		return Paths.get(getDirUrl().toURI());
	}

	protected URL getDirUrl() {
		return getSystemResource("data");
	}

	protected static DummyParser csvParser() {
		return new DummyParser(Dummy.CSV, Pattern.compile("(.+)\\.csv"));
	}

	protected static DummyParser jsonParser() {
		return new DummyParser(Dummy.JSON, Pattern.compile("(.+)\\.json"));
	}

	protected static DummyParser textParser() {
		return new DummyParser(Dummy.TEXT, Pattern.compile("(.+)\\.txt"));
	}

	protected enum Dummy {CSV, JSON, TEXT}

	protected record Text(Integer line, String text) implements Batchable {

	}

	protected static class DummyParser extends AbstractFileParser<InputStream> {

		protected DummyParser(Enum<?> type, Pattern mask) {
			super(type, mask);
		}

		@Override
		public ParseResult parse(String fileName, InputStream in) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			AtomicInteger line = new AtomicInteger();
			return new ParseResult(fileName,
					reader.lines().map(s -> (Batchable) new Text(line.getAndIncrement(), s)).toList(),
					List.of());
		}
	}

}