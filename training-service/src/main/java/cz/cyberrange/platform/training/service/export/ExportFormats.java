package cz.cyberrange.platform.training.service.export;

import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/** Selects the format an export is written in, JSON unless the client asks for another */
@Component
public class ExportFormats {

  private final List<ExportFormat> formats;

  /**
   * Creates the selector over the supported formats, JSON being the default.
   *
   * @param jsonExportFormat the JSON format
   * @param yamlExportFormat the YAML format
   */
  public ExportFormats(JsonExportFormat jsonExportFormat, YamlExportFormat yamlExportFormat) {
    this.formats = List.of(jsonExportFormat, yamlExportFormat);
  }

  /**
   * Returns the format carrying {@code name}, compared ignoring case.
   *
   * @param name the format name a client sent
   * @return the format of that name
   * @throws BadRequestException when no format carries that name
   */
  public ExportFormat byName(String name) {
    return formats.stream()
        .filter(format -> format.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () ->
                new BadRequestException(
                    "Unknown export format '" + name + "'. Supported formats are json and yaml."));
  }

  /**
   * Returns the format matching the most preferred of {@code acceptedMediaTypes}, ranked by quality
   * and specificity. A wildcard matches JSON; with no match, JSON is returned.
   *
   * @param acceptedMediaTypes the media types of the client's {@code Accept} header, possibly empty
   * @return the selected format
   */
  public ExportFormat byAcceptedMediaTypes(List<MediaType> acceptedMediaTypes) {
    List<MediaType> rankedMediaTypes = new ArrayList<>(acceptedMediaTypes);
    MimeTypeUtils.sortBySpecificity(rankedMediaTypes);
    for (MediaType acceptedMediaType : rankedMediaTypes) {
      for (ExportFormat format : formats) {
        if (format.getMediaTypes().stream().anyMatch(acceptedMediaType::isCompatibleWith)) {
          return format;
        }
      }
    }
    return formats.getFirst();
  }
}
