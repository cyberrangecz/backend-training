package cz.cyberrange.platform.training.rest.utils.error;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Turns a failure to read a hand-written file submitted for import into a description addressed to
 * the person who wrote that file: which field was rejected and where in the file it sits. Every
 * description is one text holding several statements, one per line, naming only content of the file
 * itself.
 */
@Component
public class ImportedFileErrorDescriber {

  private static final String LINE_SEPARATOR = "\n";
  private static final String VALUE_SEPARATOR = ", ";
  private static final String DESCRIPTION_SEPARATOR = " \u2014 ";
  private static final String ROOT_LOCATION = "the top level of the file";
  private static final String HEADLINE = "The training definition could not be imported";

  private final ObjectMapper objectMapper;

  /**
   * Creates the describer.
   *
   * @param objectMapper the mapper whose configuration decides which field names the file is
   *     expected to carry
   */
  public ImportedFileErrorDescriber(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Describes why the submitted file could not be turned into an object at all, covering an
   * unrecognized field, an unsupported level type, a value that does not fit its field, and text
   * that is not JSON.
   *
   * @param exception the failure raised while reading the request body
   * @return the description, its statements separated by line breaks
   */
  public String describeUnreadableContent(HttpMessageNotReadableException exception) {
    Throwable cause = exception.getCause();
    if (cause instanceof UnrecognizedPropertyException unrecognizedProperty) {
      return describeUnrecognizedField(unrecognizedProperty);
    }
    if (cause instanceof InvalidTypeIdException invalidTypeId) {
      return describeUnknownSubtype(invalidTypeId);
    }
    if (cause instanceof InvalidFormatException invalidFormat) {
      return describeUnacceptedValue(invalidFormat);
    }
    if (cause instanceof MismatchedInputException mismatchedInput) {
      return describeMalformedField(mismatchedInput);
    }
    if (cause instanceof JsonParseException brokenSyntax) {
      return describeBrokenSyntax(brokenSyntax);
    }
    return joinLines(HEADLINE, exception.getMostSpecificCause().getMessage());
  }

  /**
   * Describes which fields of the submitted file hold refused content, one statement per field,
   * each naming the field by its position in the file.
   *
   * @param exception the failure raised while validating the object read from the request body
   * @return the description, its statements separated by line breaks
   */
  public String describeInvalidFields(MethodArgumentNotValidException exception) {
    Stream<String> violations =
        exception.getBindingResult().getAllErrors().stream().map(this::describeViolation);
    return joinLines(Stream.concat(Stream.of(HEADLINE), violations).collect(Collectors.toList()));
  }

  private String describeUnrecognizedField(UnrecognizedPropertyException exception) {
    String fieldName = exception.getPropertyName();
    String location = describeLocation(renderContainerPath(exception.getPath(), fieldName));
    return joinLines(
        HEADLINE,
        "Field \"" + fieldName + "\" in " + location + " is not a recognized field",
        "Permitted fields in "
            + location
            + ": "
            + describeAcceptedFields(exception.getReferringClass()));
  }

  private String describeUnknownSubtype(InvalidTypeIdException exception) {
    Class<?> baseClass = exception.getBaseType().getRawClass();
    String discriminator =
        Optional.ofNullable(baseClass.getAnnotation(JsonTypeInfo.class))
            .map(JsonTypeInfo::property)
            .orElse("type");
    String location = describeLocation(renderPath(exception.getPath()));
    String submittedValue = exception.getTypeId();
    return joinLines(
        HEADLINE,
        submittedValue == null
            ? "Field \""
                + discriminator
                + "\" is required in "
                + location
                + " and determines the type of level described by the remaining fields"
            : "Field \""
                + discriminator
                + "\" in "
                + location
                + " contains an unsupported value "
                + renderValue(submittedValue),
        "Supported values: " + describeAcceptedSubtypes(baseClass));
  }

  private String describeUnacceptedValue(InvalidFormatException exception) {
    return joinLines(
        HEADLINE,
        describeField(exception.getPath())
            + " contains an unsupported value "
            + renderValue(exception.getValue()),
        describeAcceptedValues(exception.getTargetType()));
  }

  private String describeMalformedField(MismatchedInputException exception) {
    String requiredShape = describeShape(exception.getTargetType());
    return joinLines(
        HEADLINE,
        requiredShape == null
            ? describeField(exception.getPath()) + " does not have the required form"
            : describeField(exception.getPath()) + " must contain " + requiredShape);
  }

  private String describeBrokenSyntax(JsonParseException exception) {
    return joinLines(
        HEADLINE, "The file is not well-formed JSON", describeSyntaxFailure(exception));
  }

  private String describeSyntaxFailure(JsonParseException exception) {
    JsonLocation location = exception.getLocation();
    String position =
        location == null || location.getLineNr() < 1
            ? "Syntax error"
            : "Syntax error on line " + location.getLineNr() + ", column " + location.getColumnNr();
    return position + ": " + exception.getOriginalMessage();
  }

  private String describeShape(Class<?> targetType) {
    if (targetType == null) {
      return null;
    }
    if (Collection.class.isAssignableFrom(targetType) || targetType.isArray()) {
      return "a list";
    }
    if (Map.class.isAssignableFrom(targetType)) {
      return "a group of fields";
    }
    if (CharSequence.class.isAssignableFrom(targetType)) {
      return "text";
    }
    if (Number.class.isAssignableFrom(targetType)) {
      return "a number";
    }
    if (Boolean.class.isAssignableFrom(targetType)) {
      return "true or false";
    }
    return null;
  }

  private String describeField(List<JsonMappingException.Reference> path) {
    String renderedPath = renderPath(path);
    return renderedPath.isEmpty() ? "The submitted content" : "Field \"" + renderedPath + "\"";
  }

  private String describeViolation(ObjectError violation) {
    if (violation instanceof FieldError fieldViolation) {
      return renderFilePath(fieldViolation.getField())
          + DESCRIPTION_SEPARATOR
          + fieldViolation.getDefaultMessage();
    }
    return violation.getDefaultMessage();
  }

  private String renderFilePath(String propertyPath) {
    return Arrays.stream(propertyPath.split("\\."))
        .map(this::renderFilePathSegment)
        .collect(Collectors.joining("."));
  }

  private String renderFilePathSegment(String segment) {
    int indexStart = segment.indexOf('[');
    String name = indexStart < 0 ? segment : segment.substring(0, indexStart);
    String indexes = indexStart < 0 ? "" : segment.substring(indexStart);
    return renderFileFieldName(name) + indexes;
  }

  private String renderFileFieldName(String propertyName) {
    return objectMapper.getPropertyNamingStrategy()
            instanceof PropertyNamingStrategies.NamingBase namingStrategy
        ? namingStrategy.translate(propertyName)
        : propertyName;
  }

  private String describeAcceptedFields(Class<?> containerClass) {
    if (containerClass == null) {
      return "none";
    }
    return propertiesOf(containerClass).stream()
        .map(BeanPropertyDefinition::getName)
        .collect(Collectors.joining(VALUE_SEPARATOR));
  }

  private String describeAcceptedSubtypes(Class<?> baseClass) {
    return Optional.ofNullable(baseClass.getAnnotation(JsonSubTypes.class))
        .map(JsonSubTypes::value)
        .map(
            subtypes ->
                Arrays.stream(subtypes)
                    .map(JsonSubTypes.Type::name)
                    .distinct()
                    .collect(Collectors.joining(VALUE_SEPARATOR)))
        .orElse("none");
  }

  private String describeAcceptedValues(Class<?> targetType) {
    if (targetType == null || !targetType.isEnum()) {
      return null;
    }
    return "Supported values: "
        + Arrays.stream(targetType.getEnumConstants())
            .map(String::valueOf)
            .collect(Collectors.joining(VALUE_SEPARATOR));
  }

  private List<BeanPropertyDefinition> propertiesOf(Class<?> containerClass) {
    BeanDescription description =
        objectMapper
            .getDeserializationConfig()
            .introspect(objectMapper.constructType(containerClass));
    return description.findProperties();
  }

  private String renderContainerPath(
      List<JsonMappingException.Reference> path, String trailingFieldName) {
    int end = path.size();
    while (end > 0 && Objects.equals(path.get(end - 1).getFieldName(), trailingFieldName)) {
      end--;
    }
    return renderPath(path.subList(0, end));
  }

  private String renderPath(List<JsonMappingException.Reference> path) {
    StringBuilder rendered = new StringBuilder();
    for (JsonMappingException.Reference reference : path) {
      if (reference.getFieldName() == null) {
        rendered.append('[').append(reference.getIndex()).append(']');
      } else {
        if (rendered.length() > 0) {
          rendered.append('.');
        }
        rendered.append(reference.getFieldName());
      }
    }
    return rendered.toString();
  }

  private String describeLocation(String renderedPath) {
    return renderedPath.isEmpty() ? ROOT_LOCATION : renderedPath;
  }

  private String withoutTerminalStop(String sentence) {
    return sentence.endsWith(".") ? sentence.substring(0, sentence.length() - 1) : sentence;
  }

  private String renderValue(Object value) {
    return value == null ? "null" : "\"" + value + "\"";
  }

  private String joinLines(String... lines) {
    return joinLines(Arrays.asList(lines));
  }

  private String joinLines(List<String> lines) {
    return lines.stream()
        .filter(Objects::nonNull)
        .filter(line -> !line.isBlank())
        .map(this::withoutTerminalStop)
        .collect(Collectors.joining(LINE_SEPARATOR));
  }
}
