package uk.co.bluegecko.marine.loader.uploader.ais.raw;

import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.DEFINED;
import static uk.co.bluegecko.marine.loader.uploader.ais.core.AisConstants.GEOHASH;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.message.AisPosition;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.sentence.CommentBlock;
import dk.dma.ais.sentence.SentenceException;
import dk.dma.ais.sentence.Vdm;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import uk.co.bluegecko.marine.loader.uploader.ais.core.CommentAndMessage;
import uk.co.bluegecko.marine.loader.uploader.ais.core.CommentParser;
import uk.co.bluegecko.marine.loader.uploader.ais.core.DefaultCommentParser;

@Service
@lombok.Value
@Slf4j
public class RawMessageHandler {

	Vdm vdm;
	int precision;
	CommentParser fallbackParser;
	Map<String, CommentParser> parsers;

	public RawMessageHandler(Vdm vmd,
			@Value("${marine.geohash.precision:6}") int precision,
			CommentParser fallbackParser,
			@Qualifier(DEFINED) CommentParser... parsers) {
		this.vdm = vmd;
		this.precision = precision;
		this.fallbackParser = fallbackParser;
		this.parsers = Stream.of(parsers).collect(Collectors.toMap(CommentParser::id, p -> p));

		log.info("Registered parsers = '{}', fallback = '{}'",
				String.join(",", this.parsers.keySet()), fallbackParser.id());
		log.info("Geohash precision = {}", precision);
	}

	public Optional<CommentAndMessage> handleMessage(String feed, String rawMessage)
			throws SentenceException, AisMessageException, SixbitException {
		getVdm().parse(rawMessage);
		if (getVdm().isCompletePacket()) {
			AisMessage message = AisMessage.getInstance(getVdm());
			Map<String, Object> parameters = parse(feed, getVdm().getCommentBlock());
			calculateGeohash(message).ifPresent(h -> parameters.put(GEOHASH, h));
			return Optional.of(new CommentAndMessage(
					parameters,
					message));
		} else {
			return Optional.empty();
		}
	}

	private Optional<String> calculateGeohash(AisMessage message) {
		if (message instanceof AisPositionMessage positionMessage) {
			if (positionMessage.isPositionValid()) {
				AisPosition position = positionMessage.getPos();
				return Optional.of(GeohashUtils.encodeLatLon(
						position.getLatitudeDouble(), position.getLongitudeDouble(),
						precision));
			}
		}
		return Optional.empty();
	}

	private CommentParser parser(String id) {
		return parsers.getOrDefault(id, fallbackParser);
	}

	private Map<String, Object> parse(String id, CommentBlock commentBlock) {
		return Optional.ofNullable(commentBlock).map(parser(id)::parse).orElseGet(HashMap::new);
	}

	@Bean
	public static CommentParser fallbackParser() {
		return new DefaultCommentParser();
	}

}