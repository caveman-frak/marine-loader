package uk.co.bluegecko.marine.loader.common.files;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathExtractor extends AbstractPathExtractor {

	@SafeVarargs
	@Override
	public final Map<Enum<?>, List<ParseResult>> extract(final Path path,
			final FileParser<InputStream>... parsers) {
		return walkPath(path, results(), masks(parsers));
	}

}