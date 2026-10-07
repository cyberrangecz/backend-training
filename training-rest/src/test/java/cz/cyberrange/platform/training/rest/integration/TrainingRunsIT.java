package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.SubmissionType;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import jakarta.persistence.EntityManager;
import java.util.stream.Stream;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Exercises every endpoint of the training runs controller through the whole application: roles,
 * relationships between caller and run, documented status codes, response bodies and the effects on
 * the database and on the stubbed external services.
 */
class TrainingRunsIT extends AbstractIntegrationTest {

  private static final long TRAINEE_USER_REF_ID = 5001L;
  private static final long OTHER_TRAINEE_USER_REF_ID = 5002L;
  private static final long ORGANIZER_USER_REF_ID = 5003L;
  private static final long OTHER_ORGANIZER_USER_REF_ID = 5004L;
  private static final long DESIGNER_USER_REF_ID = 5005L;
  private static final long ADMINISTRATOR_USER_REF_ID = 5006L;
  private static final long UNKNOWN_ID = 987654L;
  private static final long POOL_ID = 11L;
  private static final String ACCESS_TOKEN = "pwn-1234";
  private static final String SANDBOX_ID = "11111111-1111-4111-8111-111111111111";
  private static final String OTHER_SANDBOX_ID = "22222222-2222-4222-8222-222222222222";
  private static final String ISO_DATE_TIME = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static final RoleTypeSecurity ADMINISTRATOR =
      RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR;
  private static final RoleTypeSecurity TRAINEE = RoleTypeSecurity.ROLE_TRAINING_TRAINEE;
  private static final RoleTypeSecurity ORGANIZER = RoleTypeSecurity.ROLE_TRAINING_ORGANIZER;
  private static final RoleTypeSecurity DESIGNER = RoleTypeSecurity.ROLE_TRAINING_DESIGNER;

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private EntityManager entityManager;

  private TrainingRunScenarioSeeder seeder;
  private UserRef trainee;
  private UserRef otherTrainee;
  private UserRef organizer;
  private UserRef otherOrganizer;
  private UserRef designer;
  private TrainingDefinition definition;
  private InfoLevel infoLevel;
  private TrainingLevel trainingLevel;
  private AccessLevel accessLevel;
  private AssessmentLevel assessmentLevel;
  private TrainingInstance instance;

  @BeforeEach
  void seedScenario() {
    seeder = new TrainingRunScenarioSeeder(transactionTemplate, entityManager, jdbcTemplate);
    seeder.deleteAll();
    trainee = seeder.userRef(TRAINEE_USER_REF_ID);
    otherTrainee = seeder.userRef(OTHER_TRAINEE_USER_REF_ID);
    organizer = seeder.userRef(ORGANIZER_USER_REF_ID);
    otherOrganizer = seeder.userRef(OTHER_ORGANIZER_USER_REF_ID);
    designer = seeder.userRef(DESIGNER_USER_REF_ID);
    definition = seeder.definition("definition", designer);
    infoLevel = seeder.infoLevel(definition, 0);
    trainingLevel = seeder.trainingLevel(definition, 1, true);
    accessLevel = seeder.accessLevel(definition, 2);
    assessmentLevel = seeder.assessmentLevel(definition, 3, AssessmentType.TEST, true);
    instance = openInstance(definition, ACCESS_TOKEN, POOL_ID, false);
    stubOpenSearchWithoutData();
  }

  @AfterEach
  void deleteScenario() {
    seeder.deleteAll();
  }

  private TrainingInstance openInstance(
      TrainingDefinition forDefinition, String accessToken, Long poolId, boolean local) {
    return seeder.instance(
        forDefinition,
        accessToken,
        poolId,
        local,
        TrainingRunScenarioSeeder.now().minusHours(1),
        TrainingRunScenarioSeeder.now().plusHours(1),
        organizer);
  }

  private TrainingRun runAt(AbstractLevel level, boolean levelAnswered) {
    return seeder.run(instance, trainee, level, TRState.RUNNING, SANDBOX_ID, levelAnswered);
  }

