package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true, makeFinal = true)
public abstract class FileProcessor<I, T, V> {

	private final FileParser<T>[] parsers;
	private final FileExtractor<I, T> fileExtractor;

	public abstract V process(I input, Map<Enum<?>, List<ParseResult>> results) throws IOException;

	public V extract(I input) throws IOException {
		return process(input, fileExtractor().extract(input, parsers()));
	}

}