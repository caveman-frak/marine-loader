package uk.co.bluegecko.marine.loader.common.files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.bluegecko.marine.wire.batch.Batch;

@ExtendWith(MockitoExtension.class)
class FileWatcherTest {

	@Mock
	FileProcessor<Path, InputStream, Batch> fileProcessor;

	@Test
	void testRegister(@TempDir Path tmpDir) throws IOException, InterruptedException {
		WatchService watchService = tmpDir.getFileSystem().newWatchService();
		FileWatcher fileWatcher = new FileWatcher(watchService);
		fileWatcher.register(tmpDir, fileProcessor);

		writeFile(tmpDir, "dummy-data.csv");
		fileWatcher.poll(2, TimeUnit.SECONDS);

		verify(fileProcessor).extract(any(Path.class));
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