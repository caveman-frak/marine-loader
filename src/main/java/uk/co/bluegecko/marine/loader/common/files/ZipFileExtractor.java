package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SuppressWarnings("rawtypes")
@Slf4j
public class ZipFileExtractor implements FileExtractor<Path, InputStream> {

	@SuppressWarnings("unchecked")
	@Override
	public MultiValueMap<Enum, ParseResult> extract(Path path, FileParser<InputStream>... parsers)
			throws IOException {
		MultiValueMap<Enum, ParseResult> results = new LinkedMultiValueMap<>();
		Map<Pattern, FileParser<InputStream>> masks =
				Stream.of(parsers).collect(Collectors.toMap(FileParser::mask,
						p -> p));

		try (FileSystem zipFile = FileSystems.newFileSystem(path)) {
			zipFile.getRootDirectories().forEach(root -> {
				try (Stream<Path> files = Files.walk(root)) {
					files.filter(f -> f.getFileName() != null).forEach(
							file -> processFile(results, masks, file));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
		return results;
	}

	private void processFile(MultiValueMap<Enum, ParseResult> results,
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

	public MultiValueMap<Enum, ParseResult> extract(URI uri, FileParser<InputStream>... parsers)
			throws IOException, URISyntaxException {
		return extract(Paths.get(uri), parsers);
	}

	public MultiValueMap<Enum, ParseResult> extract(URL url, FileParser<InputStream>... parsers)
			throws IOException, URISyntaxException {
		return extract(url.toURI(), parsers);
	}

}