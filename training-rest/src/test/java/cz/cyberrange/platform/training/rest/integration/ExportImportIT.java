package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Exercises the export and import endpoints through the whole application: content negotiation,
 * roles, documented status codes, file contents and the calls made to the stubbed external
 * services.
 */
class ExportImportIT extends AbstractIntegrationTest {

  private static final long ADMINISTRATOR_USER_REF_ID = 6001L;
  private static final long DESIGNER_USER_REF_ID = 6002L;
  private static final long OTHER_DESIGNER_USER_REF_ID = 6003L;
  private static final long ORGANIZER_USER_REF_ID = 6004L;
  private static final long OTHER_ORGANIZER_USER_REF_ID = 6005L;
  private static final long TRAINEE_USER_REF_ID = 6006L;
  private static final long UNKNOWN_ID = 987654L;
  private static final long POOL_ID = 21L;
  private static final String SANDBOX_ID = "33333333-3333-4333-8333-333333333333";
  private static final MediaType APPLICATION_ZIP = MediaType.valueOf("application/zip");
  private static final MediaType TEXT_YAML = MediaType.valueOf("text/yaml");
  private static final MediaType TEXT_YML = MediaType.valueOf("text/yml");
  private static final String EXPORTED_DEFINITION_YAML_DISPOSITION =
      "inline; filename=\"Exported definition.yaml\"; filename*=UTF-8''Exported%20definition.yaml";

  private static final RoleTypeSecurity ADMINISTRATOR =
      RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR;
  private static final RoleTypeSecurity DESIGNER = RoleTypeSecurity.ROLE_TRAINING_DESIGNER;
  private static final RoleTypeSecurity ORGANIZER = RoleTypeSecurity.ROLE_TRAINING_ORGANIZER;
  private static final RoleTypeSecurity TRAINEE = RoleTypeSecurity.ROLE_TRAINING_TRAINEE;

  private static final String IMPORT_JSON =
      """
      {
        "title": "Imported definition",
        "description": "Imported description",
        "prerequisites": ["basics"],
        "outcomes": ["skills"],
        "state": "RELEASED",
        "estimated_duration": 999,
        "levels": [
          {"level_type": "INFO_LEVEL", "title": "Read me", "content": "info content",
           "estimated_duration": 4},
          {"level_type": "TRAINING_LEVEL", "title": "Solve me", "content": "task content",
           "answer": "flag{1}", "solution": "solution text", "solution_penalized": true,
           "incorrect_answer_limit": 3, "max_score": 20, "variant_answers": false,
           "commands_required": false, "estimated_duration": 6,
           "hints": [{"title": "first hint", "content": "hint content", "hint_penalty": 5,
                      "order": 0}]},
          {"level_type": "ACCESS_LEVEL", "title": "Connect", "passkey": "open",
           "cloud_content": "cloud access", "local_content": "local access",
           "estimated_duration": 2}
        ]
      }
      """;

  private static final String IMPORT_YAML =
      """
      title: Imported yaml definition
      description: Imported description
      levels:
        - level_type: INFO_LEVEL
          title: Read me
          content: info content
          estimated_duration: 4
      """;

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private EntityManager entityManager;

  private TrainingRunScenarioSeeder seeder;
  private UserRef designer;
  private UserRef organizer;
  private UserRef trainee;
  private TrainingDefinition definition;
  private TrainingInstance instance;

  @BeforeEach
  void seedScenario() {
    seeder = new TrainingRunScenarioSeeder(transactionTemplate, entityManager, jdbcTemplate);
    seeder.deleteAll();
    designer = seeder.userRef(DESIGNER_USER_REF_ID);
    seeder.userRef(OTHER_DESIGNER_USER_REF_ID);
    organizer = seeder.userRef(ORGANIZER_USER_REF_ID);
    seeder.userRef(OTHER_ORGANIZER_USER_REF_ID);
    trainee = seeder.userRef(TRAINEE_USER_REF_ID);
    definition = seeder.definition("Exported definition", designer);
    seeder.infoLevel(definition, 0);
    seeder.trainingLevel(definition, 1, true);
    instance =
        seeder.instance(
            definition,
            "export-token",
            POOL_ID,
            false,
            TrainingRunScenarioSeeder.now().minusHours(1),
            TrainingRunScenarioSeeder.now().plusHours(1),
            organizer);
    stubOpenSearchWithoutData();
  }

