package uk.co.bluegecko.marine.loader.common.files;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.ClassLoader.getSystemResourceAsStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import lombok.NonNull;
import uk.co.bluegecko.marine.wire.batch.Batch;
import uk.co.bluegecko.marine.wire.batch.BatchType;
import uk.co.bluegecko.marine.wire.batch.Batchable;

public abstract class AbstractExtractorTest {

	protected InputStream inputStream(@NonNull final String dir, @NonNull final String suffix) {
		return Objects.requireNonNull(getSystemResourceAsStream(dir + "/" + file() + "." + suffix));
	}

	protected ZipInputStream zipInputStream(@NonNull final String dir) {
		return new ZipInputStream(inputStream(dir, zip()));
	}

	protected BufferedReader bufferedReader(@NonNull final String dir, @NonNull final String suffix) {
		return new BufferedReader(new InputStreamReader(inputStream(dir, suffix)));
	}

	protected URL url(@NonNull final String dir, @NonNull final String suffix) {
		return getSystemResource(dir + "/" + file() + "." + suffix);
	}

	protected Path path(@NonNull final String dir, @NonNull final String suffix) throws URISyntaxException {
		return Paths.get(url(dir, suffix).toURI());
	}

	protected URL url(@NonNull final String dir) {
		return getSystemResource(dir);
	}

	protected Path path(@NonNull final String dir) throws URISyntaxException {
		return Paths.get(url(dir).toURI());
	}

	protected String nested() {
		return "nested-data";
	}

	protected String data() {
		return "data";
	}

	private String file() {
		return "dummy-data";
	}

	protected String zip() {
		return "zip";
	}

	protected String csv() {
		return "csv";
	}

	protected String json() {
		return "json";
	}

	protected static DummyParser csvParser() {
		return new DummyParser(DummyType.CSV, Pattern.compile("(.+)\\.csv"));
	}

	protected static DummyParser jsonParser() {
		return new DummyParser(DummyType.JSON, Pattern.compile("(.+)\\.json"));
	}

	protected static DummyParser textParser() {
		return new DummyParser(DummyType.TEXT, Pattern.compile("(.+)\\.txt"));
	}

	protected enum DummyType {CSV, JSON, TEXT}

	protected record DummyText(@NonNull Integer line, @NonNull String text) implements Batchable {

	}

	protected static class DummyParser extends AbstractFileParser<InputStream> {

		protected DummyParser(@NonNull final Enum<?> type, @NonNull final Pattern mask) {
			super(type, mask);
		}

		@Override
		public ParseResult parse(@NonNull final Path file, @NonNull final InputStream in) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			AtomicInteger line = new AtomicInteger();
			return new ParseResult(file,
					reader.lines().map(s -> (Batchable) new DummyText(line.getAndIncrement(), s)).toList(),
					List.of());
		}
	}

	protected static class DummyFileProcessor extends AbstractNotifyingFileProcessor<Path, InputStream, Batch> {

		@SafeVarargs
		public DummyFileProcessor(@NonNull final FileExtractor<Path, InputStream> fileExtractor,
				@NonNull final Consumer<Batch> notifier,
				@NonNull final FileParser<InputStream>... parsers) {
			super(fileExtractor, notifier, parsers);
		}

		@SafeVarargs
		public DummyFileProcessor(@NonNull final FileExtractor<Path, InputStream> fileExtractor,
				@NonNull final FileParser<InputStream>... parsers) {
			this(fileExtractor, v -> {
			}, parsers);
		}

		@Override
		public Batch collect(@NonNull final Path file,
				@NonNull final Map<Enum<?>, List<ParseResult>> results) throws IOException {
			ParseResult result = results.get(DummyType.CSV).getFirst();
			return Batch.builder()
					.type(BatchType.MIXED)
					.file(file.resolve(result.file()))
					.clock(Clock.systemUTC())
					.name("Dummy Data")
					.items(result.values())
					.info(new HashMap<>())
					.logs(result.logs())
					.build();
		}
	}

}