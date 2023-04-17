package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.bluegecko.marine.wire.batch.Batch;
import uk.co.bluegecko.marine.wire.batch.BatchType;

@ExtendWith(MockitoExtension.class)
class FileWatcherTest extends AbstractExtractorTest {

	@Mock
	FileProcessor<Path, InputStream, Batch> fileProcessor;
	@Mock
	Consumer<Batch> notifier;

	@Test
	void testRegisterWithMock(@TempDir Path tmpDir) throws IOException, InterruptedException {
		WatchService watchService = tmpDir.getFileSystem().newWatchService();
		FileWatcher fileWatcher = new FileWatcher(watchService);
		fileWatcher.register(tmpDir, fileProcessor);

		writeFile(tmpDir, "dummy-data.csv");
		fileWatcher.poll(2, TimeUnit.SECONDS);

		ArgumentCaptor<Path> arg = ArgumentCaptor.forClass(Path.class);
		verify(fileProcessor).extract(arg.capture());

		assertThat(arg.getValue()).hasFileName("dummy-data.csv");
	}

	@Test
	void testPoll(@TempDir Path tmpDir) throws IOException, InterruptedException {
		WatchService watchService = tmpDir.getFileSystem().newWatchService();
		FileWatcher fileWatcher = new FileWatcher(watchService);
		fileWatcher.register(tmpDir, new DummyFileProcessor(new PathExtractor(), notifier, csvParser()));

		writeFile(tmpDir, "dummy-data.csv");
		fileWatcher.poll(2, TimeUnit.SECONDS);

		ArgumentCaptor<Batch> arg = ArgumentCaptor.forClass(Batch.class);
		verify(notifier).accept(arg.capture());

		Batch batch = arg.getValue();
		assertThat(batch).as("exists").isNotNull();
		assertThat(batch.type()).as("type").isEqualTo(BatchType.MIXED);
		assertThat(batch.fileName()).as("filename").isEqualTo("dummy-data.csv");
	}

	@Test
	void testPollRepeat(@TempDir Path tmpDir) throws IOException, InterruptedException {
		WatchService watchService = tmpDir.getFileSystem().newWatchService();
		FileWatcher fileWatcher = new FileWatcher(watchService);
		fileWatcher.register(tmpDir, fileProcessor);

		writeFile(tmpDir, "dummy-data.csv");
		writeFile(tmpDir, "dummy-data.json");
		fileWatcher.poll(2, TimeUnit.SECONDS);

		ArgumentCaptor<Path> arg = ArgumentCaptor.forClass(Path.class);
		verify(fileProcessor, times(2)).extract(arg.capture());

		assertThat(arg.getAllValues()).extracting(p -> p.getFileName().toString())
				.contains("dummy-data.csv", "dummy-data.json");
	}

	@Test
	void testUnregister(@TempDir Path tmpDir) throws IOException {
		WatchService watchService = tmpDir.getFileSystem().newWatchService();
		FileWatcher fileWatcher = new FileWatcher(watchService);

		assertThat(fileWatcher.register(tmpDir, fileProcessor)).as("1st register").isTrue();
		assertThat(fileWatcher.register(tmpDir, fileProcessor)).as("2nd register").isFalse();
		assertThat(fileWatcher.isRegistered(tmpDir)).as("is registered").isTrue();
		assertThat(fileWatcher.unregister(tmpDir)).as("1st unregister").isTrue();
		assertThat(fileWatcher.unregister(tmpDir)).as("2nd unregister").isFalse();
		assertThat(fileWatcher.isRegistered(tmpDir)).as("is registered").isFalse();
	}

	private Path writeFile(Path dir, String filename) throws IOException {
		Path file = Files.createFile(dir.resolve(filename));
		Files.writeString(file, """
				"number","name"
				100,"One Hundred"
				22,"Twenty Two"
				30,Thirty""", StandardCharsets.UTF_8);
		return file;
	}

}