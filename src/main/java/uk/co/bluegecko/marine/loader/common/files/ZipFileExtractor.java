package uk.co.bluegecko.marine.loader.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileExtractor implements FileExtractor<ZipInputStream> {

	@Override
	public Optional<InputStream> extract(ZipInputStream in, Pattern mask) throws IOException {
		ZipEntry entry = in.getNextEntry();
		while (entry != null) {
			if (mask.matcher(entry.getName()).find()) {
				return Optional.of(in);
			}
			in.closeEntry();
			entry = in.getNextEntry();
		}
		return Optional.empty();
	}
}