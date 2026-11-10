package cz.cyberrange.platform.training.api.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * The training materials of a definition with the storage budget they count against.
 *
 * @param materials the materials ordered by file name then hash
 * @param budgetBytes the most bytes the files of one definition may total
 * @param usedBytes the bytes the definition's linked files total
 */
@Schema(description = "The training materials of a definition with the budget and the bytes used")
public record AttachmentListDTO(
    List<AttachmentSummaryDTO> materials, long budgetBytes, long usedBytes) {}
