package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ZipStreamExtractor implements FileExtractor<ZipInputStream, InputStream> {

	@SafeVarargs
	@Override
	public final MultiValueMap<Enum<?>, ParseResult> extract(ZipInputStream in,
			FileParser<InputStream>... parsers)
			throws IOException {
		var results = new LinkedMultiValueMap<Enum<?>, ParseResult>();
		var masks = Stream.of(parsers).collect(Collectors.toMap(FileParser::mask, p -> p));

		ZipEntry entry = in.getNextEntry();
		while (entry != null) {
			try {
				String name = entry.getName();
				masks.forEach((k, v) -> {
							if (k.matcher(name).find()) {
								results.add(v.type(), v.parse(name, in));
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