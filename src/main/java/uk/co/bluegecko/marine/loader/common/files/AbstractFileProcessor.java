package uk.co.bluegecko.marine.loader.common.files;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * {@link FileProcessor} that extracts the input and parses it using the registered parsers to then collect the results
 * and return them.
 *
 * @param <I> the type of input.
 * @param <T> the type of content to parse.
 * @param <V> the collected value of the parsing.
 */
@Getter
@Accessors(fluent = true, makeFinal = true)
public abstract class AbstractFileProcessor<I, T, V> implements FileProcessor<I, T, V> {

	private final FileParser<T>[] parsers;
	private final FileExtractor<I, T> fileExtractor;

	/**
	 * Construct a {@link FileProcessor} with the required arguments.
	 *
	 * @param fileExtractor the extractor to use on the input.
	 * @param parsers       the parsers to generate the input for the result.
	 */
	@SafeVarargs
	protected AbstractFileProcessor(@NonNull final FileExtractor<I, T> fileExtractor,
			@NonNull final FileParser<T>... parsers) {
		this.fileExtractor = fileExtractor;
		this.parsers = parsers;
	}
}