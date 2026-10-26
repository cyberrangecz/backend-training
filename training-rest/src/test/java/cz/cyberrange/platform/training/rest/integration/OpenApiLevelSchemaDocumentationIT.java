package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Checks the OpenAPI document served for the polymorphic level request bodies: each parent schema
 * declares the required, enumerated {@code level_type} discriminator, and each subtype schema
 * inherits it through its {@code allOf} reference without declaring it again.
 */
@DisplayName("OpenAPI documentation of level request schemas")
class OpenApiLevelSchemaDocumentationIT extends AbstractIntegrationTest {

  private static final String SCHEMAS = "$.components.schemas.";

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"AbstractLevelCreateDTO", "AbstractLevelUpdateDTO"})
  @DisplayName("a level parent schema declares the required, enumerated level_type")
  void apiDocument_levelParentSchema_declaresRequiredLevelTypeEnum(String parentSchema)
      throws Exception {
    servedDocument()
        .andExpect(
            jsonPath(SCHEMAS + parentSchema + ".properties.level_type.enum[*]")
                .value(
                    Matchers.containsInAnyOrder(
                        "INFO_LEVEL", "TRAINING_LEVEL", "ACCESS_LEVEL", "ASSESSMENT_LEVEL")))
        .andExpect(
            jsonPath(SCHEMAS + parentSchema + ".required[*]")
                .value(Matchers.hasItem("level_type")));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "AccessLevelCreateDTO",
        "AssessmentLevelCreateDTO",
        "InfoLevelCreateDTO",
        "TrainingLevelCreateDTO",
        "AccessLevelUpdateDTO",
        "AssessmentLevelUpdateDTO",
        "InfoLevelUpdateDTO",
        "TrainingLevelUpdateDTO"
      })
  @DisplayName("a level subtype schema inherits level_type instead of declaring it")
  void apiDocument_levelSubtypeSchema_inheritsLevelType(String subtypeSchema) throws Exception {
    servedDocument()
        .andExpect(
            jsonPath(SCHEMAS + subtypeSchema + ".allOf[*].properties.level_type").doesNotExist())
        .andExpect(jsonPath(SCHEMAS + subtypeSchema + ".properties.level_type").doesNotExist());
  }

  private ResultActions servedDocument() throws Exception {
    return mockMvc
        .perform(get(contextPath + "/v3/api-docs").contextPath(contextPath).with(callerWithRoles()))
        .andExpect(status().isOk());
  }
}
