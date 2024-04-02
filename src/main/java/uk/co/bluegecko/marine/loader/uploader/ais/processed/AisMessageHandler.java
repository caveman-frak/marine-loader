package uk.co.bluegecko.marine.loader.uploader.ais.processed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Feed;
import uk.co.bluegecko.marine.loader.uploader.ais.core.CommentAndMessage;

@Service
@Slf4j
public class AisMessageHandler {

	public void handleMessage(Feed feed, CommentAndMessage m) {
		log.info("{} Read: {} '{}'", feed.id(), m.parameters(), m.message());
	}

}