  @AfterEach
  void deleteScenario() {
    seeder.deleteAll();
  }

  private void stubOpenSearchWithoutData() {
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*/_search"))
            .willReturn(
                okJson(
                    "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,"
                        + "\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":0,"
                        + "\"relation\":\"eq\"},\"max_score\":null,\"hits\":[]}}")));
  }

  private void stubSandboxDefinition() {
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.SANDBOX_SERVICE_PATH
                        + "/pools/"
                        + POOL_ID
                        + "/definition"))
            .willReturn(
                okJson(
                    "{\"id\":77,\"name\":\"sandbox definition\","
                        + "\"url\":\"https://example.org/definition.git\",\"rev\":\"main\"}")));
  }

  private void stubUsers(long... userRefIds) {
    StringBuilder users = new StringBuilder();
    for (long userRefId : userRefIds) {
      if (users.length() > 0) {
        users.append(',');
      }
      users.append(
          "{\"user_ref_id\":%d,\"sub\":\"login-%d\",\"full_name\":\"Person %d\",\"given_name\":\"Person\",\"family_name\":\"%d\",\"iss\":\"issuer\",\"mail\":\"person%d@example.org\"}"
              .formatted(userRefId, userRefId, userRefId, userRefId, userRefId));
    }
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/users/ids"))
            .willReturn(
                okJson(
                    "{\"content\":["
                        + users
                        + "],\"pagination\":{\"number\":0,\"number_of_elements\":"
                        + userRefIds.length
                        + ",\"size\":999,\"total_elements\":"
                        + userRefIds.length
                        + ",\"total_pages\":1}}")));
  }

  private ResultActions call(
      long callerUserRefId, RoleTypeSecurity role, MockHttpServletRequestBuilder request)
      throws Exception {
    stubLoggedInUser(callerUserRefId);
    return mockMvc.perform(request.with(callerWithRoles(role)));
  }

  private ResultActions importDefinition(
      long callerUserRefId, RoleTypeSecurity role, MediaType contentType, String body)
      throws Exception {
    return call(
        callerUserRefId,
        role,
        post("/imports/training-definitions")
            .contentType(contentType)
            .accept(MediaType.APPLICATION_JSON)
            .content(body));
  }

  private long count(String table, String where, Object... arguments) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM " + table + " WHERE " + where, Long.class, arguments);
  }

  private static void expectError(ResultActions result, int status, String statusName, String path)
      throws Exception {
    result
        .andExpect(status().is(status))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(statusName))
        .andExpect(jsonPath("$.path").value(path))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  private static void expectEntityError(
      ResultActions result, int status, String statusName, String path, String entity)
      throws Exception {
    expectError(result, status, statusName, path);
    result.andExpect(jsonPath("$.entity_error_detail.entity").value(entity));
    result.andExpect(jsonPath("$.entity_error_detail.reason").isNotEmpty());
  }

  private static List<String> zipEntryNames(MvcResult result) throws IOException {
    List<String> names = new ArrayList<>();
    try (ZipInputStream zip =
        new ZipInputStream(
            new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        names.add(entry.getName());
      }
    }
    return names;
  }

  private static String zipEntryContent(MvcResult result, String entryName) throws IOException {
    try (ZipInputStream zip =
        new ZipInputStream(
            new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        if (entry.getName().equals(entryName)) {
          return new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
      }
    }
    throw new AssertionError("No archive entry named " + entryName);
  }

  static Stream<Arguments> everyEndpoint() {
    return Stream.of(
        Arguments.of(HttpMethod.GET, "/exports/training-definitions/1"),
        Arguments.of(HttpMethod.POST, "/imports/training-definitions"),
        Arguments.of(HttpMethod.GET, "/exports/training-instances/1"),
        Arguments.of(HttpMethod.GET, "/exports/training-instances/1/scores"));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("everyEndpoint")
  @DisplayName(
      "A request without a bearer token to any export or import endpoint is rejected as unauthenticated")
  void exportImportEndpoints_withoutAuthentication_returnUnauthorizedOrForbidden(
      HttpMethod method, String uri) throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.request(method, uri))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "Exporting a definition with a JSON Accept returns the definition and its levels as inline JSON")
  void exportDefinition_acceptJson_returnsJsonBody() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(
            header()
                .string(
                    "Content-Disposition",
                    "inline; filename=\"Exported definition.json\";"
                        + " filename*=UTF-8''Exported%20definition.json"))
        .andExpect(jsonPath("$.title").value("Exported definition"))
        .andExpect(jsonPath("$.levels", hasSize(2)))
        .andExpect(
            jsonPath("$.levels[*].level_type").value(contains("INFO_LEVEL", "TRAINING_LEVEL")));
  }

  @Test
  @DisplayName("Exporting a definition with a wildcard Accept returns JSON")
  void exportDefinition_acceptAnything_returnsJsonBody() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId()).accept(MediaType.ALL))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Exported definition"));
  }

  @Test
  @DisplayName("Exporting a definition without an Accept header returns JSON")
  void exportDefinition_noAcceptHeader_returnsJsonBody() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName("Exporting a definition with an application/yaml Accept returns an inline YAML file")
  void exportDefinition_acceptApplicationYaml_returnsYamlBody() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId())
                .accept(MediaType.APPLICATION_YAML))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_YAML))
        .andExpect(header().string("Content-Disposition", EXPORTED_DEFINITION_YAML_DISPOSITION))
        .andExpect(content().string(startsWith("---")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("title: ")));
  }

  @Test
  @DisplayName("Exporting a definition with a text/yaml Accept returns YAML")
  void exportDefinition_acceptTextYaml_returnsYamlBody() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId()).accept(TEXT_YAML))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", EXPORTED_DEFINITION_YAML_DISPOSITION))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("title: ")));
  }

  @Test
  @DisplayName("Exporting a definition with a text/yml Accept returns YAML")
  void exportDefinition_acceptTextYml_returnsYamlBody() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId()).accept(TEXT_YML))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", EXPORTED_DEFINITION_YAML_DISPOSITION))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("title: ")));
  }

  @Test
  @DisplayName("Exporting a definition as its author succeeds")
  void exportDefinition_authorDesigner_returnsJsonBody() throws Exception {
    call(DESIGNER_USER_REF_ID, DESIGNER, get("/exports/training-definitions/" + definition.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Exported definition"));
  }

  @Test
  @DisplayName("Exporting a definition is forbidden for a designer who does not author it")
  void exportDefinition_designerWhoIsNotAuthor_returnsForbidden() throws Exception {
    expectError(
        call(
            OTHER_DESIGNER_USER_REF_ID,
            DESIGNER,
            get("/exports/training-definitions/" + definition.getId())),
        403,
        "FORBIDDEN",
        "/exports/training-definitions/" + definition.getId());
  }

  @Test
  @DisplayName("Exporting a definition is forbidden for a trainee")
  void exportDefinition_traineeRole_returnsForbidden() throws Exception {
    expectError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/exports/training-definitions/" + definition.getId())),
        403,
        "FORBIDDEN",
        "/exports/training-definitions/" + definition.getId());
  }

  @Test
  @DisplayName("Exporting an unknown definition is not found")
  void exportDefinition_unknownDefinition_returnsNotFound() throws Exception {
    expectEntityError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + UNKNOWN_ID)),
        404,
        "NOT_FOUND",
        "/exports/training-definitions/" + UNKNOWN_ID,
        "TrainingDefinition");
  }

  @Test
  @DisplayName("Exporting a definition whose id is not a number is a bad request")
  void exportDefinition_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/exports/training-definitions/abc")),
        400,
        "BAD_REQUEST",
        "/exports/training-definitions/abc");
  }

  @Test
  @DisplayName(
      "Exporting a definition with an Accept that allows neither JSON nor YAML is not acceptable")
  void exportDefinition_acceptOnlyOctetStream_returnsNotAcceptable() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-definitions/" + definition.getId())
                .accept(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  @DisplayName(
      "Importing a JSON definition creates an unreleased definition with its levels in the order sent")
  void importDefinition_jsonBody_createsUnreleasedDefinitionWithLevelsInSentOrder()
      throws Exception {
    importDefinition(DESIGNER_USER_REF_ID, DESIGNER, MediaType.APPLICATION_JSON, IMPORT_JSON)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.title").value("Imported definition"))
        .andExpect(jsonPath("$.state").value("UNRELEASED"))
        .andExpect(jsonPath("$.estimated_duration").value(12))
        .andExpect(jsonPath("$.levels", hasSize(3)))
        .andExpect(
            jsonPath("$.levels[*].level_type")
                .value(contains("INFO_LEVEL", "TRAINING_LEVEL", "ACCESS_LEVEL")))
        .andExpect(jsonPath("$.levels[*].order").value(contains(0, 1, 2)))
        .andExpect(jsonPath("$.levels[1].hints", hasSize(1)));

    assertThat(
            count("training_definition", "title = 'Imported definition' AND state = 'UNRELEASED'"))
        .isEqualTo(1);
    assertThat(count("abstract_level", "title IN ('Read me', 'Solve me', 'Connect')")).isEqualTo(3);
  }

  @Test
  @DisplayName("Importing a YAML definition creates the definition")
  void importDefinition_yamlBody_createsDefinition() throws Exception {
    importDefinition(DESIGNER_USER_REF_ID, DESIGNER, MediaType.APPLICATION_YAML, IMPORT_YAML)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Imported yaml definition"))
        .andExpect(jsonPath("$.state").value("UNRELEASED"))
        .andExpect(jsonPath("$.estimated_duration").value(4))
        .andExpect(jsonPath("$.levels", hasSize(1)));
  }

  @Test
  @DisplayName("Importing a definition as administrator succeeds")
  void importDefinition_administrator_createsDefinition() throws Exception {
    importDefinition(
            ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, MediaType.APPLICATION_JSON, IMPORT_JSON)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("UNRELEASED"));
  }

  @Test
  @DisplayName("Importing a definition is forbidden for an organizer")
  void importDefinition_organizerRole_returnsForbiddenAndCreatesNothing() throws Exception {
    expectError(
        importDefinition(ORGANIZER_USER_REF_ID, ORGANIZER, MediaType.APPLICATION_JSON, IMPORT_JSON),
        403,
        "FORBIDDEN",
        "/imports/training-definitions");
    assertThat(count("training_definition", "title = 'Imported definition'")).isZero();
  }

  @Test
  @DisplayName("Importing a definition is forbidden for a trainee")
  void importDefinition_traineeRole_returnsForbidden() throws Exception {
    expectError(
        importDefinition(TRAINEE_USER_REF_ID, TRAINEE, MediaType.APPLICATION_JSON, IMPORT_JSON),
        403,
        "FORBIDDEN",
        "/imports/training-definitions");
  }

  @Test
  @DisplayName("Importing a body that cannot be read is a bad request")
  void importDefinition_unreadableBody_returnsBadRequest() throws Exception {
    expectError(
        importDefinition(DESIGNER_USER_REF_ID, DESIGNER, MediaType.APPLICATION_JSON, "{not json"),
        400,
        "BAD_REQUEST",
        "/imports/training-definitions");
  }

  @Test
  @DisplayName("Importing a definition without a title is a bad request")
  void importDefinition_missingTitle_returnsBadRequest() throws Exception {
    expectError(
        importDefinition(
            DESIGNER_USER_REF_ID,
            DESIGNER,
            MediaType.APPLICATION_JSON,
            "{\"description\":\"no title\",\"levels\":[]}"),
        400,
        "BAD_REQUEST",
        "/imports/training-definitions");
    assertThat(count("training_definition", "description = 'no title'")).isZero();
  }

  @Test
  @DisplayName(
      "Importing a training level that has both a static answer and variable answers configured is a bad request")
  void importDefinition_inconsistentAnswerConfiguration_returnsBadRequest() throws Exception {
    String body = IMPORT_JSON.replace("\"variant_answers\": false", "\"variant_answers\": true");

    expectError(
        importDefinition(DESIGNER_USER_REF_ID, DESIGNER, MediaType.APPLICATION_JSON, body),
        400,
        "BAD_REQUEST",
        "/imports/training-definitions");
    assertThat(count("training_definition", "title = 'Imported definition'")).isZero();
  }

  @Test
  @DisplayName(
      "Importing a training level whose hint penalties exceed its maximum score is unprocessable")
  void importDefinition_hintPenaltiesAboveMaximumScore_returnsUnprocessableContent()
      throws Exception {
    String body = IMPORT_JSON.replace("\"hint_penalty\": 5", "\"hint_penalty\": 25");

    expectEntityError(
        importDefinition(DESIGNER_USER_REF_ID, DESIGNER, MediaType.APPLICATION_JSON, body),
        422,
        "UNPROCESSABLE_CONTENT",
        "/imports/training-definitions",
        "TrainingLevel");
    assertThat(count("training_definition", "title = 'Imported definition'")).isZero();
  }

  private static String testAssessmentBody(String statementCorrectOption) {
    return """
        {"title": "Assessment definition", "levels": [
          {"level_type": "ASSESSMENT_LEVEL", "title": "Quiz", "instructions": "answer",
           "assessment_type": "TEST", "estimated_duration": 3,
           "questions": [
             {"question_type": "EMI", "text": "match", "points": 4, "penalty": 1, "order": 0,
              "answer_required": true,
              "extended_matching_options": [{"text": "first", "order": 0}, {"text": "second", "order": 1}],
              "extended_matching_statements": [{"text": "statement", "order": 0%s}]},
             {"question_type": "FFQ", "text": "free", "points": 3, "penalty": 1, "order": 1,
              "answer_required": true}
           ]}
        ]}
        """
        .formatted(statementCorrectOption);
  }

  @Test
  @DisplayName("Importing a test assessment computes its maximum score from its questions' points")
  void importDefinition_testAssessment_computesMaximumScoreFromQuestionPoints() throws Exception {
    importDefinition(
            DESIGNER_USER_REF_ID,
            DESIGNER,
            MediaType.APPLICATION_JSON,
            testAssessmentBody(", \"correct_option_order\": 1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.levels[0].max_score").value(7))
        .andExpect(jsonPath("$.levels[0].assessment_type").value("TEST"));
  }

  @Test
  @DisplayName(
      "Importing a test assessment whose matching statement names no correct option is a bad request")
  void importDefinition_testAssessmentStatementWithoutCorrectOption_returnsBadRequest()
      throws Exception {
    expectError(
        importDefinition(
            DESIGNER_USER_REF_ID, DESIGNER, MediaType.APPLICATION_JSON, testAssessmentBody("")),
        400,
        "BAD_REQUEST",
        "/imports/training-definitions");
  }

  @Test
  @DisplayName(
      "Archiving an instance as administrator returns a zip holding the instance, definition, runs and sandbox definition")
  void archiveInstance_administrator_returnsZipWithInstanceDefinitionRunAndSandboxDefinition()
      throws Exception {
    TrainingRun run = seeder.run(instance, trainee, levelOf(), TRState.RUNNING, SANDBOX_ID, false);
    stubSandboxDefinition();

    MvcResult result =
        call(
                ADMINISTRATOR_USER_REF_ID,
                ADMINISTRATOR,
                get("/exports/training-instances/" + instance.getId()).accept(APPLICATION_ZIP))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_ZIP))
            .andExpect(
                header()
                    .string(
                        "Content-Disposition",
                        "inline; filename=\""
                            + instance.getTitle()
                            + ".zip\"; filename*=UTF-8''"
                            + instance.getTitle().replace(" ", "%20")
                            + ".zip"))
            .andReturn();

    assertThat(zipEntryNames(result))
        .contains(
            "training_instance-id" + instance.getId() + ".json",
            "training_definition-id" + definition.getId() + ".json",
            "training_runs/training_run-id" + run.getId() + ".json",
            "sandbox_definition-id77.json");
    assertThat(zipEntryContent(result, "training_instance-id" + instance.getId() + ".json"))
        .contains("\"definition_id\"");
  }

  private cz.cyberrange.platform.training.persistence.model.AbstractLevel levelOf() {
    return transactionTemplate.execute(
        status ->
            entityManager
                .createQuery(
                    "SELECT level FROM AbstractLevel level WHERE level.trainingDefinition.id = :id"
                        + " ORDER BY level.order",
                    cz.cyberrange.platform.training.persistence.model.AbstractLevel.class)
                .setParameter("id", definition.getId())
                .setMaxResults(1)
                .getSingleResult());
  }

  @Test
  @DisplayName("Archiving an instance with the yaml format writes every entry as YAML")
  void archiveInstance_formatYaml_returnsZipWithYamlEntries() throws Exception {
    seeder.run(instance, trainee, levelOf(), TRState.RUNNING, SANDBOX_ID, false);
    stubSandboxDefinition();

    MvcResult result =
        call(
                ADMINISTRATOR_USER_REF_ID,
                ADMINISTRATOR,
                get("/exports/training-instances/" + instance.getId()).param("format", "yaml"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_ZIP))
            .andReturn();

    assertThat(zipEntryNames(result))
        .isNotEmpty()
        .allMatch(entryName -> entryName.endsWith(".yaml"));
    assertThat(zipEntryNames(result)).contains("training_instance-id" + instance.getId() + ".yaml");
  }

  @Test
  @DisplayName(
      "Archiving an instance adds the events and console commands of a run that recorded events")
  void archiveInstance_runWithRecordedEvents_returnsZipWithEventAndCommandEntries()
      throws Exception {
    TrainingRun run = seeder.run(instance, trainee, levelOf(), TRState.RUNNING, SANDBOX_ID, false);
    stubSandboxDefinition();
    String event =
        ("{\"type\":\"training_run_started\",\"sandbox_id\":\"%s\",\"pool_id\":%d,"
                + "\"training_definition_id\":%d,\"training_instance_id\":%d,\"training_run_id\":%d,"
                + "\"training_time\":0,\"actual_score_in_level\":0,\"level\":1,\"level_order\":0,"
                + "\"user_ref_id\":%d,\"timestamp\":1700000000000,\"total_training_level_score\":0,"
                + "\"total_assessment_level_score\":0}")
            .formatted(
                SANDBOX_ID,
                POOL_ID,
                definition.getId(),
                instance.getId(),
                run.getId(),
                TRAINEE_USER_REF_ID);
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/crczp\\.events\\.trainings.*/_search"))
            .atPriority(1)
            .willReturn(
                okJson(
                    "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,"
                        + "\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,"
                        + "\"relation\":\"eq\"},\"max_score\":null,\"hits\":[{\"_index\":\"events\","
                        + "\"_id\":\"1\",\"_source\":"
                        + event
                        + "}]}}")));

    MvcResult result =
        call(
                ADMINISTRATOR_USER_REF_ID,
                ADMINISTRATOR,
                get("/exports/training-instances/" + instance.getId()))
            .andExpect(status().isOk())
            .andReturn();

    assertThat(zipEntryNames(result))
        .contains(
            "training_events/training_run-id" + run.getId() + "-events.json",
            "logs/sandbox-" + SANDBOX_ID + "-useractions.json");
  }

  @Test
  @DisplayName("Archiving an instance without a pool leaves out the sandbox definition")
  void archiveInstance_instanceWithoutPool_returnsZipWithoutSandboxDefinition() throws Exception {
    TrainingInstance poollessInstance =
        seeder.instance(
            definition,
            "poolless-token",
            null,
            true,
            TrainingRunScenarioSeeder.now().minusHours(1),
            TrainingRunScenarioSeeder.now().plusHours(1),
            organizer);

    MvcResult result =
        call(
                ADMINISTRATOR_USER_REF_ID,
                ADMINISTRATOR,
                get("/exports/training-instances/" + poollessInstance.getId()))
            .andExpect(status().isOk())
            .andReturn();

    assertThat(zipEntryNames(result))
        .contains("training_instance-id" + poollessInstance.getId() + ".json")
        .noneMatch(entryName -> entryName.startsWith("sandbox_definition"));
  }

  @Test
  @DisplayName("Archiving an instance as its organizer succeeds")
  void archiveInstance_organizerOfInstance_returnsZip() throws Exception {
    stubSandboxDefinition();

    call(ORGANIZER_USER_REF_ID, ORGANIZER, get("/exports/training-instances/" + instance.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_ZIP));
  }

  @Test
  @DisplayName("Archiving an instance is forbidden for an organizer of another instance")
  void archiveInstance_organizerOfOtherInstance_returnsForbidden() throws Exception {
    expectError(
        call(
            OTHER_ORGANIZER_USER_REF_ID,
            ORGANIZER,
            get("/exports/training-instances/" + instance.getId())),
        403,
        "FORBIDDEN",
        "/exports/training-instances/" + instance.getId());
  }

  @Test
  @DisplayName("Archiving an instance is forbidden for a trainee")
  void archiveInstance_traineeRole_returnsForbidden() throws Exception {
    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get("/exports/training-instances/" + instance.getId())),
        403,
        "FORBIDDEN",
        "/exports/training-instances/" + instance.getId());
  }

  @Test
  @DisplayName("Archiving an unknown instance is not found")
  void archiveInstance_unknownInstance_returnsNotFound() throws Exception {
    expectEntityError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + UNKNOWN_ID)),
        404,
        "NOT_FOUND",
        "/exports/training-instances/" + UNKNOWN_ID,
        "TrainingInstance");
  }

  @Test
  @DisplayName("Archiving an instance whose id is not a number is a bad request")
  void archiveInstance_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/exports/training-instances/abc")),
        400,
        "BAD_REQUEST",
        "/exports/training-instances/abc");
  }

  @Test
  @DisplayName("Archiving an instance with a format other than json or yaml is a bad request")
  void archiveInstance_unknownFormat_returnsBadRequest() throws Exception {
    expectError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + instance.getId()).param("format", "xml")),
        400,
        "BAD_REQUEST",
        "/exports/training-instances/" + instance.getId());
  }

  @Test
  @DisplayName("Archiving an instance with an Accept that does not allow a zip is not acceptable")
  void archiveInstance_acceptOnlyOctetStream_returnsNotAcceptable() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + instance.getId())
                .accept(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  @DisplayName("Archiving an instance passes on the status of a failing sandbox service")
  void archiveInstance_sandboxServiceFails_returnsItsStatus() throws Exception {
    externalServices.stubFor(
        WireMock.get(urlPathMatching(".*/pools/" + POOL_ID + "/definition"))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"detail\":\"Pool not found\"}")));

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + instance.getId()))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName(
      "Reporting scores returns one ranked row per run with the trainee resolved from user-and-group")
  void exportScores_instanceWithRun_returnsRankedRowPerRun() throws Exception {
    TrainingRun run = seeder.run(instance, trainee, levelOf(), TRState.RUNNING, SANDBOX_ID, false);
    stubUsers(TRAINEE_USER_REF_ID);

    call(
            ORGANIZER_USER_REF_ID,
            ORGANIZER,
            get("/exports/training-instances/" + instance.getId() + "/scores")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.training_instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.scored_levels").isArray())
        .andExpect(jsonPath("$.rows", hasSize(1)))
        .andExpect(jsonPath("$.rows[0].rank").value(1))
        .andExpect(jsonPath("$.rows[0].training_run_id").value(run.getId()))
        .andExpect(jsonPath("$.rows[0].user_ref_id").value(TRAINEE_USER_REF_ID))
        .andExpect(jsonPath("$.rows[0].login").value("login-" + TRAINEE_USER_REF_ID))
        .andExpect(jsonPath("$.rows[0].name").value("Person " + TRAINEE_USER_REF_ID))
        .andExpect(
            jsonPath("$.rows[0].mail").value("person" + TRAINEE_USER_REF_ID + "@example.org"))
        .andExpect(jsonPath("$.rows[0].finished").value(false))
        .andExpect(jsonPath("$.rows[0].total_score").value(0))
        .andExpect(jsonPath("$.rows[0].hints_taken").value(0))
        .andExpect(jsonPath("$.rows[0].wrong_answers").value(0))
        .andExpect(jsonPath("$.rows[0].solutions_displayed").value(0));
  }

  @Test
  @DisplayName("Reporting scores for an instance without runs returns no rows")
  void exportScores_instanceWithoutRuns_returnsNoRows() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + instance.getId() + "/scores"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rows", hasSize(0)));
  }

  @Test
  @DisplayName("Reporting scores is forbidden for an organizer of another instance")
  void exportScores_organizerOfOtherInstance_returnsForbidden() throws Exception {
    expectError(
        call(
            OTHER_ORGANIZER_USER_REF_ID,
            ORGANIZER,
            get("/exports/training-instances/" + instance.getId() + "/scores")),
        403,
        "FORBIDDEN",
        "/exports/training-instances/" + instance.getId() + "/scores");
  }

  @Test
  @DisplayName("Reporting scores is forbidden for a trainee")
  void exportScores_traineeRole_returnsForbidden() throws Exception {
    expectError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/exports/training-instances/" + instance.getId() + "/scores")),
        403,
        "FORBIDDEN",
        "/exports/training-instances/" + instance.getId() + "/scores");
  }

  @Test
  @DisplayName("Reporting scores for an unknown instance is not found")
  void exportScores_unknownInstance_returnsNotFound() throws Exception {
    expectEntityError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + UNKNOWN_ID + "/scores")),
        404,
        "NOT_FOUND",
        "/exports/training-instances/" + UNKNOWN_ID + "/scores",
        "TrainingInstance");
  }

  @Test
  @DisplayName("Reporting scores for an instance whose id is not a number is a bad request")
  void exportScores_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/abc/scores")),
        400,
        "BAD_REQUEST",
        "/exports/training-instances/abc/scores");
  }

  @Test
  @DisplayName("Reporting scores with an Accept that does not allow JSON is not acceptable")
  void exportScores_acceptOnlyOctetStream_returnsNotAcceptable() throws Exception {
    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + instance.getId() + "/scores")
                .accept(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  @DisplayName("Reporting scores passes on the status of a failing user service")
  void exportScores_userServiceFails_returnsItsStatus() throws Exception {
    seeder.run(instance, trainee, levelOf(), TRState.RUNNING, SANDBOX_ID, false);
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/users/ids"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\":\"boom\"}")));

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/exports/training-instances/" + instance.getId() + "/scores"))
        .andExpect(status().isInternalServerError());
  }
}
