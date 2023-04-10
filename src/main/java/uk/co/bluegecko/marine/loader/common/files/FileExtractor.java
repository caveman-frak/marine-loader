package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import org.springframework.util.MultiValueMap;

public interface FileExtractor<I, T> {

	MultiValueMap<Enum<?>, ParseResult> extract(final I in,
			final FileParser<T>... parsers) throws IOException;

}