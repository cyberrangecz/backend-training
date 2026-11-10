package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.training.service.services.attachment.AttachmentMediaTypes;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttachmentMediaTypesTest {

  private static final Map<String, String> EBML_HEADERS =
      Map.of(
          "webm",
          "1a45dfa39f4286810142f7810142f2810442f381084282847765626d4287810242858102",
          "matroska",
          "1a45dfa3a34286810142f7810142f2810442f381084282886d6174726f736b6142878104428581021853806701000000");

  private final AttachmentMediaTypes mediaTypes = new AttachmentMediaTypes();

  @Test
  void webmStreamIsVideoWebmWhateverItsName() {
    byte[] content = ebmlHeader("webm");

    assertEquals("video/webm", mediaTypes.detect(content, null));
    assertEquals("video/webm", mediaTypes.detect(content, "clip"));
    assertEquals(".webm", mediaTypes.extensionOf("video/webm"));
  }

  @Test
  void matroskaStreamIsVideoMatroska() {
    assertEquals("video/x-matroska", mediaTypes.detect(ebmlHeader("matroska"), null));
  }

  @Test
  void mpegFourFileIsVideoMp4NotQuickTime() {
    byte[] content = ftyp("isom");

    assertEquals("video/mp4", mediaTypes.detect(content, null));
    assertEquals(".mp4", mediaTypes.extensionOf("video/mp4"));
  }

  @Test
  void quickTimeBrandStaysQuickTime() {
    assertEquals("video/quicktime", mediaTypes.detect(ftyp("qt  "), null));
  }

  private static byte[] ebmlHeader(String documentType) {
    return HexFormat.of().parseHex(EBML_HEADERS.get(documentType));
  }

  private static byte[] ftyp(String brand) {
    byte[] box = new byte[24];
    box[3] = 24;
    System.arraycopy("ftyp".getBytes(StandardCharsets.US_ASCII), 0, box, 4, 4);
    System.arraycopy(brand.getBytes(StandardCharsets.US_ASCII), 0, box, 8, 4);
    return box;
  }
}
