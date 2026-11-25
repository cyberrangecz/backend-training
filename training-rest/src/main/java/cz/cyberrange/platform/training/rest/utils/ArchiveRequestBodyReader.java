package cz.cyberrange.platform.training.rest.utils;

import cz.cyberrange.platform.training.api.exceptions.PayloadTooLargeException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

/**
 * Reads the body of a definition archive import request, refusing a body larger than the attachment
 * budget of a definition plus a fixed margin for the definition document and the metadata file. The
 * declared length is checked before any byte is read and the read itself stops one byte past the
 * limit, so an oversized body is never buffered whole.
 */
@Component
public class ArchiveRequestBodyReader {

  private static final DataSize MARGIN = DataSize.ofMegabytes(32);

  private final long maximalBodyBytes;

  /**
   * Creates the reader.
   *
   * @param budgetPerDefinition the cap on the total size of the files one definition links
   */
  public ArchiveRequestBodyReader(
      @Value("${attachments.budget.per.definition}") DataSize budgetPerDefinition) {
    this.maximalBodyBytes = budgetPerDefinition.toBytes() + MARGIN.toBytes();
  }

  /**
   * Reads the whole body of the request.
   *
   * @param request the request carrying the archive
   * @return the body bytes
   * @throws PayloadTooLargeException when the declared or the actual body size exceeds the limit
   * @throws IOException when the body cannot be read
   */
  public byte[] read(HttpServletRequest request) throws IOException {
    if (request.getContentLengthLong() > maximalBodyBytes) {
      throw tooLarge();
    }
    byte[] body = request.getInputStream().readNBytes((int) (maximalBodyBytes + 1));
    if (body.length > maximalBodyBytes) {
      throw tooLarge();
    }
    return body;
  }

  private PayloadTooLargeException tooLarge() {
    return new PayloadTooLargeException(
        "The archive may be at most "
            + maximalBodyBytes
            + " bytes: the attachment budget of a definition plus a margin");
  }
}
