package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.NonNull;
import org.springframework.util.LinkedMultiValueMap;

/**
 * Extract the contents of the input zip stream and parse the input stream of any that are applicable.
 */
public class ZipStreamExtractor implements FileExtractor<ZipInputStream, InputStream> {

	/**
	 * Enumerate the content of the zip stream, apply parsers to any that matches the file masks of the supplied parsers
	 * and generate a set of results.
	 *
	 * @param in      the zip stream to extract files from.
	 * @param parsers the parsers to apply to the extracted files.
	 * @return the map of parse results.
	 * @throws IOException thrown if error occurs on the input/contents.
	 */
	@SafeVarargs
	@Override
	public final Map<Enum<?>, List<ParseResult>> extract(@NonNull final ZipInputStream in,
			@NonNull final FileParser<InputStream>... parsers)
			throws IOException {
		final var results = new LinkedMultiValueMap<Enum<?>, ParseResult>();
		final var masks = Stream.of(parsers).collect(Collectors.toMap(FileParser::mask, p -> p));

		ZipEntry entry = in.getNextEntry();
		while (entry != null) {
			try {
				String name = entry.getName();
				masks.forEach((k, v) -> {
							if (k.matcher(name).find()) {
								results.add(v.type(), v.parse(Path.of(name), in));
							}
						}
				);
			} finally {
				in.closeEntry();
			}
			entry = in.getNextEntry();
		}
		return results;
	}

}