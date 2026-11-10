package cz.cyberrange.platform.training.service.services.attachment;

/**
 * A file a definition links to, read for export.
 *
 * @param hash the lowercase hexadecimal SHA-256 of the file bytes
 * @param fileName the name the definition gives the file
 * @param mediaType the media type detected from the file bytes
 * @param content the file bytes
 * @param durationSeconds the playing time in seconds, or {@code null}
 * @param width the width in pixels, or {@code null}
 * @param height the height in pixels, or {@code null}
 * @param poster the poster image bytes, or {@code null}
 */
public record ExportedAttachment(
    String hash,
    String fileName,
    String mediaType,
    byte[] content,
    Double durationSeconds,
    Integer width,
    Integer height,
    byte[] poster) {}
