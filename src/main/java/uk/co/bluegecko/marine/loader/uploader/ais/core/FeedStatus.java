package uk.co.bluegecko.marine.loader.uploader.ais.core;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Feed;

@Data
@Builder
public class FeedStatus {

	public static final int RETRY_LIMIT = 5;

	final Feed feed;

	@Default
	int retry = 0;

	@Default
	ConnetionState state = ConnetionState.PENDING;

	public int disconnected() {
		retry++;
		state = retry >= RETRY_LIMIT ? ConnetionState.FAILED : ConnetionState.PENDING;
		return retry;
	}

	public void connected() {
		retry = 0;
		state = ConnetionState.CONNECTED;
	}

}