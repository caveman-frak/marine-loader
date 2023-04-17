package uk.co.bluegecko.marine.loader.common.files;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Extract the contents of the input path and parse the input stream of any that are applicable.
 */
public class PathExtractor extends AbstractPathExtractor {

	/**
	 * Enumerate the content of the input, apply parsers to any that matches the file masks of the supplied parsers and
	 * generate a set of results.
	 *
	 * @param path    the path to extract files from, if a directory, otherwise a single file to check.
	 * @param parsers the parsers to apply to the extracted file(s).
	 * @return the map of parse results.
	 */
	@SafeVarargs
	@Override
	public final Map<Enum<?>, List<ParseResult>> extract(@NonNull final Path path,
			@NonNull final FileParser<InputStream>... parsers) {
		return walkPath(path, results(), masks(parsers));
	}

}