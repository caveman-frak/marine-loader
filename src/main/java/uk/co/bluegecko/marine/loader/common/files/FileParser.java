package uk.co.bluegecko.marine.loader.common.files;

import java.nio.file.Path;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * Parse a file to create a list of objects and log any warnings and errors.
 *
 * @param <I> the type in input to parser (for example {@link java.io.InputStream}.
 */
public interface FileParser<I> {

	/**
	 * Parse the input to produce a {@link ParseResult} containing the parsed objects and log errors and warnings
	 * related to the parsing.
	 *
	 * @param file the input file that is being parsed.
	 * @param in   the input data to be parsed.
	 * @return the results of the parsing.
	 */
	ParseResult parse(@NonNull final Path file, @NonNull final I in);

	/**
	 * Type indicator for the returned {@link ParseResult}.
	 *
	 * @return type indicator.
	 */
	Enum<?> type();

	/**
	 * File mask to determine which input should be processed by this parser.
	 *
	 * @return file mask.
	 */
	Pattern mask();

}