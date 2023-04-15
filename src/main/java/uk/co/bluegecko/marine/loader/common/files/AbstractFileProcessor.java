package uk.co.bluegecko.marine.loader.common.files;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true, makeFinal = true)
public abstract class AbstractFileProcessor<I, T, V> implements FileProcessor<I, T, V> {

	private final FileParser<T>[] parsers;
	private final FileExtractor<I, T> fileExtractor;

}