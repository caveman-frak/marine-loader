package uk.co.bluegecko.marine.loader.common.files;

import java.util.List;
import lombok.NonNull;
import uk.co.bluegecko.marine.wire.batch.Batchable;
import uk.co.bluegecko.marine.wire.batch.Log;

public record ParseResult(
		@NonNull String fileName,
		@NonNull List<Batchable> values,
		@NonNull List<Log> logs) {

}