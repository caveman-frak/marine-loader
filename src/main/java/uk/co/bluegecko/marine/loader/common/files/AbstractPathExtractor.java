package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class AbstractPathExtractor implements FileExtractor<Path, InputStream> {

	protected Map<Enum<?>, List<ParseResult>> walkPath(@NonNull final Path path,
			@NonNull final MultiValueMap<Enum<?>, ParseResult> results,
			@NonNull final Map<Pattern, FileParser<InputStream>> masks) {
		try (Stream<Path> files = Files.walk(path)) {
			files.filter(f -> f.getFileName() != null).forEach(
					file -> processFile(file, results, masks));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return results;
	}

	protected void processFile(@NonNull final Path file,
			@NonNull final MultiValueMap<Enum<?>, ParseResult> results,
			@NonNull final Map<Pattern, FileParser<InputStream>> masks) {
		String name = file.getFileName().toString();
		masks.forEach((k, v) -> {
					if (k.matcher(name).find()) {
						try (InputStream in = Files.newInputStream(file)) {
							results.add(v.type(), v.parse(file, in));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
		);
	}

	/**
	 * Enumerate the content of the input, apply parsers to any that matches the file masks of the supplied parsers and
	 * generate a set of results.
	 *
	 * @param uri     the uri of the content to extract files from.
	 * @param parsers the parsers to apply to the extracted files.
	 * @return the set of parse results.
	 * @throws IOException thrown if error occurs on the input/contents.
	 */
	@SafeVarargs
	public final Map<Enum<?>, List<ParseResult>> extract(@NonNull final URI uri,
			@NonNull final FileParser<InputStream>... parsers)
			throws IOException {
		return extract(Paths.get(uri), parsers);
	}

	/**
	 * Enumerate the content of the input, apply parsers to any that matches the file masks of the supplied parsers and
	 * generate a set of results.
	 *
	 * @param url     the url of the content to extract files from.
	 * @param parsers the parsers to apply to the extracted files.
	 * @return the set of parse results.
	 * @throws IOException thrown if error occurs on the input/contents.
	 */
	@SafeVarargs
	public final Map<Enum<?>, List<ParseResult>> extract(@NonNull final URL url,
			@NonNull final FileParser<InputStream>... parsers)
			throws IOException, URISyntaxException {
		return extract(url.toURI(), parsers);
	}

	protected Map<Pattern, FileParser<InputStream>> masks(@NonNull final FileParser<InputStream>[] parsers) {
		return Stream.of(parsers).collect(Collectors.toMap(FileParser::mask, p -> p));
	}

	protected MultiValueMap<Enum<?>, ParseResult> results() {
		return new LinkedMultiValueMap<>();
	}
}