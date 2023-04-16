package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.function.Consumer;
import lombok.NonNull;

public abstract class AbstractNotifyingFileProcessor<I, T, V> extends AbstractFileProcessor<I, T, V> {

	private final Consumer<V> notifier;

	@SafeVarargs
	protected AbstractNotifyingFileProcessor(
			@NonNull final FileExtractor<I, T> fileExtractor,
			@NonNull final Consumer<V> notifier,
			@NonNull final FileParser<T>... parsers
	) {
		super(fileExtractor, parsers);
		this.notifier = notifier;
	}

	@Override
	public V extract(@NonNull final I input) throws IOException {
		V result = super.extract(input);

		notifier.accept(result);

		return result;
	}

}