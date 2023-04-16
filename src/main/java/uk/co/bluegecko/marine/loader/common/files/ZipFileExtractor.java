package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class ZipFileExtractor extends AbstractPathExtractor {

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