  private void stubOpenSearchWithoutData() {
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*/_search"))
            .willReturn(
                okJson(
                    "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,"
                        + "\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":0,"
                        + "\"relation\":\"eq\"},\"max_score\":null,\"hits\":[]}}")));
    externalServices.stubFor(
        WireMock.delete(urlPathMatching("/crczp\\..*"))
            .willReturn(okJson("{\"acknowledged\":true}")));
  }

  private void stubUser(long userRefId, String givenName) {
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/users/" + userRefId))
            .willReturn(okJson(userJson(userRefId, givenName))));
  }

  private static String userJson(long userRefId, String givenName) {
    return "{\"user_ref_id\":"
        + userRefId
        + ",\"sub\":\"sub-"
        + userRefId
        + "\",\"full_name\":\""
        + givenName
        + " Tester\",\"given_name\":\""
        + givenName
        + "\",\"family_name\":\"Tester\",\"iss\":\"issuer\",\"mail\":\""
        + givenName.toLowerCase()
        + "@example.org\"}";
  }

  private void stubUserPage(long... userRefIds) {
    StringBuilder users = new StringBuilder();
    for (long userRefId : userRefIds) {
      if (users.length() > 0) {
        users.append(',');
      }
      users.append(userJson(userRefId, "User" + userRefId));
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

  private static String path(long runId, String suffix) {
    return "/training-runs/" + runId + suffix;
  }

  private long count(String table, String where, Object... arguments) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM " + table + " WHERE " + where, Long.class, arguments);
  }

  private <T> T runColumn(long runId, String column, Class<T> type) {
    return jdbcTemplate.queryForObject(
        "SELECT " + column + " FROM training_run WHERE id = ?", type, runId);
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

  private static String contextPath(String path) {
    return path;
  }

  static Stream<Arguments> everyEndpoint() {
    return Stream.of(
        Arguments.of(HttpMethod.DELETE, "/training-runs?trainingRunIds=1"),
        Arguments.of(HttpMethod.DELETE, "/training-runs/1"),
        Arguments.of(HttpMethod.GET, "/training-runs/1"),
        Arguments.of(HttpMethod.GET, "/training-runs"),
        Arguments.of(HttpMethod.POST, "/training-runs?accessToken=token"),
        Arguments.of(HttpMethod.GET, "/training-runs/accessible"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/next-levels"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/solutions"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/hints/1"),
        Arguments.of(HttpMethod.POST, "/training-runs/1/is-correct-answer"),
        Arguments.of(HttpMethod.POST, "/training-runs/1/is-correct-passkey"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/resumption"),
        Arguments.of(HttpMethod.PUT, "/training-runs/1"),
        Arguments.of(HttpMethod.PUT, "/training-runs/1/assessment-evaluations"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/participant"),
        Arguments.of(HttpMethod.PATCH, "/training-runs/1/archive"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/answers"),
        Arguments.of(HttpMethod.GET, "/training-runs/1/levels/1"),
        Arguments.of(HttpMethod.GET, "/training-runs/by-ids?ids=1"),
        Arguments.of(HttpMethod.GET, "/training-runs/users?ids=1"));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("everyEndpoint")
  @DisplayName(
      "A request without a bearer token to any training runs endpoint is rejected as unauthenticated")
  void trainingRunsEndpoints_withoutAuthentication_returnUnauthorizedOrForbidden(
      HttpMethod method, String uri) throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.request(method, uri))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "Deleting several runs as administrator removes the runs and everything recorded for them")
  void deleteTrainingRuns_administratorWithFinishedRuns_removesRunsAndDependants()
      throws Exception {
    TrainingRun firstRun =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);
    TrainingRun secondRun =
        seeder.run(
            instance, otherTrainee, assessmentLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);
    seeder.submission(firstRun, trainingLevel, SubmissionType.INCORRECT, "guess");
    seeder.questionAnswer(firstRun, assessmentLevel.getQuestions().get(0).getId(), "alpha");
    seeder.lock(TRAINEE_USER_REF_ID, instance.getId());

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete("/training-runs")
                .param("trainingRunIds", firstRun.getId().toString(), secondRun.getId().toString()))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id IN (?, ?)", firstRun.getId(), secondRun.getId())).isZero();
    assertThat(count("submission", "training_run_id = ?", firstRun.getId())).isZero();
    assertThat(count("question_answer", "training_run_id = ?", firstRun.getId())).isZero();
    assertThat(
            count("training_run_acquisition_lock", "participant_ref_id = ?", TRAINEE_USER_REF_ID))
        .isZero();
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching(
                ".*instance(=|%3D)" + instance.getId() + "\\.run(=|%3D)" + firstRun.getId())));
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching(
                ".*instance(=|%3D)" + instance.getId() + "\\.run(=|%3D)" + secondRun.getId())));
    externalServices.verify(deleteRequestedFor(urlPathMatching(".*sandbox(=|%3D)" + SANDBOX_ID)));
    externalServices.verify(
        deleteRequestedFor(urlPathMatching(".*sandbox(=|%3D)" + OTHER_SANDBOX_ID)));
  }

  @Test
  @DisplayName("Deleting several runs as organizer of every run succeeds")
  void deleteTrainingRuns_organizerOfEveryRun_returnsOk() throws Exception {
    TrainingRun firstRun =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);
    TrainingRun secondRun =
        seeder.run(instance, otherTrainee, trainingLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);

    call(
            ORGANIZER_USER_REF_ID,
            ORGANIZER,
            delete("/training-runs")
                .param("trainingRunIds", firstRun.getId().toString(), secondRun.getId().toString()))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id IN (?, ?)", firstRun.getId(), secondRun.getId())).isZero();
  }

  @Test
  @DisplayName("Deleting several runs is forbidden when the organizer organizes only some of them")
  void deleteTrainingRuns_organizerOfOnlySomeRuns_returnsForbiddenAndKeepsRuns() throws Exception {
    TrainingDefinition otherDefinition = seeder.definition("other", designer);
    TrainingLevel otherLevel = seeder.trainingLevel(otherDefinition, 0, false);
    TrainingInstance foreignInstance =
        seeder.instance(
            otherDefinition,
            "foreign-token",
            POOL_ID,
            false,
            TrainingRunScenarioSeeder.now().minusHours(1),
            TrainingRunScenarioSeeder.now().plusHours(1),
            otherOrganizer);
    TrainingRun ownRun =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);
    TrainingRun foreignRun =
        seeder.run(foreignInstance, trainee, otherLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);

    ResultActions result =
        call(
            ORGANIZER_USER_REF_ID,
            ORGANIZER,
            delete("/training-runs")
                .param("trainingRunIds", ownRun.getId().toString(), foreignRun.getId().toString()));

    expectError(result, 403, "FORBIDDEN", "/training-runs");
    assertThat(count("training_run", "id IN (?, ?)", ownRun.getId(), foreignRun.getId()))
        .isEqualTo(2);
  }

  @Test
  @DisplayName("Deleting several runs with an empty list of ids changes nothing and succeeds")
  void deleteTrainingRuns_emptyIdList_returnsOkAndKeepsRuns() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete("/training-runs").param("trainingRunIds", ""))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id = ?", run.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("Deleting several runs naming an unknown run is not found")
  void deleteTrainingRuns_unknownRunId_returnsNotFound() throws Exception {
    ResultActions result =
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete("/training-runs").param("trainingRunIds", String.valueOf(UNKNOWN_ID)));

    expectEntityError(result, 404, "NOT_FOUND", "/training-runs", "TrainingRun");
  }

  @Test
  @DisplayName("Deleting a running run without force is a conflict and keeps the run")
  void deleteTrainingRuns_runningRunWithoutForce_returnsConflict() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    ResultActions result =
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete("/training-runs").param("trainingRunIds", run.getId().toString()));

    expectEntityError(result, 409, "CONFLICT", "/training-runs", "TrainingRun");
    assertThat(count("training_run", "id = ?", run.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("Deleting a running run with force removes it")
  void deleteTrainingRuns_runningRunWithForce_returnsOkAndRemovesRun() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete("/training-runs")
                .param("trainingRunIds", run.getId().toString())
                .param("forceDelete", "true"))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id = ?", run.getId())).isZero();
  }

  @Test
  @DisplayName("Deleting several runs without the ids parameter is a bad request")
  void deleteTrainingRuns_missingIds_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, delete("/training-runs")),
        400,
        "BAD_REQUEST",
        "/training-runs");
  }

  @Test
  @DisplayName("Deleting several runs with ids that are not numbers is a bad request")
  void deleteTrainingRuns_nonNumericIds_returnsBadRequest() throws Exception {
    expectError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete("/training-runs").param("trainingRunIds", "abc")),
        400,
        "BAD_REQUEST",
        "/training-runs");
  }

  @Test
  @DisplayName("Deleting several runs is forbidden for a caller holding only the trainee role")
  void deleteTrainingRuns_traineeRole_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            delete("/training-runs").param("trainingRunIds", run.getId().toString())),
        403,
        "FORBIDDEN",
        "/training-runs");
  }

  @Test
  @DisplayName(
      "Deleting a run as administrator removes it with its answers, submissions, lock and recorded data")
  void deleteTrainingRun_administrator_removesRunAndDependants() throws Exception {
    TrainingRun run =
        seeder.run(instance, trainee, assessmentLevel, TRState.FINISHED, SANDBOX_ID, true);
    seeder.submission(run, trainingLevel, SubmissionType.CORRECT, "answer");
    seeder.questionAnswer(run, assessmentLevel.getQuestions().get(0).getId(), "alpha");
    seeder.lock(TRAINEE_USER_REF_ID, instance.getId());

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, delete(path(run.getId(), "")))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id = ?", run.getId())).isZero();
    assertThat(count("submission", "training_run_id = ?", run.getId())).isZero();
    assertThat(count("question_answer", "training_run_id = ?", run.getId())).isZero();
    assertThat(
            count("training_run_acquisition_lock", "participant_ref_id = ?", TRAINEE_USER_REF_ID))
        .isZero();
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching(
                ".*instance(=|%3D)" + instance.getId() + "\\.run(=|%3D)" + run.getId())));
    externalServices.verify(deleteRequestedFor(urlPathMatching(".*sandbox(=|%3D)" + SANDBOX_ID)));
  }

  @Test
  @DisplayName("Deleting an archived run purges the commands of its previous sandbox")
  void deleteTrainingRun_archivedRunWithoutSandbox_purgesCommandsOfPreviousSandbox()
      throws Exception {
    TrainingRun run = seeder.run(instance, trainee, trainingLevel, TRState.ARCHIVED, null, true);
    seeder.updateRun(run.getId(), "previous_sandbox_instance_ref_id = '" + OTHER_SANDBOX_ID + "'");

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, delete(path(run.getId(), "")))
        .andExpect(status().isOk());

    externalServices.verify(
        deleteRequestedFor(urlPathMatching(".*sandbox(=|%3D)" + OTHER_SANDBOX_ID)));
  }

  @Test
  @DisplayName("Deleting a run as its organizer succeeds")
  void deleteTrainingRun_organizerOfRun_returnsOk() throws Exception {
    TrainingRun run =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);

    call(ORGANIZER_USER_REF_ID, ORGANIZER, delete(path(run.getId(), "")))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id = ?", run.getId())).isZero();
  }

  @Test
  @DisplayName("Deleting a run is forbidden for an organizer of another instance")
  void deleteTrainingRun_organizerOfOtherInstance_returnsForbidden() throws Exception {
    TrainingRun run =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);

    expectError(
        call(OTHER_ORGANIZER_USER_REF_ID, ORGANIZER, delete(path(run.getId(), ""))),
        403,
        "FORBIDDEN",
        path(run.getId(), ""));
    assertThat(count("training_run", "id = ?", run.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("Deleting a run is forbidden for its own participant")
  void deleteTrainingRun_participantTrainee_returnsForbidden() throws Exception {
    TrainingRun run =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);

    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, delete(path(run.getId(), ""))),
        403,
        "FORBIDDEN",
        path(run.getId(), ""));
  }

  @Test
  @DisplayName("Deleting an unknown run is not found")
  void deleteTrainingRun_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, delete(path(UNKNOWN_ID, ""))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, ""),
        "TrainingRun");
  }

  @Test
  @DisplayName("Deleting a running run without force is a conflict")
  void deleteTrainingRun_runningRunWithoutForce_returnsConflict() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, delete(path(run.getId(), ""))),
        409,
        "CONFLICT",
        path(run.getId(), ""),
        "TrainingRun");
    assertThat(count("training_run", "id = ?", run.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("Deleting a running run with force removes it")
  void deleteTrainingRun_runningRunWithForce_returnsOkAndRemovesRun() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            delete(path(run.getId(), "")).param("forceDelete", "true"))
        .andExpect(status().isOk());

    assertThat(count("training_run", "id = ?", run.getId())).isZero();
  }

  @Test
  @DisplayName("Deleting a run whose id is not a number is a bad request")
  void deleteTrainingRun_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, delete("/training-runs/abc")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc");
  }

  @Test
  @DisplayName(
      "Finding a run as administrator returns it with its participant resolved from user-and-group")
  void findTrainingRunById_administrator_returnsRunWithResolvedParticipant() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(run.getId(), "")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(run.getId()))
        .andExpect(jsonPath("$.state").value("RUNNING"))
        .andExpect(jsonPath("$.definition_id").value(definition.getId()))
        .andExpect(jsonPath("$.instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.sandbox_instance_ref_id").value(SANDBOX_ID))
        .andExpect(jsonPath("$.start_time").value(matchesPattern(ISO_DATE_TIME)))
        .andExpect(jsonPath("$.participant_ref.user_ref_id").value(TRAINEE_USER_REF_ID))
        .andExpect(jsonPath("$.participant_ref.given_name").value("Trainee"));
    externalServices.verify(
        getRequestedFor(
            urlPathEqualTo(
                IntegrationTestInfrastructure.USER_AND_GROUP_PATH
                    + "/users/"
                    + TRAINEE_USER_REF_ID)));
  }

  @Test
  @DisplayName("Finding a run as its own participant returns the run")
  void findTrainingRunById_participant_returnsRun() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(run.getId()));
  }

  @Test
  @DisplayName("Finding a run is forbidden for a trainee who is not its participant")
  void findTrainingRunById_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(OTHER_TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), ""))),
        403,
        "FORBIDDEN",
        path(run.getId(), ""));
  }

  @Test
  @DisplayName("Finding an unknown run is not found")
  void findTrainingRunById_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(UNKNOWN_ID, ""))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, ""),
        "TrainingRun");
  }

  @Test
  @DisplayName("Finding a run whose id is not a number is a bad request")
  void findTrainingRunById_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/abc")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc");
  }

  @Test
  @DisplayName(
      "Listing runs as administrator returns a page of every run with participants resolved")
  void findAllTrainingRuns_administrator_returnsPageOfEveryRun() throws Exception {
    TrainingRun firstRun = runAt(trainingLevel, false);
    TrainingRun secondRun =
        seeder.run(instance, otherTrainee, infoLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");
    stubUser(OTHER_TRAINEE_USER_REF_ID, "Other");

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(
            jsonPath("$.content[*].id")
                .value(
                    containsInAnyOrder(firstRun.getId().intValue(), secondRun.getId().intValue())))
        .andExpect(
            jsonPath("$.content[*].participant_ref.given_name")
                .value(containsInAnyOrder("Trainee", "Other")))
        .andExpect(jsonPath("$.pagination.total_elements").value(2))
        .andExpect(jsonPath("$.pagination.number").value(0));
  }

  @Test
  @DisplayName("Listing runs honours the requested page size")
  void findAllTrainingRuns_pageSizeOne_returnsSinglePagedRun() throws Exception {
    runAt(trainingLevel, false);
    seeder.run(instance, otherTrainee, infoLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");
    stubUser(OTHER_TRAINEE_USER_REF_ID, "Other");

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs").param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.pagination.total_pages").value(2))
        .andExpect(jsonPath("$.pagination.total_elements").value(2));
  }

  @Test
  @DisplayName("Listing runs matches text filters partially and ignoring case")
  void findAllTrainingRuns_partialUppercaseSandboxFilter_returnsMatchingRun() throws Exception {
    TrainingRun matchingRun = runAt(trainingLevel, false);
    seeder.run(instance, otherTrainee, infoLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/training-runs").param("sandboxInstanceRefId", "1111-4111"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(matchingRun.getId()));
  }

  @Test
  @DisplayName("Listing runs is forbidden for a caller without the administrator role")
  void findAllTrainingRuns_organizerRole_returnsForbidden() throws Exception {
    expectError(
        call(ORGANIZER_USER_REF_ID, ORGANIZER, get("/training-runs")),
        403,
        "FORBIDDEN",
        "/training-runs");
  }

  private void stubSandboxAvailable(String sandboxId, String accessToken) {
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.SANDBOX_SERVICE_PATH
                        + "/pools/"
                        + POOL_ID
                        + "/sandboxes/get-and-lock/"
                        + accessToken))
            .willReturn(
                okJson("{\"id\":\"" + sandboxId + "\",\"lock_id\":3,\"allocation_unit_id\":7}")));
  }

  @Test
  @DisplayName("Accessing an instance starts a run on the first level with a sandbox from the pool")
  void accessTrainingRun_cloudInstanceWithoutRun_createsRunWithSandbox() throws Exception {
    stubSandboxAvailable(SANDBOX_ID, ACCESS_TOKEN);

    call(TRAINEE_USER_REF_ID, TRAINEE, post("/training-runs").param("accessToken", ACCESS_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.training_run_id").isNumber())
        .andExpect(jsonPath("$.instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.sandbox_instance_ref_id").value(SANDBOX_ID))
        .andExpect(jsonPath("$.local_environment").value(false))
        .andExpect(jsonPath("$.abstract_level_dto.id").value(infoLevel.getId()))
        .andExpect(jsonPath("$.abstract_level_dto.level_type").value("INFO_LEVEL"))
        .andExpect(jsonPath("$.info_about_levels", hasSize(4)))
        .andExpect(jsonPath("$.start_time").value(matchesPattern(ISO_DATE_TIME)));

    assertThat(count("training_run", "training_instance_id = ?", instance.getId())).isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT sandbox_instance_ref_id FROM training_run", String.class))
        .isEqualTo(SANDBOX_ID);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT sandbox_instance_allocation_id FROM training_run", Integer.class))
        .isEqualTo(7);
    assertThat(jdbcTemplate.queryForObject("SELECT state FROM training_run", String.class))
        .isEqualTo("RUNNING");
    assertThat(count("user_ref", "user_ref_id = ?", TRAINEE_USER_REF_ID)).isEqualTo(1);
    externalServices.verify(
        1,
        getRequestedFor(
            urlPathEqualTo(
                IntegrationTestInfrastructure.SANDBOX_SERVICE_PATH
                    + "/pools/"
                    + POOL_ID
                    + "/sandboxes/get-and-lock/"
                    + ACCESS_TOKEN)));
  }

  @Test
  @DisplayName("Accessing a local environment instance starts a run without requesting a sandbox")
  void accessTrainingRun_localInstance_createsRunWithoutSandbox() throws Exception {
    TrainingInstance localInstance = openInstance(definition, "local-token", null, true);

    call(TRAINEE_USER_REF_ID, TRAINEE, post("/training-runs").param("accessToken", "local-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.instance_id").value(localInstance.getId()))
        .andExpect(jsonPath("$.local_environment").value(true))
        .andExpect(jsonPath("$.sandbox_instance_ref_id").doesNotExist());

    assertThat(count("training_run", "training_instance_id = ?", localInstance.getId()))
        .isEqualTo(1);
    externalServices.verify(0, getRequestedFor(urlPathMatching(".*/sandboxes/get-and-lock/.*")));
  }

  @Test
  @DisplayName(
      "Accessing an instance again resumes the caller's running run instead of starting another")
  void accessTrainingRun_existingRunningRun_resumesRunWithoutNewSandbox() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, post("/training-runs").param("accessToken", ACCESS_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.training_run_id").value(run.getId()))
        .andExpect(jsonPath("$.abstract_level_dto.id").value(trainingLevel.getId()))
        .andExpect(jsonPath("$.sandbox_instance_ref_id").value(SANDBOX_ID));

    assertThat(count("training_run", "training_instance_id = ?", instance.getId())).isEqualTo(1);
    externalServices.verify(0, getRequestedFor(urlPathMatching(".*/sandboxes/get-and-lock/.*")));
  }

  @Test
  @DisplayName("Accessing an instance as administrator starts a run")
  void accessTrainingRun_administrator_createsRun() throws Exception {
    stubSandboxAvailable(SANDBOX_ID, ACCESS_TOKEN);

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            post("/training-runs").param("accessToken", ACCESS_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.training_run_id").isNumber());
  }

  @Test
  @DisplayName(
      "Accessing an instance without a free sandbox is forbidden and leaves no run or lock")
  void accessTrainingRun_noFreeSandbox_returnsForbiddenAndReleasesLock() throws Exception {
    externalServices.stubFor(
        WireMock.get(urlPathMatching(".*/sandboxes/get-and-lock/.*"))
            .willReturn(
                aResponse()
                    .withStatus(409)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"detail\":\"No free sandbox\"}")));

    expectError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            post("/training-runs").param("accessToken", ACCESS_TOKEN)),
        403,
        "FORBIDDEN",
        "/training-runs");

    assertThat(count("training_run", "training_instance_id = ?", instance.getId())).isZero();
    assertThat(count("training_run_acquisition_lock", "training_instance_id = ?", instance.getId()))
        .isZero();
  }

  @Test
  @DisplayName("Accessing an instance with an unknown access token is not found")
  void accessTrainingRun_unknownAccessToken_returnsNotFound() throws Exception {
    expectEntityError(
        call(
            TRAINEE_USER_REF_ID, TRAINEE, post("/training-runs").param("accessToken", "nope-0000")),
        404,
        "NOT_FOUND",
        "/training-runs",
        "TrainingInstance");
  }

  @Test
  @DisplayName("Accessing an instance that has not started yet is not found")
  void accessTrainingRun_instanceNotStarted_returnsNotFound() throws Exception {
    seeder.instance(
        definition,
        "future-token",
        POOL_ID,
        false,
        TrainingRunScenarioSeeder.now().plusHours(1),
        TrainingRunScenarioSeeder.now().plusHours(2),
        organizer);

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            post("/training-runs").param("accessToken", "future-token")),
        404,
        "NOT_FOUND",
        "/training-runs",
        "TrainingInstance");
  }

  @Test
  @DisplayName("Accessing an instance that has already ended is not found")
  void accessTrainingRun_instanceEnded_returnsNotFound() throws Exception {
    seeder.instance(
        definition,
        "past-token",
        POOL_ID,
        false,
        TrainingRunScenarioSeeder.now().minusHours(3),
        TrainingRunScenarioSeeder.now().minusHours(2),
        organizer);

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            post("/training-runs").param("accessToken", "past-token")),
        404,
        "NOT_FOUND",
        "/training-runs",
        "TrainingInstance");
  }

  @Test
  @DisplayName("Accessing an instance without an allocated pool is a conflict")
  void accessTrainingRun_instanceWithoutPool_returnsConflict() throws Exception {
    openInstance(definition, "no-pool-token", null, false);

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            post("/training-runs").param("accessToken", "no-pool-token")),
        409,
        "CONFLICT",
        "/training-runs",
        "TrainingInstance");
  }

  @Test
  @DisplayName("Accessing an instance whose existing run cannot be resumed is a conflict")
  void accessTrainingRun_existingRunNotResumable_returnsConflict() throws Exception {
    seeder.run(instance, trainee, trainingLevel, TRState.ARCHIVED, SANDBOX_ID, false);

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            post("/training-runs").param("accessToken", ACCESS_TOKEN)),
        409,
        "CONFLICT",
        "/training-runs",
        "TrainingRun");
  }

  @Test
  @DisplayName(
      "Accessing an instance the caller is already entering is rejected as too many requests")
  void accessTrainingRun_accessAlreadyInProgress_returnsTooManyRequests() throws Exception {
    seeder.lock(TRAINEE_USER_REF_ID, instance.getId());

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            post("/training-runs").param("accessToken", ACCESS_TOKEN)),
        429,
        "TOO_MANY_REQUESTS",
        "/training-runs",
        "TrainingInstance");
    assertThat(count("training_run", "training_instance_id = ?", instance.getId())).isZero();
  }

  @Test
  @DisplayName("Accessing an instance without the access token parameter is a bad request")
  void accessTrainingRun_missingAccessToken_returnsBadRequest() throws Exception {
    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, post("/training-runs")),
        400,
        "BAD_REQUEST",
        "/training-runs");
  }

  @Test
  @DisplayName("Accessing an instance is forbidden for an organizer role")
  void accessTrainingRun_organizerRole_returnsForbidden() throws Exception {
    expectError(
        call(
            ORGANIZER_USER_REF_ID,
            ORGANIZER,
            post("/training-runs").param("accessToken", ACCESS_TOKEN)),
        403,
        "FORBIDDEN",
        "/training-runs");
  }

  @Test
  @DisplayName(
      "Listing own runs returns only the caller's runs with their progress and next action")
  void getAllAccessedTrainingRuns_trainee_returnsOnlyOwnRuns() throws Exception {
    TrainingRun ownRun = runAt(trainingLevel, false);
    seeder.run(instance, otherTrainee, infoLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get("/training-runs/accessible"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(ownRun.getId()))
        .andExpect(jsonPath("$.content[0].title").value(instance.getTitle()))
        .andExpect(jsonPath("$.content[0].instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.content[0].number_of_levels").value(4))
        .andExpect(jsonPath("$.content[0].current_level_order").value(2))
        .andExpect(jsonPath("$.content[0].possible_action").value("RESUME"))
        .andExpect(
            jsonPath("$.content[0].training_instance_start_date")
                .value(matchesPattern(ISO_DATE_TIME)))
        .andExpect(jsonPath("$.pagination.total_elements").value(1));
  }

  @Test
  @DisplayName("Listing own runs offers results for a finished run")
  void getAllAccessedTrainingRuns_finishedRun_offersResults() throws Exception {
    seeder.run(instance, trainee, assessmentLevel, TRState.FINISHED, SANDBOX_ID, true);

    call(TRAINEE_USER_REF_ID, TRAINEE, get("/training-runs/accessible"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].possible_action").value("RESULTS"));
  }

  private void seedRunsOfTwoInstancesTitledBetaAndAlpha() {
    TrainingInstance alphaInstance = openInstance(definition, "alpha-token", POOL_ID, false);
    jdbcTemplate.update(
        "UPDATE training_instance SET title = 'Alpha' WHERE id = ?", alphaInstance.getId());
    jdbcTemplate.update(
        "UPDATE training_instance SET title = 'Beta' WHERE id = ?", instance.getId());
    runAt(trainingLevel, false);
    seeder.run(alphaInstance, trainee, trainingLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);
  }

  @Test
  @DisplayName("Listing own runs sorted by title ascending orders the page alphabetically")
  void getAllAccessedTrainingRuns_sortByTitleAscending_ordersPageByTitle() throws Exception {
    seedRunsOfTwoInstancesTitledBetaAndAlpha();

    call(TRAINEE_USER_REF_ID, TRAINEE, get("/training-runs/accessible").param("sortByTitle", "asc"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[*].title").value(org.hamcrest.Matchers.contains("Alpha", "Beta")));
  }

  @Test
  @DisplayName("Listing own runs sorted by title descending orders the page reverse alphabetically")
  void getAllAccessedTrainingRuns_sortByTitleDescending_ordersPageByTitleReversed()
      throws Exception {
    seedRunsOfTwoInstancesTitledBetaAndAlpha();

    call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/training-runs/accessible").param("sortByTitle", "desc"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[*].title").value(org.hamcrest.Matchers.contains("Beta", "Alpha")));
  }

  @Test
  @DisplayName("Listing own runs with an unrecognised sort value still returns every run")
  void getAllAccessedTrainingRuns_unrecognisedSortValue_returnsEveryRun() throws Exception {
    seedRunsOfTwoInstancesTitledBetaAndAlpha();

    call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/training-runs/accessible").param("sortByTitle", "sideways"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("Alpha", "Beta")));
  }

  @Test
  @DisplayName("Listing own runs matches text filters partially and ignoring case")
  void getAllAccessedTrainingRuns_partialUppercaseFilter_returnsMatchingRun() throws Exception {
    TrainingRun matchingRun = runAt(trainingLevel, false);
    seeder.run(instance, trainee, infoLevel, TRState.FINISHED, OTHER_SANDBOX_ID, true);

    call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/training-runs/accessible").param("sandboxInstanceRefId", "1111-4111"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(matchingRun.getId()));
  }

  @Test
  @DisplayName("Listing own runs as administrator returns the administrator's own runs")
  void getAllAccessedTrainingRuns_administrator_returnsOwnRuns() throws Exception {
    UserRef administrator = seeder.userRef(ADMINISTRATOR_USER_REF_ID);
    TrainingRun administratorRun =
        seeder.run(instance, administrator, infoLevel, TRState.RUNNING, SANDBOX_ID, false);
    runAt(trainingLevel, false);

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/accessible"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(administratorRun.getId()));
  }

  @Test
  @DisplayName("Listing own runs is forbidden for an organizer role")
  void getAllAccessedTrainingRuns_organizerRole_returnsForbidden() throws Exception {
    expectError(
        call(ORGANIZER_USER_REF_ID, ORGANIZER, get("/training-runs/accessible")),
        403,
        "FORBIDDEN",
        "/training-runs/accessible");
  }

  @Test
  @DisplayName(
      "Moving to the next level advances the run, resets its wrong answers and returns that level")
  void getNextLevel_answeredLevel_returnsNextLevelAndMovesRun() throws Exception {
    TrainingRun run = runAt(infoLevel, true);
    seeder.updateRun(run.getId(), "incorrect_answer_count = 2");

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/next-levels")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(trainingLevel.getId()))
        .andExpect(jsonPath("$.level_type").value("TRAINING_LEVEL"))
        .andExpect(jsonPath("$.order").value(1))
        .andExpect(jsonPath("$.content").value("training content 1"))
        .andExpect(jsonPath("$.incorrect_answer_limit").value(2));

    assertThat(runColumn(run.getId(), "current_level_id", Long.class))
        .isEqualTo(trainingLevel.getId());
    assertThat(runColumn(run.getId(), "incorrect_answer_count", Integer.class)).isZero();
  }

  @Test
  @DisplayName("Moving to the next level as administrator succeeds for another participant's run")
  void getNextLevel_administrator_returnsNextLevel() throws Exception {
    TrainingRun run = runAt(infoLevel, true);

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(run.getId(), "/next-levels")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(trainingLevel.getId()));
  }

  @Test
  @DisplayName("Moving to the next level before answering the current one is a conflict")
  void getNextLevel_unansweredLevel_returnsConflict() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/next-levels"))),
        409,
        "CONFLICT",
        path(run.getId(), "/next-levels"),
        "TrainingRun");
    assertThat(runColumn(run.getId(), "current_level_id", Long.class))
        .isEqualTo(trainingLevel.getId());
  }

  @Test
  @DisplayName("Moving to the next level from the last level is not found")
  void getNextLevel_lastLevel_returnsNotFound() throws Exception {
    TrainingRun run = runAt(assessmentLevel, true);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/next-levels"))),
        404,
        "NOT_FOUND",
        path(run.getId(), "/next-levels"),
        "AbstractLevel");
  }

  @Test
  @DisplayName("Moving to the next level of an unknown run is not found")
  void getNextLevel_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(UNKNOWN_ID, "/next-levels"))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/next-levels"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Moving to the next level of a run whose id is not a number is a bad request")
  void getNextLevel_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/abc/next-levels")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc/next-levels");
  }

  @Test
  @DisplayName("Moving to the next level is forbidden for a trainee who is not the participant")
  void getNextLevel_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(infoLevel, true);

    expectError(
        call(OTHER_TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/next-levels"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/next-levels"));
  }

  @Test
  @DisplayName("Revealing the solution returns it and records it, wiping the score when penalized")
  void getSolution_firstRevealOfPenalizedLevel_returnsSolutionAndPenalizesScore() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.solution").value(TrainingRunScenarioSeeder.TRAINING_LEVEL_SOLUTION));

    assertThat(runColumn(run.getId(), "solution_taken", Boolean.class)).isTrue();
    assertThat(runColumn(run.getId(), "current_penalty", Integer.class))
        .isEqualTo(TrainingRunScenarioSeeder.TRAINING_LEVEL_MAX_SCORE);
    assertThat(count("solution_info", "training_run_id = ?", run.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Revealing the solution of a level that does not penalize it leaves the penalty untouched")
  void getSolution_unpenalizedLevel_returnsSolutionWithoutPenalty() throws Exception {
    TrainingLevel unpenalizedLevel = seeder.trainingLevel(definition, 4, false);
    TrainingRun run = runAt(unpenalizedLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions")))
        .andExpect(status().isOk());

    assertThat(runColumn(run.getId(), "solution_taken", Boolean.class)).isTrue();
    assertThat(runColumn(run.getId(), "current_penalty", Integer.class)).isZero();
  }

  @Test
  @DisplayName("Revealing the solution a second time records it only once")
  void getSolution_secondReveal_returnsSolutionAndRecordsOnce() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions")));
    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.solution").value(TrainingRunScenarioSeeder.TRAINING_LEVEL_SOLUTION));

    assertThat(count("solution_info", "training_run_id = ?", run.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Revealing the solution resolves the answer placeholder from answers-storage for a variant level")
  void getSolution_variantLevelWithAnswerPlaceholder_returnsSolutionWithResolvedAnswer()
      throws Exception {
    TrainingLevel variantLevel =
        seeder.variantTrainingLevel(definition, 4, "flag", "the answer is ${ANSWER}");
    TrainingRun run = runAt(variantLevel, false);
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.ANSWERS_STORAGE_PATH
                        + "/sandboxes/"
                        + SANDBOX_ID
                        + "/answers/flag"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody("variant-secret")));

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.solution").value("the answer is variant-secret"));
  }

  @Test
  @DisplayName(
      "Revealing the solution of a run standing on a level that is not a training level is a bad request")
  void getSolution_currentLevelIsNotTrainingLevel_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(infoLevel, false);

    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions"))),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/solutions"));
  }

  @Test
  @DisplayName("Revealing the solution is forbidden for a trainee who is not the participant")
  void getSolution_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(OTHER_TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/solutions"));
    assertThat(runColumn(run.getId(), "solution_taken", Boolean.class)).isFalse();
  }

  @Test
  @DisplayName("Revealing the solution of an unknown run is not found")
  void getSolution_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(UNKNOWN_ID, "/solutions"))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/solutions"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Revealing the solution as administrator succeeds for another participant's run")
  void getSolution_administrator_returnsSolution() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(run.getId(), "/solutions")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.solution").value(TrainingRunScenarioSeeder.TRAINING_LEVEL_SOLUTION));
  }

  private long firstHintId() {
    return trainingLevel.getHints().iterator().next().getId();
  }

  @Test
  @DisplayName("Taking a hint returns it, adds its penalty to the run and records it as taken")
  void getHint_hintOfCurrentLevel_returnsHintAndAddsPenalty() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/hints/" + firstHintId())))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(firstHintId()))
        .andExpect(jsonPath("$.title").value("hint title"))
        .andExpect(jsonPath("$.content").value("hint content"))
        .andExpect(jsonPath("$.order").value(0));

    assertThat(runColumn(run.getId(), "current_penalty", Integer.class))
        .isEqualTo(TrainingRunScenarioSeeder.HINT_PENALTY);
    assertThat(
            count("hint_info", "training_run_id = ? AND hint_id = ?", run.getId(), firstHintId()))
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Taking a hint of a run standing on a level that is not a training level is a bad request")
  void getHint_currentLevelIsNotTrainingLevel_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(infoLevel, false);

    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/hints/" + firstHintId()))),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/hints/" + firstHintId()));
  }

  @Test
  @DisplayName("Taking a hint that does not exist is not found")
  void getHint_unknownHint_returnsNotFound() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/hints/" + UNKNOWN_ID))),
        404,
        "NOT_FOUND",
        path(run.getId(), "/hints/" + UNKNOWN_ID),
        "Hint");
  }

  @Test
  @DisplayName("Taking a hint of an unknown run is not found")
  void getHint_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get(path(UNKNOWN_ID, "/hints/" + firstHintId()))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/hints/" + firstHintId()),
        "TrainingRun");
  }

  @Test
  @DisplayName("Taking a hint that belongs to another level than the current one is a conflict")
  void getHint_hintOfOtherLevel_returnsConflict() throws Exception {
    TrainingLevel otherLevel = seeder.trainingLevel(definition, 4, false);
    TrainingRun run = runAt(otherLevel, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/hints/" + firstHintId()))),
        409,
        "CONFLICT",
        path(run.getId(), "/hints/" + firstHintId()),
        "Hint");
    assertThat(runColumn(run.getId(), "current_penalty", Integer.class)).isZero();
  }

  @Test
  @DisplayName("Taking a hint is forbidden for a trainee who is not the participant")
  void getHint_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(OTHER_TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/hints/" + firstHintId()))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/hints/" + firstHintId()));
  }

  private static String answerBody(String answer) {
    return "{\"answer\":\"" + answer + "\"}";
  }

  private ResultActions submitAnswer(
      long runId, long callerUserRefId, RoleTypeSecurity role, String body) throws Exception {
    return call(
        callerUserRefId,
        role,
        post(path(runId, "/is-correct-answer"))
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-real-ip", "10.1.2.3")
            .content(body));
  }

  @Test
  @DisplayName(
      "Submitting the correct answer marks the level answered, scores it and records a correct submission")
  void isCorrectAnswer_correctAnswer_returnsCorrectAndScoresLevel() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    submitAnswer(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            answerBody(TrainingRunScenarioSeeder.TRAINING_LEVEL_ANSWER))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.correct").value(true))
        .andExpect(jsonPath("$.remaining_attempts").value(2))
        .andExpect(jsonPath("$.solution").doesNotExist());

    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isTrue();
    assertThat(runColumn(run.getId(), "total_training_score", Integer.class))
        .isEqualTo(TrainingRunScenarioSeeder.TRAINING_LEVEL_MAX_SCORE);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT type || '/' || provided || '/' || ip_address FROM submission WHERE training_run_id = ?",
                String.class,
                run.getId()))
        .isEqualTo("CORRECT/" + TrainingRunScenarioSeeder.TRAINING_LEVEL_ANSWER + "/10.1.2.3");
  }

  @Test
  @DisplayName(
      "Submitting a wrong answer returns incorrect, costs an attempt and records an incorrect submission")
  void isCorrectAnswer_wrongAnswer_returnsIncorrectAndCostsAttempt() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    submitAnswer(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, answerBody("wrong"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(false))
        .andExpect(jsonPath("$.remaining_attempts").value(1))
        .andExpect(jsonPath("$.solution").doesNotExist());

    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isFalse();
    assertThat(runColumn(run.getId(), "incorrect_answer_count", Integer.class)).isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT type FROM submission WHERE training_run_id = ?", String.class, run.getId()))
        .isEqualTo("INCORRECT");
  }

  @Test
  @DisplayName("Submitting an answer that differs only in case is incorrect")
  void isCorrectAnswer_answerWithDifferentCase_returnsIncorrect() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    submitAnswer(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            answerBody(TrainingRunScenarioSeeder.TRAINING_LEVEL_ANSWER.toUpperCase()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(false));
  }

  @Test
  @DisplayName("Submitting a wrong answer with the last attempt returns the solution")
  void isCorrectAnswer_lastAttemptUsed_returnsSolution() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    seeder.updateRun(run.getId(), "incorrect_answer_count = 1");

    submitAnswer(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, answerBody("wrong"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(false))
        .andExpect(jsonPath("$.remaining_attempts").value(0))
        .andExpect(jsonPath("$.solution").value(TrainingRunScenarioSeeder.TRAINING_LEVEL_SOLUTION));
  }

  @Test
  @DisplayName(
      "Submitting the answer a variant level expects, resolved per trainee from answers-storage, is correct")
  void isCorrectAnswer_variantLevelWithStoredAnswer_returnsCorrect() throws Exception {
    TrainingLevel variantLevel = seeder.variantTrainingLevel(definition, 4, "flag", "solution");
    TrainingRun run = runAt(variantLevel, false);
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.ANSWERS_STORAGE_PATH
                        + "/sandboxes/"
                        + SANDBOX_ID
                        + "/answers/flag"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody("variant-secret")));

    submitAnswer(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, answerBody("variant-secret"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(true));
  }

  @Test
  @DisplayName("Submitting an answer to an already answered level is a conflict")
  void isCorrectAnswer_levelAlreadyAnswered_returnsConflict() throws Exception {
    TrainingRun run = runAt(trainingLevel, true);

    expectEntityError(
        submitAnswer(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, answerBody("anything")),
        409,
        "CONFLICT",
        path(run.getId(), "/is-correct-answer"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Submitting an answer on a level that is not a training level is a bad request")
  void isCorrectAnswer_currentLevelIsNotTrainingLevel_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(infoLevel, false);

    expectError(
        submitAnswer(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, answerBody("anything")),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/is-correct-answer"));
  }

  @Test
  @DisplayName("Submitting an empty answer is a bad request")
  void isCorrectAnswer_emptyAnswer_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        submitAnswer(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, answerBody("")),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/is-correct-answer"));
  }

  @Test
  @DisplayName("Submitting an answer for an unknown run is not found")
  void isCorrectAnswer_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        submitAnswer(UNKNOWN_ID, ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, answerBody("anything")),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/is-correct-answer"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Submitting an answer is forbidden for a trainee who is not the participant")
  void isCorrectAnswer_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        submitAnswer(run.getId(), OTHER_TRAINEE_USER_REF_ID, TRAINEE, answerBody("anything")),
        403,
        "FORBIDDEN",
        path(run.getId(), "/is-correct-answer"));
    assertThat(count("submission", "training_run_id = ?", run.getId())).isZero();
  }

  private ResultActions submitPasskey(
      long runId, long callerUserRefId, RoleTypeSecurity role, String body) throws Exception {
    return call(
        callerUserRefId,
        role,
        post(path(runId, "/is-correct-passkey"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  private static String passkeyBody(String passkey) {
    return "{\"passkey\":\"" + passkey + "\"}";
  }

  @Test
  @DisplayName("Submitting the correct passkey returns true and marks the level answered")
  void isCorrectPasskey_correctPasskey_returnsTrueAndMarksLevelAnswered() throws Exception {
    TrainingRun run = runAt(accessLevel, false);

    submitPasskey(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            passkeyBody(TrainingRunScenarioSeeder.ACCESS_LEVEL_PASSKEY))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.correct").value(true));

    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isTrue();
  }

  @Test
  @DisplayName("Submitting a wrong passkey returns false and leaves the level unanswered")
  void isCorrectPasskey_wrongPasskey_returnsFalseAndLeavesLevelUnanswered() throws Exception {
    TrainingRun run = runAt(accessLevel, false);

    submitPasskey(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, passkeyBody("wrong"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(false));

    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isFalse();
  }

  @Test
  @DisplayName("Submitting a passkey on a level that is not an access level is a bad request")
  void isCorrectPasskey_currentLevelIsNotAccessLevel_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        submitPasskey(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, passkeyBody("anything")),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/is-correct-passkey"));
  }

  @Test
  @DisplayName("Submitting a passkey to an already answered level is a conflict")
  void isCorrectPasskey_levelAlreadyAnswered_returnsConflict() throws Exception {
    TrainingRun run = runAt(accessLevel, true);

    expectEntityError(
        submitPasskey(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, passkeyBody("anything")),
        409,
        "CONFLICT",
        path(run.getId(), "/is-correct-passkey"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Submitting an empty passkey is a bad request")
  void isCorrectPasskey_emptyPasskey_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(accessLevel, false);

    expectError(
        submitPasskey(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, passkeyBody("")),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/is-correct-passkey"));
  }

  @Test
  @DisplayName("Submitting a passkey for an unknown run is not found")
  void isCorrectPasskey_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        submitPasskey(
            UNKNOWN_ID, ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, passkeyBody("anything")),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/is-correct-passkey"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Submitting a passkey is forbidden for a trainee who is not the participant")
  void isCorrectPasskey_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(accessLevel, false);

    expectError(
        submitPasskey(run.getId(), OTHER_TRAINEE_USER_REF_ID, TRAINEE, passkeyBody("anything")),
        403,
        "FORBIDDEN",
        path(run.getId(), "/is-correct-passkey"));
  }

  @Test
  @DisplayName("Resuming a run returns its current level and context")
  void resumeTrainingRun_runningRun_returnsCurrentLevelAndContext() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.training_run_id").value(run.getId()))
        .andExpect(jsonPath("$.abstract_level_dto.id").value(trainingLevel.getId()))
        .andExpect(jsonPath("$.instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.sandbox_instance_ref_id").value(SANDBOX_ID))
        .andExpect(jsonPath("$.level_answered").value(false))
        .andExpect(jsonPath("$.info_about_levels", hasSize(4)))
        .andExpect(jsonPath("$.taken_solution").doesNotExist())
        .andExpect(jsonPath("$.taken_hints", hasSize(0)));
  }

  @Test
  @DisplayName(
      "Resuming a run on a training level returns the solution and hints already taken there")
  void resumeTrainingRun_trainingLevelWithTakenSolutionAndHint_returnsTakenSolutionAndHints()
      throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/solutions")));
    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/hints/" + firstHintId())));

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption")))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.taken_solution").value(TrainingRunScenarioSeeder.TRAINING_LEVEL_SOLUTION))
        .andExpect(jsonPath("$.taken_hints", hasSize(1)))
        .andExpect(jsonPath("$.taken_hints[0].id").value(firstHintId()))
        .andExpect(jsonPath("$.taken_hints[0].content").value("hint content"));
  }

  @Test
  @DisplayName("Resuming a run on an access level replaces the placeholders of its local content")
  void resumeTrainingRun_accessLevel_returnsLocalContentWithReplacedPlaceholders()
      throws Exception {
    TrainingRun run = runAt(accessLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.abstract_level_dto.level_type").value("ACCESS_LEVEL"))
        .andExpect(
            jsonPath("$.abstract_level_dto.local_content")
                .value(
                    "token=" + ACCESS_TOKEN + " user=" + TRAINEE_USER_REF_ID + " definition=77"));
  }

  @Test
  @DisplayName("Resuming a run as administrator succeeds for another participant's run")
  void resumeTrainingRun_administrator_returnsRun() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(run.getId(), "/resumption")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.training_run_id").value(run.getId()));
  }

  @Test
  @DisplayName("Resuming a finished run is a conflict")
  void resumeTrainingRun_finishedRun_returnsConflict() throws Exception {
    TrainingRun run =
        seeder.run(instance, trainee, trainingLevel, TRState.FINISHED, SANDBOX_ID, true);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption"))),
        409,
        "CONFLICT",
        path(run.getId(), "/resumption"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Resuming an archived run is a conflict")
  void resumeTrainingRun_archivedRun_returnsConflict() throws Exception {
    TrainingRun run =
        seeder.run(instance, trainee, trainingLevel, TRState.ARCHIVED, SANDBOX_ID, true);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption"))),
        409,
        "CONFLICT",
        path(run.getId(), "/resumption"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Resuming a run of an instance that has ended is a conflict")
  void resumeTrainingRun_instanceEnded_returnsConflict() throws Exception {
    TrainingInstance endedInstance =
        seeder.instance(
            definition,
            "ended-token",
            POOL_ID,
            false,
            TrainingRunScenarioSeeder.now().minusHours(3),
            TrainingRunScenarioSeeder.now().minusHours(2),
            organizer);
    TrainingRun run =
        seeder.run(endedInstance, trainee, trainingLevel, TRState.RUNNING, SANDBOX_ID, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption"))),
        409,
        "CONFLICT",
        path(run.getId(), "/resumption"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Resuming a run of an instance whose pool assignment is gone is a conflict")
  void resumeTrainingRun_instanceWithoutPool_returnsConflict() throws Exception {
    TrainingInstance poollessInstance = openInstance(definition, "poolless-token", null, false);
    TrainingRun run =
        seeder.run(poollessInstance, trainee, trainingLevel, TRState.RUNNING, SANDBOX_ID, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption"))),
        409,
        "CONFLICT",
        path(run.getId(), "/resumption"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Resuming a run whose sandbox was deleted on a cloud instance is a conflict")
  void resumeTrainingRun_sandboxDeletedOnCloudInstance_returnsConflict() throws Exception {
    TrainingRun run = seeder.run(instance, trainee, trainingLevel, TRState.RUNNING, null, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption"))),
        409,
        "CONFLICT",
        path(run.getId(), "/resumption"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Resuming a run without a sandbox on a local instance succeeds")
  void resumeTrainingRun_localInstanceWithoutSandbox_returnsRun() throws Exception {
    TrainingInstance localInstance = openInstance(definition, "local-token", null, true);
    TrainingRun run =
        seeder.run(localInstance, trainee, trainingLevel, TRState.RUNNING, null, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.training_run_id").value(run.getId()))
        .andExpect(jsonPath("$.local_environment").value(true));
  }

  @Test
  @DisplayName("Resuming an unknown run is not found")
  void resumeTrainingRun_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(UNKNOWN_ID, "/resumption"))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/resumption"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Resuming a run whose id is not a number is a bad request")
  void resumeTrainingRun_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/abc/resumption")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc/resumption");
  }

  @Test
  @DisplayName("Resuming a run is forbidden for a trainee who is not the participant")
  void resumeTrainingRun_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(OTHER_TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/resumption"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/resumption"));
  }

  @Test
  @DisplayName(
      "Finishing a run on its answered last level finishes it and frees its lock")
  void finishTrainingRun_answeredLastLevel_finishesRun() throws Exception {
    TrainingRun run = runAt(assessmentLevel, true);
    seeder.lock(TRAINEE_USER_REF_ID, instance.getId());

    call(TRAINEE_USER_REF_ID, TRAINEE, put(path(run.getId(), ""))).andExpect(status().isOk());

    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("FINISHED");
    assertThat(
            count("training_run_acquisition_lock", "participant_ref_id = ?", TRAINEE_USER_REF_ID))
        .isZero();
    assertThat(runColumn(run.getId(), "end_time", java.sql.Timestamp.class).toLocalDateTime())
        .isAfter(TrainingRunScenarioSeeder.now().minusMinutes(1));
  }

  @Test
  @DisplayName("Finishing a run as administrator finishes another participant's run")
  void finishTrainingRun_administrator_finishesRun() throws Exception {
    TrainingRun run = runAt(assessmentLevel, true);

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, put(path(run.getId(), "")))
        .andExpect(status().isOk());

    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("FINISHED");
  }

  @Test
  @DisplayName("Finishing a run that is not on its last level is a conflict")
  void finishTrainingRun_notOnLastLevel_returnsConflict() throws Exception {
    TrainingRun run = runAt(trainingLevel, true);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, put(path(run.getId(), ""))),
        409,
        "CONFLICT",
        path(run.getId(), ""),
        "TrainingRun");
    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("RUNNING");
  }

  @Test
  @DisplayName("Finishing a run whose last level is unanswered is a conflict")
  void finishTrainingRun_lastLevelUnanswered_returnsConflict() throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, put(path(run.getId(), ""))),
        409,
        "CONFLICT",
        path(run.getId(), ""),
        "TrainingRun");
    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("RUNNING");
  }

  @Test
  @DisplayName("Finishing an unknown run is not found")
  void finishTrainingRun_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, put(path(UNKNOWN_ID, ""))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, ""),
        "TrainingRun");
  }

  @Test
  @DisplayName("Finishing a run whose id is not a number is a bad request")
  void finishTrainingRun_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, put("/training-runs/abc")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc");
  }

  @Test
  @DisplayName("Finishing a run is forbidden for a trainee who is not the participant")
  void finishTrainingRun_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(assessmentLevel, true);

    expectError(
        call(OTHER_TRAINEE_USER_REF_ID, TRAINEE, put(path(run.getId(), ""))),
        403,
        "FORBIDDEN",
        path(run.getId(), ""));
    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("RUNNING");
  }

  private long questionId(AssessmentLevel level, int order) {
    return level.getQuestions().get(order).getId();
  }

  private String assessmentAnswers(
      AssessmentLevel level, String freeFormAnswer, String choiceAnswer, int matchedOption) {
    StringBuilder body = new StringBuilder("[");
    if (freeFormAnswer != null) {
      body.append("{\"question_id\":")
          .append(questionId(level, 0))
          .append(",\"answers\":[\"")
          .append(freeFormAnswer)
          .append("\"]},");
    }
    body.append("{\"question_id\":")
        .append(questionId(level, 1))
        .append(",\"answers\":[\"")
        .append(choiceAnswer)
        .append("\"]},{\"question_id\":")
        .append(questionId(level, 2))
        .append(",\"extended_matching_pairs\":{\"0\":")
        .append(matchedOption)
        .append("}}]");
    return body.toString();
  }

  private ResultActions submitAssessment(
      long runId, long callerUserRefId, RoleTypeSecurity role, String body) throws Exception {
    return call(
        callerUserRefId,
        role,
        put(path(runId, "/assessment-evaluations"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  @Test
  @DisplayName(
      "Submitting correct assessment answers stores them, marks the level answered and scores the test")
  void evaluateResponsesToAssessment_correctTestAnswers_storesAnswersAndScoresTest()
      throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    submitAssessment(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(assessmentLevel, "alpha", "red", 1))
        .andExpect(status().isNoContent());

    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isTrue();
    assertThat(runColumn(run.getId(), "total_assessment_score", Integer.class)).isEqualTo(15);
    assertThat(runColumn(run.getId(), "current_penalty", Integer.class)).isZero();
    assertThat(count("question_answer", "training_run_id = ?", run.getId())).isEqualTo(3);
  }

  @Test
  @DisplayName("Submitting wrong assessment answers to a test deducts the questions' penalties")
  void evaluateResponsesToAssessment_wrongTestAnswers_deductsPenalties() throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    submitAssessment(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(assessmentLevel, "wrong", "blue", 0))
        .andExpect(status().isNoContent());

    assertThat(runColumn(run.getId(), "total_assessment_score", Integer.class)).isEqualTo(-3);
    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isTrue();
  }

  @Test
  @DisplayName("Submitting assessment answers that omit a question of a test is a bad request")
  void evaluateResponsesToAssessment_testQuestionUnanswered_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    expectError(
        submitAssessment(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(assessmentLevel, null, "red", 1)),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/assessment-evaluations"));
    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isFalse();
  }

  @Test
  @DisplayName("Submitting questionnaire answers stores them without scoring")
  void evaluateResponsesToAssessment_questionnaire_storesAnswersWithoutScore() throws Exception {
    AssessmentLevel questionnaire =
        seeder.assessmentLevel(definition, 4, AssessmentType.QUESTIONNAIRE, false);
    TrainingRun run = runAt(questionnaire, false);

    submitAssessment(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(questionnaire, null, "blue", 0))
        .andExpect(status().isNoContent());

    assertThat(runColumn(run.getId(), "level_answered", Boolean.class)).isTrue();
    assertThat(runColumn(run.getId(), "total_assessment_score", Integer.class)).isZero();
    assertThat(count("question_answer", "training_run_id = ?", run.getId())).isEqualTo(2);
  }

  @Test
  @DisplayName("Submitting questionnaire answers that omit a required question is a bad request")
  void evaluateResponsesToAssessment_requiredQuestionnaireQuestionUnanswered_returnsBadRequest()
      throws Exception {
    AssessmentLevel questionnaire =
        seeder.assessmentLevel(definition, 4, AssessmentType.QUESTIONNAIRE, true);
    TrainingRun run = runAt(questionnaire, false);

    expectError(
        submitAssessment(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(questionnaire, null, "blue", 0)),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/assessment-evaluations"));
  }

  @Test
  @DisplayName(
      "Submitting assessment answers on a level that is not an assessment is a bad request")
  void evaluateResponsesToAssessment_currentLevelIsNotAssessment_returnsBadRequest()
      throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        submitAssessment(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, "[]"),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/assessment-evaluations"));
  }

  @Test
  @DisplayName("Submitting assessment answers with a missing question id is a bad request")
  void evaluateResponsesToAssessment_answerWithoutQuestionId_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    expectError(
        submitAssessment(run.getId(), TRAINEE_USER_REF_ID, TRAINEE, "[{\"answers\":[\"alpha\"]}]"),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/assessment-evaluations"));
  }

  @Test
  @DisplayName("Submitting assessment answers to an already answered level is a conflict")
  void evaluateResponsesToAssessment_levelAlreadyAnswered_returnsConflict() throws Exception {
    TrainingRun run = runAt(assessmentLevel, true);

    expectEntityError(
        submitAssessment(
            run.getId(),
            TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(assessmentLevel, "alpha", "red", 1)),
        409,
        "CONFLICT",
        path(run.getId(), "/assessment-evaluations"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Submitting assessment answers for an unknown run is not found")
  void evaluateResponsesToAssessment_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        submitAssessment(UNKNOWN_ID, ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, "[]"),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/assessment-evaluations"),
        "TrainingRun");
  }

  @Test
  @DisplayName(
      "Submitting assessment answers as administrator stores them for another participant's run")
  void evaluateResponsesToAssessment_administrator_returnsNoContent() throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    submitAssessment(
            run.getId(),
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            assessmentAnswers(assessmentLevel, "alpha", "red", 1))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "Submitting assessment answers is forbidden for a trainee who is not the participant")
  void evaluateResponsesToAssessment_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(assessmentLevel, false);

    expectError(
        submitAssessment(
            run.getId(),
            OTHER_TRAINEE_USER_REF_ID,
            TRAINEE,
            assessmentAnswers(assessmentLevel, "alpha", "red", 1)),
        403,
        "FORBIDDEN",
        path(run.getId(), "/assessment-evaluations"));
  }

  @Test
  @DisplayName("Finding the participant of a run returns the user described by user-and-group")
  void getParticipant_participant_returnsUserFromUserAndGroup() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/participant")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.user_ref_id").value(TRAINEE_USER_REF_ID))
        .andExpect(jsonPath("$.full_name").value("Trainee Tester"))
        .andExpect(jsonPath("$.given_name").value("Trainee"))
        .andExpect(jsonPath("$.family_name").value("Tester"))
        .andExpect(jsonPath("$.mail").value("trainee@example.org"));
    externalServices.verify(
        getRequestedFor(
            urlPathEqualTo(
                IntegrationTestInfrastructure.USER_AND_GROUP_PATH
                    + "/users/"
                    + TRAINEE_USER_REF_ID)));
  }

  @Test
  @DisplayName("Finding the participant of a run as administrator succeeds")
  void getParticipant_administrator_returnsUser() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    stubUser(TRAINEE_USER_REF_ID, "Trainee");

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(run.getId(), "/participant")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user_ref_id").value(TRAINEE_USER_REF_ID));
  }

  @Test
  @DisplayName("Finding the participant of an unknown run is not found")
  void getParticipant_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(UNKNOWN_ID, "/participant"))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/participant"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Finding the participant of a run whose id is not a number is a bad request")
  void getParticipant_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/abc/participant")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc/participant");
  }

  @Test
  @DisplayName(
      "Finding the participant of a run is forbidden for an organizer who is not the participant")
  void getParticipant_organizerRole_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(ORGANIZER_USER_REF_ID, ORGANIZER, get(path(run.getId(), "/participant"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/participant"));
  }

  @Test
  @DisplayName(
      "Archiving a run as administrator archives it and moves its sandbox reference to the previous one")
  void archiveTrainingRun_administrator_archivesRunAndMovesSandboxReference() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    seeder.lock(TRAINEE_USER_REF_ID, instance.getId());

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, patch(path(run.getId(), "/archive")))
        .andExpect(status().isOk());

    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("ARCHIVED");
    assertThat(runColumn(run.getId(), "sandbox_instance_ref_id", String.class)).isNull();
    assertThat(runColumn(run.getId(), "previous_sandbox_instance_ref_id", String.class))
        .isEqualTo(SANDBOX_ID);
    assertThat(
            count("training_run_acquisition_lock", "participant_ref_id = ?", TRAINEE_USER_REF_ID))
        .isZero();
  }

  @Test
  @DisplayName("Archiving a run as its organizer archives it")
  void archiveTrainingRun_organizerOfRun_archivesRun() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(ORGANIZER_USER_REF_ID, ORGANIZER, patch(path(run.getId(), "/archive")))
        .andExpect(status().isOk());

    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("ARCHIVED");
  }

  @Test
  @DisplayName("Archiving a run is forbidden for an organizer of another instance")
  void archiveTrainingRun_organizerOfOtherInstance_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(OTHER_ORGANIZER_USER_REF_ID, ORGANIZER, patch(path(run.getId(), "/archive"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/archive"));
    assertThat(runColumn(run.getId(), "state", String.class)).isEqualTo("RUNNING");
  }

  @Test
  @DisplayName("Archiving a run is forbidden for its own participant")
  void archiveTrainingRun_participantTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, patch(path(run.getId(), "/archive"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/archive"));
  }

  @Test
  @DisplayName("Archiving an unknown run is not found")
  void archiveTrainingRun_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, patch(path(UNKNOWN_ID, "/archive"))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/archive"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Archiving a run whose id is not a number is a bad request")
  void archiveTrainingRun_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, patch("/training-runs/abc/archive")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc/archive");
  }

  private void stubVariantAnswers(String sandboxId, String variableName, String content) {
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.ANSWERS_STORAGE_PATH
                        + "/sandboxes/"
                        + sandboxId
                        + "/answers"))
            .willReturn(
                okJson(
                    "{\"sandbox_ref_id\":\""
                        + sandboxId
                        + "\",\"sandbox_answers\":[{\"answer_variable_name\":\""
                        + variableName
                        + "\",\"answer_content\":\""
                        + content
                        + "\"}]}")));
  }

  @Test
  @DisplayName(
      "Listing correct answers returns one per training level in level order, resolving variant answers from answers-storage")
  void getCorrectAnswers_organizer_returnsAnswerPerTrainingLevelInOrder() throws Exception {
    TrainingLevel variantLevel = seeder.variantTrainingLevel(definition, 4, "flag", "solution");
    TrainingRun run = runAt(trainingLevel, false);
    stubVariantAnswers(SANDBOX_ID, "flag", "variant-secret");

    call(ORGANIZER_USER_REF_ID, ORGANIZER, get(path(run.getId(), "/answers")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].level_id").value(trainingLevel.getId()))
        .andExpect(jsonPath("$[0].level_title").value("training level 1"))
        .andExpect(jsonPath("$[0].level_order").value(1))
        .andExpect(
            jsonPath("$[0].correct_answer").value(TrainingRunScenarioSeeder.TRAINING_LEVEL_ANSWER))
        .andExpect(jsonPath("$[0].variable_name").doesNotExist())
        .andExpect(jsonPath("$[1].level_id").value(variantLevel.getId()))
        .andExpect(jsonPath("$[1].level_order").value(4))
        .andExpect(jsonPath("$[1].correct_answer").value("variant-secret"))
        .andExpect(jsonPath("$[1].variable_name").value("flag"));
  }

  @Test
  @DisplayName(
      "Listing correct answers of a run on a local instance resolves variant answers by access token and participant")
  void getCorrectAnswers_localInstance_resolvesVariantAnswersByAccessTokenAndUser()
      throws Exception {
    seeder.variantTrainingLevel(definition, 4, "flag", "solution");
    TrainingInstance localInstance = openInstance(definition, "local-token", null, true);
    TrainingRun run =
        seeder.run(localInstance, trainee, trainingLevel, TRState.RUNNING, null, false);
    externalServices.stubFor(
        WireMock.get(
                urlPathEqualTo(
                    IntegrationTestInfrastructure.ANSWERS_STORAGE_PATH
                        + "/sandboxes/access-tokens/local-token/users/"
                        + TRAINEE_USER_REF_ID))
            .willReturn(
                okJson(
                    "{\"access_token\":\"local-token\",\"user_id\":"
                        + TRAINEE_USER_REF_ID
                        + ",\"sandbox_answers\":[{\"answer_variable_name\":\"flag\","
                        + "\"answer_content\":\"local-secret\"}]}")));

    call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(run.getId(), "/answers")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[1].correct_answer").value("local-secret"));
  }

  @Test
  @DisplayName("Listing correct answers is forbidden for a trainee")
  void getCorrectAnswers_traineeRole_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/answers"))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/answers"));
  }

  @Test
  @DisplayName("Listing correct answers of an unknown run is not found")
  void getCorrectAnswers_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get(path(UNKNOWN_ID, "/answers"))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/answers"),
        "TrainingRun");
  }

  @Test
  @DisplayName("Listing correct answers of a run whose id is not a number is a bad request")
  void getCorrectAnswers_nonNumericId_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/abc/answers")),
        400,
        "BAD_REQUEST",
        "/training-runs/abc/answers");
  }

  @Test
  @DisplayName("Finding the current level of a run counts it as reached")
  void getVisitedLevel_currentLevel_returnsLevel() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/levels/" + trainingLevel.getId())))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(trainingLevel.getId()))
        .andExpect(jsonPath("$.level_type").value("TRAINING_LEVEL"));
  }

  @Test
  @DisplayName("Finding an earlier level of a run returns it")
  void getVisitedLevel_earlierLevel_returnsLevel() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/levels/" + infoLevel.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(infoLevel.getId()))
        .andExpect(jsonPath("$.level_type").value("INFO_LEVEL"));
  }

  @Test
  @DisplayName("Finding a level the run has not reached yet is a conflict naming the level")
  void getVisitedLevel_laterLevel_returnsConflictWithEntityDetail() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/levels/" + accessLevel.getId()))),
        409,
        "CONFLICT",
        path(run.getId(), "/levels/" + accessLevel.getId()),
        "AbstractLevel");
  }

  @Test
  @DisplayName("Finding a level of another definition is a conflict naming the level")
  void getVisitedLevel_levelOfOtherDefinition_returnsConflictWithEntityDetail() throws Exception {
    TrainingDefinition otherDefinition = seeder.definition("other", designer);
    InfoLevel foreignLevel = seeder.infoLevel(otherDefinition, 0);
    TrainingRun run = runAt(trainingLevel, false);

    expectEntityError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get(path(run.getId(), "/levels/" + foreignLevel.getId()))),
        409,
        "CONFLICT",
        path(run.getId(), "/levels/" + foreignLevel.getId()),
        "AbstractLevel");
  }

  @Test
  @DisplayName("Finding a level that does not exist is not found")
  void getVisitedLevel_unknownLevel_returnsNotFound() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectEntityError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/levels/" + UNKNOWN_ID))),
        404,
        "NOT_FOUND",
        path(run.getId(), "/levels/" + UNKNOWN_ID),
        "AbstractLevel");
  }

  @Test
  @DisplayName("Finding a level of an unknown run is not found")
  void getVisitedLevel_unknownRun_returnsNotFound() throws Exception {
    expectEntityError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get(path(UNKNOWN_ID, "/levels/" + infoLevel.getId()))),
        404,
        "NOT_FOUND",
        path(UNKNOWN_ID, "/levels/" + infoLevel.getId()),
        "TrainingRun");
  }

  @Test
  @DisplayName("Finding a level with an id that is not a number is a bad request")
  void getVisitedLevel_nonNumericLevelId_returnsBadRequest() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(TRAINEE_USER_REF_ID, TRAINEE, get(path(run.getId(), "/levels/abc"))),
        400,
        "BAD_REQUEST",
        path(run.getId(), "/levels/abc"));
  }

  @Test
  @DisplayName("Finding a visited level is forbidden for a trainee who is not the participant")
  void getVisitedLevel_otherTrainee_returnsForbidden() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);

    expectError(
        call(
            OTHER_TRAINEE_USER_REF_ID,
            TRAINEE,
            get(path(run.getId(), "/levels/" + infoLevel.getId()))),
        403,
        "FORBIDDEN",
        path(run.getId(), "/levels/" + infoLevel.getId()));
  }

  @Test
  @DisplayName(
      "Finding runs by ids as administrator returns them with plain sandbox ids and resolved participants")
  void findTrainingRunsByIds_administrator_returnsRunsWithPlainSandboxIds() throws Exception {
    TrainingRun firstRun = runAt(trainingLevel, false);
    TrainingRun secondRun =
        seeder.run(instance, otherTrainee, infoLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);
    stubUserPage(TRAINEE_USER_REF_ID, OTHER_TRAINEE_USER_REF_ID);

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/training-runs/by-ids")
                .param("ids", firstRun.getId().toString(), secondRun.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(
            jsonPath("$[*].sandbox_instance_ref_id")
                .value(containsInAnyOrder(SANDBOX_ID, OTHER_SANDBOX_ID)))
        .andExpect(
            jsonPath("$[*].participant_ref.given_name")
                .value(
                    containsInAnyOrder(
                        "User" + TRAINEE_USER_REF_ID, "User" + OTHER_TRAINEE_USER_REF_ID)));
  }

  @Test
  @DisplayName("Finding runs by ids as the organizer of every run returns plain sandbox ids")
  void findTrainingRunsByIds_organizerOfEveryRun_returnsPlainSandboxIds() throws Exception {
    TrainingRun firstRun = runAt(trainingLevel, false);
    TrainingRun secondRun =
        seeder.run(instance, otherTrainee, infoLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);
    stubUserPage(TRAINEE_USER_REF_ID, OTHER_TRAINEE_USER_REF_ID);

    call(
            ORGANIZER_USER_REF_ID,
            ORGANIZER,
            get("/training-runs/by-ids")
                .param("ids", firstRun.getId().toString(), secondRun.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$[*].sandbox_instance_ref_id")
                .value(containsInAnyOrder(SANDBOX_ID, OTHER_SANDBOX_ID)));
  }

  @Test
  @DisplayName(
      "Finding runs by ids as their participant returns the plain sandbox id of the participant's own run")
  void findTrainingRunsByIds_participantOfRun_returnsPlainSandboxId() throws Exception {
    TrainingRun run = runAt(trainingLevel, false);
    stubUserPage(TRAINEE_USER_REF_ID);

    call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/training-runs/by-ids").param("ids", run.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].sandbox_instance_ref_id").value(SANDBOX_ID));
  }

  @Test
  @DisplayName(
      "Finding runs by ids is forbidden for a trainee asking for their own run and another trainee's run")
  void findTrainingRunsByIds_callerWithOwnAndForeignRun_returnsForbidden() throws Exception {
    TrainingRun ownRun = runAt(trainingLevel, false);
    TrainingRun foreignRun =
        seeder.run(instance, otherTrainee, infoLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);

    expectError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/training-runs/by-ids")
                .param("ids", ownRun.getId().toString(), foreignRun.getId().toString())),
        403,
        "FORBIDDEN",
        "/training-runs/by-ids");
  }

  @Test
  @DisplayName(
      "Finding runs by ids is forbidden for a trainee who is not the participant of every run")
  void findTrainingRunsByIds_runOfOtherTrainee_returnsForbidden() throws Exception {
    TrainingRun foreignRun =
        seeder.run(instance, otherTrainee, infoLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);

    expectError(
        call(
            TRAINEE_USER_REF_ID,
            TRAINEE,
            get("/training-runs/by-ids").param("ids", foreignRun.getId().toString())),
        403,
        "FORBIDDEN",
        "/training-runs/by-ids");
  }

  @Test
  @DisplayName("Finding runs by ids without the ids parameter is a bad request")
  void findTrainingRunsByIds_missingIds_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/by-ids")),
        400,
        "BAD_REQUEST",
        "/training-runs/by-ids");
  }

  @Test
  @DisplayName("Finding runs by ids that are not numbers is a bad request")
  void findTrainingRunsByIds_nonNumericIds_returnsBadRequest() throws Exception {
    expectError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/training-runs/by-ids").param("ids", "abc")),
        400,
        "BAD_REQUEST",
        "/training-runs/by-ids");
  }

  @Test
  @DisplayName("Finding users by ids as administrator returns them from user-and-group")
  void findUsersByIds_administrator_returnsUsers() throws Exception {
    stubUserPage(TRAINEE_USER_REF_ID, OTHER_TRAINEE_USER_REF_ID);

    call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/training-runs/users")
                .param(
                    "ids",
                    String.valueOf(TRAINEE_USER_REF_ID),
                    String.valueOf(OTHER_TRAINEE_USER_REF_ID)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(
            jsonPath("$[*].user_ref_id")
                .value(
                    containsInAnyOrder(
                        (int) TRAINEE_USER_REF_ID, (int) OTHER_TRAINEE_USER_REF_ID)));
  }

  @Test
  @DisplayName(
      "Finding users by ids as a caller who shares a training instance with every user returns them")
  void findUsersByIds_callerSharingInstanceWithEveryUser_returnsUsers() throws Exception {
    runAt(trainingLevel, false);
    seeder.run(instance, otherTrainee, infoLevel, TRState.RUNNING, OTHER_SANDBOX_ID, false);
    stubUserPage(TRAINEE_USER_REF_ID, OTHER_TRAINEE_USER_REF_ID);

    call(
            ORGANIZER_USER_REF_ID,
            ORGANIZER,
            get("/training-runs/users")
                .param(
                    "ids",
                    String.valueOf(TRAINEE_USER_REF_ID),
                    String.valueOf(OTHER_TRAINEE_USER_REF_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName(
      "Finding users by ids is forbidden for a caller who shares no training instance with a listed user")
  void findUsersByIds_callerSharingNoInstanceWithUser_returnsForbidden() throws Exception {
    runAt(trainingLevel, false);

    expectError(
        call(
            OTHER_ORGANIZER_USER_REF_ID,
            ORGANIZER,
            get("/training-runs/users").param("ids", String.valueOf(TRAINEE_USER_REF_ID))),
        403,
        "FORBIDDEN",
        "/training-runs/users");
  }

  @Test
  @DisplayName("Finding users by ids without the ids parameter is a bad request")
  void findUsersByIds_missingIds_returnsBadRequest() throws Exception {
    expectError(
        call(ADMINISTRATOR_USER_REF_ID, ADMINISTRATOR, get("/training-runs/users")),
        400,
        "BAD_REQUEST",
        "/training-runs/users");
  }

  @Test
  @DisplayName("Finding users by ids that are not numbers is a bad request")
  void findUsersByIds_nonNumericIds_returnsBadRequest() throws Exception {
    expectError(
        call(
            ADMINISTRATOR_USER_REF_ID,
            ADMINISTRATOR,
            get("/training-runs/users").param("ids", "abc")),
        400,
        "BAD_REQUEST",
        "/training-runs/users");
  }
}
