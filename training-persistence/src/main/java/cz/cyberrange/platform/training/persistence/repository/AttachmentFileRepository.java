package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.AttachmentFile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The JPA repository interface to manage {@link AttachmentFile} instances. {@code findById} and
 * {@code findAllById} read metadata only; the file bytes are reached through {@link
 * #findContentByHash(String)}, and files are written through {@link #insertIfAbsent}.
 */
@Repository
public interface AttachmentFileRepository
    extends JpaRepository<AttachmentFile, String>, QuerydslPredicateExecutor<AttachmentFile> {

  /**
   * Finds which of the given hashes name a stored file, without reading any file bytes.
   *
   * @param hashes the hashes to look up
   * @return the subset of the hashes that is stored
   */
  @Query("SELECT f.hash FROM AttachmentFile f WHERE f.hash IN :hashes")
  Set<String> findExistingHashes(@Param("hashes") Collection<String> hashes);

  /**
   * Reads the bytes of one stored file.
   *
   * @param hash the hash of the file
   * @return the file bytes, or empty when no file has the hash
   */
  @Query(value = "SELECT content FROM attachment_file WHERE hash = :hash", nativeQuery = true)
  Optional<byte[]> findContentByHash(@Param("hash") String hash);

  /**
   * Reads the bytes and the poster of every stored file among the given hashes in one query.
   *
   * @param hashes the hashes of the files
   * @return the bytes of each stored file with its poster, which is {@code null} when it has none
   */
  @Query(
      value =
          "SELECT hash AS hash, content AS content, poster AS poster FROM attachment_file"
              + " WHERE hash IN (:hashes)",
      nativeQuery = true)
  List<FileBytes> findBytesByHashIn(@Param("hashes") Collection<String> hashes);

  /** The stored bytes of one file. */
  interface FileBytes {

    /**
     * Returns the hash of the file.
     *
     * @return the lowercase hexadecimal SHA-256
     */
    String getHash();

    /**
     * Returns the file bytes.
     *
     * @return the content of the file
     */
    byte[] getContent();

    /**
     * Returns the poster image bytes.
     *
     * @return the poster, or {@code null} when the file has none
     */
    byte[] getPoster();
  }

  /**
   * Stores a file unless one with the same hash is already stored. Concurrent calls with the same
   * hash leave exactly one row, and none of them fails.
   *
   * @param hash the lowercase hexadecimal SHA-256 of {@code content}
   * @param mediaType the media type of the file
   * @param sizeBytes the length of {@code content}
   * @param content the file bytes
   * @return 1 when the file was stored, 0 when it already existed
   */
  @Modifying
  @Query(
      value =
          "INSERT INTO attachment_file (hash, media_type, size_bytes, content)"
              + " VALUES (:hash, :mediaType, :sizeBytes, :content) ON CONFLICT (hash) DO NOTHING",
      nativeQuery = true)
  int insertIfAbsent(
      @Param("hash") String hash,
      @Param("mediaType") String mediaType,
      @Param("sizeBytes") long sizeBytes,
      @Param("content") byte[] content);

  /**
   * Reads the poster image of one stored file.
   *
   * @param hash the hash of the file
   * @return the poster bytes, or empty when no file has the hash or the file has no poster
   */
  @Query(value = "SELECT poster FROM attachment_file WHERE hash = :hash", nativeQuery = true)
  Optional<byte[]> findPosterByHash(@Param("hash") String hash);

  /**
   * Fills the media metadata of a stored file, leaving every value the file already has untouched.
   * The poster and its media type are filled together.
   *
   * @param hash the hash of the file
   * @param durationSeconds the playing time, or {@code null}
   * @param width the width in pixels, or {@code null}
   * @param height the height in pixels, or {@code null}
   * @param poster the poster image bytes, or {@code null}
   * @param posterMediaType the media type of {@code poster}, or {@code null}
   */
  @Modifying
  @Query(
      value =
          "UPDATE attachment_file SET"
              + " duration_seconds = COALESCE(duration_seconds, CAST(:durationSeconds AS float8)),"
              + " width = COALESCE(width, CAST(:width AS int4)),"
              + " height = COALESCE(height, CAST(:height AS int4)),"
              + " poster_media_type = CASE WHEN poster IS NULL"
              + " THEN CAST(:posterMediaType AS varchar) ELSE poster_media_type END,"
              + " poster = COALESCE(poster, CAST(:poster AS bytea))"
              + " WHERE hash = :hash",
      nativeQuery = true)
  void fillMissingMetadata(
      @Param("hash") String hash,
      @Param("durationSeconds") Double durationSeconds,
      @Param("width") Integer width,
      @Param("height") Integer height,
      @Param("poster") byte[] poster,
      @Param("posterMediaType") String posterMediaType);

  /**
   * Deletes the files among the given hashes that no definition links to. A file whose row a
   * concurrent transaction holds locked, as it does while inserting a link to it, is skipped, so a
   * link created at the same time keeps its file. Runs as a bulk delete bypassing the persistence
   * context.
   *
   * @param hashes the hashes of the candidate files
   * @return the number of deleted files
   */
  @Modifying
  @Query(
      value =
          "DELETE FROM attachment_file WHERE hash IN (SELECT f.hash FROM attachment_file f"
              + " WHERE f.hash IN (:hashes) AND NOT EXISTS (SELECT 1 FROM definition_attachment l"
              + " WHERE l.file_hash = f.hash) FOR UPDATE SKIP LOCKED)",
      nativeQuery = true)
  int deleteUnlinkedByHashIn(@Param("hashes") Collection<String> hashes);
}
