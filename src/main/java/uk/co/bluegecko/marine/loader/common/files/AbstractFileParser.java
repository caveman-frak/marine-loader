package uk.co.bluegecko.marine.loader.common.files;

import java.util.regex.Pattern;
import lombok.NonNull;

public abstract class AbstractFileParser<I> implements FileParser<I> {

	private final Enum<?> type;
	private final Pattern mask;

	protected AbstractFileParser(@NonNull final Enum<?> type, @NonNull final Pattern mask) {
		this.type = type;
		this.mask = mask;
	}

	/**
	 * Type indicator for the returned {@link ParseResult}.
	 *
	 * @return type indicator.
	 */
	@Override
	public Enum<?> type() {
		return type;
	}

	/**
	 * File mask to determine which input should be processed by this parser.
	 *
	 * @return file mask.
	 */
	@Override
	public Pattern mask() {
		return mask;
	}
}