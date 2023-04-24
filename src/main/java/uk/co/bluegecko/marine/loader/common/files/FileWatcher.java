package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * A watcher that allows {@link FileProcessor} to be registered against a particular {@link Path}.
 */
public class FileWatcher {

	private final Map<WatchKey, FileProcessor<Path, ?, ?>> watchers;
	private final Map<Path, WatchKey> mapping;
	private final WatchService watchService;

	/**
	 * Construct a file watcher with the passed {@link WatchService}.
	 *
	 * @param watchService the watch service to use internally.
	 */
	public FileWatcher(@NonNull final WatchService watchService) {
		watchers = new HashMap<>();
		mapping = new HashMap<>();
		this.watchService = watchService;
	}

	/**
	 * Register a file processor against a path, to produce a result when the path changes.
	 *
	 * @param path      the path to watch.
	 * @param processor the file processor to apply.
	 * @param events    the type of events to watch for.
	 * @return true if the path registered correctly.
	 * @throws IOException thrown if the path is not accessible.
	 */
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

	/**
	 * Register a file processor against a path, to produce a result when the path fires creation events.
	 *
	 * @param path      the path to watch.
	 * @param processor the file processor to apply.
	 * @return true if the path registered correctly.
	 * @throws IOException thrown if the path is not accessible.
	 */
	public boolean register(Path path, FileProcessor<Path, ?, ?> processor) throws IOException {
		return register(path, processor, StandardWatchEventKinds.ENTRY_CREATE);
	}

	/**
	 * Unregister the path and associated file extractor.
	 *
	 * @param path the path to unregister.
	 * @return true if successfully unregistered.
	 */
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

	/**
	 * Return if the path is already registered.
	 *
	 * @param path the path to check.
	 * @return true if registered.
	 */
	public boolean isRegistered(@NonNull final Path path) {
		return mapping.containsKey(path);
	}

	/**
	 * Close the file watcher and all internal structures.
	 *
	 * @throws IOException if the watcher service errors during close.
	 */
	public void close() throws IOException {
		watchService.close();
		mapping.clear();
		watchers.clear();
	}

	/**
	 * Poll the watch service to see if any of the paths have fired change events. If an event is detected, once the
	 * processor has completed, the watcher will be checked again to ensure there are no more waiting events.
	 *
	 * @param timeout the duration to watch for changes before returning.
	 * @param unit    the time unit of the timeout duration.
	 * @return if any events had been fired.
	 * @throws InterruptedException thrown if the timeout is interrupted.
	 */
	public boolean poll(final long timeout, @NonNull final TimeUnit unit) throws InterruptedException {
		boolean result = false;
		WatchKey key = watchService.poll(timeout, unit);
		while (key != null) {
			result = true;
			Set<WatchEvent.Kind<?>> valid = Set.of(StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			FileProcessor<Path, ?, ?> processor = watchers.get(key);
			if (processor != null) {
				final Path directory = (Path) key.watchable();
				key.pollEvents()
						.stream().filter(e -> valid.contains(e.kind()))
						.forEach(e -> processor.extract(directory.resolve((Path) e.context())));
			}
			key.reset();
			key = watchService.poll();
		}
		return result;
	}

	/**
	 * Poll the watch service to see if any of the paths have fired change events.
	 *
	 * @return if any events had been fired.
	 */
	@SneakyThrows
	public boolean poll() {
		return poll(0, TimeUnit.SECONDS);
	}

}