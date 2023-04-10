package uk.co.bluegecko.marine.loader.common.files;

import java.util.regex.Pattern;

public abstract class AbstractFileParser<I> implements FileParser<I> {

	private final Enum<?> type;
	private final Pattern mask;

	protected AbstractFileParser(final Enum<?> type, final Pattern mask) {
		this.type = type;
		this.mask = mask;
	}

	@Override
	public Enum<?> type() {
		return type;
	}

	@Override
	public Pattern mask() {
		return mask;
	}
}