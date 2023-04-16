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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
public abstract class AbstractPathExtractor implements FileExtractor<Path, InputStream> {

	protected Map<Enum<?>, List<ParseResult>> walkPath(@NonNull final Path path,
			@NonNull final MultiValueMap<Enum<?>, ParseResult> results,
			@NonNull final Map<Pattern, FileParser<InputStream>> masks) {
		log.info("Path: {} / {}", path, path.toAbsolutePath());
		try (Stream<Path> files = Files.walk(path)) {
			log.info("walking");
			files.filter(f -> f.getFileName() != null).forEach(
					file -> {
						log.info("File: {} / {}", file, file.getFileName());
						processFile(file, results, masks);
					});
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

	@SafeVarargs
	public final Map<Enum<?>, List<ParseResult>> extract(@NonNull final URI uri,
			@NonNull final FileParser<InputStream>... parsers)
			throws IOException {
		return extract(Paths.get(uri), parsers);
	}

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