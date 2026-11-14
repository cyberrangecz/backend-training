package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentMediaTypes;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentMetadataValidator;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AttachmentMetadataValidatorTest {

  private static final String HASH = "a".repeat(64);
  private static final byte[] PNG_SIGNATURE = {
    (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0
  };

  private final AttachmentMetadataValidator validator =
      new AttachmentMetadataValidator(new AttachmentMediaTypes());

  private static AttachmentFileMetadataDTO metadata(
      Double duration, Integer width, Integer height, byte[] poster) {
    return new AttachmentFileMetadataDTO(HASH, duration, width, height, poster);
  }

  @Test
  void metadataWithinRangeIsAcceptedAndAbsentPosterHasNoMediaType() {
    assertNull(validator.validate(metadata(0.0, 1, 16384, null)));
    assertNull(validator.validate(metadata(null, null, null, null)));
  }

  @Test
  void aPngPosterIsAcceptedAndItsDetectedMediaTypeReturned() {
    assertEquals("image/png", validator.validate(metadata(1.5, 10, 10, PNG_SIGNATURE)));
  }

  @Test
  void aDurationThatIsNegativeOrNotFiniteIsRefused() {
    for (double duration :
        new double[] {-0.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      assertThrows(
          BadRequestException.class,
          () -> validator.validate(metadata(duration, null, null, null)));
    }
  }

  @Test
  void aDimensionBelowOneOrAboveTheUpperBoundIsRefused() {
    for (int dimension : new int[] {0, -5, 16385, Integer.MAX_VALUE}) {
      assertThrows(
          BadRequestException.class, () -> validator.validate(metadata(null, dimension, 1, null)));
      assertThrows(
          BadRequestException.class, () -> validator.validate(metadata(null, 1, dimension, null)));
    }
  }

  @Test
  void anEmptyPosterAndAPosterAboveOneMebibyteAreRefused() {
    byte[] oversized = Arrays.copyOf(PNG_SIGNATURE, 1024 * 1024 + 1);

    assertThrows(
        BadRequestException.class, () -> validator.validate(metadata(null, 1, 1, new byte[0])));
    assertThrows(
        BadRequestException.class, () -> validator.validate(metadata(null, 1, 1, oversized)));
  }

  @Test
  void aPosterThatIsNotAJpegWebpOrPngByItsBytesIsRefused() {
    byte[] text = "<svg xmlns='http://www.w3.org/2000/svg'/>".getBytes();

    assertThrows(BadRequestException.class, () -> validator.validate(metadata(null, 1, 1, text)));
  }
}
