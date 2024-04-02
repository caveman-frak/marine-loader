package uk.co.bluegecko.marine.loader.uploader.ais.core;

import dk.dma.ais.sentence.CommentBlock;
import java.util.Map;

public interface CommentParser {

	String id();

	Map<String, Object> parse(CommentBlock commentBlock);
}