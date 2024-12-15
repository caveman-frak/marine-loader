package uk.co.bluegecko.marine.loader.uploader.ais.processed;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.bluegecko.marine.loader.uploader.ais.core.AisProperties.Feed;
import uk.co.bluegecko.marine.loader.uploader.ais.core.CommentAndMessage;

@Service
@Slf4j
public class AisMessageHandler {

	private final ConcurrentMap<Integer, AtomicInteger> messageCounter;

	public AisMessageHandler() {
		this.messageCounter = new ConcurrentHashMap<>();
	}

	public void handleMessage(Feed feed, CommentAndMessage m) {
		log.info("{} Read: {} '{}'", feed.id(), m.parameters(), m.message());

		messageCounter.compute(m.message().getMsgId(), (_, c) -> c == null ? new AtomicInteger() : c).incrementAndGet();
	}

	public Map<Integer, Integer> counts() {
		return messageCounter.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get()));
	}

}