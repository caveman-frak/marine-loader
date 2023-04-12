package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.co.bluegecko.marine.wire.batch.Batch;
import uk.co.bluegecko.marine.wire.batch.BatchType;

class FileProcessorTest extends AbstractExtractorTest {

	@Test
	void testProcess() throws URISyntaxException, IOException {
		var batch = new DummyFileProcessor(new PathExtractor(), csvParser()).extract(getPath(data()));

		assertThat(batch).as("exists").isNotNull();
		assertThat(batch.type()).as("type").isEqualTo(BatchType.MIXED);
		assertThat(batch.fileName()).as("filename").isEqualTo("dummy-data.csv");
		assertThat(batch.name()).as("name").isEqualTo("Dummy Data");
		assertThat(batch.info()).as("info").isEmpty();
		assertThat(batch.logs()).as("logs").isEmpty();
		assertThat(batch.items()).as("items").hasSize(4);
	}

	private static class DummyFileProcessor extends FileProcessor<Path, InputStream, Batch> {

		@SafeVarargs
		public DummyFileProcessor(final FileExtractor<Path, InputStream> fileExtractor,
				final FileParser<InputStream>... parsers) {
			super(parsers, fileExtractor);
		}

		@Override
		public Batch process(final Path file, final Map<Enum<?>, List<ParseResult>> results) throws IOException {
			ParseResult result = results.get(Dummy.CSV).get(0);
			return Batch.builder()
					.type(BatchType.MIXED)
					.file(file.resolve(result.fileName()))
					.clock(Clock.systemUTC())
					.name("Dummy Data")
					.items(result.values())
					.info(new HashMap<>())
					.logs(result.logs())
					.build();
		}
	}
}