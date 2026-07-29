package cz.cyberrange.platform.training.service.utils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

/**
 * Utility for one-way masking of sandbox identifiers for callers not authorized to see real sandbox
 * UUIDs. Produces a deterministic, correlation-preserving token: events from the same sandbox yield
 * the same hash value, but the original UUID cannot be recovered.
 */
public final class SandboxIdHasher {

  private static final HashFunction SHA_256 = Hashing.sha256();

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
    return SHA_256.hashString(sandboxId, StandardCharsets.UTF_8).toString();
  }
}
