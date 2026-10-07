package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.CheatingDetectionState;
import cz.cyberrange.platform.training.persistence.model.enums.CommandType;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.model.enums.SubmissionType;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** Exercises every endpoint of {@code /cheating-detections} through the secured application. */
@Transactional
class CheatingDetectionsIT extends AbstractIntegrationTest {

  private static final String BASE_PATH = "/cheating-detections";
  private static final String USER_AND_GROUP = IntegrationTestInfrastructure.USER_AND_GROUP_PATH;
  private static final String ANSWERS_STORAGE = IntegrationTestInfrastructure.ANSWERS_STORAGE_PATH;
  private static final String ISO_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
  private static final DateTimeFormatter COMMAND_TIMESTAMP =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final String EMPTY_SEARCH_RESPONSE =
      "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,"
          + "\"failed\":0},\"hits\":{\"total\":{\"value\":0,\"relation\":\"eq\"},"
          + "\"max_score\":null,\"hits\":[]}}";
  private static final long ORGANIZER_REF_ID = 1000L;
  private static final long OTHER_ORGANIZER_REF_ID = 1001L;
  private static final long FIRST_TRAINEE_REF_ID = 1002L;
  private static final long SECOND_TRAINEE_REF_ID = 1003L;
  private static final String SHARED_IP_ADDRESS = "10.0.0.7";

  private final JsonMapper jsonMapper = new JsonMapper();

  @Autowired private TrainingInstanceDataSeeder seeder;
  @Autowired private CheatingDetectionRepository cheatingDetectionRepository;
  @Autowired private AbstractDetectionEventRepository abstractDetectionEventRepository;
  @Autowired private TrainingRunRepository trainingRunRepository;
  @Autowired private EntityManager entityManager;

  private UserRef organizer;
  private UserRef otherOrganizer;
  private UserRef firstTrainee;
  private UserRef secondTrainee;
  private TrainingDefinition definition;
  private TrainingLevel firstLevel;
  private TrainingLevel secondLevel;
  private TrainingInstance instance;
  private TrainingRun firstRun;
  private TrainingRun secondRun;
  private CheatingDetection detection;
  private final Map<DetectionEventType, AbstractDetectionEvent> seededEvents = new HashMap<>();

