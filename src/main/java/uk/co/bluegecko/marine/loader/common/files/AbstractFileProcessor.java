package uk.co.bluegecko.marine.loader.common.files;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true, makeFinal = true)
public abstract class AbstractFileProcessor<I, T, V> implements FileProcessor<I, T, V> {

	private final FileParser<T>[] parsers;
	private final FileExtractor<I, T> fileExtractor;

	@SafeVarargs
	protected AbstractFileProcessor(@NonNull final FileExtractor<I, T> fileExtractor,
			@NonNull final FileParser<T>... parsers) {
		this.fileExtractor = fileExtractor;
		this.parsers = parsers;
	}
}