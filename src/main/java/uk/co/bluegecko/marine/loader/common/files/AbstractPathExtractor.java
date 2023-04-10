package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class AbstractPathExtractor implements FileExtractor<Path, InputStream> {

	protected void walkPath(Path path, MultiValueMap<Enum<?>, ParseResult> results,
			Map<Pattern, FileParser<InputStream>> masks) {
		try (Stream<Path> files = Files.walk(path)) {
			files.filter(f -> f.getFileName() != null).forEach(
					file -> processFile(results, masks, file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void processFile(MultiValueMap<Enum<?>, ParseResult> results,
			Map<Pattern, FileParser<InputStream>> masks,
			Path file) {
		String name = file.getFileName().toString();
		masks.forEach((k, v) -> {
					if (k.matcher(name).find()) {
						try (InputStream in = Files.newInputStream(file)) {
							results.add(v.type(), v.parse(name, in));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
		);
	}

	@SafeVarargs
	public final MultiValueMap<Enum<?>, ParseResult> extract(URI uri, FileParser<InputStream>... parsers)
			throws IOException {
		return extract(Paths.get(uri), parsers);
	}

	@SafeVarargs
	public final MultiValueMap<Enum<?>, ParseResult> extract(URL url, FileParser<InputStream>... parsers)
			throws IOException, URISyntaxException {
		return extract(url.toURI(), parsers);
	}

	protected Map<Pattern, FileParser<InputStream>> masks(FileParser<InputStream>[] parsers) {
		return Stream.of(parsers).collect(Collectors.toMap(FileParser::mask, p -> p));
	}

	protected LinkedMultiValueMap<Enum<?>, ParseResult> results() {
		return new LinkedMultiValueMap<>();
	}
}