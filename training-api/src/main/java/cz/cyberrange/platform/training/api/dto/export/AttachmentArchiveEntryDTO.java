package cz.cyberrange.platform.training.api.dto.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * What the archive metadata file records about one attachment file: only what the server cannot
 * compute from the file bytes.
 *
 * @param fileName the original name the definition gives the file
 * @param durationSeconds the playing time in seconds, or {@code null}
 * @param width the width in pixels, or {@code null}
 * @param height the height in pixels, or {@code null}
 * @param poster the poster image bytes, Base64 encoded, or {@code null}
 */
@Schema(description = "Metadata of one attachment file inside an exported archive")
public record AttachmentArchiveEntryDTO(
    @JsonProperty("fileName")
        @Schema(description = "The original name the definition gives the file", example = "a.png")
        String fileName,
    @JsonProperty("durationSeconds")
        @Schema(description = "Playing time of audio or video in seconds", example = "12.48")
        Double durationSeconds,
    @JsonProperty("width")
        @Schema(description = "Width of an image or video in pixels", example = "1280")
        Integer width,
    @JsonProperty("height")
        @Schema(description = "Height of an image or video in pixels", example = "720")
        Integer height,
    @JsonProperty("poster") @Schema(description = "The poster image bytes, Base64 encoded")
        String poster) {}
