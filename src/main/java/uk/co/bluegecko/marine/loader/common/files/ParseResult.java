package uk.co.bluegecko.marine.loader.common.files;

import java.nio.file.Path;
import java.util.List;
import lombok.NonNull;
import uk.co.bluegecko.marine.wire.batch.Batchable;
import uk.co.bluegecko.marine.wire.batch.Log;

/**
 * Results of parsing an input.
 *
 * @param file   the input file.
 * @param values the objects that were parsed.
 * @param logs   any errors or warnings that occurred during parsing.
 */
public record ParseResult(
		@NonNull Path file,
		@NonNull List<Batchable> values,
		@NonNull List<Log> logs) {

}