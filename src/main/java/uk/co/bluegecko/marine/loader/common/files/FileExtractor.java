package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileExtractor<I, T> {

	@SuppressWarnings("unchecked")
	Map<Enum<?>, List<ParseResult>> extract(final I in,
			final FileParser<T>... parsers) throws IOException;

}