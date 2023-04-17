package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Process the contents of an input, and extract and parse any matching files.
 *
 * @param <I> the type of input.
 * @param <T> the type of content to parse.
 * @param <V> the collected value of the parsing.
 */
public interface FileProcessor<I, T, V> {

	/**
	 * Collect the parse results and generate a collected return value.
	 *
	 * @param input   the input to process.
	 * @param results the set of parsed values.
	 * @return the collected result.
	 * @throws IOException thrown if errors occurred processing the input.
	 */
	V collect(@NonNull final I input, @NonNull final Map<Enum<?>, List<ParseResult>> results) throws IOException;

	/**
	 * Process the contents using the supplied file extractor to generate a collected result.
	 *
	 * @param input the input to process.
	 * @return the collected result.
	 * @throws IOException thrown if errors occurred processing the input.
	 */
	default V extract(@NonNull final I input) throws IOException {
		return collect(input, fileExtractor().extract(input, parsers()));
	}

	/**
	 * The file parsers to use with the extractor.
	 *
	 * @return the file parsers.
	 */
	FileParser<T>[] parsers();

	/**
	 * The file extractor to use on the contents.
	 *
	 * @return the file extractor.
	 */
	FileExtractor<I, T> fileExtractor();
}