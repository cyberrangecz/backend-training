package cz.cyberrange.platform.training.api.dto.attachment;

/**
 * Describes an attachment file as one training definition uses it.
 *
 * @param fileName the name the definition gives the file
 * @param mediaType the detected media type of the file
 * @param sizeBytes the length of the file in bytes
 * @param durationSeconds the playing time in seconds, or {@code null} when not stored
 * @param width the width in pixels, or {@code null} when not stored
 * @param height the height in pixels, or {@code null} when not stored
 */
public record AttachmentMetadataDTO(
    String fileName,
    String mediaType,
    long sizeBytes,
    Double durationSeconds,
    Integer width,
    Integer height) {}
