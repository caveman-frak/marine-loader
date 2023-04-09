package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import org.springframework.util.MultiValueMap;

public interface FileExtractor<I, T> {

	MultiValueMap<? extends Enum, ParseResult> extract(I in,
			FileParser<T>... parsers) throws IOException;

}