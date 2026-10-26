package cz.cyberrange.platform.training.rest.utils.sort;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import java.util.stream.Stream;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Rewrites the {@code sort} parameter of every operation annotated with {@link SortableProperties}
 * in the generated OpenAPI document: the {@code property,(asc|desc)} format, the default order and
 * the always appended {@code id} key are described, the accepted values are listed as an enum and a
 * {@code 400} response is added.
 */
@Component
public class SortParameterOperationCustomizer implements OperationCustomizer {

  private static final String SORT_PARAMETER_NAME = "sort";
  private static final String BAD_REQUEST_STATUS = "400";
  private static final String ID_APPENDED_SENTENCE =
      "The id property is always appended as the last criterion, so rows that tie on every other"
          + " criterion keep one order across pages.";

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    SortableProperties sortableProperties =
        handlerMethod.getMethodAnnotation(SortableProperties.class);
    if (sortableProperties != null && operation.getParameters() != null) {
      operation.getParameters().stream()
          .filter(parameter -> SORT_PARAMETER_NAME.equals(parameter.getName()))
          .findFirst()
          .ifPresent(
              sortParameter -> {
                describeSortableProperties(sortParameter, sortableProperties);
                addBadRequestResponse(operation);
              });
    }
    return operation;
  }

  private static void describeSortableProperties(
      Parameter sortParameter, SortableProperties sortableProperties) {
    List<String> properties = sortableProperties.value().properties();
    sortParameter.setDescription(
        "Sorting criteria in the format property,(asc|desc), repeated for several criteria."
            + " Sortable properties: "
            + String.join(", ", properties)
            + ". Without a sort, the order is "
            + sortableProperties.defaultProperty()
            + " ascending. "
            + ID_APPENDED_SENTENCE
            + " A property outside the list is answered with 400.");
    StringSchema itemSchema = new StringSchema();
    properties.stream()
        .flatMap(property -> Stream.of(property + ",asc", property + ",desc"))
        .forEach(itemSchema::addEnumItem);
    sortParameter.getSchema().setItems(itemSchema);
  }

  private static void addBadRequestResponse(Operation operation) {
    if (operation.getResponses() == null) {
      operation.setResponses(new ApiResponses());
    }
    operation
        .getResponses()
        .computeIfAbsent(
            BAD_REQUEST_STATUS,
            status ->
                new ApiResponse().description("The sort names a property that is not sortable."));
  }
}
