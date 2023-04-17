package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Extract the contents of the input zip file and parse the input stream of any that are applicable.
 */
public class ZipFileExtractor extends AbstractPathExtractor {

	/**
	 * Enumerate the content of the zip file, apply parsers to any that matches the file masks of the supplied parsers
	 * and generate a set of results.
	 *
	 * @param path    the path of the zip file to extract files from.
	 * @param parsers the parsers to apply to the extracted files.
	 * @return the set of parse results.
	 * @throws IOException thrown if error occurs on the input/contents.
	 */
	@SafeVarargs
	@Override
	public final Map<Enum<?>, List<ParseResult>> extract(@NonNull final Path path,
			@NonNull final FileParser<InputStream>... parsers)
			throws IOException {
		final var results = results();
		final var masks = masks(parsers);

		try (FileSystem zipFile = FileSystems.newFileSystem(path)) {
			zipFile.getRootDirectories().forEach(root -> walkPath(root, results, masks));
		}
		return results;
	}

}