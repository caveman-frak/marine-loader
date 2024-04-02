package uk.co.bluegecko.marine.loader.uploader.ais.core;

import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.TIMESTAMP;

import dk.dma.ais.sentence.CommentBlock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DefaultCommentParser implements CommentParser {

	@Override
	public String id() {
		return "*";
	}

	@Override
	public Map<String, Object> parse(CommentBlock commentBlock) {
		Map<String, Object> params = new HashMap<>();
		params.put(TIMESTAMP, Instant.ofEpochSecond(commentBlock.getTimestamp()));
		return params;
	}

}