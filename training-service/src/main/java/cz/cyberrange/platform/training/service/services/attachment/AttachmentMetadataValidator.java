package cz.cyberrange.platform.training.service.services.attachment;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Checks the media metadata that arrives with an attachment file, whether uploaded or imported. */
@Component
public class AttachmentMetadataValidator {

  private static final int MAXIMAL_POSTER_BYTES = 1024 * 1024;
  private static final int MAXIMAL_DIMENSION = 16384;
  private static final Set<String> POSTER_MEDIA_TYPES =
      Set.of("image/jpeg", "image/webp", "image/png");

  private final AttachmentMediaTypes mediaTypes;

  /**
   * Creates the validator.
   *
   * @param mediaTypes the media type detector
   */
  public AttachmentMetadataValidator(AttachmentMediaTypes mediaTypes) {
    this.mediaTypes = mediaTypes;
  }

  /**
   * Checks that the duration is finite and not negative, each dimension lies between 1 and the
   * largest accepted pixel count, and the poster, when present, is a JPEG, WebP or PNG image of 1
   * byte up to 1 MiB, as detected from its bytes.
   *
   * @param metadata the metadata to check
   * @return the detected media type of the poster, or {@code null} when there is none
   * @throws BadRequestException when a value is out of range or the poster is not acceptable
   */
  public String validate(AttachmentFileMetadataDTO metadata) {
    String hash = metadata.hash();
    Double duration = metadata.durationSeconds();
    require(
        duration == null || (duration >= 0 && Double.isFinite(duration)),
        hash,
        "a duration must be finite and not negative");
    requireDimension(metadata.width(), hash, "width");
    requireDimension(metadata.height(), hash, "height");
    return validatePoster(metadata.poster(), hash);
  }

  private String validatePoster(byte[] poster, String hash) {
    if (poster == null) {
      return null;
    }
    require(
        poster.length > 0 && poster.length <= MAXIMAL_POSTER_BYTES,
        hash,
        "a poster must have 1 byte to " + MAXIMAL_POSTER_BYTES + " bytes");
    String posterMediaType = mediaTypes.detect(poster, null);
    require(
        POSTER_MEDIA_TYPES.contains(posterMediaType),
        hash,
        "a poster must be a JPEG, WebP or PNG image");
    return posterMediaType;
  }

  private static void requireDimension(Integer dimension, String hash, String name) {
    require(
        dimension == null || (dimension > 0 && dimension <= MAXIMAL_DIMENSION),
        hash,
        "a " + name + " must be between 1 and " + MAXIMAL_DIMENSION + " pixels");
  }

  private static void require(boolean valid, String hash, String rule) {
    if (!valid) {
      throw new BadRequestException("Invalid media metadata of attachment " + hash + ": " + rule);
    }
  }
}
