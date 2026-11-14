package cz.cyberrange.platform.training.service.unit.services.attachment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Builds ZIP bytes entry by entry, in insertion order, with any entry name. */
final class ArchiveBytes {

  private final Map<String, byte[]> entries = new LinkedHashMap<>();

  static ArchiveBytes archive() {
    return new ArchiveBytes();
  }

  ArchiveBytes entry(String name, String content) {
    return entry(name, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  ArchiveBytes entry(String name, byte[] content) {
    entries.put(name, content);
    return this;
  }

  byte[] build() {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bytes)) {
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        zip.putNextEntry(new ZipEntry(entry.getKey()));
        zip.write(entry.getValue());
        zip.closeEntry();
      }
      zip.finish();
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
