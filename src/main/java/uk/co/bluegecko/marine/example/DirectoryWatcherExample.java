package uk.co.bluegecko.marine.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectoryWatcherExample {

	public static void main(String... args)
			throws IOException, InterruptedException {

		Path path = Files.createTempDirectory("test");
		log.info("Dir: {}", path);
		WatchService watchService = path.getFileSystem().newWatchService();
		path.register(
				watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);

		WatchKey key;
		while ((key = watchService.take()) != null) {
			for (WatchEvent<?> event : key.pollEvents()) {
				log.info("Event kind: {} File affected: {}", event.kind(), event.context());
			}
			key.reset();
		}
	}
}