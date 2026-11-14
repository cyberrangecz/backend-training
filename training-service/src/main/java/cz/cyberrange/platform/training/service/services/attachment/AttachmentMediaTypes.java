package cz.cyberrange.platform.training.service.services.attachment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;

/** Detects the media type of attachment bytes and derives a file extension from a media type. */
@Slf4j
@Component
public class AttachmentMediaTypes {

  private static final String MATROSKA = "application/x-matroska";
  private static final String QUICKTIME = "video/quicktime";
  private static final int CONTAINER_HEADER_LENGTH = 64;
  private static final int BRAND_OFFSET = 8;
  private static final int BRAND_LENGTH = 4;
  private static final String QUICKTIME_BRAND = "qt  ";

  private final Tika tika = new Tika();

  /**
   * Detects a media type from the content of a file, using its name as a secondary hint. A WebM
   * stream is reported as {@code video/webm} and an MPEG-4 file as {@code video/mp4} even when the
   * name gives no hint, where the generic detection answers Matroska and QuickTime.
   *
   * @param content the file bytes
   * @param fileName the file name, may be {@code null}
   * @return the detected media type, never {@code null}
   */
  public String detect(byte[] content, String fileName) {
    String detected = tika.detect(content, fileName);
    if (MATROSKA.equals(detected)) {
      return isWebm(content) ? "video/webm" : "video/x-matroska";
    }
    if (QUICKTIME.equals(detected) && !hasQuickTimeBrand(content)) {
      return "video/mp4";
    }
    return detected;
  }

  /**
   * Looks up the extension conventionally used for a media type.
   *
   * @param mediaType the media type
   * @return the extension with its leading dot, or an empty string when the type has no known
   *     extension or is malformed
   */
  public String extensionOf(String mediaType) {
    try {
      return MimeTypes.getDefaultMimeTypes().forName(mediaType).getExtension();
    } catch (MimeTypeException e) {
      log.warn("Malformed media type {}, no extension derived", mediaType, e);
      return "";
    }
  }

  private static boolean isWebm(byte[] content) {
    byte[] header = Arrays.copyOf(content, Math.min(content.length, CONTAINER_HEADER_LENGTH));
    return new String(header, StandardCharsets.ISO_8859_1).contains("webm");
  }

  private static boolean hasQuickTimeBrand(byte[] content) {
    if (content.length < BRAND_OFFSET + BRAND_LENGTH) {
      return true;
    }
    String brand = new String(content, BRAND_OFFSET, BRAND_LENGTH, StandardCharsets.ISO_8859_1);
    return QUICKTIME_BRAND.equals(brand);
  }
}
