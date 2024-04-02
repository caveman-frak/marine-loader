package uk.co.bluegecko.marine.loader.uploader.ais.raw.proprietary.norway;

import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.COAST_STATION;
import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.COAST_STATION_TAG;
import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.DEFINED;
import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.NORWAY;

import dk.dma.ais.sentence.CommentBlock;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.co.bluegecko.marine.loader.uploader.ais.core.DefaultCommentParser;

@Component
@Qualifier(DEFINED)
public class NorwayCommentParser extends DefaultCommentParser {

	public String id() {
		return NORWAY;
	}

	public Map<String, Object> parse(CommentBlock commentBlock) {
		Map<String, Object> params = super.parse(commentBlock);

		if (commentBlock.contains(COAST_STATION_TAG)) {
			params.put(COAST_STATION, commentBlock.getString(COAST_STATION_TAG));
		}

		return params;
	}

}