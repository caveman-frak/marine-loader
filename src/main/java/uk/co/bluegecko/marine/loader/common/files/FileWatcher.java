package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileWatcher {

	private final Map<WatchKey, FileProcessor<Path, ?, ?>> watchers;
	private final Map<Path, WatchKey> mapping;
	private final WatchService watchService;

	public FileWatcher(WatchService watchService) {
		watchers = new HashMap<>();
		mapping = new HashMap<>();
		this.watchService = watchService;
	}

	public boolean register(Path path, FileProcessor<Path, ?, ?> processor, WatchEvent.Kind<?>... events)
			throws IOException {
		if (mapping.containsKey(path)) {
			return false;
		}
		WatchKey key = path.register(watchService, events);

		mapping.put(path, key);
		watchers.put(key, processor);

		return true;
	}

	public boolean register(Path path, FileProcessor<Path, ?, ?> processor) throws IOException {
		return register(path, processor, StandardWatchEventKinds.ENTRY_CREATE);
	}

	public boolean unregister(Path path) {
		WatchKey key = mapping.get(path);
		if (key != null) {
			key.cancel();
			mapping.remove(path);
			watchers.remove(key);
			return true;
		}
		return false;
	}

	public void poll(long timeout, TimeUnit unit) throws InterruptedException {
		WatchKey key = watchService.poll(timeout, unit);
		if (key != null) {
			key.pollEvents()
					.forEach(e -> {
						FileProcessor<Path, ?, ?> processor = watchers.get(key);
						if (processor != null) {
							try {
								processor.extract((Path) e.context());
							} catch (IOException ex) {
								throw new RuntimeException(ex);
							}
						}
					});
			key.reset();
		}

	}
}