package uk.co.bluegecko.marine.loader.common.files;

import java.nio.file.Path;
import java.util.regex.Pattern;

public interface FileParser<I> {

	ParseResult parse(Path file, I in);

	Enum<?> type();

	Pattern mask();

}