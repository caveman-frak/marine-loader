package uk.co.bluegecko.marine.loader.uploader.ais.core;

import dk.dma.ais.message.AisMessage;
import java.util.Map;

public record CommentAndMessage(Map<String, Object> parameters, AisMessage message) {

}