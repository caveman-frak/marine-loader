package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileProcessor<I, T, V> {

	V process(I input, Map<Enum<?>, List<ParseResult>> results) throws IOException;

	default V extract(I input) throws IOException {
		return process(input, fileExtractor().extract(input, parsers()));
	}

	FileParser<T>[] parsers();

	FileExtractor<I, T> fileExtractor();
}