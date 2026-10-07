package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notContaining;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.utils.SandboxIdHasher;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** Exercises every endpoint of {@code /training-instances} through the secured application. */
@Transactional
class TrainingInstancesIT extends AbstractIntegrationTest {

  private static final String BASE_PATH = "/training-instances";
  private static final String SANDBOX = IntegrationTestInfrastructure.SANDBOX_SERVICE_PATH;
  private static final String USER_AND_GROUP = IntegrationTestInfrastructure.USER_AND_GROUP_PATH;
  private static final DateTimeFormatter ISO_UTC =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final String EMPTY_SEARCH_RESPONSE =
      "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,"
          + "\"failed\":0},\"hits\":{\"total\":{\"value\":0,\"relation\":\"eq\"},"
          + "\"max_score\":null,\"hits\":[]}}";
  private static final long ORGANIZER_REF_ID = 1000L;
  private static final long OTHER_ORGANIZER_REF_ID = 1001L;
  private static final long TRAINEE_REF_ID = 1002L;
  private static final long POOL_ID = 5L;
  private static final long OTHER_POOL_ID = 6L;
  private static final long POOL_LOCK_ID = 9L;

  private final JsonMapper jsonMapper = new JsonMapper();

  @Autowired private TrainingInstanceDataSeeder seeder;
  @Autowired private TrainingInstanceRepository trainingInstanceRepository;
  @Autowired private TrainingRunRepository trainingRunRepository;
  @Autowired private CheatingDetectionRepository cheatingDetectionRepository;
  @Autowired private EntityManager entityManager;

  private UserRef organizer;
  private UserRef otherOrganizer;
  private UserRef trainee;
  private TrainingDefinition definition;
  private TrainingLevel level;
  private TrainingInstance upcomingInstance;

  @BeforeEach
  void seedBaseData() {
    organizer = seeder.userRef(ORGANIZER_REF_ID);
    otherOrganizer = seeder.userRef(OTHER_ORGANIZER_REF_ID);
    trainee = seeder.userRef(TRAINEE_REF_ID);
    definition = seeder.definition("Seed definition");
    level = seeder.level(definition, 1, "Seed level", null);
    upcomingInstance =
        seeder.instance(
            definition,
            "Upcoming instance",
            TrainingInstanceDataSeeder.now().plusDays(10),
            TrainingInstanceDataSeeder.now().plusDays(11),
            "upcoming-1234",
            null,
            organizer);
    stubLoggedInUser(ORGANIZER_REF_ID);
    stubEmptyOpenSearch();
  }

