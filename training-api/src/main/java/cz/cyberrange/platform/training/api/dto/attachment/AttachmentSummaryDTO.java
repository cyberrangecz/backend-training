package cz.cyberrange.platform.training.api.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One training material of a definition as the list endpoint describes it.
 *
 * @param hash the lowercase hexadecimal SHA-256 of the file bytes
 * @param fileName the name the definition gives the file
 * @param mediaType the detected media type of the file
 * @param sizeBytes the length of the file in bytes
 * @param durationSeconds the playing time in seconds, or {@code null} when not stored
 * @param width the width in pixels, or {@code null} when not stored
 * @param height the height in pixels, or {@code null} when not stored
 * @param hasPoster whether a poster image is stored for the file
 */
@Schema(description = "A training material of a definition")
public record AttachmentSummaryDTO(
    String hash,
    String fileName,
    String mediaType,
    long sizeBytes,
    Double durationSeconds,
    Integer width,
    Integer height,
    boolean hasPoster) {}
