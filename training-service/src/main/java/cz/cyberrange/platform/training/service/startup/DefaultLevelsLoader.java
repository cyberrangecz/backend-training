package cz.cyberrange.platform.training.service.startup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Holds the level content a newly created level starts out with, read once as the component is
 * built. The content comes from the configured file, or from the copy bundled with the application
 * when no path is configured.
 */
@Component
public class DefaultLevelsLoader {

  @Value("${path.to.default.levels:}")
  private String pathToDefaultLevels;

  private DefaultLevels defaultLevels;
  private final Validator validator;

  @Autowired
  public DefaultLevelsLoader(Validator validator) {
    this.validator = validator;
  }

  /**
   * Reads and validates the default level content, refusing to start the component rather than
   * carrying content that is unusable. Property names are read in snake case, and a property the
   * shape does not declare is rejected instead of ignored, so an unrecognised key fails the startup
   * rather than passing silently.
   *
   * @throws InternalServerErrorException when the content cannot be read, or when any required
   *     piece of it is missing or empty
   */
  @PostConstruct
  private void loadDefaultLevels() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    try {
      InputStream inputStream =
          pathToDefaultLevels.isBlank()
              ? getClass().getResourceAsStream("/default-levels.json")
              : new FileInputStream(pathToDefaultLevels);
      defaultLevels = mapper.readValue(inputStream, DefaultLevels.class);
      Set<ConstraintViolation<DefaultLevels>> violations = this.validator.validate(defaultLevels);
      if (!violations.isEmpty()) {
        throw new InternalServerErrorException(
            "Could not load the default phases. Reason: "
                + violations.stream().map(ConstraintViolation::getMessage).toList());
      }
    } catch (IOException e) {
      throw new InternalServerErrorException("Could not load file with the default levels.", e);
    }
  }

  public DefaultAccessLevel getDefaultAccessLevel() {
    return this.defaultLevels.getDefaultAccessLevel();
  }

  public DefaultInfoLevel getDefaultInfoLevel() {
    return this.defaultLevels.getDefaultInfoLevel();
  }
}
