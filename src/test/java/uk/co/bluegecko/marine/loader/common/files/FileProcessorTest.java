package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import uk.co.bluegecko.marine.wire.batch.BatchType;

class FileProcessorTest extends AbstractExtractorTest {

	@Test
	void testProcessPath() throws URISyntaxException, IOException {
		var batch = new DummyFileProcessor(new PathExtractor(), csvParser()).extract(path(data()));

		assertThat(batch).as("exists").isNotNull();
		assertThat(batch.type()).as("type").isEqualTo(BatchType.MIXED);
		assertThat(batch.fileName()).as("filename").isEqualTo("dummy-data.csv");
		assertThat(batch.name()).as("name").isEqualTo("Dummy Data");
		assertThat(batch.info()).as("info").isEmpty();
		assertThat(batch.logs()).as("logs").isEmpty();
		assertThat(batch.items()).as("items").hasSize(4);
	}

}