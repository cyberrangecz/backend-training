package cz.cyberrange.platform.training.service.export;

import java.util.List;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

/**
 * A file format training content is exported in. It names the format, lists the media types a
 * client requests it by and the extension its files carry, and serializes values through its
 * mapper.
 */
public abstract class ExportFormat {

  private final String name;
  private final List<MediaType> mediaTypes;
  private final String fileExtension;
  protected final ObjectMapper mapper;

  /**
   * Creates the format.
   *
   * @param name the name a client selects the format by
   * @param mediaTypes the media types a client requests the format by
   * @param fileExtension the extension a file in this format carries, leading dot included
   * @param mapper the mapper values are serialized through
   */
  protected ExportFormat(
      String name, List<MediaType> mediaTypes, String fileExtension, ObjectMapper mapper) {
    this.name = name;
    this.mediaTypes = List.copyOf(mediaTypes);
    this.fileExtension = fileExtension;
    this.mapper = mapper;
  }

  /**
   * Returns the name a client selects the format by.
   *
   * @return the format's name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the media types a client requests the format by.
   *
   * @return the format's media types
   */
  public List<MediaType> getMediaTypes() {
    return mediaTypes;
  }

  /**
   * Returns the media type a file in this format is served under, the first of its media types.
   *
   * @return the format's content type
   */
  public MediaType getContentType() {
    return mediaTypes.getFirst();
  }

  /**
   * Returns the extension a file in this format carries, leading dot included.
   *
   * @return the format's file extension
   */
  public String getFileExtension() {
    return fileExtension;
  }

  /**
   * Serializes {@code value} as one complete document.
   *
   * @param value the value to serialize
   * @return the document's bytes
   * @throws tools.jackson.core.JacksonException when {@code value} cannot be serialized
   */
  public byte[] writeDocument(Object value) {
    return mapper.writeValueAsBytes(value);
  }

  /**
   * Serializes {@code value} as one record of a file that holds one record after another, separator
   * included, so records written one after another form a valid file.
   *
   * @param value the value to serialize
   * @return the record's bytes
   * @throws tools.jackson.core.JacksonException when {@code value} cannot be serialized
   */
  public abstract byte[] writeRecord(Object value);
}
