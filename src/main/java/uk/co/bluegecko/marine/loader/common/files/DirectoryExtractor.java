package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

@Slf4j
public class DirectoryExtractor extends AbstractPathExtractor {

	@SafeVarargs
	@Override
	public final MultiValueMap<Enum<?>, ParseResult> extract(Path path,
			final FileParser<InputStream>... parsers)
			throws IOException {
		var results = results();

		walkPath(path, results, masks(parsers));

		return results;
	}

}