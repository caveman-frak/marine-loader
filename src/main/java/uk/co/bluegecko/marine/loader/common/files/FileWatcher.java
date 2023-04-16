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
import lombok.NonNull;
import lombok.SneakyThrows;

public class FileWatcher {

	private final Map<WatchKey, FileProcessor<Path, ?, ?>> watchers;
	private final Map<Path, WatchKey> mapping;
	private final WatchService watchService;

	public FileWatcher(@NonNull final WatchService watchService) {
		watchers = new HashMap<>();
		mapping = new HashMap<>();
		this.watchService = watchService;
	}

	public boolean register(@NonNull final Path path,
			@NonNull final FileProcessor<Path, ?, ?> processor,
			@NonNull final WatchEvent.Kind<?>... events)
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

	public boolean unregister(@NonNull final Path path) {
		WatchKey key = mapping.get(path);
		if (key != null) {
			key.cancel();
			mapping.remove(path);
			watchers.remove(key);
			return true;
		}
		return false;
	}

	public void close() throws IOException {
		watchService.close();
	}

	public void poll(final long timeout, @NonNull final TimeUnit unit) throws InterruptedException {
		WatchKey key = watchService.poll(timeout, unit);
		if (key != null) {
			final Path directory = (Path) key.watchable();
			key.pollEvents()
					.forEach(e -> {
						FileProcessor<Path, ?, ?> processor = watchers.get(key);
						if (processor != null) {
							try {
								Path file = (Path) e.context();
								processor.extract(directory.resolve(file));
							} catch (IOException ex) {
								throw new RuntimeException(ex);
							}
						}
					});
			key.reset();
		}
	}

	@SneakyThrows
	public void poll() {
		poll(0, TimeUnit.SECONDS);
	}

}