  @BeforeEach
  void seedBaseData() {
    organizer = seeder.userRef(ORGANIZER_REF_ID);
    otherOrganizer = seeder.userRef(OTHER_ORGANIZER_REF_ID);
    firstTrainee = seeder.userRef(FIRST_TRAINEE_REF_ID);
    secondTrainee = seeder.userRef(SECOND_TRAINEE_REF_ID);
    definition = seeder.definition("Detection definition");
    firstLevel = seeder.level(definition, 1, "First level", null);
    secondLevel = seeder.level(definition, 2, "Second level", null);
    LocalDateTime runStart = TrainingInstanceDataSeeder.now().minusHours(2);
    instance =
        seeder.instance(
            definition,
            "Detection instance",
            TrainingInstanceDataSeeder.now().minusDays(1),
            TrainingInstanceDataSeeder.now().plusDays(1),
            "detection-1234",
            null,
            organizer);
    firstRun =
        seeder.run(instance, firstLevel, firstTrainee, "sandbox-first", runStart, TRState.FINISHED);
    secondRun =
        seeder.run(
            instance, firstLevel, secondTrainee, "sandbox-second", runStart, TRState.FINISHED);
    detection = seeder.detection(instance.getId(), TrainingInstanceDataSeeder.now().minusHours(1));
    seedEveryKindOfFinding();
    stubLoggedInUser(ORGANIZER_REF_ID);
    stubParticipantNames();
    stubEmptyOpenSearch();
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with every detector queued stores a finished detection")
  void createAndExecuteCheatingDetection_everyDetectorQueued_storesFinishedDetection()
      throws Exception {
    Map<String, Object> body = detectionBody(instance, "QUEUED");
    body.put("proximity_threshold", 90);

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(created.getExecutedBy()).isEqualTo("User " + ORGANIZER_REF_ID);
    assertThat(created.getCurrentState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getProximityThreshold()).isEqualTo(90L);
    assertThat(created.getExecuteTime()).isNotNull();
    assertThat(created.getAnswerSimilarityState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getLocationSimilarityState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getTimeProximityState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getMinimalSolveTimeState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getForbiddenCommandsState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getNoCommandsState()).isEqualTo(CheatingDetectionState.FINISHED);
  }

  @Test
  @DisplayName("createAndExecuteCheatingDetection leaves a disabled detector disabled")
  void createAndExecuteCheatingDetection_disabledDetector_staysDisabled() throws Exception {
    Map<String, Object> body = detectionBody(instance, "QUEUED");
    body.put("location_similarity_state", "DISABLED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(created.getLocationSimilarityState()).isEqualTo(CheatingDetectionState.DISABLED);
    assertThat(created.getTimeProximityState()).isEqualTo(CheatingDetectionState.FINISHED);
  }

  @Test
  @DisplayName("createAndExecuteCheatingDetection runs only the detectors sent as queued")
  void createAndExecuteCheatingDetection_onlyOneDetectorQueued_runsOnlyThatDetector()
      throws Exception {
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("time_proximity_state", "QUEUED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(created.getTimeProximityState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(created.getAnswerSimilarityState()).isEqualTo(CheatingDetectionState.DISABLED);
    assertThat(created.getLocationSimilarityState()).isEqualTo(CheatingDetectionState.DISABLED);
    assertThat(created.getMinimalSolveTimeState()).isEqualTo(CheatingDetectionState.DISABLED);
    assertThat(created.getForbiddenCommandsState()).isEqualTo(CheatingDetectionState.DISABLED);
    assertThat(created.getNoCommandsState()).isEqualTo(CheatingDetectionState.DISABLED);
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection without a proximity threshold uses the default threshold for time proximity")
  void createAndExecuteCheatingDetection_noProximityThreshold_usesDefaultThreshold()
      throws Exception {
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("time_proximity_state", "QUEUED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    assertThat(newestDetectionOfInstance(instance.getId()).getProximityThreshold()).isEqualTo(120L);
  }

  @Test
  @DisplayName("createAndExecuteCheatingDetection stores the forbidden commands sent in the body")
  void createAndExecuteCheatingDetection_forbiddenCommands_storesForbiddenCommands()
      throws Exception {
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("forbidden_commands_state", "QUEUED");
    body.put(
        "forbidden_commands",
        List.of(
            Map.of("command", "nmap", "type", "BASH"),
            Map.of("command", "exploit", "type", "MSF")));

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(created.getCommands())
        .extracting(command -> command.getCommand() + ":" + command.getType())
        .containsExactlyInAnyOrder("nmap:" + CommandType.BASH, "exploit:" + CommandType.MSF);
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with a forbidden command found in a sandbox records a forbidden commands finding")
  void createAndExecuteCheatingDetection_forbiddenCommandUsed_recordsForbiddenCommandsFinding()
      throws Exception {
    LocalDateTime commandTime = firstRun.getStartTime().plusMinutes(5);
    seeder.submission(
        firstRun,
        firstLevel,
        SubmissionType.CORRECT,
        "secret",
        firstRun.getStartTime().plusMinutes(30),
        SHARED_IP_ADDRESS);
    stubSandboxCommands(commandTime, "nmap -sV target", "bash-command");
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("forbidden_commands_state", "QUEUED");
    body.put("forbidden_commands", List.of(Map.of("command", "nmap", "type", "BASH")));

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(eventsOfDetection(created, ForbiddenCommandsDetectionEvent.class))
        .singleElement()
        .satisfies(
            event -> {
              assertThat(event.getCommandCount()).isEqualTo(1);
              assertThat(event.getParticipants()).contains("User " + FIRST_TRAINEE_REF_ID);
            });
    assertThat(created.getResults()).isEqualTo(1L);
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with two trainees submitting close in time records a time proximity finding")
  void createAndExecuteCheatingDetection_closeCorrectSubmissions_recordsTimeProximityFinding()
      throws Exception {
    seedSubmissionsOfBothTrainees();
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("time_proximity_state", "QUEUED");
    body.put("proximity_threshold", 60);

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(eventsOfDetection(created, TimeProximityDetectionEvent.class))
        .singleElement()
        .satisfies(
            event -> {
              assertThat(event.getParticipantCount()).isEqualTo(2);
              assertThat(event.getParticipants())
                  .contains("User " + FIRST_TRAINEE_REF_ID, "User " + SECOND_TRAINEE_REF_ID);
              assertThat(event.getLevelId()).isEqualTo(firstLevel.getId());
            });
    assertThat(created.getResults()).isEqualTo(1L);
    assertThat(trainingRunRepository.findById(firstRun.getId()).orElseThrow().isHasDetectionEvent())
        .isTrue();
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with two trainees submitting from one address records a location similarity finding")
  void createAndExecuteCheatingDetection_sharedAddress_recordsLocationSimilarityFinding()
      throws Exception {
    seedSubmissionsOfBothTrainees();
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("location_similarity_state", "QUEUED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(eventsOfDetection(created, LocationSimilarityDetectionEvent.class))
        .singleElement()
        .satisfies(
            event -> {
              assertThat(event.getIpAddress()).isEqualTo(SHARED_IP_ADDRESS);
              assertThat(event.getParticipantCount()).isEqualTo(2);
            });
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with solves faster than the minimal solve time records a minimal solve time finding")
  void createAndExecuteCheatingDetection_fastSolves_recordsMinimalSolveTimeFinding()
      throws Exception {
    seedSubmissionsOfBothTrainees();
    firstLevel.setMinimalPossibleSolveTime(60L);
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("minimal_solve_time_state", "QUEUED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(eventsOfDetection(created, MinimalSolveTimeDetectionEvent.class))
        .isNotEmpty()
        .allSatisfy(event -> assertThat(event.getLevelId()).isEqualTo(firstLevel.getId()));
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with levels solved without any console command records a no commands finding")
  void createAndExecuteCheatingDetection_solvedWithoutCommands_recordsNoCommandsFinding()
      throws Exception {
    seedSubmissionsOfBothTrainees();
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("no_commands_state", "QUEUED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(eventsOfDetection(created, NoCommandsDetectionEvent.class))
        .anySatisfy(
            event -> {
              assertThat(event.getLevelId()).isEqualTo(firstLevel.getId());
              assertThat(event.getParticipants())
                  .contains("User " + FIRST_TRAINEE_REF_ID, "User " + SECOND_TRAINEE_REF_ID);
            });
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with a trainee submitting the answer of another sandbox records an answer similarity finding")
  void createAndExecuteCheatingDetection_foreignSandboxAnswer_recordsAnswerSimilarityFinding()
      throws Exception {
    TrainingLevel variantLevel = seeder.level(definition, 3, "Variant level", "secret");
    variantLevel.setVariantAnswers(true);
    LocalDateTime base = firstRun.getStartTime();
    seeder.submission(
        firstRun,
        variantLevel,
        SubmissionType.INCORRECT,
        "second-sandbox-answer",
        base.plusMinutes(50),
        SHARED_IP_ADDRESS);
    seeder.submission(
        secondRun,
        variantLevel,
        SubmissionType.CORRECT,
        "second-sandbox-answer",
        base.plusMinutes(55),
        "10.0.0.9");
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(ANSWERS_STORAGE + "/sandboxes"))
            .willReturn(
                okJson(
                    "{\"content\":[{\"sandbox_ref_id\":\"sandbox-first\",\"sandbox_answers\":[]},"
                        + "{\"sandbox_ref_id\":\"sandbox-second\",\"sandbox_answers\":["
                        + "{\"answer_content\":\"second-sandbox-answer\","
                        + "\"answer_variable_name\":\"secret\"}]}],"
                        + "\"pagination\":{\"number\":0,\"number_of_elements\":2,\"size\":2,"
                        + "\"total_elements\":2,\"total_pages\":1}}")));
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("answer_similarity_state", "QUEUED");

    perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    CheatingDetection created = newestDetectionOfInstance(instance.getId());
    assertThat(eventsOfDetection(created, AnswerSimilarityDetectionEvent.class))
        .singleElement()
        .satisfies(
            event -> {
              assertThat(event.getAnswer()).isEqualTo("second-sandbox-answer");
              assertThat(event.getAnswerOwner()).isEqualTo("User " + SECOND_TRAINEE_REF_ID);
              assertThat(event.getParticipants()).contains("User " + FIRST_TRAINEE_REF_ID);
            });
  }

  @Test
  @DisplayName("createAndExecuteCheatingDetection as administrator creates the detection")
  void createAndExecuteCheatingDetection_asAdministrator_createsDetection() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            post(BASE_PATH + "/detection"),
            detectionBody(instance, "DISABLED"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk());

    assertThat(newestDetectionOfInstance(instance.getId()).getExecutedBy())
        .isEqualTo("User " + DEFAULT_USER_REF_ID);
  }

  @Test
  @DisplayName("createAndExecuteCheatingDetection as organizer of another instance is forbidden")
  void createAndExecuteCheatingDetection_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            post(BASE_PATH + "/detection"),
            detectionBody(instance, "DISABLED"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("createAndExecuteCheatingDetection with a role that cannot organize is forbidden")
  void createAndExecuteCheatingDetection_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(post(BASE_PATH + "/detection"), detectionBody(instance, "DISABLED"), role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection without authentication is rejected as unauthenticated")
  void createAndExecuteCheatingDetection_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            post(BASE_PATH + "/detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(detectionBody(instance, "DISABLED"))))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with a blank forbidden command returns bad request")
  void createAndExecuteCheatingDetection_blankForbiddenCommand_returnsBadRequest()
      throws Exception {
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("forbidden_commands", List.of(Map.of("command", " ", "type", "BASH")));

    expectApiError(
            perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
            400,
            "BAD_REQUEST")
        .andExpect(jsonPath("$.errors").isNotEmpty());
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection with a forbidden command without a type returns bad request")
  void createAndExecuteCheatingDetection_forbiddenCommandWithoutType_returnsBadRequest()
      throws Exception {
    Map<String, Object> body = detectionBody(instance, "DISABLED");
    body.put("forbidden_commands", List.of(Map.of("command", "nmap")));

    expectApiError(
        perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "createAndExecuteCheatingDetection without a current state returns bad request and stores nothing")
  void createAndExecuteCheatingDetection_missingCurrentState_returnsBadRequest() throws Exception {
    Map<String, Object> body = detectionBody(instance, "QUEUED");
    body.remove("current_state");
    long detectionsBefore = cheatingDetectionRepository.count();

    expectApiError(
            perform(post(BASE_PATH + "/detection"), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
            400,
            "BAD_REQUEST")
        .andExpect(jsonPath("$.errors").isNotEmpty());
    assertThat(cheatingDetectionRepository.count()).isEqualTo(detectionsBefore);
  }

  @Test
  @DisplayName("rerunCheatingDetection deletes the earlier findings and runs the detection again")
  void rerunCheatingDetection_existingDetection_replacesFindingsAndFinishes() throws Exception {
    seedSubmissionsOfBothTrainees();
    Long staleEventId = seededEvents.get(DetectionEventType.NO_COMMANDS).getId();

    perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(abstractDetectionEventRepository.findById(staleEventId)).isEmpty();
    CheatingDetection rerun = cheatingDetectionRepository.findById(detection.getId()).orElseThrow();
    assertThat(rerun.getCurrentState()).isEqualTo(CheatingDetectionState.FINISHED);
    assertThat(rerun.getResults())
        .isEqualTo(
            abstractDetectionEventRepository.findAllByCheatingDetectionId(rerun.getId()).size());
  }

  @Test
  @DisplayName("rerunCheatingDetection leaves a disabled detector disabled")
  void rerunCheatingDetection_disabledDetector_staysDisabled() throws Exception {
    detection.setLocationSimilarityState(CheatingDetectionState.DISABLED);

    perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    CheatingDetection rerun = cheatingDetectionRepository.findById(detection.getId()).orElseThrow();
    assertThat(rerun.getLocationSimilarityState()).isEqualTo(CheatingDetectionState.DISABLED);
    assertThat(rerun.getTimeProximityState()).isEqualTo(CheatingDetectionState.FINISHED);
  }

  @Test
  @DisplayName("rerunCheatingDetection ignores the training instance id in the path")
  void rerunCheatingDetection_unrelatedInstanceId_stillRerunsDetection() throws Exception {
    perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), unknownId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("rerunCheatingDetection as administrator runs the detection again")
  void rerunCheatingDetection_asAdministrator_returnsOk() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("rerunCheatingDetection as organizer of another instance is forbidden")
  void rerunCheatingDetection_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("rerunCheatingDetection with a role that cannot organize is forbidden")
  void rerunCheatingDetection_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("rerunCheatingDetection without authentication is rejected as unauthenticated")
  void rerunCheatingDetection_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("rerunCheatingDetection with an unknown detection id returns not found")
  void rerunCheatingDetection_unknownDetection_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", unknownId(), instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("rerunCheatingDetection with a non-numeric id returns bad request")
  void rerunCheatingDetection_nonNumericId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", "abc", instance.getId())
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/rerun/{instanceId}", detection.getId(), "abc")
                .contentType(MediaType.APPLICATION_JSON),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "deleteDetectionEvents removes the detection with its findings, participants and detected commands")
  void deleteDetectionEvents_existingDetection_removesDetectionAndFindings() throws Exception {
    Long forbiddenEventId = seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId();

    perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(cheatingDetectionRepository.findById(detection.getId())).isEmpty();
    assertThat(abstractDetectionEventRepository.findAllByCheatingDetectionId(detection.getId()))
        .isEmpty();
    assertThat(abstractDetectionEventRepository.findById(forbiddenEventId)).isEmpty();
  }

  @Test
  @DisplayName(
      "deleteDetectionEvents clears the detection flag of every training run of the instance")
  void deleteDetectionEvents_existingDetection_clearsDetectionFlagOfEveryRun() throws Exception {
    TrainingRun unrelatedRun =
        seeder.run(
            instance,
            firstLevel,
            seeder.userRef(1004L),
            "sandbox-third",
            TrainingInstanceDataSeeder.now().minusHours(2),
            TRState.FINISHED);
    firstRun.setHasDetectionEvent(true);
    unrelatedRun.setHasDetectionEvent(true);

    perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(trainingRunRepository.findAllByTrainingInstanceId(instance.getId()))
        .isNotEmpty()
        .allSatisfy(run -> assertThat(run.isHasDetectionEvent()).isFalse());
  }

  @Test
  @DisplayName("deleteDetectionEvents as administrator removes the detection")
  void deleteDetectionEvents_asAdministrator_removesDetection() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(cheatingDetectionRepository.findById(detection.getId())).isEmpty();
  }

  @Test
  @DisplayName(
      "deleteDetectionEvents as organizer of the detection but not of the given instance is forbidden")
  void deleteDetectionEvents_organizerOfDetectionOnly_returnsForbidden() throws Exception {
    TrainingInstance foreignInstance =
        seeder.instance(
            definition,
            "Foreign instance",
            TrainingInstanceDataSeeder.now().minusDays(1),
            TrainingInstanceDataSeeder.now().plusDays(1),
            "foreign-1234",
            null,
            otherOrganizer);

    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(foreignInstance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("deleteDetectionEvents as organizer of another instance is forbidden")
  void deleteDetectionEvents_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("deleteDetectionEvents with a role that cannot organize is forbidden")
  void deleteDetectionEvents_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("deleteDetectionEvents without authentication is rejected as unauthenticated")
  void deleteDetectionEvents_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("deleteDetectionEvents without the training instance id returns bad request")
  void deleteDetectionEvents_missingInstanceId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("deleteDetectionEvents with a non-numeric training instance id returns bad request")
  void deleteDetectionEvents_nonNumericInstanceId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}/delete", detection.getId())
                .param("trainingInstanceId", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("deleteDetectionEvents with an unknown detection id returns not found")
  void deleteDetectionEvents_unknownDetection_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            delete(BASE_PATH + "/{id}/delete", unknownId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("findAllDetectionEventsOfCheatingDetection returns every finding of the detection")
  void findAllDetectionEventsOfCheatingDetection_asOrganizer_returnsEveryFinding()
      throws Exception {
    perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(6)))
        .andExpect(
            jsonPath(
                "$.content[*].detection_event_type",
                hasItems(
                    "ANSWER_SIMILARITY",
                    "LOCATION_SIMILARITY",
                    "TIME_PROXIMITY",
                    "MINIMAL_SOLVE_TIME",
                    "NO_COMMANDS",
                    "FORBIDDEN_COMMANDS")))
        .andExpect(jsonPath("$.content[0].cheating_detection_id").value(detection.getId()))
        .andExpect(jsonPath("$.content[0].training_instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.content[0].detected_at", matchesPattern(ISO_PATTERN)))
        .andExpect(jsonPath("$.pagination.total_elements").value(6));
  }

  @Test
  @DisplayName(
      "findAllDetectionEventsOfCheatingDetection with a type filter returns only matching findings")
  void findAllDetectionEventsOfCheatingDetection_typeFilter_returnsOnlyMatchingFindings()
      throws Exception {
    perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId()))
                .param("detectionEventType", "TIME_PROXIMITY"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].detection_event_type").value("TIME_PROXIMITY"));
  }

  @Test
  @DisplayName("findAllDetectionEventsOfCheatingDetection returns the requested page")
  void findAllDetectionEventsOfCheatingDetection_pageRequest_returnsRequestedPage()
      throws Exception {
    perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId()))
                .param("page", "1")
                .param("size", "4"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.pagination.number").value(1))
        .andExpect(jsonPath("$.pagination.size").value(4))
        .andExpect(jsonPath("$.pagination.total_pages").value(2));
  }

  @Test
  @DisplayName("findAllDetectionEventsOfCheatingDetection as administrator returns the findings")
  void findAllDetectionEventsOfCheatingDetection_asAdministrator_returnsFindings()
      throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(6)));
  }

  @Test
  @DisplayName(
      "findAllDetectionEventsOfCheatingDetection as organizer of another instance is forbidden")
  void findAllDetectionEventsOfCheatingDetection_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName(
      "findAllDetectionEventsOfCheatingDetection with a role that cannot organize is forbidden")
  void findAllDetectionEventsOfCheatingDetection_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findAllDetectionEventsOfCheatingDetection without authentication is rejected as unauthenticated")
  void
      findAllDetectionEventsOfCheatingDetection_withoutAuthentication_returnsUnauthorizedOrForbidden()
          throws Exception {
    mockMvc
        .perform(
            get(BASE_PATH + "/{id}/events", detection.getId())
                .param("trainingInstanceId", String.valueOf(instance.getId())))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "findAllDetectionEventsOfCheatingDetection without the training instance id returns bad request")
  void findAllDetectionEventsOfCheatingDetection_missingInstanceId_returnsBadRequest()
      throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/events", detection.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "findAllDetectionEventsOfCheatingDetection with an unknown detection id returns not found")
  void findAllDetectionEventsOfCheatingDetection_unknownDetection_returnsNotFound()
      throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/{id}/events", unknownId())
                .param("trainingInstanceId", String.valueOf(instance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("findAllParticipantsOfDetectionEvent returns the trainees a finding implicates")
  void findAllParticipantsOfDetectionEvent_asOrganizer_returnsParticipants() throws Exception {
    Long eventId = seededEvents.get(DetectionEventType.TIME_PROXIMITY).getId();

    perform(
            get(BASE_PATH + "/participants").param("eventId", String.valueOf(eventId)),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(
            jsonPath("$.content[*].participant_name", hasItems("First Trainee", "Second Trainee")))
        .andExpect(
            jsonPath(
                "$.content[*].user_id",
                hasItems((int) FIRST_TRAINEE_REF_ID, (int) SECOND_TRAINEE_REF_ID)))
        .andExpect(jsonPath("$.content[0].detection_event_id").value(eventId))
        .andExpect(jsonPath("$.content[0].occurred_at", matchesPattern(ISO_PATTERN)))
        .andExpect(jsonPath("$.pagination.total_elements").value(2));
  }

  @Test
  @DisplayName("findAllParticipantsOfDetectionEvent as administrator returns the participants")
  void findAllParticipantsOfDetectionEvent_asAdministrator_returnsParticipants() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);
    Long eventId = seededEvents.get(DetectionEventType.TIME_PROXIMITY).getId();

    perform(
            get(BASE_PATH + "/participants").param("eventId", String.valueOf(eventId)),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  @DisplayName("findAllParticipantsOfDetectionEvent as organizer of another instance is forbidden")
  void findAllParticipantsOfDetectionEvent_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/participants")
                .param(
                    "eventId",
                    String.valueOf(seededEvents.get(DetectionEventType.TIME_PROXIMITY).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("findAllParticipantsOfDetectionEvent with a role that cannot organize is forbidden")
  void findAllParticipantsOfDetectionEvent_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/participants")
                .param(
                    "eventId",
                    String.valueOf(seededEvents.get(DetectionEventType.TIME_PROXIMITY).getId())),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findAllParticipantsOfDetectionEvent without authentication is rejected as unauthenticated")
  void findAllParticipantsOfDetectionEvent_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            get(BASE_PATH + "/participants")
                .param(
                    "eventId",
                    String.valueOf(seededEvents.get(DetectionEventType.TIME_PROXIMITY).getId())))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("findAllParticipantsOfDetectionEvent without the event id returns bad request")
  void findAllParticipantsOfDetectionEvent_missingEventId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(get(BASE_PATH + "/participants"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "findAllParticipantsOfDetectionEvent with a non-numeric event id returns bad request")
  void findAllParticipantsOfDetectionEvent_nonNumericEventId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/participants").param("eventId", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("findAllParticipantsOfDetectionEvent with an unknown event id returns not found")
  void findAllParticipantsOfDetectionEvent_unknownEvent_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/participants").param("eventId", String.valueOf(unknownId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("findAllForbiddenCommandsOfDetectionEvent returns the commands a finding caught")
  void findAllForbiddenCommandsOfDetectionEvent_asOrganizer_returnsDetectedCommands()
      throws Exception {
    Long eventId = seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId();

    perform(
            get(BASE_PATH + "/forbidden-commands").param("eventId", String.valueOf(eventId)),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].command").value("nmap -sV"))
        .andExpect(jsonPath("$.content[0].type").value("BASH"))
        .andExpect(jsonPath("$.content[0].hostname").value("attacker-host"))
        .andExpect(jsonPath("$.content[0].occurred_at", matchesPattern(ISO_PATTERN)))
        .andExpect(jsonPath("$.pagination.total_elements").value(1));
  }

  @Test
  @DisplayName("findAllForbiddenCommandsOfDetectionEvent as administrator returns the commands")
  void findAllForbiddenCommandsOfDetectionEvent_asAdministrator_returnsDetectedCommands()
      throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/forbidden-commands")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent as organizer of another instance is forbidden")
  void findAllForbiddenCommandsOfDetectionEvent_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/forbidden-commands")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent with a role that cannot organize is forbidden")
  void findAllForbiddenCommandsOfDetectionEvent_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/forbidden-commands")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId())),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent without authentication is rejected as unauthenticated")
  void
      findAllForbiddenCommandsOfDetectionEvent_withoutAuthentication_returnsUnauthorizedOrForbidden()
          throws Exception {
    mockMvc
        .perform(
            get(BASE_PATH + "/forbidden-commands")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId())))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("findAllForbiddenCommandsOfDetectionEvent without the event id returns bad request")
  void findAllForbiddenCommandsOfDetectionEvent_missingEventId_returnsBadRequest()
      throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/forbidden-commands"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent with an unknown event id returns not found")
  void findAllForbiddenCommandsOfDetectionEvent_unknownEvent_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/forbidden-commands").param("eventId", String.valueOf(unknownId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent with a non-numeric event id returns bad request")
  void findAllForbiddenCommandsOfDetectionEvent_nonNumericEventId_returnsBadRequest()
      throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/forbidden-commands").param("eventId", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path returns every command of the finding unpaged")
  void findAllForbiddenCommandsOfDetectionEventByPath_asOrganizer_returnsAllCommands()
      throws Exception {
    AbstractDetectionEvent event = seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS);
    seeder.detectedForbiddenCommand(event, "hydra -l admin", "attacker-host");
    seeder.detectedForbiddenCommand(event, "nikto -h", "attacker-host");

    perform(
            get(BASE_PATH + "/detected-commands/{eventId}", event.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[*].command", hasItems("nmap -sV", "hydra -l admin", "nikto -h")));
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path as administrator returns the commands")
  void findAllForbiddenCommandsOfDetectionEventByPath_asAdministrator_returnsCommands()
      throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(
                BASE_PATH + "/detected-commands/{eventId}",
                seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path as organizer of another instance is forbidden")
  void findAllForbiddenCommandsOfDetectionEventByPath_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(
                BASE_PATH + "/detected-commands/{eventId}",
                seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path with a role that cannot organize is forbidden")
  void findAllForbiddenCommandsOfDetectionEventByPath_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(
                BASE_PATH + "/detected-commands/{eventId}",
                seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId()),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path without authentication is rejected as unauthenticated")
  void
      findAllForbiddenCommandsOfDetectionEventByPath_withoutAuthentication_returnsUnauthorizedOrForbidden()
          throws Exception {
    mockMvc
        .perform(
            get(
                BASE_PATH + "/detected-commands/{eventId}",
                seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path with an unknown event id returns not found")
  void findAllForbiddenCommandsOfDetectionEventByPath_unknownEvent_returnsNotFound()
      throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/detected-commands/{eventId}", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName(
      "findAllForbiddenCommandsOfDetectionEvent by path with a non-numeric event id returns bad request")
  void findAllForbiddenCommandsOfDetectionEventByPath_nonNumericEventId_returnsBadRequest()
      throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/detected-commands/{eventId}", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "archiveCheatingDetectionResults without a format returns a zip archive with json entries")
  void archiveCheatingDetectionResults_defaultFormat_returnsZipWithJsonEntries() throws Exception {
    MvcResult result =
        perform(
                get(BASE_PATH + "/exports/{id}", detection.getId()),
                RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/zip"))
            .andExpect(
                header()
                    .string(
                        "Content-Disposition",
                        "inline; filename=\"cheating-detection-"
                            + detection.getId()
                            + ".zip\"; filename*=UTF-8''cheating-detection-"
                            + detection.getId()
                            + ".zip"))
            .andReturn();

    List<String> entryNames = zipEntryNames(result.getResponse().getContentAsByteArray());
    assertThat(entryNames)
        .contains(
            "cheating-detection-id" + detection.getId() + ".json",
            eventEntry("answer_similarity", DetectionEventType.ANSWER_SIMILARITY, ".json"),
            eventEntry("location_similarity", DetectionEventType.LOCATION_SIMILARITY, ".json"),
            eventEntry("time_proximity", DetectionEventType.TIME_PROXIMITY, ".json"),
            eventEntry("minimal_solve_time", DetectionEventType.MINIMAL_SOLVE_TIME, ".json"),
            eventEntry("no_commands", DetectionEventType.NO_COMMANDS, ".json"),
            eventEntry("forbidden_commands", DetectionEventType.FORBIDDEN_COMMANDS, ".json"));
    assertThat(entryNames)
        .anyMatch(
            name ->
                name.matches(
                    "detection_events/time_proximity/detection-event-id\\d+-participants\\.json"));
    assertThat(entryNames)
        .anyMatch(name -> name.startsWith("participant_groups/") && name.endsWith(".csv"));
  }

  @Test
  @DisplayName(
      "archiveCheatingDetectionResults with the yaml format returns a zip archive with yaml entries")
  void archiveCheatingDetectionResults_yamlFormat_returnsZipWithYamlEntries() throws Exception {
    MvcResult result =
        perform(
                get(BASE_PATH + "/exports/{id}", detection.getId()).param("format", "yaml"),
                RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/zip"))
            .andReturn();

    List<String> entryNames = zipEntryNames(result.getResponse().getContentAsByteArray());
    assertThat(entryNames)
        .contains(
            "cheating-detection-id" + detection.getId() + ".yaml",
            eventEntry("time_proximity", DetectionEventType.TIME_PROXIMITY, ".yaml"));
    assertThat(entryNames)
        .noneMatch(name -> name.startsWith("detection_events/") && name.endsWith(".json"));
  }

  @Test
  @DisplayName("archiveCheatingDetectionResults with an accept header for zip returns the archive")
  void archiveCheatingDetectionResults_acceptZip_returnsArchive() throws Exception {
    perform(
            get(BASE_PATH + "/exports/{id}", detection.getId()).accept("application/zip"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/zip"));
  }

  @Test
  @DisplayName(
      "archiveCheatingDetectionResults with an accept header for octet stream only is not acceptable")
  void archiveCheatingDetectionResults_acceptOctetStreamOnly_returnsNotAcceptable()
      throws Exception {
    perform(
            get(BASE_PATH + "/exports/{id}", detection.getId())
                .accept(MediaType.APPLICATION_OCTET_STREAM),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isNotAcceptable());
  }

  @Test
  @DisplayName("archiveCheatingDetectionResults as administrator returns the archive")
  void archiveCheatingDetectionResults_asAdministrator_returnsArchive() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/exports/{id}", detection.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/zip"));
  }

  @Test
  @DisplayName("archiveCheatingDetectionResults as organizer of another instance is forbidden")
  void archiveCheatingDetectionResults_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
                get(BASE_PATH + "/exports/{id}", detection.getId()),
                RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
            .andDo(result -> {}),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("archiveCheatingDetectionResults with a role that cannot organize is forbidden")
  void archiveCheatingDetectionResults_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(get(BASE_PATH + "/exports/{id}", detection.getId()), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "archiveCheatingDetectionResults without authentication is rejected as unauthenticated")
  void archiveCheatingDetectionResults_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/exports/{id}", detection.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("archiveCheatingDetectionResults with an unknown format returns bad request")
  void archiveCheatingDetectionResults_unknownFormat_returnsBadRequest() throws Exception {
    expectApiError(
            perform(
                get(BASE_PATH + "/exports/{id}", detection.getId()).param("format", "xml"),
                RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
            400,
            "BAD_REQUEST")
        .andExpect(jsonPath("$.message", containsString("xml")));
  }

  @Test
  @DisplayName(
      "archiveCheatingDetectionResults with a non-numeric detection id returns bad request")
  void archiveCheatingDetectionResults_nonNumericId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/exports/{id}", "abc"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("archiveCheatingDetectionResults with an unknown detection id returns not found")
  void archiveCheatingDetectionResults_unknownDetection_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/exports/{id}", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("findDetectionEventById returns a finding of any kind")
  void findDetectionEventById_asOrganizer_returnsFinding() throws Exception {
    AbstractDetectionEvent event = seededEvents.get(DetectionEventType.TIME_PROXIMITY);

    perform(
            get(BASE_PATH + "/event").param("eventId", String.valueOf(event.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(event.getId()))
        .andExpect(jsonPath("$.detection_event_type").value("TIME_PROXIMITY"))
        .andExpect(jsonPath("$.training_instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.cheating_detection_id").value(detection.getId()))
        .andExpect(jsonPath("$.level_id").value(firstLevel.getId()))
        .andExpect(jsonPath("$.level_order").value(1))
        .andExpect(jsonPath("$.level_title").value("First level"))
        .andExpect(jsonPath("$.participant_count").value(2))
        .andExpect(jsonPath("$.participants").value("First Trainee, Second Trainee"))
        .andExpect(jsonPath("$.detected_at", matchesPattern(ISO_PATTERN)));
  }

  @Test
  @DisplayName("findAnswerSimilarityDetectionEventById returns the answer and its owner")
  void findAnswerSimilarityDetectionEventById_asOrganizer_returnsAnswerSimilarityFinding()
      throws Exception {
    perform(
            get(BASE_PATH + "/answer-similarity")
                .param(
                    "eventId",
                    String.valueOf(seededEvents.get(DetectionEventType.ANSWER_SIMILARITY).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.detection_event_type").value("ANSWER_SIMILARITY"))
        .andExpect(jsonPath("$.answer").value("copied-answer"))
        .andExpect(jsonPath("$.answer_owner").value("Second Trainee"));
  }

  @Test
  @DisplayName("findLocationSimilarityDetectionEventById returns the shared address")
  void findLocationSimilarityDetectionEventById_asOrganizer_returnsLocationSimilarityFinding()
      throws Exception {
    perform(
            get(BASE_PATH + "/location-similarity")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.LOCATION_SIMILARITY).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.detection_event_type").value("LOCATION_SIMILARITY"))
        .andExpect(jsonPath("$.ip_address").value(SHARED_IP_ADDRESS))
        .andExpect(jsonPath("$.dns").value("host.example"));
  }

  @Test
  @DisplayName("findTimeProximityDetectionEventById returns the threshold")
  void findTimeProximityDetectionEventById_asOrganizer_returnsTimeProximityFinding()
      throws Exception {
    perform(
            get(BASE_PATH + "/time-proximity")
                .param(
                    "eventId",
                    String.valueOf(seededEvents.get(DetectionEventType.TIME_PROXIMITY).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.detection_event_type").value("TIME_PROXIMITY"))
        .andExpect(jsonPath("$.threshold").value(120));
  }

  @Test
  @DisplayName("findMinimalSolveTimeDetectionEventById returns the minimal solve time")
  void findMinimalSolveTimeDetectionEventById_asOrganizer_returnsMinimalSolveTimeFinding()
      throws Exception {
    perform(
            get(BASE_PATH + "/minimal-solve-time")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.MINIMAL_SOLVE_TIME).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.detection_event_type").value("MINIMAL_SOLVE_TIME"))
        .andExpect(jsonPath("$.minimal_solve_time").value(300));
  }

  @Test
  @DisplayName("findNoCommandsDetectionEventById returns the finding")
  void findNoCommandsDetectionEventById_asOrganizer_returnsNoCommandsFinding() throws Exception {
    perform(
            get(BASE_PATH + "/no-commands")
                .param(
                    "eventId",
                    String.valueOf(seededEvents.get(DetectionEventType.NO_COMMANDS).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.detection_event_type").value("NO_COMMANDS"))
        .andExpect(jsonPath("$.level_title").value("First level"));
  }

  @Test
  @DisplayName("findForbiddenCommandsDetectionEventById returns the command count")
  void findForbiddenCommandsDetectionEventById_asOrganizer_returnsForbiddenCommandsFinding()
      throws Exception {
    perform(
            get(BASE_PATH + "/detected-forbidden-commands")
                .param(
                    "eventId",
                    String.valueOf(
                        seededEvents.get(DetectionEventType.FORBIDDEN_COMMANDS).getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.detection_event_type").value("FORBIDDEN_COMMANDS"))
        .andExpect(jsonPath("$.command_count").value(1));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName("findDetectionEventById variants as administrator return the finding")
  void findDetectionEventByIdVariants_asAdministrator_returnFinding(String path) throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + path).param("eventId", String.valueOf(eventIdForPath(path))),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cheating_detection_id").value(detection.getId()));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName("findDetectionEventById variants as organizer of another instance are forbidden")
  void findDetectionEventByIdVariants_asOrganizerOfOtherInstance_returnForbidden(String path)
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + path).param("eventId", String.valueOf(eventIdForPath(path))),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName("findDetectionEventById variants with a role that cannot organize are forbidden")
  void findDetectionEventByIdVariants_withNonOrganizerRole_returnForbidden(String path)
      throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + path).param("eventId", String.valueOf(eventIdForPath(path))),
            RoleTypeSecurity.ROLE_TRAINING_TRAINEE),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName(
      "findDetectionEventById variants without authentication are rejected as unauthenticated")
  void findDetectionEventByIdVariants_withoutAuthentication_returnUnauthorizedOrForbidden(
      String path) throws Exception {
    mockMvc
        .perform(get(BASE_PATH + path).param("eventId", String.valueOf(eventIdForPath(path))))
        .andExpect(rejectedAsUnauthenticated());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName("findDetectionEventById variants without the event id return bad request")
  void findDetectionEventByIdVariants_missingEventId_returnBadRequest(String path)
      throws Exception {
    expectApiError(
        perform(get(BASE_PATH + path), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName("findDetectionEventById variants with a non-numeric event id return bad request")
  void findDetectionEventByIdVariants_nonNumericEventId_returnBadRequest(String path)
      throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + path).param("eventId", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName(
      "findDetectionEventById variants with an unknown event id as organizer return not found")
  void findDetectionEventByIdVariants_unknownEventAsOrganizer_returnNotFound(String path)
      throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + path).param("eventId", String.valueOf(unknownId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/event",
        "/answer-similarity",
        "/location-similarity",
        "/time-proximity",
        "/minimal-solve-time",
        "/no-commands",
        "/detected-forbidden-commands"
      })
  @DisplayName(
      "findDetectionEventById variants with an unknown event id as administrator return not found")
  void findDetectionEventByIdVariants_unknownEventAsAdministrator_returnNotFound(String path)
      throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + path).param("eventId", String.valueOf(unknownId())),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName(
      "findAllCheatingDetectionsOfInstance returns the detections of the instance oldest first")
  void findAllCheatingDetectionsOfInstance_asOrganizer_returnsDetectionsOldestFirst()
      throws Exception {
    CheatingDetection oldest =
        seeder.detection(instance.getId(), TrainingInstanceDataSeeder.now().minusDays(3));
    CheatingDetection newest =
        seeder.detection(instance.getId(), TrainingInstanceDataSeeder.now().plusHours(1));
    TrainingInstance foreignInstance =
        seeder.instance(
            definition,
            "Foreign instance",
            TrainingInstanceDataSeeder.now().minusDays(1),
            TrainingInstanceDataSeeder.now().plusDays(1),
            "foreign-1234",
            null,
            otherOrganizer);
    seeder.detection(foreignInstance.getId(), TrainingInstanceDataSeeder.now());

    perform(
            get(BASE_PATH + "/{instanceId}/detections", instance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(
            jsonPath(
                "$.content[*].id",
                contains(
                    oldest.getId().intValue(),
                    detection.getId().intValue(),
                    newest.getId().intValue())))
        .andExpect(jsonPath("$.content[0].training_instance_id").value(instance.getId()))
        .andExpect(jsonPath("$.content[0].executed_by").value("Seed Organizer"))
        .andExpect(jsonPath("$.content[0].execute_time", matchesPattern(ISO_PATTERN)))
        .andExpect(jsonPath("$.content[0].current_state").value("FINISHED"))
        .andExpect(jsonPath("$.content[0].proximity_threshold").value(120))
        .andExpect(jsonPath("$.content[0].time_proximity_state").value("FINISHED"))
        .andExpect(jsonPath("$.pagination.total_elements").value(3));
  }

  @Test
  @DisplayName("findAllCheatingDetectionsOfInstance as administrator returns the detections")
  void findAllCheatingDetectionsOfInstance_asAdministrator_returnsDetections() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{instanceId}/detections", instance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @DisplayName("findAllCheatingDetectionsOfInstance as organizer of another instance is forbidden")
  void findAllCheatingDetectionsOfInstance_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{instanceId}/detections", instance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("findAllCheatingDetectionsOfInstance with a role that cannot organize is forbidden")
  void findAllCheatingDetectionsOfInstance_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(FIRST_TRAINEE_REF_ID);

    expectApiError(
        perform(get(BASE_PATH + "/{instanceId}/detections", instance.getId()), role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findAllCheatingDetectionsOfInstance without authentication is rejected as unauthenticated")
  void findAllCheatingDetectionsOfInstance_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/{instanceId}/detections", instance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "findAllCheatingDetectionsOfInstance with a non-numeric instance id returns bad request")
  void findAllCheatingDetectionsOfInstance_nonNumericInstanceId_returnsBadRequest()
      throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/{instanceId}/detections", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  private void seedEveryKindOfFinding() {
    AnswerSimilarityDetectionEvent answerEvent =
        seeder.answerSimilarityEvent(detection, firstRun, firstLevel);
    LocationSimilarityDetectionEvent locationEvent =
        seeder.locationSimilarityEvent(detection, firstRun, firstLevel);
    TimeProximityDetectionEvent timeEvent =
        seeder.timeProximityEvent(detection, firstRun, firstLevel);
    MinimalSolveTimeDetectionEvent minimalEvent =
        seeder.minimalSolveTimeEvent(detection, firstRun, firstLevel);
    NoCommandsDetectionEvent noCommandsEvent =
        seeder.noCommandsEvent(detection, firstRun, firstLevel);
    ForbiddenCommandsDetectionEvent forbiddenEvent =
        seeder.forbiddenCommandsEvent(detection, firstRun, firstLevel);
    for (AbstractDetectionEvent event :
        List.of(
            answerEvent, locationEvent, timeEvent, minimalEvent, noCommandsEvent, forbiddenEvent)) {
      event.setParticipants("First Trainee, Second Trainee");
      event.setParticipantCount(2);
      seeder.participant(event, FIRST_TRAINEE_REF_ID, "First Trainee");
      seeder.participant(event, SECOND_TRAINEE_REF_ID, "Second Trainee");
      seededEvents.put(event.getDetectionEventType(), event);
    }
    answerEvent.setAnswerOwner("Second Trainee");
    seeder.detectedForbiddenCommand(forbiddenEvent, "nmap -sV", "attacker-host");
  }

  private void seedSubmissionsOfBothTrainees() {
    LocalDateTime base = firstRun.getStartTime();
    seeder.submission(
        firstRun,
        firstLevel,
        SubmissionType.CORRECT,
        "secret",
        base.plusMinutes(30),
        SHARED_IP_ADDRESS);
    seeder.submission(
        secondRun,
        firstLevel,
        SubmissionType.CORRECT,
        "secret",
        base.plusMinutes(30).plusSeconds(20),
        SHARED_IP_ADDRESS);
    seeder.submission(
        firstRun,
        secondLevel,
        SubmissionType.CORRECT,
        "secret",
        base.plusMinutes(40),
        SHARED_IP_ADDRESS);
    seeder.submission(
        secondRun, secondLevel, SubmissionType.CORRECT, "secret", base.plusMinutes(60), "10.0.0.9");
  }

  private int eventIdForPath(String path) {
    DetectionEventType type =
        switch (path) {
          case "/answer-similarity" -> DetectionEventType.ANSWER_SIMILARITY;
          case "/location-similarity" -> DetectionEventType.LOCATION_SIMILARITY;
          case "/minimal-solve-time" -> DetectionEventType.MINIMAL_SOLVE_TIME;
          case "/no-commands" -> DetectionEventType.NO_COMMANDS;
          case "/detected-forbidden-commands" -> DetectionEventType.FORBIDDEN_COMMANDS;
          default -> DetectionEventType.TIME_PROXIMITY;
        };
    return seededEvents.get(type).getId().intValue();
  }

  private String eventEntry(String folder, DetectionEventType type, String extension) {
    return "detection_events/"
        + folder
        + "/detection-event-id"
        + seededEvents.get(type).getId()
        + extension;
  }

  private static List<String> zipEntryNames(byte[] archive) throws IOException {
    List<String> names = new ArrayList<>();
    try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(archive))) {
      for (ZipEntry entry = zipInputStream.getNextEntry();
          entry != null;
          entry = zipInputStream.getNextEntry()) {
        names.add(entry.getName());
      }
    }
    return names;
  }

  private CheatingDetection newestDetectionOfInstance(Long instanceId) {
    flushAndClear();
    List<CheatingDetection> detections =
        new ArrayList<>(cheatingDetectionRepository.findAllByTrainingInstanceId(instanceId));
    detections.removeIf(candidate -> candidate.getId().equals(detection.getId()));
    assertThat(detections).hasSize(1);
    return detections.get(0);
  }

  private <E extends AbstractDetectionEvent> List<E> eventsOfDetection(
      CheatingDetection owner, Class<E> eventType) {
    return abstractDetectionEventRepository.findAllByCheatingDetectionId(owner.getId()).stream()
        .filter(eventType::isInstance)
        .map(eventType::cast)
        .toList();
  }

  private Map<String, Object> detectionBody(TrainingInstance targetInstance, String state) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("training_instance_id", targetInstance.getId());
    body.put("current_state", "QUEUED");
    body.put("answer_similarity_state", state);
    body.put("location_similarity_state", state);
    body.put("time_proximity_state", state);
    body.put("minimal_solve_time_state", state);
    body.put("forbidden_commands_state", state);
    body.put("no_commands_state", state);
    return body;
  }

  private long unknownId() {
    return 9_000_000L + System.nanoTime() % 1_000_000L;
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private ResultActions perform(MockHttpServletRequestBuilder request, RoleTypeSecurity... roles)
      throws Exception {
    return mockMvc.perform(request.with(callerWithRoles(roles)));
  }

  private ResultActions perform(
      MockHttpServletRequestBuilder request, Object jsonBody, RoleTypeSecurity... roles)
      throws Exception {
    return perform(
        request
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(jsonBody)),
        roles);
  }

  private ResultActions expectApiError(ResultActions result, int statusCode, String statusName)
      throws Exception {
    return result
        .andExpect(status().is(statusCode))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(statusName))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.path").isNotEmpty());
  }

  private ResultActions expectEntityNotFound(ResultActions result) throws Exception {
    return expectApiError(result, 404, "NOT_FOUND")
        .andExpect(jsonPath("$.entity_error_detail.entity").isNotEmpty())
        .andExpect(jsonPath("$.entity_error_detail.reason").isNotEmpty());
  }

  private void stubParticipantNames() {
    externalServices.stubFor(
        WireMock.get(urlPathMatching(USER_AND_GROUP + "/users/\\d+"))
            .willReturn(okJson("{\"user_ref_id\": 0, \"full_name\": \"unresolved\"}")));
    for (long userRefId : List.of(FIRST_TRAINEE_REF_ID, SECOND_TRAINEE_REF_ID)) {
      externalServices.stubFor(
          WireMock.get(urlPathEqualTo(USER_AND_GROUP + "/users/" + userRefId))
              .willReturn(
                  okJson(
                      "{\"user_ref_id\": "
                          + userRefId
                          + ", \"full_name\": \"User "
                          + userRefId
                          + "\"}")));
    }
  }

  private void stubEmptyOpenSearch() {
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*_search")).willReturn(okJson(EMPTY_SEARCH_RESPONSE)));
  }

  private void stubSandboxCommands(LocalDateTime commandTime, String command, String commandType) {
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*_search"))
            .willReturn(
                okJson(
                    "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,"
                        + "\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,"
                        + "\"relation\":\"eq\"},\"max_score\":null,\"hits\":[{\"_index\":\"commands\","
                        + "\"_id\":\"c1\",\"_source\":{\"sandbox_id\":\"sandbox-first\","
                        + "\"timestamp_str\":\""
                        + COMMAND_TIMESTAMP.format(commandTime)
                        + "\",\"cmd_type\":\""
                        + commandType
                        + "\",\"cmd\":\""
                        + command
                        + "\",\"hostname\":\"attacker-host\",\"username\":\"user\",\"wd\":\"/\","
                        + "\"ip\":\"10.0.0.1\"}}]}}")));
  }
}