  @Test
  @DisplayName("findTrainingInstanceById as organizer of the instance returns the instance")
  void findTrainingInstanceById_asOrganizerOfInstance_returnsInstanceWithDefinition()
      throws Exception {
    perform(
            get(BASE_PATH + "/{id}", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(upcomingInstance.getId()))
        .andExpect(jsonPath("$.title").value("Upcoming instance"))
        .andExpect(jsonPath("$.access_token").value("upcoming-1234"))
        .andExpect(jsonPath("$.training_definition.id").value(definition.getId()))
        .andExpect(jsonPath("$.training_definition.title").value("Seed definition"))
        .andExpect(jsonPath("$.local_environment").value(true))
        .andExpect(jsonPath("$.start_time", matchesPattern(ISO_PATTERN)))
        .andExpect(jsonPath("$.end_time", matchesPattern(ISO_PATTERN)));
  }

  @Test
  @DisplayName("findTrainingInstanceById as administrator returns an instance they do not organize")
  void findTrainingInstanceById_asAdministrator_returnsInstance() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(upcomingInstance.getId()));
  }

  @Test
  @DisplayName("findTrainingInstanceById as organizer of another instance is forbidden")
  void findTrainingInstanceById_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("findTrainingInstanceById with a role that cannot organize is forbidden")
  void findTrainingInstanceById_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);

    expectApiError(
        perform(get(BASE_PATH + "/{id}", upcomingInstance.getId()), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName("findTrainingInstanceById without authentication is rejected as unauthenticated")
  void findTrainingInstanceById_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/{id}", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("findTrainingInstanceById with an unknown id returns not found")
  void findTrainingInstanceById_unknownId_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/{id}", unknownId()), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("findTrainingInstanceById with a non-numeric id returns bad request")
  void findTrainingInstanceById_nonNumericId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(get(BASE_PATH + "/{id}", "abc"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "findInstanceAccessTokenByPoolId as organizer returns the access token of the pool's instance")
  void findInstanceAccessTokenByPoolId_asOrganizer_returnsAccessToken() throws Exception {
    TrainingInstance pooledInstance = pooledInstance(POOL_ID, "pooled-1234");

    perform(get(BASE_PATH + "/access/{poolId}", POOL_ID), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.access_token").value(pooledInstance.getAccessToken()));
  }

  @Test
  @DisplayName("findInstanceAccessTokenByPoolId as administrator returns the access token")
  void findInstanceAccessTokenByPoolId_asAdministrator_returnsAccessToken() throws Exception {
    pooledInstance(POOL_ID, "pooled-1234");

    perform(
            get(BASE_PATH + "/access/{poolId}", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("pooled-1234"));
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("findInstanceAccessTokenByPoolId with a role that cannot organize is forbidden")
  void findInstanceAccessTokenByPoolId_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    pooledInstance(POOL_ID, "pooled-1234");

    expectApiError(perform(get(BASE_PATH + "/access/{poolId}", POOL_ID), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findInstanceAccessTokenByPoolId without authentication is rejected as unauthenticated")
  void findInstanceAccessTokenByPoolId_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/access/{poolId}", POOL_ID))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("findInstanceAccessTokenByPoolId with a pool no instance holds returns not found")
  void findInstanceAccessTokenByPoolId_unusedPool_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/access/{poolId}", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER));
  }

  @Test
  @DisplayName("findAllTrainingInstances as administrator lists every instance")
  void findAllTrainingInstances_asAdministrator_listsEveryInstance() throws Exception {
    seeder.instance(
        definition,
        "Foreign instance",
        TrainingInstanceDataSeeder.now().plusDays(1),
        TrainingInstanceDataSeeder.now().plusDays(2),
        "foreign-1234",
        null,
        otherOrganizer);
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(get(BASE_PATH).param("size", "100"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(
            jsonPath("$.content[*].title", hasItems("Upcoming instance", "Foreign instance")))
        .andExpect(jsonPath("$.pagination.number").value(0))
        .andExpect(jsonPath("$.pagination.size").value(100));
  }

  @Test
  @DisplayName("findAllTrainingInstances as organizer lists only the instances they organize")
  void findAllTrainingInstances_asOrganizer_listsOnlyOrganizedInstances() throws Exception {
    seeder.instance(
        definition,
        "Foreign instance",
        TrainingInstanceDataSeeder.now().plusDays(1),
        TrainingInstanceDataSeeder.now().plusDays(2),
        "foreign-1234",
        null,
        otherOrganizer);

    perform(get(BASE_PATH).param("size", "100"), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].title", hasItem("Upcoming instance")))
        .andExpect(jsonPath("$.content[*].title", not(hasItem("Foreign instance"))));
  }

  @Test
  @DisplayName("findAllTrainingInstances with a title filter matches partially and ignores case")
  void findAllTrainingInstances_titleFilter_matchesPartiallyIgnoringCase() throws Exception {
    perform(get(BASE_PATH).param("title", "UPCOMING"), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].title", hasItem("Upcoming instance")));
    perform(get(BASE_PATH).param("title", "nomatch"), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)));
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("findAllTrainingInstances with a role that cannot organize is forbidden")
  void findAllTrainingInstances_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    expectApiError(perform(get(BASE_PATH), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName("findAllTrainingInstances without authentication is rejected as unauthenticated")
  void findAllTrainingInstances_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc.perform(get(BASE_PATH)).andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "createTrainingInstance with a local environment returns the instance and makes the caller an organizer")
  void createTrainingInstance_localEnvironment_returnsInstanceWithCallerAsOrganizer()
      throws Exception {
    perform(post(BASE_PATH), createBody(), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Created instance"))
        .andExpect(jsonPath("$.training_definition.id").value(definition.getId()))
        .andExpect(jsonPath("$.local_environment").value(true))
        .andExpect(jsonPath("$.start_time", matchesPattern(ISO_PATTERN)));

    flushAndClear();
    TrainingInstance created = findByTitle("Created instance");
    assertThat(created.getOrganizers())
        .extracting(UserRef::getUserRefId)
        .containsExactly(ORGANIZER_REF_ID);
    externalServices.verify(0, postRequestedFor(urlPathMatching(SANDBOX + "/pools/.*")));
  }

  @Test
  @DisplayName("createTrainingInstance appends a generated pin to the access token")
  void createTrainingInstance_validRequest_appendsGeneratedPinToAccessToken() throws Exception {
    perform(post(BASE_PATH), createBody(), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token", matchesPattern("created-token-\\d{4}")));
  }

  @Test
  @DisplayName("createTrainingInstance with a pool locks the pool with the stored access token")
  void createTrainingInstance_withPool_locksPoolWithStoredAccessToken() throws Exception {
    stubPoolLock(POOL_ID);
    Map<String, Object> body = createBody();
    body.put("local_environment", false);
    body.put("pool_id", POOL_ID);

    perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pool_id").value(POOL_ID));

    flushAndClear();
    TrainingInstance created = findByTitle("Created instance");
    assertThat(created.getPoolId()).isEqualTo(POOL_ID);
    externalServices.verify(
        postRequestedFor(urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks"))
            .withRequestBody(
                containing("\"training_access_token\": \"" + created.getAccessToken() + "\"")));
  }

  @Test
  @DisplayName("createTrainingInstance as administrator creates the instance")
  void createTrainingInstance_asAdministrator_returnsInstance() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(post(BASE_PATH), createBody(), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Created instance"));
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("createTrainingInstance with a role that cannot organize is forbidden")
  void createTrainingInstance_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    expectApiError(perform(post(BASE_PATH), createBody(), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName("createTrainingInstance without authentication is rejected as unauthenticated")
  void createTrainingInstance_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createBody())))
        .andExpect(rejectedAsUnauthenticated());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"title", "access_token", "start_time", "end_time", "training_definition_id"})
  @DisplayName("createTrainingInstance without a required field returns bad request")
  void createTrainingInstance_missingRequiredField_returnsBadRequest(String missingField)
      throws Exception {
    Map<String, Object> body = createBody();
    body.remove(missingField);

    expectApiError(
            perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
            400,
            "BAD_REQUEST")
        .andExpect(jsonPath("$.errors").isNotEmpty());
  }

  @Test
  @DisplayName("createTrainingInstance with a local environment and a pool returns bad request")
  void createTrainingInstance_localEnvironmentWithPool_returnsBadRequest() throws Exception {
    Map<String, Object> body = createBody();
    body.put("pool_id", POOL_ID);

    expectApiError(
        perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "createTrainingInstance without a pool and without a local environment returns bad request")
  void createTrainingInstance_noPoolAndNoLocalEnvironment_returnsBadRequest() throws Exception {
    Map<String, Object> body = createBody();
    body.put("local_environment", false);

    expectApiError(
        perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "createTrainingInstance with a sandbox definition and without a local environment returns bad request")
  void createTrainingInstance_sandboxDefinitionWithoutLocalEnvironment_returnsBadRequest()
      throws Exception {
    Map<String, Object> body = createBody();
    body.put("local_environment", false);
    body.put("pool_id", POOL_ID);
    body.put("sandbox_definition_id", 7);

    expectApiError(
        perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("createTrainingInstance with an unknown training definition returns not found")
  void createTrainingInstance_unknownDefinition_returnsNotFound() throws Exception {
    Map<String, Object> body = createBody();
    body.put("training_definition_id", unknownId());

    expectEntityNotFound(perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER));
  }

  @Test
  @DisplayName("createTrainingInstance with a start time after the end time returns conflict")
  void createTrainingInstance_startAfterEnd_returnsConflict() throws Exception {
    Map<String, Object> body = createBody();
    body.put("start_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(5)));
    body.put("end_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(2)));

    expectEntityError(
        perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "createTrainingInstance with a pool lacking a variable the definition needs returns conflict")
  void createTrainingInstance_poolLacksDefinitionVariable_returnsConflict() throws Exception {
    seeder.level(definition, 2, "Variable level", "secret");
    stubPoolVariables(POOL_ID, "port");
    Map<String, Object> body = createBody();
    body.put("local_environment", false);
    body.put("pool_id", POOL_ID);

    expectEntityError(
            perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
            409,
            "CONFLICT")
        .andExpect(jsonPath("$.entity_error_detail.reason", containsString("secret")));

    externalServices.verify(0, postRequestedFor(urlPathMatching(SANDBOX + "/pools/.*/locks")));
  }

  @Test
  @DisplayName(
      "createTrainingInstance with a pool offering every variable the definition needs succeeds")
  void createTrainingInstance_poolOffersDefinitionVariables_returnsInstance() throws Exception {
    seeder.level(definition, 2, "Variable level", "secret");
    stubPoolVariables(POOL_ID, "port", "secret");
    stubPoolLock(POOL_ID);
    Map<String, Object> body = createBody();
    body.put("local_environment", false);
    body.put("pool_id", POOL_ID);

    perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    externalServices.verify(
        getRequestedFor(urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/variables")));
  }

  @Test
  @DisplayName(
      "createTrainingInstance with a sandbox definition lacking a variable the definition needs returns conflict")
  void createTrainingInstance_sandboxDefinitionLacksVariable_returnsConflict() throws Exception {
    seeder.level(definition, 2, "Variable level", "secret");
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(SANDBOX + "/definitions/7/variables"))
            .willReturn(okJson("[\"port\"]")));
    Map<String, Object> body = createBody();
    body.put("sandbox_definition_id", 7);

    expectEntityError(
        perform(post(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance with an unchanged access token returns the stored access token")
  void updateTrainingInstance_unchangedAccessToken_returnsStoredAccessToken() throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");

    perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.access_token").value("upcoming-1234"));
  }

  @Test
  @DisplayName(
      "updateTrainingInstance with a changed access token before the start returns a token with a new pin")
  void updateTrainingInstance_changedAccessTokenBeforeStart_returnsTokenWithNewPin()
      throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "renamed");
    body.put("title", "Renamed instance");

    perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token", matchesPattern("renamed-\\d{4}")));

    flushAndClear();
    TrainingInstance updated =
        trainingInstanceRepository.findById(upcomingInstance.getId()).orElseThrow();
    assertThat(updated.getTitle()).isEqualTo("Renamed instance");
    assertThat(updated.getAccessToken()).matches("renamed-\\d{4}");
  }

  @Test
  @DisplayName("updateTrainingInstance as administrator updates an instance they do not organize")
  void updateTrainingInstance_asAdministrator_returnsAccessToken() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            put(BASE_PATH),
            updateBody(upcomingInstance, "upcoming"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("upcoming-1234"));
  }

  @Test
  @DisplayName("updateTrainingInstance as organizer of another instance is forbidden")
  void updateTrainingInstance_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            put(BASE_PATH),
            updateBody(upcomingInstance, "upcoming"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("updateTrainingInstance with a role that cannot organize is forbidden")
  void updateTrainingInstance_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(put(BASE_PATH), updateBody(upcomingInstance, "upcoming"), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName("updateTrainingInstance without authentication is rejected as unauthenticated")
  void updateTrainingInstance_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            put(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateBody(upcomingInstance, "upcoming"))))
        .andExpect(rejectedAsUnauthenticated());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"id", "title", "access_token", "start_time", "end_time", "training_definition_id"})
  @DisplayName("updateTrainingInstance without a required field returns bad request")
  void updateTrainingInstance_missingRequiredField_returnsBadRequest(String missingField)
      throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.remove(missingField);

    expectApiError(
            perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
            400,
            "BAD_REQUEST")
        .andExpect(jsonPath("$.errors").isNotEmpty());
  }

  @Test
  @DisplayName("updateTrainingInstance with an unknown id returns not found")
  void updateTrainingInstance_unknownId_returnsNotFound() throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("id", unknownId());

    expectEntityNotFound(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("updateTrainingInstance with an unknown training definition returns not found")
  void updateTrainingInstance_unknownDefinition_returnsNotFound() throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("training_definition_id", unknownId());

    expectEntityNotFound(perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER));
  }

  @Test
  @DisplayName("updateTrainingInstance with a local environment and a pool returns bad request")
  void updateTrainingInstance_localEnvironmentWithPool_returnsBadRequest() throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("pool_id", POOL_ID);

    expectApiError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance without a pool and without a local environment returns bad request")
  void updateTrainingInstance_noPoolAndNoLocalEnvironment_returnsBadRequest() throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("local_environment", false);

    expectApiError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("updateTrainingInstance with a start time after the end time returns conflict")
  void updateTrainingInstance_startAfterEnd_returnsConflict() throws Exception {
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("start_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(20)));
    body.put("end_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(12)));

    expectEntityError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance with a pool lacking a variable the definition needs returns conflict")
  void updateTrainingInstance_poolLacksDefinitionVariable_returnsConflict() throws Exception {
    seeder.level(definition, 2, "Variable level", "secret");
    stubPoolVariables(OTHER_POOL_ID, "port");
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("local_environment", false);
    body.put("pool_id", OTHER_POOL_ID);

    expectEntityError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance giving a started instance another training definition returns conflict")
  void updateTrainingInstance_startedInstanceOtherDefinition_returnsConflict() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    TrainingDefinition otherDefinition = seeder.definition("Other definition");
    Map<String, Object> body = updateBody(started, "started");
    body.put("training_definition_id", otherDefinition.getId());

    expectEntityError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance changing the start time of a started instance returns conflict")
  void updateTrainingInstance_startedInstanceOtherStartTime_returnsConflict() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    Map<String, Object> body = updateBody(started, "started");
    body.put("start_time", isoUtc(started.getStartTime().minusHours(1)));

    expectEntityError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance changing the access token of a started instance returns conflict")
  void updateTrainingInstance_startedInstanceOtherAccessToken_returnsConflict() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);

    expectEntityError(
        perform(
            put(BASE_PATH),
            updateBody(started, "changed"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        409,
        "CONFLICT");
  }

  @Test
  @DisplayName("updateTrainingInstance changing the pool of a started instance returns conflict")
  void updateTrainingInstance_startedInstanceOtherPool_returnsConflict() throws Exception {
    TrainingInstance started = startedInstance("started-1234", POOL_ID);
    Map<String, Object> body = updateBody(started, "started");
    body.put("local_environment", false);
    body.put("pool_id", OTHER_POOL_ID);

    expectEntityError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName(
      "updateTrainingInstance keeping the unchangeable fields of a started instance succeeds")
  void updateTrainingInstance_startedInstanceUnchangedFields_returnsAccessToken() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    Map<String, Object> body = updateBody(started, "started");
    body.put("title", "Started renamed");

    perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("started-1234"));
  }

  @Test
  @DisplayName(
      "updateTrainingInstance moving the end of a finished instance into the future returns conflict")
  void updateTrainingInstance_finishedInstanceEndMovedToFuture_returnsConflict() throws Exception {
    TrainingInstance finished = finishedInstance("finished-1234", null);
    Map<String, Object> body = updateBody(finished, "finished");
    body.put("end_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(3)));

    expectEntityError(
        perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER), 409, "CONFLICT");
  }

  @Test
  @DisplayName("updateTrainingInstance assigning a first pool locks the pool")
  void updateTrainingInstance_firstPool_locksPool() throws Exception {
    stubPoolLock(POOL_ID);
    Map<String, Object> body = updateBody(upcomingInstance, "upcoming");
    body.put("local_environment", false);
    body.put("pool_id", POOL_ID);

    perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(
            trainingInstanceRepository.findById(upcomingInstance.getId()).orElseThrow().getPoolId())
        .isEqualTo(POOL_ID);
    externalServices.verify(
        postRequestedFor(urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks"))
            .withRequestBody(containing("upcoming-1234")));
    externalServices.verify(0, getRequestedFor(urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID)));
  }

  @Test
  @DisplayName(
      "updateTrainingInstance replacing the pool unlocks the old pool, deletes its commands and locks the new pool")
  void updateTrainingInstance_replacedPool_unlocksOldDeletesCommandsAndLocksNew() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");
    stubPoolInfo(POOL_ID);
    stubPoolUnlock(POOL_ID);
    stubCommandIndexDelete();
    stubPoolLock(OTHER_POOL_ID);
    Map<String, Object> body = updateBody(pooled, "pooled");
    body.put("local_environment", false);
    body.put("pool_id", OTHER_POOL_ID);

    perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("pooled-1234"));

    externalServices.verify(
        deleteRequestedFor(
            urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks/" + POOL_LOCK_ID)));
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching("/crczp\\.logs\\.console\\.pool(=|%3D)" + POOL_ID + "\\..*")));
    externalServices.verify(
        postRequestedFor(urlPathEqualTo(SANDBOX + "/pools/" + OTHER_POOL_ID + "/locks"))
            .withRequestBody(containing("pooled-1234")));
  }

  @Test
  @DisplayName("updateTrainingInstance removing the pool unlocks it and deletes its commands")
  void updateTrainingInstance_removedPool_unlocksPoolAndDeletesCommands() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");
    stubPoolInfo(POOL_ID);
    stubPoolUnlock(POOL_ID);
    stubCommandIndexDelete();
    Map<String, Object> body = updateBody(pooled, "pooled");
    body.remove("pool_id");
    body.put("local_environment", true);

    perform(put(BASE_PATH), body, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    externalServices.verify(
        deleteRequestedFor(
            urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks/" + POOL_LOCK_ID)));
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching("/crczp\\.logs\\.console\\.pool(=|%3D)" + POOL_ID + "\\..*")));
    externalServices.verify(0, postRequestedFor(urlPathMatching(SANDBOX + "/pools/.*/locks")));
  }

  @Test
  @DisplayName(
      "deleteTrainingInstance of a finished instance without a pool removes it with its runs and audited events")
  void deleteTrainingInstance_finishedInstanceWithoutPool_removesInstanceRunsAndEvents()
      throws Exception {
    TrainingInstance finished = finishedInstance("finished-1234", null);
    TrainingRun run =
        seeder.run(
            finished, level, trainee, "sandbox-1", finished.getStartTime(), TRState.FINISHED);
    stubEventsIndexDelete();

    perform(delete(BASE_PATH + "/{id}", finished.getId()), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(trainingInstanceRepository.findById(finished.getId())).isEmpty();
    assertThat(trainingRunRepository.findById(run.getId())).isEmpty();
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching(
                "/crczp\\.events\\.trainings.*instance(=|%3D)" + finished.getId() + "\\..*")));
  }

  @Test
  @DisplayName(
      "deleteTrainingInstance of an instance with a cheating detection removes the detection")
  void deleteTrainingInstance_instanceWithCheatingDetection_removesCheatingDetection()
      throws Exception {
    TrainingInstance finished = finishedInstance("finished-1234", null);
    seeder.detection(finished.getId(), TrainingInstanceDataSeeder.now());
    stubEventsIndexDelete();

    perform(delete(BASE_PATH + "/{id}", finished.getId()), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(cheatingDetectionRepository.findAll())
        .noneMatch(detection -> finished.getId().equals(detection.getTrainingInstanceId()));
  }

  @Test
  @DisplayName("deleteTrainingInstance as administrator removes an instance they do not organize")
  void deleteTrainingInstance_asAdministrator_removesInstance() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);
    stubEventsIndexDelete();

    perform(
            delete(BASE_PATH + "/{id}", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(trainingInstanceRepository.findById(upcomingInstance.getId())).isEmpty();
  }

  @Test
  @DisplayName("deleteTrainingInstance as organizer of another instance is forbidden")
  void deleteTrainingInstance_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("deleteTrainingInstance with a role that cannot organize is forbidden")
  void deleteTrainingInstance_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(delete(BASE_PATH + "/{id}", upcomingInstance.getId()), role), 403, "FORBIDDEN");
  }

  @Test
  @DisplayName("deleteTrainingInstance without authentication is rejected as unauthenticated")
  void deleteTrainingInstance_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(delete(BASE_PATH + "/{id}", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("deleteTrainingInstance with an unknown id returns not found")
  void deleteTrainingInstance_unknownId_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            delete(BASE_PATH + "/{id}", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("deleteTrainingInstance with a non-numeric id returns bad request")
  void deleteTrainingInstance_nonNumericId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(delete(BASE_PATH + "/{id}", "abc"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("deleteTrainingInstance with a non-boolean force flag returns bad request")
  void deleteTrainingInstance_invalidForceFlag_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            delete(BASE_PATH + "/{id}", upcomingInstance.getId()).param("forceDelete", "maybe"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("deleteTrainingInstance of an unfinished instance with runs returns conflict")
  void deleteTrainingInstance_unfinishedInstanceWithRuns_returnsConflict() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);

    expectEntityError(
        perform(
            delete(BASE_PATH + "/{id}", started.getId()), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        409,
        "CONFLICT");

    flushAndClear();
    assertThat(trainingInstanceRepository.findById(started.getId())).isPresent();
  }

  @Test
  @DisplayName("deleteTrainingInstance of a finished instance with a pool returns conflict")
  void deleteTrainingInstance_finishedInstanceWithPool_returnsConflict() throws Exception {
    TrainingInstance finished = finishedInstance("finished-1234", POOL_ID);

    expectEntityError(
        perform(
            delete(BASE_PATH + "/{id}", finished.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        409,
        "CONFLICT");

    externalServices.verify(0, getRequestedFor(urlPathMatching(SANDBOX + "/pools/.*")));
  }

  @Test
  @DisplayName(
      "deleteTrainingInstance forced on an unfinished instance with runs and a pool removes it and unlocks the pool")
  void deleteTrainingInstance_forcedUnfinishedInstanceWithPool_removesInstanceAndUnlocksPool()
      throws Exception {
    TrainingInstance started = startedInstance("started-1234", POOL_ID);
    TrainingRun run =
        seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);
    stubPoolInfo(POOL_ID);
    stubPoolUnlock(POOL_ID);
    stubCommandIndexDelete();
    stubEventsIndexDelete();

    perform(
            delete(BASE_PATH + "/{id}", started.getId()).param("forceDelete", "true"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    flushAndClear();
    assertThat(trainingInstanceRepository.findById(started.getId())).isEmpty();
    assertThat(trainingRunRepository.findById(run.getId())).isEmpty();
    externalServices.verify(
        deleteRequestedFor(
            urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks/" + POOL_LOCK_ID)));
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching("/crczp\\.logs\\.console\\.pool(=|%3D)" + POOL_ID + "\\..*")));
  }

  @Test
  @DisplayName(
      "deleteTrainingInstance forced on a local instance leaves the sandbox service untouched")
  void deleteTrainingInstance_forcedLocalInstance_leavesSandboxServiceUntouched() throws Exception {
    stubEventsIndexDelete();

    perform(
            delete(BASE_PATH + "/{id}", upcomingInstance.getId()).param("forceDelete", "true"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk());

    externalServices.verify(0, getRequestedFor(urlPathMatching(SANDBOX + "/.*")));
    externalServices.verify(0, deleteRequestedFor(urlPathMatching(SANDBOX + "/.*")));
  }

  @Test
  @DisplayName(
      "assignPool assigns the pool, locks it with the access token and returns the instance")
  void assignPool_instanceWithoutPool_locksPoolAndReturnsInstance() throws Exception {
    TrainingInstance unassigned = nonLocalInstanceWithoutPool("unassigned-1234");
    stubPoolLock(POOL_ID);

    perform(
            patch(BASE_PATH + "/{id}/assign-pool", unassigned.getId()),
            Map.of("pool_id", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(unassigned.getId()))
        .andExpect(jsonPath("$.pool_id").value(POOL_ID));

    flushAndClear();
    assertThat(trainingInstanceRepository.findById(unassigned.getId()).orElseThrow().getPoolId())
        .isEqualTo(POOL_ID);
    externalServices.verify(
        postRequestedFor(urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks"))
            .withRequestBody(containing("unassigned-1234")));
  }

  @Test
  @DisplayName("assignPool as administrator assigns the pool")
  void assignPool_asAdministrator_returnsInstance() throws Exception {
    TrainingInstance unassigned = nonLocalInstanceWithoutPool("unassigned-1234");
    stubPoolLock(POOL_ID);
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            patch(BASE_PATH + "/{id}/assign-pool", unassigned.getId()),
            Map.of("pool_id", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pool_id").value(POOL_ID));
  }

  @Test
  @DisplayName("assignPool as organizer of another instance is forbidden")
  void assignPool_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    TrainingInstance unassigned = nonLocalInstanceWithoutPool("unassigned-1234");
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/assign-pool", unassigned.getId()),
            Map.of("pool_id", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("assignPool with a role that cannot organize is forbidden")
  void assignPool_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/assign-pool", upcomingInstance.getId()),
            Map.of("pool_id", POOL_ID),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("assignPool without authentication is rejected as unauthenticated")
  void assignPool_withoutAuthentication_returnsUnauthorizedOrForbidden() throws Exception {
    mockMvc
        .perform(
            patch(BASE_PATH + "/{id}/assign-pool", upcomingInstance.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pool_id\": 5}"))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("assignPool on an instance with a local environment returns bad request")
  void assignPool_localEnvironment_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/assign-pool", upcomingInstance.getId()),
            Map.of("pool_id", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("assignPool without a pool id returns bad request")
  void assignPool_missingPoolId_returnsBadRequest() throws Exception {
    TrainingInstance unassigned = nonLocalInstanceWithoutPool("unassigned-1234");

    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/assign-pool", unassigned.getId()),
            Map.of(),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("assignPool with an unknown instance id returns not found")
  void assignPool_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            patch(BASE_PATH + "/{id}/assign-pool", unknownId()),
            Map.of("pool_id", POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("assignPool on an instance that already has a pool returns conflict")
  void assignPool_instanceWithPool_returnsConflict() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");

    expectEntityError(
        perform(
            patch(BASE_PATH + "/{id}/assign-pool", pooled.getId()),
            Map.of("pool_id", OTHER_POOL_ID),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        409,
        "CONFLICT");

    externalServices.verify(0, postRequestedFor(urlPathMatching(SANDBOX + "/pools/.*/locks")));
  }

  @Test
  @DisplayName(
      "unassignPool unlocks the pool, deletes its commands and clears the pool of the instance")
  void unassignPool_instanceWithPool_unlocksPoolAndClearsIt() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");
    stubPoolInfo(POOL_ID);
    stubPoolUnlock(POOL_ID);
    stubCommandIndexDelete();

    perform(
            patch(BASE_PATH + "/{id}/unassign-pool", pooled.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(pooled.getId()))
        .andExpect(jsonPath("$.pool_id").doesNotExist());

    flushAndClear();
    assertThat(trainingInstanceRepository.findById(pooled.getId()).orElseThrow().getPoolId())
        .isNull();
    externalServices.verify(
        deleteRequestedFor(
            urlPathEqualTo(SANDBOX + "/pools/" + POOL_ID + "/locks/" + POOL_LOCK_ID)));
    externalServices.verify(
        deleteRequestedFor(
            urlPathMatching("/crczp\\.logs\\.console\\.pool(=|%3D)" + POOL_ID + "\\..*")));
  }

  @Test
  @DisplayName("unassignPool as administrator unassigns the pool")
  void unassignPool_asAdministrator_returnsInstance() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");
    stubPoolInfo(POOL_ID);
    stubPoolUnlock(POOL_ID);
    stubCommandIndexDelete();
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            patch(BASE_PATH + "/{id}/unassign-pool", pooled.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("unassignPool as organizer of another instance is forbidden")
  void unassignPool_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            patch(BASE_PATH + "/{id}/unassign-pool", pooled.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("unassignPool with a role that cannot organize is forbidden")
  void unassignPool_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(patch(BASE_PATH + "/{id}/unassign-pool", upcomingInstance.getId()), role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("unassignPool without authentication is rejected as unauthenticated")
  void unassignPool_withoutAuthentication_returnsUnauthorizedOrForbidden() throws Exception {
    mockMvc
        .perform(patch(BASE_PATH + "/{id}/unassign-pool", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("unassignPool with an unknown instance id returns not found")
  void unassignPool_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            patch(BASE_PATH + "/{id}/unassign-pool", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("unassignPool on an instance without a pool returns conflict")
  void unassignPool_instanceWithoutPool_returnsConflict() throws Exception {
    expectEntityError(
        perform(
            patch(BASE_PATH + "/{id}/unassign-pool", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        409,
        "CONFLICT");
  }

  @Test
  @DisplayName("findAllTrainingRunsByTrainingInstanceId returns every run of the instance")
  void findAllTrainingRunsByTrainingInstanceId_noFilter_returnsEveryRun() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    TrainingRun running =
        seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);
    TrainingRun archived =
        seeder.run(started, level, trainee, "sandbox-2", started.getStartTime(), TRState.ARCHIVED);
    stubParticipant(TRAINEE_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/training-runs", started.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(
            jsonPath(
                "$.content[*].id",
                hasItems(running.getId().intValue(), archived.getId().intValue())))
        .andExpect(jsonPath("$.content[0].participant_ref.user_ref_id").value(TRAINEE_REF_ID))
        .andExpect(jsonPath("$.content[0].participant_ref.full_name").value("Trainee Seed"))
        .andExpect(jsonPath("$.pagination.total_elements").value(2));
  }

  @Test
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId with isActive true returns only runs that are not archived")
  void findAllTrainingRunsByTrainingInstanceId_isActiveTrue_returnsNonArchivedRuns()
      throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    TrainingRun running =
        seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);
    seeder.run(started, level, trainee, "sandbox-2", started.getStartTime(), TRState.ARCHIVED);
    stubParticipant(TRAINEE_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/training-runs", started.getId()).param("isActive", "true"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(running.getId()));
  }

  @Test
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId with isActive false returns only archived runs")
  void findAllTrainingRunsByTrainingInstanceId_isActiveFalse_returnsArchivedRuns()
      throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);
    TrainingRun archived =
        seeder.run(started, level, trainee, "sandbox-2", started.getStartTime(), TRState.ARCHIVED);
    stubParticipant(TRAINEE_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/training-runs", started.getId()).param("isActive", "false"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(archived.getId()));
  }

  @Test
  @DisplayName("findAllTrainingRunsByTrainingInstanceId as administrator returns the runs")
  void findAllTrainingRunsByTrainingInstanceId_asAdministrator_returnsRuns() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);
    stubParticipant(TRAINEE_REF_ID);
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/training-runs", started.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId as organizer of another instance is forbidden")
  void findAllTrainingRunsByTrainingInstanceId_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/training-runs", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId with a role that cannot organize is forbidden")
  void findAllTrainingRunsByTrainingInstanceId_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(get(BASE_PATH + "/{id}/training-runs", upcomingInstance.getId()), role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId without authentication is rejected as unauthenticated")
  void
      findAllTrainingRunsByTrainingInstanceId_withoutAuthentication_returnsUnauthorizedOrForbidden()
          throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/{id}/training-runs", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId with an unknown instance id returns not found")
  void findAllTrainingRunsByTrainingInstanceId_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/{id}/training-runs", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName(
      "findAllTrainingRunsByTrainingInstanceId with a non-numeric instance id returns bad request")
  void findAllTrainingRunsByTrainingInstanceId_nonNumericId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/training-runs", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("getOrganizersOfTrainingInstance returns the organizers described by user-and-group")
  void getOrganizersOfTrainingInstance_asOrganizer_returnsOrganizersFromUserAndGroup()
      throws Exception {
    stubUserPage("/users/ids", ORGANIZER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/organizers", upcomingInstance.getId())
                .param("givenName", "Org")
                .param("familyName", "Seed"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].user_ref_id").value(ORGANIZER_REF_ID))
        .andExpect(jsonPath("$.content[0].full_name").value("User " + ORGANIZER_REF_ID));

    externalServices.verify(
        getRequestedFor(urlPathEqualTo(USER_AND_GROUP + "/users/ids"))
            .withQueryParam("ids", equalTo(String.valueOf(ORGANIZER_REF_ID)))
            .withQueryParam("givenName", equalTo("Org"))
            .withQueryParam("familyName", equalTo("Seed")));
  }

  @Test
  @DisplayName("getOrganizersOfTrainingInstance as administrator returns the organizers")
  void getOrganizersOfTrainingInstance_asAdministrator_returnsOrganizers() throws Exception {
    stubUserPage("/users/ids", ORGANIZER_REF_ID);
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].user_ref_id").value(ORGANIZER_REF_ID));
  }

  @Test
  @DisplayName("getOrganizersOfTrainingInstance as organizer of another instance is forbidden")
  void getOrganizersOfTrainingInstance_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("getOrganizersOfTrainingInstance with a role that cannot organize is forbidden")
  void getOrganizersOfTrainingInstance_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(get(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()), role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "getOrganizersOfTrainingInstance without authentication is rejected as unauthenticated")
  void getOrganizersOfTrainingInstance_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("getOrganizersOfTrainingInstance with an unknown instance id returns not found")
  void getOrganizersOfTrainingInstance_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/{id}/organizers", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("getOrganizersOfTrainingInstance with a non-numeric instance id returns bad request")
  void getOrganizersOfTrainingInstance_nonNumericId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/organizers", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "getOrganizersNotInGivenTrainingInstance returns the organizers outside the instance asking user-and-group to exclude its organizers")
  void getOrganizersNotInGivenTrainingInstance_asOrganizer_returnsOrganizersOutsideInstance()
      throws Exception {
    stubUserPage("/roles/users-not-with-ids", OTHER_ORGANIZER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/organizers-not-in-training-instance", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].user_ref_id").value(OTHER_ORGANIZER_REF_ID));

    externalServices.verify(
        getRequestedFor(urlPathEqualTo(USER_AND_GROUP + "/roles/users-not-with-ids"))
            .withQueryParam("roleType", equalTo("ROLE_TRAINING_ORGANIZER"))
            .withQueryParam("ids", equalTo(String.valueOf(ORGANIZER_REF_ID))));
  }

  @Test
  @DisplayName(
      "getOrganizersNotInGivenTrainingInstance as administrator returns the organizers outside the instance")
  void getOrganizersNotInGivenTrainingInstance_asAdministrator_returnsOrganizers()
      throws Exception {
    stubUserPage("/roles/users-not-with-ids", OTHER_ORGANIZER_REF_ID);
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/organizers-not-in-training-instance", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].user_ref_id").value(OTHER_ORGANIZER_REF_ID));
  }

  @Test
  @DisplayName(
      "getOrganizersNotInGivenTrainingInstance as organizer of another instance is forbidden")
  void getOrganizersNotInGivenTrainingInstance_asOrganizerOfOtherInstance_returnsForbidden()
      throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/organizers-not-in-training-instance", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName(
      "getOrganizersNotInGivenTrainingInstance with a role that cannot organize is forbidden")
  void getOrganizersNotInGivenTrainingInstance_withNonOrganizerRole_returnsForbidden(
      RoleTypeSecurity role) throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/organizers-not-in-training-instance", upcomingInstance.getId()),
            role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "getOrganizersNotInGivenTrainingInstance without authentication is rejected as unauthenticated")
  void
      getOrganizersNotInGivenTrainingInstance_withoutAuthentication_returnsUnauthorizedOrForbidden()
          throws Exception {
    mockMvc
        .perform(
            get(BASE_PATH + "/{id}/organizers-not-in-training-instance", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName(
      "getOrganizersNotInGivenTrainingInstance with an unknown instance id returns not found")
  void getOrganizersNotInGivenTrainingInstance_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/{id}/organizers-not-in-training-instance", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName(
      "editOrganizers adds the organizers fetched from user-and-group and removes the listed ones")
  void editOrganizers_additionAndRemoval_updatesOrganizersAndReturnsNoContent() throws Exception {
    upcomingInstance.addOrganizer(otherOrganizer);
    seeder.userRef(1003L);
    stubUserPage("/users/ids", 1003L);

    perform(
            put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId())
                .param("organizersAddition", "1003")
                .param("organizersRemoval", String.valueOf(OTHER_ORGANIZER_REF_ID)),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isNoContent());

    flushAndClear();
    TrainingInstance updated =
        trainingInstanceRepository.findById(upcomingInstance.getId()).orElseThrow();
    assertThat(updated.getOrganizers())
        .extracting(UserRef::getUserRefId)
        .containsExactlyInAnyOrder(ORGANIZER_REF_ID, 1003L);
  }

  @Test
  @DisplayName("editOrganizers ignores the removal of the calling user")
  void editOrganizers_removalOfCaller_keepsCallerAsOrganizer() throws Exception {
    perform(
            put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId())
                .param("organizersRemoval", String.valueOf(ORGANIZER_REF_ID)),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isNoContent());

    flushAndClear();
    TrainingInstance updated =
        trainingInstanceRepository.findById(upcomingInstance.getId()).orElseThrow();
    assertThat(updated.getOrganizers())
        .extracting(UserRef::getUserRefId)
        .contains(ORGANIZER_REF_ID);
  }

  @Test
  @DisplayName("editOrganizers without any parameter returns no content and changes nothing")
  void editOrganizers_noParameters_returnsNoContent() throws Exception {
    perform(
            put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isNoContent());

    flushAndClear();
    TrainingInstance updated =
        trainingInstanceRepository.findById(upcomingInstance.getId()).orElseThrow();
    assertThat(updated.getOrganizers())
        .extracting(UserRef::getUserRefId)
        .containsExactly(ORGANIZER_REF_ID);
  }

  @Test
  @DisplayName("editOrganizers as administrator edits the organizers")
  void editOrganizers_asAdministrator_returnsNoContent() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId())
                .param("organizersRemoval", String.valueOf(ORGANIZER_REF_ID)),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isNoContent());

    flushAndClear();
    assertThat(
            trainingInstanceRepository
                .findById(upcomingInstance.getId())
                .orElseThrow()
                .getOrganizers())
        .isEmpty();
  }

  @Test
  @DisplayName("editOrganizers as organizer of another instance is forbidden")
  void editOrganizers_asOrganizerOfOtherInstance_returnsForbidden() throws Exception {
    stubLoggedInUser(OTHER_ORGANIZER_REF_ID);

    expectApiError(
        perform(
            put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @ParameterizedTest
  @EnumSource(
      value = RoleTypeSecurity.class,
      names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
  @DisplayName("editOrganizers with a role that cannot organize is forbidden")
  void editOrganizers_withNonOrganizerRole_returnsForbidden(RoleTypeSecurity role)
      throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);
    expectApiError(
        perform(put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()), role),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("editOrganizers without authentication is rejected as unauthenticated")
  void editOrganizers_withoutAuthentication_returnsUnauthorizedOrForbidden() throws Exception {
    mockMvc
        .perform(put(BASE_PATH + "/{id}/organizers", upcomingInstance.getId()))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("editOrganizers with an unknown instance id returns not found")
  void editOrganizers_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            put(BASE_PATH + "/{id}/organizers", unknownId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("findTrainingInstancesByIds as administrator returns the listed instances")
  void findTrainingInstancesByIds_asAdministrator_returnsListedInstances() throws Exception {
    TrainingInstance foreign =
        seeder.instance(
            definition,
            "Foreign instance",
            TrainingInstanceDataSeeder.now().plusDays(1),
            TrainingInstanceDataSeeder.now().plusDays(2),
            "foreign-1234",
            null,
            otherOrganizer);
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/by-ids")
                .param("ids", upcomingInstance.getId() + "," + foreign.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].title", hasItems("Upcoming instance", "Foreign instance")))
        .andExpect(jsonPath("$[0].definition_id").value(definition.getId()))
        .andExpect(jsonPath("$[0].start_time", matchesPattern(ISO_PATTERN)));
  }

  @Test
  @DisplayName("findTrainingInstancesByIds as organizer of every listed instance returns them")
  void findTrainingInstancesByIds_asOrganizerOfEveryInstance_returnsListedInstances()
      throws Exception {
    perform(
            get(BASE_PATH + "/by-ids").param("ids", String.valueOf(upcomingInstance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(upcomingInstance.getId()));
  }

  @Test
  @DisplayName("findTrainingInstancesByIds as participant of every listed instance returns them")
  void findTrainingInstancesByIds_asParticipantOfEveryInstance_returnsListedInstances()
      throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    seeder.run(started, level, trainee, "sandbox-1", started.getStartTime(), TRState.RUNNING);
    stubLoggedInUser(TRAINEE_REF_ID);

    perform(
            get(BASE_PATH + "/by-ids").param("ids", String.valueOf(started.getId())),
            RoleTypeSecurity.ROLE_TRAINING_TRAINEE)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(started.getId()));
  }

  @Test
  @DisplayName("findTrainingInstancesByIds as organizer of only some listed instances is forbidden")
  void findTrainingInstancesByIds_asOrganizerOfSomeInstances_returnsForbidden() throws Exception {
    TrainingInstance foreign =
        seeder.instance(
            definition,
            "Foreign instance",
            TrainingInstanceDataSeeder.now().plusDays(1),
            TrainingInstanceDataSeeder.now().plusDays(2),
            "foreign-1234",
            null,
            otherOrganizer);

    expectApiError(
        perform(
            get(BASE_PATH + "/by-ids")
                .param("ids", upcomingInstance.getId() + "," + foreign.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName(
      "findTrainingInstancesByIds as trainee without a run in the listed instance is forbidden")
  void findTrainingInstancesByIds_asTraineeWithoutRun_returnsForbidden() throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/by-ids").param("ids", String.valueOf(upcomingInstance.getId())),
            RoleTypeSecurity.ROLE_TRAINING_TRAINEE),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("findTrainingInstancesByIds without authentication is rejected as unauthenticated")
  void findTrainingInstancesByIds_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/by-ids").param("ids", String.valueOf(upcomingInstance.getId())))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("findTrainingInstancesByIds without ids returns bad request")
  void findTrainingInstancesByIds_missingIds_returnsBadRequest() throws Exception {
    expectApiError(
        perform(get(BASE_PATH + "/by-ids"), RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("findTrainingInstancesByIds with non-numeric ids returns bad request")
  void findTrainingInstancesByIds_nonNumericIds_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/by-ids").param("ids", "abc"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName(
      "getTrainingInstanceEvents as organizer returns the audit events with plain sandbox ids")
  void getTrainingInstanceEvents_asOrganizer_returnsEventsWithPlainSandboxIds() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    stubSearchHits(trainingEventHit(started, "sandbox-foreign", "level_started", 11L));

    perform(
            get(BASE_PATH + "/{id}/events", started.getId())
                .param("eventType", "level_started")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].type").value("level_started"))
        .andExpect(jsonPath("$[0].sandbox_id").value("sandbox-foreign"))
        .andExpect(jsonPath("$[0].timestamp", matchesPattern(ISO_PATTERN)));

    externalServices.verify(
        postRequestedFor(urlPathMatching("/.*_search"))
            .withRequestBody(notContaining("user_ref_id")));
  }

  @Test
  @DisplayName("getTrainingInstanceEvents as administrator returns the audit events")
  void getTrainingInstanceEvents_asAdministrator_returnsEvents() throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    stubSearchHits(trainingEventHit(started, "sandbox-foreign", "level_started", 11L));
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/events", started.getId())
                .param("eventType", "level_started")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sandbox_id").value("sandbox-foreign"));
  }

  @Test
  @DisplayName(
      "getTrainingInstanceEvents as participant restricts answer events to their own and hashes foreign sandbox ids")
  void getTrainingInstanceEvents_asParticipant_restrictsAnswersAndHashesForeignSandboxIds()
      throws Exception {
    TrainingInstance started = startedInstance("started-1234", null);
    seeder.run(started, level, trainee, "sandbox-own", started.getStartTime(), TRState.RUNNING);
    stubSearchHits(
        trainingEventHit(started, "sandbox-own", "correct_answer_submitted", 11L),
        trainingEventHit(started, "sandbox-foreign", "correct_answer_submitted", 12L));
    stubLoggedInUser(TRAINEE_REF_ID);

    perform(
            get(BASE_PATH + "/{id}/events", started.getId())
                .param("eventType", "correct_answer_submitted")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_TRAINEE)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sandbox_id").value("sandbox-own"))
        .andExpect(jsonPath("$[1].sandbox_id").value(SandboxIdHasher.hash("sandbox-foreign")));

    externalServices.verify(
        postRequestedFor(urlPathMatching("/.*_search"))
            .withRequestBody(containing("user_ref_id"))
            .withRequestBody(containing(String.valueOf(TRAINEE_REF_ID))));
  }

  @Test
  @DisplayName(
      "getTrainingInstanceEvents of type COMMAND reads the console commands of the pool of the instance")
  void getTrainingInstanceEvents_commandType_returnsConsoleCommandsOfPool() throws Exception {
    TrainingInstance pooled = pooledInstance(POOL_ID, "pooled-1234");
    stubSearchHits(
        "{\"_index\":\"crczp.logs.console.pool=5.sandbox=sandbox-1\",\"_id\":\"c1\","
            + "\"_source\":{\"sandbox_id\":\"sandbox-1\",\"timestamp_str\":\"2030-01-01T10:00:00.000Z\","
            + "\"cmd_type\":\"bash-command\",\"cmd\":\"ls -la\",\"hostname\":\"host\","
            + "\"username\":\"user\",\"wd\":\"/\",\"ip\":\"10.0.0.1\"}}");

    perform(
            get(BASE_PATH + "/{id}/events", pooled.getId())
                .param("eventType", "COMMAND")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].type").value("COMMAND"))
        .andExpect(jsonPath("$[0].sandbox_id").value("sandbox-1"))
        .andExpect(jsonPath("$[0].command").value("ls"));

    externalServices.verify(
        postRequestedFor(
            urlPathMatching("/crczp\\.logs\\.console\\.pool(=|%3D)" + POOL_ID + "\\..*/_search")));
  }

  @Test
  @DisplayName(
      "getTrainingInstanceEvents of type COMMAND on an instance without a pool returns no events")
  void getTrainingInstanceEvents_commandTypeWithoutPool_returnsNoEvents() throws Exception {
    perform(
            get(BASE_PATH + "/{id}/events", upcomingInstance.getId())
                .param("eventType", "COMMAND")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    externalServices.verify(0, postRequestedFor(urlPathMatching("/.*_search")));
  }

  @Test
  @DisplayName(
      "getTrainingInstanceEvents as a user neither organizing nor taking part is forbidden")
  void getTrainingInstanceEvents_asUnrelatedTrainee_returnsForbidden() throws Exception {
    stubLoggedInUser(TRAINEE_REF_ID);

    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/events", upcomingInstance.getId())
                .param("eventType", "level_started")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_TRAINEE),
        403,
        "FORBIDDEN");
  }

  @Test
  @DisplayName("getTrainingInstanceEvents without authentication is rejected as unauthenticated")
  void getTrainingInstanceEvents_withoutAuthentication_returnsUnauthorizedOrForbidden()
      throws Exception {
    mockMvc
        .perform(
            get(BASE_PATH + "/{id}/events", upcomingInstance.getId())
                .param("eventType", "level_started")
                .param("sinceTimestamp", "0"))
        .andExpect(rejectedAsUnauthenticated());
  }

  @Test
  @DisplayName("getTrainingInstanceEvents with an unknown instance id returns not found")
  void getTrainingInstanceEvents_unknownInstance_returnsNotFound() throws Exception {
    expectEntityNotFound(
        perform(
            get(BASE_PATH + "/{id}/events", unknownId())
                .param("eventType", "level_started")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR));
  }

  @Test
  @DisplayName("getTrainingInstanceEvents with a non-numeric timestamp returns bad request")
  void getTrainingInstanceEvents_nonNumericTimestamp_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/events", upcomingInstance.getId())
                .param("eventType", "level_started")
                .param("sinceTimestamp", "yesterday"),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER),
        400,
        "BAD_REQUEST");
  }

  @Test
  @DisplayName("getTrainingInstanceEvents with a non-numeric instance id returns bad request")
  void getTrainingInstanceEvents_nonNumericInstanceId_returnsBadRequest() throws Exception {
    expectApiError(
        perform(
            get(BASE_PATH + "/{id}/events", "abc")
                .param("eventType", "level_started")
                .param("sinceTimestamp", "0"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR),
        400,
        "BAD_REQUEST");
  }

  private static final String ISO_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static String isoUtc(LocalDateTime dateTime) {
    return ISO_UTC.format(dateTime);
  }

  private long unknownId() {
    return 9_000_000L + System.nanoTime() % 1_000_000L;
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private TrainingInstance findByTitle(String title) {
    return trainingInstanceRepository.findAll().stream()
        .filter(instance -> title.equals(instance.getTitle()))
        .findFirst()
        .orElseThrow();
  }

  private TrainingInstance pooledInstance(long poolId, String accessToken) {
    return seeder.instance(
        definition,
        "Pooled instance " + poolId,
        TrainingInstanceDataSeeder.now().plusDays(10),
        TrainingInstanceDataSeeder.now().plusDays(11),
        accessToken,
        poolId,
        organizer);
  }

  private TrainingInstance nonLocalInstanceWithoutPool(String accessToken) {
    TrainingInstance instance =
        seeder.instance(
            definition,
            "Unassigned instance",
            TrainingInstanceDataSeeder.now().plusDays(10),
            TrainingInstanceDataSeeder.now().plusDays(11),
            accessToken,
            null,
            organizer);
    instance.setLocalEnvironment(false);
    return instance;
  }

  private TrainingInstance startedInstance(String accessToken, Long poolId) {
    return seeder.instance(
        definition,
        "Started instance",
        TrainingInstanceDataSeeder.now().minusDays(1),
        TrainingInstanceDataSeeder.now().plusDays(1),
        accessToken,
        poolId,
        organizer);
  }

  private TrainingInstance finishedInstance(String accessToken, Long poolId) {
    return seeder.instance(
        definition,
        "Finished instance",
        TrainingInstanceDataSeeder.now().minusDays(3),
        TrainingInstanceDataSeeder.now().minusDays(2),
        accessToken,
        poolId,
        organizer);
  }

  private Map<String, Object> createBody() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("title", "Created instance");
    body.put("access_token", "created-token");
    body.put("start_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(5)));
    body.put("end_time", isoUtc(TrainingInstanceDataSeeder.now().plusDays(6)));
    body.put("training_definition_id", definition.getId());
    body.put("local_environment", true);
    body.put("show_stepper_bar", true);
    body.put("backward_mode", false);
    return body;
  }

  private Map<String, Object> updateBody(TrainingInstance instance, String baseAccessToken) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", instance.getId());
    body.put("title", instance.getTitle());
    body.put("access_token", baseAccessToken);
    body.put("start_time", isoUtc(instance.getStartTime()));
    body.put("end_time", isoUtc(instance.getEndTime()));
    body.put("training_definition_id", instance.getTrainingDefinition().getId());
    body.put("local_environment", instance.isLocalEnvironment());
    if (instance.getPoolId() != null) {
      body.put("pool_id", instance.getPoolId());
    }
    body.put("show_stepper_bar", true);
    body.put("backward_mode", false);
    return body;
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

  private ResultActions expectEntityError(ResultActions result, int statusCode, String statusName)
      throws Exception {
    return expectApiError(result, statusCode, statusName)
        .andExpect(jsonPath("$.entity_error_detail.reason").isNotEmpty());
  }

  private ResultActions expectEntityNotFound(ResultActions result) throws Exception {
    return expectEntityError(result, 404, "NOT_FOUND")
        .andExpect(jsonPath("$.entity_error_detail.entity").isNotEmpty());
  }

  private void stubParticipant(long userRefId) {
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(USER_AND_GROUP + "/users/" + userRefId))
            .willReturn(
                okJson("{\"user_ref_id\": " + userRefId + ", \"full_name\": \"Trainee Seed\"}")));
  }

  private void stubUserPage(String path, long... userRefIds) {
    StringBuilder users = new StringBuilder();
    for (long userRefId : userRefIds) {
      if (users.length() > 0) {
        users.append(',');
      }
      users
          .append("{\"user_ref_id\":")
          .append(userRefId)
          .append(",\"full_name\":\"User ")
          .append(userRefId)
          .append("\"}");
    }
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(USER_AND_GROUP + path))
            .willReturn(
                okJson(
                    "{\"content\":["
                        + users
                        + "],\"pagination\":{\"number\":0,\"number_of_elements\":"
                        + userRefIds.length
                        + ",\"size\":20,\"total_elements\":"
                        + userRefIds.length
                        + ",\"total_pages\":1}}")));
  }

  private void stubPoolLock(long poolId) {
    externalServices.stubFor(
        WireMock.post(urlPathEqualTo(SANDBOX + "/pools/" + poolId + "/locks"))
            .willReturn(okJson("{\"id\": " + POOL_LOCK_ID + ", \"pool_id\": " + poolId + "}")));
  }

  private void stubPoolInfo(long poolId) {
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(SANDBOX + "/pools/" + poolId))
            .willReturn(okJson("{\"id\": " + poolId + ", \"lock_id\": " + POOL_LOCK_ID + "}")));
  }

  private void stubPoolUnlock(long poolId) {
    externalServices.stubFor(
        WireMock.delete(urlPathEqualTo(SANDBOX + "/pools/" + poolId + "/locks/" + POOL_LOCK_ID))
            .willReturn(WireMock.aResponse().withStatus(204)));
  }

  private void stubPoolVariables(long poolId, String... variables) {
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(SANDBOX + "/pools/" + poolId + "/variables"))
            .willReturn(okJson("{\"variables\": [\"" + String.join("\",\"", variables) + "\"]}")));
  }

  private void stubCommandIndexDelete() {
    externalServices.stubFor(
        WireMock.delete(urlPathMatching("/crczp\\.logs\\.console.*"))
            .willReturn(okJson("{\"acknowledged\":true}")));
  }

  private void stubEventsIndexDelete() {
    externalServices.stubFor(
        WireMock.delete(urlPathMatching("/crczp\\.events\\.trainings.*"))
            .willReturn(okJson("{\"acknowledged\":true}")));
  }

  private void stubEmptyOpenSearch() {
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*_search")).willReturn(okJson(EMPTY_SEARCH_RESPONSE)));
  }

  private void stubSearchHits(String... hits) {
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*_search"))
            .willReturn(
                okJson(
                    "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,"
                        + "\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":"
                        + hits.length
                        + ",\"relation\":\"eq\"},\"max_score\":null,\"hits\":["
                        + String.join(",", hits)
                        + "]}}")));
  }

  private String trainingEventHit(
      TrainingInstance instance, String sandboxId, String type, long timestamp) {
    return "{\"_index\":\"events\",\"_id\":\"e"
        + timestamp
        + "\",\"_source\":{\"type\":\""
        + type
        + "\",\"sandbox_id\":\""
        + sandboxId
        + "\",\"pool_id\":"
        + POOL_ID
        + ",\"training_definition_id\":"
        + definition.getId()
        + ",\"training_instance_id\":"
        + instance.getId()
        + ",\"training_run_id\":1,\"training_time\":5000,\"actual_score_in_level\":0,\"level\":"
        + level.getId()
        + ",\"level_order\":1,\"user_ref_id\":"
        + TRAINEE_REF_ID
        + ",\"timestamp\":"
        + timestamp
        + ",\"total_training_level_score\":0,\"total_assessment_level_score\":0,"
        + "\"answer_content\":\"answer\",\"level_type\":\"TRAINING\",\"max_score\":100,"
        + "\"level_title\":\"Seed level\"}}";
  }
}
