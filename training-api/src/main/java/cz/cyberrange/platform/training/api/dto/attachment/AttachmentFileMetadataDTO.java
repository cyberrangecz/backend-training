package cz.cyberrange.platform.training.api.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Media metadata the designer's browser captured for one file of a save request.
 *
 * @param hash the lowercase hexadecimal SHA-256 of the file the metadata describes
 * @param durationSeconds the playing time of audio or video in seconds, or {@code null}
 * @param width the width of an image or video in pixels, or {@code null}
 * @param height the height of an image or video in pixels, or {@code null}
 * @param poster a small JPEG, WebP or PNG image standing for the file, or {@code null}
 */
@Schema(description = "Media metadata captured in the browser for one uploaded file")
public record AttachmentFileMetadataDTO(
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Lowercase hexadecimal SHA-256 of the file the metadata describes",
            example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
        String hash,
    @Schema(description = "Playing time of audio or video in seconds", example = "12.48")
        Double durationSeconds,
    @Schema(description = "Width of an image or video in pixels", example = "1280") Integer width,
    @Schema(description = "Height of an image or video in pixels", example = "720") Integer height,
    @Schema(
            description =
                "Poster image bytes, Base64 encoded: a JPEG, WebP or PNG of at most 1 MiB, the"
                    + " first frame of a video or a downscaled image")
        byte[] poster) {}
