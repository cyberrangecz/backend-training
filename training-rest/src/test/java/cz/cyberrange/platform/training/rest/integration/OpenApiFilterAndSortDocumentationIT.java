package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Checks the OpenAPI document served for the QueryDSL-filtered, paged operations: each documents
 * the required {@code predicate} query parameter next to the enumerated {@code sort} values, and
 * the document's server URL carries the context path.
 */
@DisplayName("OpenAPI documentation of filter and sort parameters")
class OpenApiFilterAndSortDocumentationIT extends AbstractIntegrationTest {

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @ParameterizedTest(name = "{0}")
  @MethodSource("filteredPagedOperations")
  @DisplayName("a filtered paged operation documents a required predicate and its sort values")
  void apiDocument_filteredPagedOperation_documentsPredicateAndSortValues(
      String path, String sortValue) throws Exception {
    String parameters = "$.paths['" + path + "'].get.parameters";

    servedDocument()
        .andExpect(jsonPath(parameters + "[?(@.name == 'predicate')].required").value(true))
        .andExpect(
            jsonPath(parameters + "[?(@.name == 'sort')].schema.items.enum[*]")
                .value(Matchers.hasItems("id,asc", sortValue)));
  }

  @Test
  @DisplayName("the document's server URL carries the context path")
  void apiDocument_serverUrl_carriesContextPath() throws Exception {
    servedDocument()
        .andExpect(jsonPath("$.servers[*].url").value("http://localhost" + contextPath));
  }

  private static Stream<Arguments> filteredPagedOperations() {
    return Stream.of(
        Arguments.of("/training-definitions", "createdAt,desc"),
        Arguments.of("/training-instances", "title,asc"),
        Arguments.of("/training-runs", "id,desc"));
  }

  private ResultActions servedDocument() throws Exception {
    return mockMvc
        .perform(get(contextPath + "/v3/api-docs").contextPath(contextPath).with(callerWithRoles()))
        .andExpect(status().isOk());
  }
}
