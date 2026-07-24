package cz.cyberrange.platform.training.service.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility for one-way masking of sandbox identifiers for callers not authorized to see real sandbox
 * UUIDs. Produces a deterministic, correlation-preserving token: events from the same sandbox yield
 * the same hash value, but the original UUID cannot be recovered.
 */
public final class SandboxIdHasher {

  private SandboxIdHasher() {}

  /**
   * Returns the SHA-256 hash of the given sandbox identifier, encoded as lowercase hexadecimal.
   *
   * @param sandboxId the plain sandbox UUID to hash; may be {@code null}
   * @return lowercase hex SHA-256 digest of the input, or {@code null} if {@code sandboxId} is
   *     {@code null}
   */
  public static String hash(String sandboxId) {
    if (sandboxId == null) {
      return null;
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(sandboxId.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm is unavailable on this JVM", e);
    }
  }
}
