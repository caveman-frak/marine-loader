package uk.co.bluegecko.marine.loader.common.files;

import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Pattern;

public interface FileExtractor<T> {

	Optional<InputStream> extract(T in, Pattern mask);

}