package cz.cyberrange.platform.training.service.services.attachment;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;

/**
 * A file that arrived with a save or import request, hashed once when it is created.
 *
 * @param fileName the name the file had on the caller's side
 * @param content the file bytes
 * @param hash the lowercase hexadecimal SHA-256 of {@code content}
 * @param metadata the media metadata that came with the file, or {@code null} when none did
 */
public record SuppliedAttachment(
    String fileName, byte[] content, String hash, AttachmentFileMetadataDTO metadata) {

  /**
   * Creates a supplied file without media metadata, computing the hash of its bytes.
   *
   * @param fileName the name the file had on the caller's side
   * @param content the file bytes
   * @return the file carrying the hash of its bytes
   */
  public static SuppliedAttachment of(String fileName, byte[] content) {
    return new SuppliedAttachment(fileName, content, AttachmentHashes.sha256Hex(content), null);
  }

  /**
   * Returns this file carrying the given media metadata.
   *
   * @param newMetadata the media metadata, or {@code null}
   * @return a copy of this file with the metadata
   */
  public SuppliedAttachment withMetadata(AttachmentFileMetadataDTO newMetadata) {
    return new SuppliedAttachment(fileName, content, hash, newMetadata);
  }
}
