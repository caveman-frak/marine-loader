package uk.co.bluegecko.marine.loader.common.files;

import java.util.regex.Pattern;

public interface FileParser<I> {

	ParseResult parse(String fileName, I in);

	Enum type();

	Pattern mask();

}