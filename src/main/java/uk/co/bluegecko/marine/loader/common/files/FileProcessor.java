package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public interface FileProcessor<I, T, V> {

	V process(@NonNull final I input, @NonNull final Map<Enum<?>, List<ParseResult>> results) throws IOException;

	default V extract(@NonNull final I input) throws IOException {
		return process(input, fileExtractor().extract(input, parsers()));
	}

	FileParser<T>[] parsers();

	FileExtractor<I, T> fileExtractor();
}