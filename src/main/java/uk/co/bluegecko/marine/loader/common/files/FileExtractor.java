package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public interface FileExtractor<I, T> {

	@SuppressWarnings("unchecked")
	Map<Enum<?>, List<ParseResult>> extract(@NonNull final I in,
			@NonNull final FileParser<T>... parsers) throws IOException;

}