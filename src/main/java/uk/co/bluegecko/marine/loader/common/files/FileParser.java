package uk.co.bluegecko.marine.loader.common.files;

import java.nio.file.Path;
import java.util.regex.Pattern;
import lombok.NonNull;

public interface FileParser<I> {

	ParseResult parse(@NonNull final Path file, @NonNull final I in);

	Enum<?> type();

	Pattern mask();

}