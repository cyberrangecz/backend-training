package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Integration tests of the {@code sort} request parameter: the rejection of a property an endpoint
 * does not list, the order of that rejection after authorization and the listing of the accepted
 * values in the generated OpenAPI document.
 */
@DisplayName("Sort parameter of paged endpoints")
class SortParameterIT extends AbstractIntegrationTest {

  private static final String DEFINITIONS = "/training-definitions";

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "sort=title,desc",
        "sort=title,desc,ignorecase",
        "sort=title,asc&sort=createdAt,desc"
      })
  @DisplayName("a request sorting only by listed properties is accepted")
  void listDefinitions_listedSortProperties_returnsOk(String sortQuery) throws Exception {
    administratorGet(DEFINITIONS + "?" + sortQuery).andExpect(status().isOk());
  }

  @ParameterizedTest(name = "{0} names {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "sort=password,asc;password",
        "sort=title,asc&sort=password,desc;password",
        "sort=%20unknown;unknown",
        "sort=startTime,%20desc;startTime"
      })
  @DisplayName("a request sorting by an unlisted property is answered with a 400 problem detail")
  void listDefinitions_unlistedSortProperty_returnsBadRequestProblemDetail(
      String sortQuery, String unlistedProperty) throws Exception {
    administratorGet(DEFINITIONS + "?" + sortQuery)
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value(Matchers.containsString(unlistedProperty)));
  }

  @ParameterizedTest(name = "{0} is answered with {1}")
  @CsvSource({
    "ROLE_TRAINING_TRAINEE,403",
    "ROLE_TRAINING_ORGANIZER,403",
    "ROLE_TRAINING_ADMINISTRATOR,400"
  })
  @DisplayName("an unlisted sort property is refused only after the caller is authorized")
  void listDefinitions_unlistedSortProperty_authorizationAnswersBeforeSortValidation(
      RoleTypeSecurity role, int expectedStatus) throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(DEFINITIONS + "?sort=password,asc")
                .with(callerWithRoles(role)))
        .andExpect(status().is(expectedStatus));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("userListingTemplates")
  @DisplayName("a user list refuses a sort property the user-and-group service does not list")
  void listUsers_unlistedSortProperty_returnsBadRequest(String template) throws Exception {
    administratorGet(template + "?sort=title,asc", 1L).andExpect(status().isBadRequest());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "/training-definitions,createdAt,desc",
    "/training-runs/accessible,trainingInstance.title,asc",
    "/training-instances/{instanceId}/organizers,familyName,desc"
  })
  @DisplayName("the OpenAPI document lists the sortable values and a 400 response of an operation")
  void apiDocument_pagedOperation_listsSortableValuesAndBadRequestResponse(
      String path, String property, String direction) throws Exception {
    String operation = "$.paths['" + path + "'].get";
    String sortItems = operation + ".parameters[?(@.name == 'sort')].schema.items.enum[*]";

    administratorGet("/v3/api-docs")
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(sortItems).value(Matchers.hasItems("id,asc", property + "," + direction)))
        .andExpect(jsonPath(sortItems).value(Matchers.not(Matchers.hasItem("password,asc"))))
        .andExpect(jsonPath(operation + ".responses['400']").exists());
  }

  private ResultActions administratorGet(String uriTemplate, Object... uriVariables)
      throws Exception {
    return mockMvc.perform(
        MockMvcRequestBuilders.get(uriTemplate, uriVariables)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)));
  }
}
