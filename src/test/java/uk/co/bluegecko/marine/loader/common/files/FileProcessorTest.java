package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.net.URISyntaxException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.co.bluegecko.marine.wire.batch.Batch;
import uk.co.bluegecko.marine.wire.batch.BatchType;

@SpringJUnitConfig
class FileProcessorTest extends AbstractExtractorTest {

	@MockBean
	Consumer<Batch> notifier;

	@Test
	void testProcessPath() throws URISyntaxException {
		var batch = new DummyFileProcessor(new PathExtractor(), csvParser()).extract(path(data()));

		assertThat(batch).as("exists").isNotNull();
		assertThat(batch.type()).as("type").isEqualTo(BatchType.MIXED);
		assertThat(batch.fileName()).as("filename").isEqualTo("dummy-data.csv");
		assertThat(batch.name()).as("name").isEqualTo("Dummy Data");
		assertThat(batch.info()).as("info").isEmpty();
		assertThat(batch.logs()).as("logs").isEmpty();
		assertThat(batch.items()).as("items").hasSize(4);
	}

	@Test
	void testProcessPathWithNotify() throws URISyntaxException {
		new DummyFileProcessor(new PathExtractor(), notifier, csvParser()).extract(path(data()));

		ArgumentCaptor<Batch> arg = ArgumentCaptor.forClass(Batch.class);
		verify(notifier).accept(arg.capture());

		Batch batch = arg.getValue();
		assertThat(batch).as("exists").isNotNull();
		assertThat(batch.type()).as("type").isEqualTo(BatchType.MIXED);
		assertThat(batch.fileName()).as("filename").isEqualTo("dummy-data.csv");
	}

}