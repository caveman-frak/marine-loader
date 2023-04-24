package uk.co.bluegecko.marine.loader.common.files;

import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Extract the contents of the input and parse any that are applicable.
 *
 * @param <I> the type of input to extract content from.
 * @param <T> the type to be parsed.
 */
public interface FileExtractor<I, T> {

	/**
	 * Enumerate the content of the input, apply parsers to any that matches the file masks of the supplied parsers and
	 * generate a set of results.
	 *
	 * @param in      the content to extract files from.
	 * @param parsers the parsers to apply to the extracted files.
	 * @return the map of parse results.
	 */
	@SuppressWarnings("unchecked")
	Map<Enum<?>, List<ParseResult>> extract(@NonNull final I in,
			@NonNull final FileParser<T>... parsers);

}