package uk.co.bluegecko.marine.loader.common.files;

import java.util.function.Consumer;
import lombok.NonNull;

/**
 * {@link FileProcessor} that registers a {@link Consumer} to be notified when the result has been processed.
 *
 * @param <I> the type of input.
 * @param <T> the type of content to parse.
 * @param <V> the collected value of the parsing.
 */
public abstract class AbstractNotifyingFileProcessor<I, T, V> extends AbstractFileProcessor<I, T, V> {

	private final Consumer<V> notifier;

	/**
	 * Construct a {@link FileProcessor} with the required arguments.
	 *
	 * @param fileExtractor the extractor to use on the input.
	 * @param notifier      the consumer to be notified of the result.
	 * @param parsers       the parsers to generate the input for the result.
	 */
	@SafeVarargs
	protected AbstractNotifyingFileProcessor(
			@NonNull final FileExtractor<I, T> fileExtractor,
			@NonNull final Consumer<V> notifier,
			@NonNull final FileParser<T>... parsers
	) {
		super(fileExtractor, parsers);
		this.notifier = notifier;
	}

	/**
	 * Send the result to the registered consumer.
	 *
	 * @param result result to send.
	 * @return forward the result.
	 */
	@Override
	public V notify(V result) {
		notifier.accept(result);

		return result;
	}

}