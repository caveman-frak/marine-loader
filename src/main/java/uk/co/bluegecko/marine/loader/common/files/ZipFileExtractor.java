package uk.co.bluegecko.marine.loader.common.files;

import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Pattern;

public class ZipFileExtractor<T> implements FileExtractor<T> {

	@Override
	public Optional<InputStream> extract(T in, Pattern mask) {
		return Optional.empty();
	}
}