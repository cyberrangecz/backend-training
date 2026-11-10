package cz.cyberrange.platform.training.rest.utils;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.service.services.attachment.SuppliedAttachment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** Turns the file parts of a multipart request into the files the service layer stores. */
public final class MultipartAttachments {

  private static final int MAXIMAL_FILE_NAME_LENGTH = 255;

  private MultipartAttachments() {}

  /**
   * Reads the file parts of a request.
   *
   * <p>Each file keeps only the last segment of the name its part carries, without control
   * characters. A part with no name yields an empty file name.
   *
   * <p>Each file takes the media metadata entry carrying its hash; entries for no supplied file are
   * ignored, and the first of several entries with one hash wins.
   *
   * @param files the file parts of the request, or {@code null} when the request carried none
   * @param fileMetadata the metadata part of the request, or {@code null} when it carried none
   * @return the supplied files in part order, empty when there were no parts
   * @throws BadRequestException when a file name is longer than a stored file name may be, or a
   *     metadata entry has no hash
   * @throws UncheckedIOException when the bytes of a part cannot be read
   */
  public static List<SuppliedAttachment> toSuppliedAttachments(
      List<MultipartFile> files, List<AttachmentFileMetadataDTO> fileMetadata) {
    if (files == null) {
      return List.of();
    }
    Map<String, AttachmentFileMetadataDTO> metadataByHash = metadataByHash(fileMetadata);
    return files.stream()
        .map(MultipartAttachments::toSuppliedAttachment)
        .map(file -> file.withMetadata(metadataByHash.get(file.hash())))
        .toList();
  }

  /**
   * Reads the one file part of an upload request, carrying its media metadata.
   *
   * <p>The file keeps only the last segment of the name its part carries, without control
   * characters; a part with no name yields an empty file name.
   *
   * @param file the file part of the request
   * @param metadata the media metadata part of the request
   * @return the file carrying the metadata
   * @throws BadRequestException when the file name is longer than a stored file name may be
   * @throws UncheckedIOException when the bytes of the part cannot be read
   */
  public static SuppliedAttachment toSuppliedAttachment(
      MultipartFile file, AttachmentFileMetadataDTO metadata) {
    return toSuppliedAttachment(file).withMetadata(metadata);
  }

  private static Map<String, AttachmentFileMetadataDTO> metadataByHash(
      List<AttachmentFileMetadataDTO> fileMetadata) {
    if (fileMetadata == null) {
      return Map.of();
    }
    if (fileMetadata.stream().filter(Objects::nonNull).anyMatch(entry -> entry.hash() == null)) {
      throw new BadRequestException("Every media metadata entry needs the hash of its file.");
    }
    return fileMetadata.stream()
        .filter(Objects::nonNull)
        .collect(
            Collectors.toMap(
                AttachmentFileMetadataDTO::hash, Function.identity(), (first, second) -> first));
  }

  private static SuppliedAttachment toSuppliedAttachment(MultipartFile file) {
    String fileName = normalisedFileName(file.getOriginalFilename());
    if (fileName.length() > MAXIMAL_FILE_NAME_LENGTH) {
      throw new BadRequestException(
          "An attachment file name may have at most " + MAXIMAL_FILE_NAME_LENGTH + " characters.");
    }
    try {
      return SuppliedAttachment.of(fileName, file.getBytes());
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read the attachment file " + fileName, e);
    }
  }

  private static String normalisedFileName(String originalFileName) {
    if (originalFileName == null) {
      return "";
    }
    String lastSegment = StringUtils.getFilename(StringUtils.cleanPath(originalFileName));
    return lastSegment == null ? "" : lastSegment.replaceAll("\\p{Cntrl}", "").strip();
  }
}
