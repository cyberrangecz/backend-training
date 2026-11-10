package cz.cyberrange.platform.training.service.services.attachment;

import com.google.common.hash.Hashing;

/** Computes the hash by which level content references an attachment file. */
public final class AttachmentHashes {

  private AttachmentHashes() {}

  /**
   * Hashes file bytes.
   *
   * @param content the file bytes
   * @return the lowercase hexadecimal SHA-256 of the bytes
   */
  public static String sha256Hex(byte[] content) {
    return Hashing.sha256().hashBytes(content).toString();
  }
}
