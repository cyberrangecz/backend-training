package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Integration tests of the training definition endpoints, run against the whole application with
 * its database and the external services answered by stubs. Every test seeds the rows it needs in
 * committed transactions, because the application authorizes through transactions of its own.
 */
@DisplayName("Training definition endpoints")
class TrainingDefinitionsIT extends AbstractIntegrationTest {

  private static final String DEFINITIONS = "/training-definitions";
  private static final String USERS_BY_IDS_PATH =
      IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/users/ids";
  private static final String USERS_BY_ROLE_PATH =
      IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/roles/users";
  private static final String USERS_BY_ROLE_EXCLUDING_PATH =
      IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/roles/users-not-with-ids";

  private static final long CALLER_REF_ID = 700_001L;
  private static final long OTHER_DESIGNER_REF_ID = 700_002L;
  private static final long ORGANIZER_REF_ID = 700_003L;
  private static final long TRAINEE_REF_ID = 700_004L;
  private static final long NEW_AUTHOR_REF_ID = 700_005L;
  private static final String CALLER_FULL_NAME = "Dee Signer";
  private static final String DATE_TIME_PATTERN =
      "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
  private static final String SEEDED_DATE_TIME = "2026-01-15T10:30:45.000Z";

  /** Total of the four level durations seeded by {@link #seedFourLevelDefinition}. */
  private static final int FOUR_LEVEL_DURATION = 19;

  @Autowired private ApplicationContext applicationContext;

  private MockMvcTester tester;
  private TrainingDefinitionSeeder seeder;

  /** Primary keys of a definition seeded with one level of every type, in presentation order. */
  private record SeededDefinition(
      Long id,
      Long infoLevelId,
      Long trainingLevelId,
      Long accessLevelId,
      Long assessmentLevelId) {}

  @BeforeEach
  void prepareTester() {
    tester = MockMvcTester.create(mockMvc);
    seeder = new TrainingDefinitionSeeder(applicationContext);
    stubCaller(CALLER_REF_ID);
  }

  @AfterEach
  void removeSeededRows() {
    seeder.clear();
  }

  private void stubCaller(long userRefId) {
    externalServices.stubFor(
        get(urlPathEqualTo(LOGGED_IN_USER_PATH))
            .willReturn(
                okJson(
                    "{\"user_ref_id\": %d, \"full_name\": \"%s\"}"
                        .formatted(userRefId, CALLER_FULL_NAME))));
  }

  private static RequestPostProcessor designer() {
    return callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER);
  }

  private static RequestPostProcessor organizer() {
    return callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER);
  }

  private static RequestPostProcessor trainee() {
    return callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_TRAINEE);
  }

  private static RequestPostProcessor administrator() {
    return callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR);
  }

  private static RequestPostProcessor designerAndOrganizer() {
    return callerWithRoles(
        RoleTypeSecurity.ROLE_TRAINING_DESIGNER, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER);
  }

  private MockMvcRequestBuilder getAs(
      RequestPostProcessor caller, String uriTemplate, Object... uriVariables) {
    return tester.get().uri(uriTemplate, uriVariables).with(caller);
  }

  private MockMvcRequestBuilder postAs(
      RequestPostProcessor caller, String uriTemplate, Object... uriVariables) {
    return tester.post().uri(uriTemplate, uriVariables).with(caller);
  }

  private MockMvcRequestBuilder putAs(
      RequestPostProcessor caller, String uriTemplate, Object... uriVariables) {
    return tester.put().uri(uriTemplate, uriVariables).with(caller);
  }

  private MockMvcRequestBuilder deleteAs(
      RequestPostProcessor caller, String uriTemplate, Object... uriVariables) {
    return tester.delete().uri(uriTemplate, uriVariables).with(caller);
  }

  private static MockMvcRequestBuilder withJson(MockMvcRequestBuilder request, String json) {
    return request.contentType(MediaType.APPLICATION_JSON).content(json);
  }

  /** Sends the request, expects a JSON body with the given status, and returns the parsed body. */
  private static DocumentContext respond(MockMvcRequestBuilder request, HttpStatus status) {
    MvcTestResult result = request.exchange();
    assertThat(result).hasStatus(status).hasContentType(MediaType.APPLICATION_JSON);
    return JsonPath.parse(
        new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
  }

  private static void expectEmptyResponse(MockMvcRequestBuilder request, HttpStatus status) {
    assertThat(request).hasStatus(status).body().isEmpty();
  }

  private static void expectError(MockMvcRequestBuilder request, HttpStatus status) {
    DocumentContext body = respond(request, status);
    assertThat(body.<String>read("$.status")).isEqualTo(status.name());
  }

  /** Expects an error whose body names the entity that was not found or is in conflict. */
  private static void expectEntityError(
      MockMvcRequestBuilder request, HttpStatus status, String entity, long identifierValue) {
    DocumentContext body = respond(request, status);
    assertThat(body.<String>read("$.status")).isEqualTo(status.name());
    assertThat(body.<String>read("$.entity_error_detail.entity")).isEqualTo(entity);
    assertThat(body.<String>read("$.entity_error_detail.identifier")).isEqualTo("id");
    assertThat(body.<Number>read("$.entity_error_detail.identifier_value").longValue())
        .isEqualTo(identifierValue);
  }

  private static <T> List<T> listAt(DocumentContext body, String path) {
    return body.read(path);
  }

  private DocumentContext readDefinitionAsAdministrator(Long definitionId) {
    return respond(getAs(administrator(), DEFINITIONS + "/{id}", definitionId), HttpStatus.OK);
  }

  private DocumentContext readLevelAsAdministrator(Long levelId) {
    return respond(getAs(administrator(), DEFINITIONS + "/levels/{id}", levelId), HttpStatus.OK);
  }

  /** Seeds a definition with an info, training, access and assessment level, 19 minutes long. */
  private SeededDefinition seedFourLevelDefinition(
      String title, TDState state, long... authorUserRefIds) {
    Long definitionId = seeder.definition(title, state, authorUserRefIds);
    return new SeededDefinition(
        definitionId,
        seeder.infoLevel(definitionId, 0, 5),
        seeder.trainingLevel(definitionId, 1, 7, "T1059"),
        seeder.accessLevel(definitionId, 2, 3),
        seeder.assessmentLevel(definitionId, 3, 4));
  }

  private SeededDefinition seedFourLevelDefinition(TDState state, long... authorUserRefIds) {
    return seedFourLevelDefinition("Seeded definition", state, authorUserRefIds);
  }

  private static LocalDateTime inTheFuture() {
    return LocalDateTime.now().plusDays(5);
  }

  private static LocalDateTime inThePast() {
    return LocalDateTime.now().minusDays(5);
  }

  /** Builds the page body the user-and-group service answers for the given users. */
  private static String upstreamUserPage(long... userRefIds) {
    String users =
        LongStream.of(userRefIds)
            .mapToObj(
                userRefId ->
                    ("{\"id\": %d, \"sub\": \"user%d@example.org\", \"full_name\": \"Full %d\","
                            + " \"given_name\": \"Given%d\", \"family_name\": \"Family%d\","
                            + " \"iss\": \"http://issuer\", \"mail\": \"user%d@example.org\"}")
                        .formatted(
                            userRefId, userRefId, userRefId, userRefId, userRefId, userRefId))
            .collect(Collectors.joining(","));
    return ("{\"content\": [%s], \"pagination\": {\"number\": 0, \"number_of_elements\": %d,"
            + " \"size\": 20, \"total_elements\": %d, \"total_pages\": 1}}")
        .formatted(users, userRefIds.length, userRefIds.length);
  }

  private void stubUpstreamUsers(String path, long... userRefIds) {
    externalServices.stubFor(
        get(urlPathEqualTo(path)).willReturn(okJson(upstreamUserPage(userRefIds))));
  }

  private static Set<Long> setOf(Long... values) {
    return Set.of(values);
  }

  private static String quoted(String value) {
    return value == null ? "null" : "\"" + value + "\"";
  }

  private static List<Object> orderedLevelIds(DocumentContext body) {
    return listAt(body, "$[*].id");
  }

  private static List<Object> idsOf(Long... ids) {
    return Arrays.stream(ids).map(Long::intValue).collect(Collectors.toList());
  }

  private static Stream<Arguments> noAuthenticationRequests() {
    return Stream.of(
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/1"),
        Arguments.of(HttpMethod.GET, DEFINITIONS),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/for-organizers?state=RELEASED"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/played-mitre-techniques"),
        Arguments.of(HttpMethod.POST, DEFINITIONS),
        Arguments.of(HttpMethod.PUT, DEFINITIONS),
        Arguments.of(HttpMethod.POST, DEFINITIONS + "/1?title=Copy"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/levels/1/swap-with/2"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/levels/1/move-to/0"),
        Arguments.of(HttpMethod.DELETE, DEFINITIONS + "/1"),
        Arguments.of(HttpMethod.DELETE, DEFINITIONS + "/1/levels/1"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/training-levels"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/info-levels"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/assessment-levels"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/levels"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/levels/1"),
        Arguments.of(HttpMethod.POST, DEFINITIONS + "/1/levels/INFO"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/designers"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/organizers"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/1/designers-not-in-training-definition"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/1/beta-testers"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/1/authors"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/authors"),
        Arguments.of(HttpMethod.PUT, DEFINITIONS + "/1/states/RELEASED"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/by-ids?ids=1"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/levels/by-ids?ids=1"),
        Arguments.of(HttpMethod.GET, DEFINITIONS + "/hints/by-ids?ids=1"));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("noAuthenticationRequests")
  @DisplayName("a request without authentication is rejected as unauthenticated")
  void anyEndpoint_withoutAuthentication_returnsUnauthorizedOrForbidden(
      HttpMethod method, String uri) {
    MockMvcRequestBuilder request =
        tester.method(method).uri(uri).contentType(MediaType.APPLICATION_JSON).content("{}");

    assertThat(request).matches(rejectedAsUnauthenticated());
  }

  @Nested
  @DisplayName("GET /training-definitions/{definitionId}")
  class FindDefinition {

    @Test
    @DisplayName("an author receives the definition with its levels in presentation order")
    void findDefinition_callerIsAuthor_returnsDefinitionWithLevelsInOrder() {
      SeededDefinition seeded = seedFourLevelDefinition("Alpha", TDState.UNRELEASED, CALLER_REF_ID);
      seeder.betaTestingGroup(seeded.id(), ORGANIZER_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);

      assertThat(body.<Number>read("$.id").longValue()).isEqualTo(seeded.id());
      assertThat(body.<String>read("$.title")).isEqualTo("Alpha");
      assertThat(body.<String>read("$.description")).isEqualTo("Description of Alpha");
      assertThat(body.<String>read("$.state")).isEqualTo("UNRELEASED");
      assertThat(body.<Number>read("$.estimated_duration")).isEqualTo(FOUR_LEVEL_DURATION);
      assertThat(body.<Object>read("$.beta_testing_group_id")).isNotNull();
      assertThat(body.<String>read("$.created_at")).isEqualTo(SEEDED_DATE_TIME);
      assertThat(body.<String>read("$.last_edited")).isEqualTo(SEEDED_DATE_TIME);
      assertThat(body.<String>read("$.last_edited_by"))
          .isEqualTo(TrainingDefinitionSeeder.SEEDED_EDITOR);
      assertThat(listAt(body, "$.levels[*].level_type"))
          .containsExactly("INFO_LEVEL", "TRAINING_LEVEL", "ACCESS_LEVEL", "ASSESSMENT_LEVEL");
      assertThat(listAt(body, "$.levels[*].order")).containsExactly(0, 1, 2, 3);
      assertThat(listAt(body, "$.levels[*].id"))
          .isEqualTo(
              idsOf(
                  seeded.infoLevelId(),
                  seeded.trainingLevelId(),
                  seeded.accessLevelId(),
                  seeded.assessmentLevelId()));
      assertThat(body.<String>read("$.levels[0].content")).isEqualTo("Content of info level 0");
      assertThat(body.<String>read("$.levels[1].answer")).isEqualTo("answer-1");
      assertThat(listAt(body, "$.levels[1].mitre_techniques[*].technique_key"))
          .containsExactly("T1059");
      assertThat(body.<String>read("$.levels[2].passkey")).isEqualTo("passkey-2");
      assertThat(body.<String>read("$.levels[3].assessment_type")).isEqualTo("QUESTIONNAIRE");
    }

    @Test
    @DisplayName("an administrator receives a definition they do not author")
    void findDefinition_callerIsAdministratorWithoutAuthorship_returnsDefinition() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, OTHER_DESIGNER_REF_ID);

      DocumentContext body = readDefinitionAsAdministrator(seeded.id());

      assertThat(body.<String>read("$.state")).isEqualTo("RELEASED");
      assertThat(listAt(body, "$.levels")).hasSize(4);
    }

    @Test
    @DisplayName("an organizer of an instance of the definition receives it")
    void findDefinition_callerOrganizesInstance_returnsDefinition() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);
      stubCaller(ORGANIZER_REF_ID);

      respond(getAs(organizer(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);
    }

    @Test
    @DisplayName("an organizer of the beta testing group receives the definition")
    void findDefinition_callerIsBetaTesterWithoutInstance_returnsDefinition() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      seeder.betaTestingGroup(seeded.id(), ORGANIZER_REF_ID);
      stubCaller(ORGANIZER_REF_ID);

      respond(getAs(organizer(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);
    }

    @Test
    @DisplayName("the definition can be archived when no instance ends in the future")
    void findDefinition_noInstanceEndsInFuture_canBeArchived() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inThePast(), ORGANIZER_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);

      assertThat(body.<Boolean>read("$.can_be_archived")).isTrue();
    }

    @Test
    @DisplayName("the definition cannot be archived while an instance ends in the future")
    void findDefinition_instanceEndsInFuture_cannotBeArchived() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);

      assertThat(body.<Boolean>read("$.can_be_archived")).isFalse();
    }

    @Test
    @DisplayName("an unknown id is answered with status 404 naming the definition")
    void findDefinition_unknownId_returnsNotFound() {
      expectEntityError(
          getAs(administrator(), DEFINITIONS + "/{id}", 99_999L),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName(
        "a caller who neither authors, organizes an instance of, nor beta tests it is refused")
    void findDefinition_callerHasNoRelationToDefinition_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          getAs(callerWithRoles(role), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions")
  class ListDefinitions {

    private void seedThreeDefinitions() {
      seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID);
      Long betaTested = seeder.definition("Bravo", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      seeder.betaTestingGroup(betaTested, CALLER_REF_ID);
      seeder.definition("Charlie", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
    }

    @Test
    @DisplayName("an administrator receives every definition")
    void findAllDefinitions_callerIsAdministrator_returnsEveryDefinition() {
      seedThreeDefinitions();

      DocumentContext body = respond(getAs(administrator(), DEFINITIONS), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title"))
          .containsExactlyInAnyOrder("Alpha", "Bravo", "Charlie");
    }

    @Test
    @DisplayName("a designer receives only the definitions they author or beta test")
    void findAllDefinitions_callerIsDesigner_returnsAuthoredAndBetaTestedDefinitions() {
      seedThreeDefinitions();

      DocumentContext body = respond(getAs(designer(), DEFINITIONS), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title")).containsExactlyInAnyOrder("Alpha", "Bravo");
      assertThat(body.<Number>read("$.pagination.total_elements")).isEqualTo(2);
    }

    @Test
    @DisplayName("each listed definition carries its summary fields")
    void findAllDefinitions_definitionListed_carriesSummaryFields() {
      Long definitionId = seeder.definition("Alpha", TDState.RELEASED, CALLER_REF_ID);
      seeder.betaTestingGroup(definitionId, ORGANIZER_REF_ID);
      seeder.infoLevel(definitionId, 0, 5);

      DocumentContext body = respond(getAs(designer(), DEFINITIONS), HttpStatus.OK);

      assertThat(body.<Number>read("$.content[0].id").longValue()).isEqualTo(definitionId);
      assertThat(body.<String>read("$.content[0].title")).isEqualTo("Alpha");
      assertThat(body.<String>read("$.content[0].state")).isEqualTo("RELEASED");
      assertThat(body.<Number>read("$.content[0].estimated_duration")).isEqualTo(5);
      assertThat(body.<Object>read("$.content[0].beta_testing_group_id")).isNotNull();
      assertThat(body.<Boolean>read("$.content[0].can_be_archived")).isTrue();
      assertThat(body.<String>read("$.content[0].created_at")).isEqualTo(SEEDED_DATE_TIME);
      assertThat(body.<String>read("$.content[0].last_edited")).isEqualTo(SEEDED_DATE_TIME);
    }

    @ParameterizedTest(name = "title filter {0}")
    @CsvSource({"ravo", "RAVO", "BrA"})
    @DisplayName("a text filter matches partially and ignores case")
    void findAllDefinitions_titleFilter_matchesPartiallyIgnoringCase(String titleFilter) {
      seedThreeDefinitions();

      DocumentContext body =
          respond(
              getAs(administrator(), DEFINITIONS + "?title={title}", titleFilter), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title")).containsExactly("Bravo");
    }

    @Test
    @DisplayName("the requested page and sort order shape the result and its pagination")
    void findAllDefinitions_pageRequested_returnsThatPageWithPagination() {
      seedThreeDefinitions();

      DocumentContext body =
          respond(
              getAs(administrator(), DEFINITIONS + "?size=2&page=1&sort=title,asc"), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title")).containsExactly("Charlie");
      assertThat(body.<Number>read("$.pagination.number")).isEqualTo(1);
      assertThat(body.<Number>read("$.pagination.size")).isEqualTo(2);
      assertThat(body.<Number>read("$.pagination.number_of_elements")).isEqualTo(1);
      assertThat(body.<Number>read("$.pagination.total_elements")).isEqualTo(3);
      assertThat(body.<Number>read("$.pagination.total_pages")).isEqualTo(2);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who is neither designer nor administrator is refused")
    void findAllDefinitions_callerIsNeitherDesignerNorAdministrator_returnsForbidden(
        RoleTypeSecurity role) {
      expectError(getAs(callerWithRoles(role), DEFINITIONS), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/for-organizers")
  class ListDefinitionsForOrganizers {

    @BeforeEach
    void seedDefinitions() {
      seeder.definition("Released", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      Long betaTested = seeder.definition("BetaTested", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      seeder.betaTestingGroup(betaTested, CALLER_REF_ID);
      seeder.definition("Authored", TDState.UNRELEASED, CALLER_REF_ID);
      seeder.definition("Foreign", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
    }

    @Test
    @DisplayName("released definitions are returned to every organizer")
    void findAllForOrganizers_releasedState_returnsReleasedDefinitions() {
      DocumentContext body =
          respond(
              getAs(organizer(), DEFINITIONS + "/for-organizers?state=RELEASED"), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title")).containsExactly("Released");
      assertThat(body.<String>read("$.content[0].state")).isEqualTo("RELEASED");
      assertThat(body.<Number>read("$.content[0].id")).isNotNull();
    }

    @Test
    @DisplayName("an administrator receives every unreleased definition")
    void findAllForOrganizers_unreleasedStateAsAdministrator_returnsEveryUnreleasedDefinition() {
      DocumentContext body =
          respond(
              getAs(administrator(), DEFINITIONS + "/for-organizers?state=UNRELEASED"),
              HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title"))
          .containsExactlyInAnyOrder("BetaTested", "Authored", "Foreign");
    }

    @Test
    @DisplayName("an organizer who does not design receives only the definitions they beta test")
    void findAllForOrganizers_unreleasedStateAsOrganizer_returnsBetaTestedDefinitions() {
      DocumentContext body =
          respond(
              getAs(organizer(), DEFINITIONS + "/for-organizers?state=UNRELEASED"), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title")).containsExactly("BetaTested");
    }

    @Test
    @DisplayName(
        "a caller who designs and organizes receives the definitions they author or beta test")
    void findAllForOrganizers_unreleasedStateAsDesignerAndOrganizer_returnsAuthoredAndBetaTested() {
      DocumentContext body =
          respond(
              getAs(designerAndOrganizer(), DEFINITIONS + "/for-organizers?state=UNRELEASED"),
              HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].title"))
          .containsExactlyInAnyOrder("BetaTested", "Authored");
    }

    @Test
    @DisplayName("an unrecognized state is refused with status 400")
    void findAllForOrganizers_unrecognizedState_returnsBadRequest() {
      expectError(
          getAs(organizer(), DEFINITIONS + "/for-organizers?state=BOGUS"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("the archived state is refused with status 400")
    void findAllForOrganizers_archivedState_returnsBadRequest() {
      expectError(
          getAs(organizer(), DEFINITIONS + "/for-organizers?state=ARCHIVED"),
          HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who is neither organizer nor administrator is refused")
    void findAllForOrganizers_callerIsNeitherOrganizerNorAdministrator_returnsForbidden(
        RoleTypeSecurity role) {
      expectError(
          getAs(callerWithRoles(role), DEFINITIONS + "/for-organizers?state=RELEASED"),
          HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/played-mitre-techniques")
  class PlayedMitreTechniques {

    private Long seedReleasedDefinitionWithTechniques(String title, String... techniqueKeys) {
      Long definitionId = seeder.definition(title, TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      seeder.trainingLevel(definitionId, 0, 3, techniqueKeys);
      return definitionId;
    }

    @Test
    @DisplayName("released definitions using techniques are listed, flagged by the caller's runs")
    void findPlayedMitreTechniques_callerIsTrainee_returnsReleasedDefinitionsWithPlayedFlag() {
      Long played = seedReleasedDefinitionWithTechniques("Played", "T1059", "T1003");
      Long playedInfoLevel = seeder.infoLevel(played, 1, 1);
      seeder.run(
          seeder.instance(played, inThePast(), ORGANIZER_REF_ID), playedInfoLevel, TRAINEE_REF_ID);
      seedReleasedDefinitionWithTechniques("Unplayed", "T1110");
      stubCaller(TRAINEE_REF_ID);

      DocumentContext body =
          respond(getAs(trainee(), DEFINITIONS + "/played-mitre-techniques"), HttpStatus.OK);

      assertThat(listAt(body, "$[*].title")).containsExactlyInAnyOrder("Played", "Unplayed");
      assertThat(listAt(body, "$[?(@.title=='Played')].played")).containsExactly(true);
      assertThat(listAt(body, "$[?(@.title=='Unplayed')].played")).containsExactly(false);
      assertThat(listAt(body, "$[?(@.title=='Played')].mitre_techniques[*]"))
          .containsExactlyInAnyOrder("T1059", "T1003");
      assertThat(listAt(body, "$[?(@.title=='Unplayed')].mitre_techniques[*]"))
          .containsExactly("T1110");
      assertThat(listAt(body, "$[?(@.title=='Played')].id")).isEqualTo(idsOf(played));
    }

    @Test
    @DisplayName("definitions without techniques or not released are left out")
    void findPlayedMitreTechniques_definitionsWithoutTechniquesOrUnreleased_areLeftOut() {
      Long withoutTechniques = seeder.definition("Plain", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      seeder.infoLevel(withoutTechniques, 0, 1);
      Long draft = seeder.definition("Draft", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      seeder.trainingLevel(draft, 0, 1, "T1059");
      seedReleasedDefinitionWithTechniques("Listed", "T1059");

      DocumentContext body =
          respond(getAs(administrator(), DEFINITIONS + "/played-mitre-techniques"), HttpStatus.OK);

      assertThat(listAt(body, "$[*].title")).containsExactly("Listed");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER"})
    @DisplayName("a caller who is neither trainee nor administrator is refused")
    void findPlayedMitreTechniques_callerIsNeitherTraineeNorAdministrator_returnsForbidden(
        RoleTypeSecurity role) {
      expectError(
          getAs(callerWithRoles(role), DEFINITIONS + "/played-mitre-techniques"),
          HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("POST /training-definitions")
  class CreateDefinition {

    private static final String MINIMAL_BODY =
        """
        {"title": "New definition", "description": "About it", "prerequisites": ["Linux basics"],
         "outcomes": ["Reads logs"], "state": "UNRELEASED", "default_content": false}
        """;

    @Test
    @DisplayName("a designer creates a definition and becomes its author")
    void createDefinition_validBody_returnsStoredDefinitionAuthoredByCaller() {
      DocumentContext body =
          respond(withJson(postAs(designer(), DEFINITIONS), MINIMAL_BODY), HttpStatus.OK);

      Long createdId = body.<Number>read("$.id").longValue();
      assertThat(body.<String>read("$.title")).isEqualTo("New definition");
      assertThat(body.<String>read("$.description")).isEqualTo("About it");
      assertThat(listAt(body, "$.prerequisites")).containsExactly("Linux basics");
      assertThat(listAt(body, "$.outcomes")).containsExactly("Reads logs");
      assertThat(body.<String>read("$.state")).isEqualTo("UNRELEASED");
      assertThat(body.<Number>read("$.estimated_duration")).isEqualTo(0);
      assertThat(body.<Object>read("$.beta_testing_group_id")).isNull();
      assertThat(listAt(body, "$.levels")).isEmpty();
      assertThat(body.<String>read("$.created_at")).matches(DATE_TIME_PATTERN);
      assertThat(body.<String>read("$.last_edited")).matches(DATE_TIME_PATTERN);
      assertThat(body.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
      assertThat(seeder.authorUserRefIds(createdId)).containsExactly(CALLER_REF_ID);
    }

    @Test
    @DisplayName("an administrator creates a definition and becomes its author")
    void createDefinition_callerIsAdministrator_returnsStoredDefinitionAuthoredByCaller() {
      DocumentContext body =
          respond(withJson(postAs(administrator(), DEFINITIONS), MINIMAL_BODY), HttpStatus.OK);

      assertThat(seeder.authorUserRefIds(body.<Number>read("$.id").longValue()))
          .containsExactly(CALLER_REF_ID);
    }

    @Test
    @DisplayName("default content adds an info level followed by an access level")
    void createDefinition_defaultContentRequested_returnsInfoLevelThenAccessLevel() {
      String body =
          """
          {"title": "With content", "state": "UNRELEASED", "default_content": true}
          """;

      DocumentContext created =
          respond(withJson(postAs(designer(), DEFINITIONS), body), HttpStatus.OK);

      assertThat(listAt(created, "$.levels[*].level_type"))
          .containsExactly("INFO_LEVEL", "ACCESS_LEVEL");
      assertThat(listAt(created, "$.levels[*].order")).containsExactly(0, 1);
    }

    @Test
    @DisplayName("a beta testing group is stored with the requested organizers")
    void createDefinition_betaTestingGroupGiven_storesGroupOrganizers() {
      String body =
          """
          {"title": "Beta tested", "state": "UNRELEASED",
           "beta_testing_group": {"organizers_ref_ids": [%d]}}
          """
              .formatted(ORGANIZER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, ORGANIZER_REF_ID);

      DocumentContext created =
          respond(withJson(postAs(designer(), DEFINITIONS), body), HttpStatus.OK);

      assertThat(created.<Object>read("$.beta_testing_group_id")).isNotNull();
      assertThat(seeder.betaTesterUserRefIds(created.<Number>read("$.id").longValue()))
          .containsExactly(ORGANIZER_REF_ID);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        delimiter = '|',
        value = {
          "missing title|{\"state\": \"UNRELEASED\"}",
          "empty title|{\"title\": \"\", \"state\": \"UNRELEASED\"}",
          "missing state|{\"title\": \"No state\"}",
          "unrecognized state|{\"title\": \"T\", \"state\": \"BOGUS\"}",
          "beta testing group without organizers|{\"title\": \"T\", \"state\": \"UNRELEASED\", \"beta_testing_group\": {}}"
        })
    @DisplayName("a body failing validation is refused with status 400 and stores nothing")
    void createDefinition_invalidBody_returnsBadRequestAndStoresNothing(
        String description, String body) {
      long definitionsBefore = seeder.definitionCount();

      expectError(withJson(postAs(designer(), DEFINITIONS), body), HttpStatus.BAD_REQUEST);

      assertThat(seeder.definitionCount()).isEqualTo(definitionsBefore);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who is neither designer nor administrator is refused")
    void createDefinition_callerIsNeitherDesignerNorAdministrator_returnsForbidden(
        RoleTypeSecurity role) {
      expectError(
          withJson(postAs(callerWithRoles(role), DEFINITIONS), MINIMAL_BODY), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions")
  class UpdateDefinition {

    private String updateBody(Long definitionId, String additionalFields) {
      return """
          {"id": %d, "title": "Renamed", "description": "New description",
           "prerequisites": ["New prerequisite"], "outcomes": ["New outcome"],
           "state": "UNRELEASED", "show_stepper_bar": true%s}
          """
          .formatted(definitionId, additionalFields);
    }

    @Test
    @DisplayName("an author overwrites the definition fields")
    void updateDefinition_callerIsAuthor_overwritesFields() {
      SeededDefinition seeded =
          seedFourLevelDefinition("Original", TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(putAs(designer(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.NO_CONTENT);

      DocumentContext body = readDefinitionAsAdministrator(seeded.id());
      assertThat(body.<String>read("$.title")).isEqualTo("Renamed");
      assertThat(body.<String>read("$.description")).isEqualTo("New description");
      assertThat(listAt(body, "$.prerequisites")).containsExactly("New prerequisite");
      assertThat(listAt(body, "$.outcomes")).containsExactly("New outcome");
      assertThat(listAt(body, "$.levels")).hasSize(4);
    }

    @Test
    @DisplayName("an administrator who does not author the definition overwrites it")
    void updateDefinition_callerIsAdministratorWithoutAuthorship_overwritesFields() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          withJson(putAs(administrator(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.NO_CONTENT);

      assertThat(readDefinitionAsAdministrator(seeded.id()).<String>read("$.title"))
          .isEqualTo("Renamed");
    }

    @Test
    @DisplayName("the stored estimated duration is kept whatever the body carries")
    void updateDefinition_bodyCarriesEstimatedDuration_keepsStoredDuration() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), DEFINITIONS),
              updateBody(seeded.id(), ", \"estimated_duration\": 999")),
          HttpStatus.NO_CONTENT);

      assertThat(readDefinitionAsAdministrator(seeded.id()).<Number>read("$.estimated_duration"))
          .isEqualTo(FOUR_LEVEL_DURATION);
    }

    @Test
    @DisplayName("the caller joins the existing authors and the creation time is kept")
    void updateDefinition_callerNotYetAuthor_addsCallerAndKeepsExistingAuthors() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          withJson(putAs(administrator(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(seeded.id()))
          .containsExactlyInAnyOrder(OTHER_DESIGNER_REF_ID, CALLER_REF_ID);
      assertThat(readDefinitionAsAdministrator(seeded.id()).<String>read("$.created_at"))
          .isEqualTo(SEEDED_DATE_TIME);
    }

    @Test
    @DisplayName("the edit is recorded with the caller's full name and the current time")
    void updateDefinition_validBody_recordsCallerAsLastEditor() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(putAs(designer(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.NO_CONTENT);

      DocumentContext body = readDefinitionAsAdministrator(seeded.id());
      assertThat(body.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
      assertThat(body.<String>read("$.last_edited"))
          .matches(DATE_TIME_PATTERN)
          .isNotEqualTo(SEEDED_DATE_TIME);
    }

    @Test
    @DisplayName("a body without the stored beta testing group is refused with status 409")
    void updateDefinition_betaTestingGroupOmittedWhileStored_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.betaTestingGroup(seeded.id(), ORGANIZER_REF_ID);

      expectError(
          withJson(putAs(designer(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.CONFLICT);

      assertThat(seeder.betaTesterUserRefIds(seeded.id())).containsExactly(ORGANIZER_REF_ID);
    }

    @Test
    @DisplayName("a beta testing group with no organizers empties the stored group")
    void updateDefinition_betaTestingGroupWithoutOrganizers_emptiesStoredGroup() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.betaTestingGroup(seeded.id(), ORGANIZER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), DEFINITIONS),
              updateBody(seeded.id(), ", \"beta_testing_group\": {\"organizers_ref_ids\": []}")),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.betaTesterUserRefIds(seeded.id())).isEmpty();
      assertThat(readDefinitionAsAdministrator(seeded.id()).<Object>read("$.beta_testing_group_id"))
          .isNotNull();
    }

    @Test
    @DisplayName("a released definition is refused with status 409")
    void updateDefinition_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          withJson(putAs(designer(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("a definition with a training instance is refused with status 409")
    void updateDefinition_definitionHasInstance_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          withJson(putAs(designer(), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void updateDefinition_unknownDefinition_returnsNotFound() {
      expectEntityError(
          withJson(putAs(administrator(), DEFINITIONS), updateBody(99_999L, "")),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        delimiter = '|',
        value = {
          "missing id|{\"title\": \"T\", \"state\": \"UNRELEASED\", \"show_stepper_bar\": true}",
          "missing title|{\"id\": 1, \"state\": \"UNRELEASED\", \"show_stepper_bar\": true}",
          "missing state|{\"id\": 1, \"title\": \"T\", \"show_stepper_bar\": true}"
        })
    @DisplayName("a body failing validation is refused with status 400")
    void updateDefinition_invalidBody_returnsBadRequest(String description, String body) {
      expectError(withJson(putAs(administrator(), DEFINITIONS), body), HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void updateDefinition_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          withJson(putAs(callerWithRoles(role), DEFINITIONS), updateBody(seeded.id(), "")),
          HttpStatus.FORBIDDEN);

      assertThat(readDefinitionAsAdministrator(seeded.id()).<String>read("$.title"))
          .isEqualTo("Seeded definition");
    }
  }

  @Nested
  @DisplayName("POST /training-definitions/{definitionId}")
  class CloneDefinition {

    @Test
    @DisplayName(
        "the copy carries every level, starts unreleased and is authored by the caller only")
    void cloneDefinition_validRequest_returnsUnreleasedCopyWithAllLevels() {
      SeededDefinition source =
          seedFourLevelDefinition("Source", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      seeder.betaTestingGroup(source.id(), ORGANIZER_REF_ID);
      seeder.trainingLevelWithHints(source.id(), 4, "First hint", "Second hint");

      DocumentContext copy =
          respond(
              postAs(administrator(), DEFINITIONS + "/{id}?title={title}", source.id(), "Copy"),
              HttpStatus.OK);

      Long copyId = copy.<Number>read("$.id").longValue();
      assertThat(copyId).isNotEqualTo(source.id());
      assertThat(copy.<String>read("$.title")).isEqualTo("Copy");
      assertThat(copy.<String>read("$.description")).isEqualTo("Description of Source");
      assertThat(copy.<String>read("$.state")).isEqualTo("UNRELEASED");
      assertThat(copy.<Object>read("$.beta_testing_group_id")).isNull();
      assertThat(copy.<Number>read("$.estimated_duration")).isEqualTo(FOUR_LEVEL_DURATION + 1);
      assertThat(seeder.authorUserRefIds(copyId)).containsExactly(CALLER_REF_ID);
      assertThat(copy.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
      assertThat(listAt(copy, "$.levels[*].level_type"))
          .containsExactly(
              "INFO_LEVEL", "TRAINING_LEVEL", "ACCESS_LEVEL", "ASSESSMENT_LEVEL", "TRAINING_LEVEL");
      assertThat(listAt(copy, "$.levels[*].order")).containsExactly(0, 1, 2, 3, 4);
      assertThat(listAt(copy, "$.levels[*].title"))
          .containsExactly(
              "Info level 0",
              "Training level 1",
              "Access level 2",
              "Assessment level 3",
              "Training level 4");
      assertThat(listAt(copy, "$.levels[4].hints[*].title"))
          .containsExactlyInAnyOrder("First hint", "Second hint");
      assertThat(listAt(copy, "$.levels[*].id"))
          .doesNotContainAnyElementsOf(
              idsOf(
                  source.infoLevelId(),
                  source.trainingLevelId(),
                  source.accessLevelId(),
                  source.assessmentLevelId()));
    }

    @Test
    @DisplayName("the original definition is left as it was")
    void cloneDefinition_validRequest_leavesOriginalUnchanged() {
      SeededDefinition source =
          seedFourLevelDefinition("Source", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      seeder.betaTestingGroup(source.id(), ORGANIZER_REF_ID);

      respond(
          postAs(administrator(), DEFINITIONS + "/{id}?title={title}", source.id(), "Copy"),
          HttpStatus.OK);

      DocumentContext original = readDefinitionAsAdministrator(source.id());
      assertThat(original.<String>read("$.title")).isEqualTo("Source");
      assertThat(original.<String>read("$.state")).isEqualTo("RELEASED");
      assertThat(original.<Object>read("$.beta_testing_group_id")).isNotNull();
      assertThat(listAt(original, "$.levels")).hasSize(4);
      assertThat(seeder.authorUserRefIds(source.id())).containsExactly(OTHER_DESIGNER_REF_ID);
    }

    @Test
    @DisplayName("an author of the definition copies it")
    void cloneDefinition_callerIsAuthor_returnsCopy() {
      SeededDefinition source =
          seedFourLevelDefinition("Source", TDState.UNRELEASED, CALLER_REF_ID);

      respond(
          postAs(designer(), DEFINITIONS + "/{id}?title={title}", source.id(), "Copy"),
          HttpStatus.OK);
    }

    @Test
    @DisplayName("a missing title is refused with status 400")
    void cloneDefinition_titleMissing_returnsBadRequest() {
      SeededDefinition source = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(postAs(designer(), DEFINITIONS + "/{id}", source.id()), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void cloneDefinition_unknownDefinition_returnsNotFound() {
      expectEntityError(
          postAs(administrator(), DEFINITIONS + "/{id}?title=Copy", 99_999L),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void cloneDefinition_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition source = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      long definitionsBefore = seeder.definitionCount();

      expectError(
          postAs(callerWithRoles(role), DEFINITIONS + "/{id}?title=Copy", source.id()),
          HttpStatus.FORBIDDEN);

      assertThat(seeder.definitionCount()).isEqualTo(definitionsBefore);
    }
  }

  @Nested
  @DisplayName(
      "PUT /training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}")
  class SwapLevels {

    private static final String SWAP = DEFINITIONS + "/{definitionId}/levels/{from}/swap-with/{to}";

    @Test
    @DisplayName("two levels exchange positions and the levels are returned in their new order")
    void swapLevels_twoLevels_exchangesPositionsAndReturnsNewOrder() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(
              putAs(
                  designer(), SWAP, seeded.id(), seeded.trainingLevelId(), seeded.accessLevelId()),
              HttpStatus.OK);

      assertThat(orderedLevelIds(body))
          .isEqualTo(
              idsOf(
                  seeded.infoLevelId(),
                  seeded.accessLevelId(),
                  seeded.trainingLevelId(),
                  seeded.assessmentLevelId()));
      assertThat(listAt(body, "$[*].order")).containsExactly(0, 1, 2, 3);
      assertThat(listAt(body, "$[*].level_type"))
          .containsExactly("INFO_LEVEL", "ACCESS_LEVEL", "TRAINING_LEVEL", "ASSESSMENT_LEVEL");
      assertThat(listAt(body, "$[*].title"))
          .containsExactly(
              "Info level 0", "Access level 2", "Training level 1", "Assessment level 3");
      assertThat(listAt(readDefinitionAsAdministrator(seeded.id()), "$.levels[*].id"))
          .isEqualTo(orderedLevelIds(body));
    }

    @Test
    @DisplayName("an administrator who does not author the definition swaps its levels")
    void swapLevels_callerIsAdministratorWithoutAuthorship_returnsNewOrder() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      respond(
          putAs(
              administrator(), SWAP, seeded.id(), seeded.infoLevelId(), seeded.assessmentLevelId()),
          HttpStatus.OK);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void swapLevels_unknownDefinition_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          putAs(administrator(), SWAP, 99_999L, seeded.infoLevelId(), seeded.accessLevelId()),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @Test
    @DisplayName("an unknown level is answered with status 404 and nothing moves")
    void swapLevels_unknownLevel_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          putAs(administrator(), SWAP, seeded.id(), seeded.infoLevelId(), 99_999L),
          HttpStatus.NOT_FOUND,
          "AbstractLevel",
          99_999L);

      assertThat(listAt(readDefinitionAsAdministrator(seeded.id()), "$.levels[*].id"))
          .isEqualTo(
              idsOf(
                  seeded.infoLevelId(),
                  seeded.trainingLevelId(),
                  seeded.accessLevelId(),
                  seeded.assessmentLevelId()));
    }

    @Test
    @DisplayName("a released definition is refused with status 409")
    void swapLevels_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          putAs(designer(), SWAP, seeded.id(), seeded.infoLevelId(), seeded.accessLevelId()),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("a definition with a training instance is refused with status 409")
    void swapLevels_definitionHasInstance_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          putAs(designer(), SWAP, seeded.id(), seeded.infoLevelId(), seeded.accessLevelId()),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void swapLevels_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          putAs(
              callerWithRoles(role),
              SWAP,
              seeded.id(),
              seeded.infoLevelId(),
              seeded.accessLevelId()),
          HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName(
      "PUT /training-definitions/{definitionId}/levels/{levelIdToBeMoved}/move-to/{newPosition}")
  class MoveLevel {

    private static final String MOVE =
        DEFINITIONS + "/{definitionId}/levels/{level}/move-to/{position}";

    private void assertLevelOrder(DocumentContext body, Long... expectedLevelIds) {
      assertThat(orderedLevelIds(body)).isEqualTo(idsOf(expectedLevelIds));
      assertThat(listAt(body, "$[*].order")).containsExactly(0, 1, 2, 3);
    }

    @Test
    @DisplayName("a level moved toward the end shifts the levels in between one place back")
    void moveLevel_toLaterPosition_shiftsLevelsInBetween() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(putAs(designer(), MOVE, seeded.id(), seeded.infoLevelId(), 2), HttpStatus.OK);

      assertLevelOrder(
          body,
          seeded.trainingLevelId(),
          seeded.accessLevelId(),
          seeded.infoLevelId(),
          seeded.assessmentLevelId());
    }

    @Test
    @DisplayName("a level moved toward the start shifts the levels in between one place forward")
    void moveLevel_toEarlierPosition_shiftsLevelsInBetween() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(
              putAs(designer(), MOVE, seeded.id(), seeded.assessmentLevelId(), 1), HttpStatus.OK);

      assertLevelOrder(
          body,
          seeded.infoLevelId(),
          seeded.assessmentLevelId(),
          seeded.trainingLevelId(),
          seeded.accessLevelId());
    }

    @Test
    @DisplayName("a position past the last level is pulled to the last position")
    void moveLevel_positionBeyondLastLevel_movesToEnd() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(putAs(designer(), MOVE, seeded.id(), seeded.infoLevelId(), 99), HttpStatus.OK);

      assertLevelOrder(
          body,
          seeded.trainingLevelId(),
          seeded.accessLevelId(),
          seeded.assessmentLevelId(),
          seeded.infoLevelId());
    }

    @Test
    @DisplayName("a negative position is pulled to the first position")
    void moveLevel_negativePosition_movesToStart() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(putAs(designer(), MOVE, seeded.id(), seeded.accessLevelId(), -4), HttpStatus.OK);

      assertLevelOrder(
          body,
          seeded.accessLevelId(),
          seeded.infoLevelId(),
          seeded.trainingLevelId(),
          seeded.assessmentLevelId());
    }

    @Test
    @DisplayName("moving a level to the position it holds changes nothing")
    void moveLevel_toCurrentPosition_keepsOrder() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(putAs(designer(), MOVE, seeded.id(), seeded.trainingLevelId(), 1), HttpStatus.OK);

      assertLevelOrder(
          body,
          seeded.infoLevelId(),
          seeded.trainingLevelId(),
          seeded.accessLevelId(),
          seeded.assessmentLevelId());
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void moveLevel_unknownDefinition_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          putAs(administrator(), MOVE, 99_999L, seeded.infoLevelId(), 1),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @Test
    @DisplayName("an unknown level is answered with status 404")
    void moveLevel_unknownLevel_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          putAs(administrator(), MOVE, seeded.id(), 99_999L, 1),
          HttpStatus.NOT_FOUND,
          "AbstractLevel",
          99_999L);
    }

    @Test
    @DisplayName("a released definition is refused with status 409")
    void moveLevel_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          putAs(designer(), MOVE, seeded.id(), seeded.infoLevelId(), 1),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("a definition with a training instance is refused with status 409")
    void moveLevel_definitionHasInstance_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          putAs(designer(), MOVE, seeded.id(), seeded.infoLevelId(), 1),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void moveLevel_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          putAs(callerWithRoles(role), MOVE, seeded.id(), seeded.infoLevelId(), 1),
          HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("DELETE /training-definitions/{definitionId}")
  class DeleteDefinition {

    @Test
    @DisplayName("an author deletes the definition together with its levels")
    void deleteDefinition_callerIsAuthor_removesDefinitionAndLevels() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(deleteAs(designer(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);

      expectError(getAs(administrator(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.NOT_FOUND);
      expectError(
          getAs(administrator(), DEFINITIONS + "/levels/{id}", seeded.infoLevelId()),
          HttpStatus.NOT_FOUND);
      expectError(
          getAs(administrator(), DEFINITIONS + "/levels/{id}", seeded.trainingLevelId()),
          HttpStatus.NOT_FOUND);
      expectError(
          getAs(administrator(), DEFINITIONS + "/levels/{id}", seeded.accessLevelId()),
          HttpStatus.NOT_FOUND);
      expectError(
          getAs(administrator(), DEFINITIONS + "/levels/{id}", seeded.assessmentLevelId()),
          HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("an administrator who does not author the definition deletes it")
    void deleteDefinition_callerIsAdministratorWithoutAuthorship_removesDefinition() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          deleteAs(administrator(), DEFINITIONS + "/{id}", seeded.id()), HttpStatus.OK);

      assertThat(seeder.definitionCount()).isZero();
    }

    @Test
    @DisplayName("a released definition is refused with status 409")
    void deleteDefinition_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          deleteAs(designer(), DEFINITIONS + "/{id}", seeded.id()),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());

      assertThat(seeder.definitionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("a definition with a training instance is refused with status 409")
    void deleteDefinition_definitionHasInstance_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          deleteAs(designer(), DEFINITIONS + "/{id}", seeded.id()),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());

      assertThat(seeder.definitionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void deleteDefinition_unknownDefinition_returnsNotFound() {
      expectEntityError(
          deleteAs(administrator(), DEFINITIONS + "/{id}", 99_999L),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void deleteDefinition_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          deleteAs(callerWithRoles(role), DEFINITIONS + "/{id}", seeded.id()),
          HttpStatus.FORBIDDEN);

      assertThat(seeder.definitionCount()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("DELETE /training-definitions/{definitionId}/levels/{levelId}")
  class DeleteLevel {

    private static final String LEVEL = DEFINITIONS + "/{definitionId}/levels/{levelId}";

    @Test
    @DisplayName("the gap in the level order is closed and the remaining levels are returned")
    void deleteLevel_middleLevel_closesOrderGapAndReturnsRemainingLevels() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body =
          respond(
              deleteAs(designer(), LEVEL, seeded.id(), seeded.trainingLevelId()), HttpStatus.OK);

      assertThat(orderedLevelIds(body))
          .isEqualTo(
              idsOf(seeded.infoLevelId(), seeded.accessLevelId(), seeded.assessmentLevelId()));
      assertThat(listAt(body, "$[*].order")).containsExactly(0, 1, 2);
      assertThat(listAt(body, "$[*].level_type"))
          .containsExactly("INFO_LEVEL", "ACCESS_LEVEL", "ASSESSMENT_LEVEL");
      expectError(
          getAs(administrator(), DEFINITIONS + "/levels/{id}", seeded.trainingLevelId()),
          HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("the estimated duration drops by the duration of the removed level")
    void deleteLevel_levelRemoved_reducesEstimatedDuration() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      respond(deleteAs(designer(), LEVEL, seeded.id(), seeded.trainingLevelId()), HttpStatus.OK);

      assertThat(readDefinitionAsAdministrator(seeded.id()).<Number>read("$.estimated_duration"))
          .isEqualTo(FOUR_LEVEL_DURATION - 7);
    }

    @Test
    @DisplayName("an administrator who does not author the definition removes a level")
    void deleteLevel_callerIsAdministratorWithoutAuthorship_returnsRemainingLevels() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      DocumentContext body =
          respond(
              deleteAs(administrator(), LEVEL, seeded.id(), seeded.infoLevelId()), HttpStatus.OK);

      assertThat(listAt(body, "$[*].order")).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void deleteLevel_unknownDefinition_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          deleteAs(administrator(), LEVEL, 99_999L, seeded.infoLevelId()),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @Test
    @DisplayName("an unknown level is answered with status 404")
    void deleteLevel_unknownLevel_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          deleteAs(administrator(), LEVEL, seeded.id(), 99_999L),
          HttpStatus.NOT_FOUND,
          "AbstractLevel",
          99_999L);
    }

    @Test
    @DisplayName("a definition that is not unreleased is refused with status 409")
    void deleteLevel_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectError(
          deleteAs(designer(), LEVEL, seeded.id(), seeded.infoLevelId()), HttpStatus.CONFLICT);

      assertThat(listAt(readDefinitionAsAdministrator(seeded.id()), "$.levels[*].id")).hasSize(4);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void deleteLevel_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          deleteAs(callerWithRoles(role), LEVEL, seeded.id(), seeded.infoLevelId()),
          HttpStatus.FORBIDDEN);
    }
  }

  private enum UpdatableLevel {
    TRAINING("training-levels"),
    INFO("info-levels"),
    ASSESSMENT("assessment-levels");

    private final String pathSegment;

    UpdatableLevel(String pathSegment) {
      this.pathSegment = pathSegment;
    }

    String path(Object definitionId) {
      return DEFINITIONS + "/" + definitionId + "/" + pathSegment;
    }
  }

  private static Long levelOf(UpdatableLevel kind, SeededDefinition seeded) {
    return switch (kind) {
      case TRAINING -> seeded.trainingLevelId();
      case INFO -> seeded.infoLevelId();
      case ASSESSMENT -> seeded.assessmentLevelId();
    };
  }

  private static String validPayload(UpdatableLevel kind, Long levelId) {
    return switch (kind) {
      case TRAINING -> trainingLevelPayload(levelId, 50, quoted("a"), quoted(null), false, "[]", 9);
      case INFO -> infoLevelPayload(levelId, "Updated content");
      case ASSESSMENT -> assessmentLevelPayload(levelId, "QUESTIONNAIRE", 6, "");
    };
  }

  private static String trainingLevelPayload(
      Long levelId,
      int maxScore,
      String answerJson,
      String answerVariableNameJson,
      boolean variantAnswers,
      String hintsJson,
      int estimatedDuration) {
    return """
        {"id": %d, "title": "Updated training level", "level_type": "TRAINING_LEVEL",
         "max_score": %d, "answer": %s, "answer_variable_name": %s, "content": "Updated content",
         "solution": "Updated solution", "solution_penalized": true, "incorrect_answer_limit": 3,
         "estimated_duration": %d, "variant_answers": %b, "commands_required": true,
         "hints": %s, "mitre_techniques": [{"technique_key": "T1059"}]}
        """
        .formatted(
            levelId,
            maxScore,
            answerJson,
            answerVariableNameJson,
            estimatedDuration,
            variantAnswers,
            hintsJson);
  }

  private static String hintJson(String title, int penalty, int order) {
    return "{\"title\": \"%s\", \"content\": \"Content of %s\", \"hint_penalty\": %d, \"order\": %d}"
        .formatted(title, title, penalty, order);
  }

  private static String infoLevelPayload(Long levelId, String content) {
    return """
        {"id": %d, "title": "Updated info level", "level_type": "INFO_LEVEL", "content": %s}
        """
        .formatted(levelId, quoted(content));
  }

  private static String accessLevelPayload(Long levelId) {
    return """
        {"id": %d, "title": "Updated access level", "level_type": "ACCESS_LEVEL",
         "passkey": "new-passkey", "cloud_content": "New cloud content",
         "local_content": "New local content"}
        """
        .formatted(levelId);
  }

  /**
   * Builds an assessment level with one extended matching question worth 5 points and one multiple
   * choice question worth 3 points.
   *
   * @param correctOptionOrderJson the JSON member naming the correct option of the statement, or an
   *     empty text to leave it out
   */
  private static String assessmentLevelPayload(
      Long levelId, String assessmentType, int estimatedDuration, String correctOptionOrderJson) {
    return """
        {"id": %d, "title": "Updated assessment level", "level_type": "ASSESSMENT_LEVEL",
         "type": "%s", "instructions": "Updated instructions", "estimated_duration": %d,
         "questions": [
           {"question_type": "EMI", "text": "Match them", "points": 5, "penalty": 1, "order": 0,
            "answer_required": true,
            "extended_matching_options": [{"text": "First option", "order": 0},
                                          {"text": "Second option", "order": 1}],
            "extended_matching_statements": [{"text": "A statement", "order": 0%s}]},
           {"question_type": "MCQ", "text": "Pick one", "points": 3, "penalty": 0, "order": 1,
            "answer_required": false,
            "choices": [{"text": "Right", "correct": true, "order": 0},
                        {"text": "Wrong", "correct": false, "order": 1}]}]}
        """
        .formatted(levelId, assessmentType, estimatedDuration, correctOptionOrderJson);
  }

  private static Stream<Arguments> updatableLevelsWithNonAuthorRoles() {
    return Stream.of(UpdatableLevel.values())
        .flatMap(
            kind ->
                Stream.of(
                        RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                        RoleTypeSecurity.ROLE_TRAINING_ORGANIZER,
                        RoleTypeSecurity.ROLE_TRAINING_TRAINEE)
                    .map(role -> Arguments.of(kind, role)));
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/{training,info,assessment}-levels")
  class UpdateSingleLevelGuards {

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("an unknown definition is answered with status 404")
    void updateLevel_unknownDefinition_returnsNotFound(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          withJson(
              putAs(administrator(), kind.path(99_999L)),
              validPayload(kind, levelOf(kind, seeded))),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("an unknown level is answered with status 404")
    void updateLevel_unknownLevel_returnsNotFound(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(
          withJson(putAs(designer(), kind.path(seeded.id())), validPayload(kind, 99_999L)),
          HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("a level of another definition is answered with status 404 and stays unchanged")
    void updateLevel_levelBelongsToOtherDefinition_returnsNotFound(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition("Own", TDState.UNRELEASED, CALLER_REF_ID);
      SeededDefinition other = seedFourLevelDefinition("Other", TDState.UNRELEASED, CALLER_REF_ID);

      expectError(
          withJson(
              putAs(designer(), kind.path(seeded.id())), validPayload(kind, levelOf(kind, other))),
          HttpStatus.NOT_FOUND);

      assertThat(readLevelAsAdministrator(levelOf(kind, other)).<String>read("$.title"))
          .doesNotStartWith("Updated");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("a released definition is refused with status 409")
    void updateLevel_definitionIsReleased_returnsConflict(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          withJson(
              putAs(designer(), kind.path(seeded.id())), validPayload(kind, levelOf(kind, seeded))),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("a definition with a training instance is refused with status 409")
    void updateLevel_definitionHasInstance_returnsConflict(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          withJson(
              putAs(designer(), kind.path(seeded.id())), validPayload(kind, levelOf(kind, seeded))),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource(
        "cz.cyberrange.platform.training.rest.integration.TrainingDefinitionsIT#updatableLevelsWithNonAuthorRoles")
    @DisplayName("a caller who does not author the definition is refused")
    void updateLevel_callerIsNotAuthor_returnsForbidden(
        UpdatableLevel kind, RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          withJson(
              putAs(callerWithRoles(role), kind.path(seeded.id())),
              validPayload(kind, levelOf(kind, seeded))),
          HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("a body without the level type is refused with status 400")
    void updateLevel_levelTypeMissing_returnsBadRequest(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String body =
          validPayload(kind, levelOf(kind, seeded))
              .replaceFirst("\"level_type\": \"[A-Z_]+\",", "");

      expectError(
          withJson(putAs(designer(), kind.path(seeded.id())), body), HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("a body with an empty title is refused with status 400")
    void updateLevel_titleEmpty_returnsBadRequest(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String body =
          validPayload(kind, levelOf(kind, seeded))
              .replaceFirst("\"title\": \"[^\"]*\"", "\"title\": \"\"");

      expectError(
          withJson(putAs(designer(), kind.path(seeded.id())), body), HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UpdatableLevel.class)
    @DisplayName("the edit is recorded on the definition with the caller's full name")
    void updateLevel_validPayload_recordsCallerAsLastEditor(UpdatableLevel kind) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), kind.path(seeded.id())), validPayload(kind, levelOf(kind, seeded))),
          HttpStatus.NO_CONTENT);

      DocumentContext body = readDefinitionAsAdministrator(seeded.id());
      assertThat(body.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
      assertThat(body.<String>read("$.last_edited")).isNotEqualTo(SEEDED_DATE_TIME);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/training-levels")
  class UpdateTrainingLevel {

    @Test
    @DisplayName("the level takes the submitted values and keeps its position")
    void updateTrainingLevel_validPayload_overwritesLevelKeepingPosition() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String payload =
          trainingLevelPayload(
              seeded.trainingLevelId(),
              50,
              quoted("secret"),
              quoted(null),
              false,
              "[" + hintJson("Hint", 10, 0) + "]",
              9);

      expectEmptyResponse(
          withJson(putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())), payload),
          HttpStatus.NO_CONTENT);

      DocumentContext level = readLevelAsAdministrator(seeded.trainingLevelId());
      assertThat(level.<String>read("$.title")).isEqualTo("Updated training level");
      assertThat(level.<Number>read("$.max_score")).isEqualTo(50);
      assertThat(level.<String>read("$.answer")).isEqualTo("secret");
      assertThat(level.<String>read("$.content")).isEqualTo("Updated content");
      assertThat(level.<String>read("$.solution")).isEqualTo("Updated solution");
      assertThat(level.<Boolean>read("$.solution_penalized")).isTrue();
      assertThat(level.<Number>read("$.incorrect_answer_limit")).isEqualTo(3);
      assertThat(level.<Number>read("$.estimated_duration")).isEqualTo(9);
      assertThat(level.<Boolean>read("$.commands_required")).isTrue();
      assertThat(level.<Number>read("$.order")).isEqualTo(1);
      assertThat(listAt(level, "$.hints[*].title")).containsExactly("Hint");
      assertThat(listAt(level, "$.hints[*].hint_penalty")).containsExactly(10);
      assertThat(listAt(level, "$.hints[*].order")).containsExactly(0);
      assertThat(listAt(level, "$.mitre_techniques[*].technique_key")).containsExactly("T1059");
    }

    @Test
    @DisplayName("the definition's estimated duration follows the change of the level's duration")
    void updateTrainingLevel_durationChanged_adjustsDefinitionEstimatedDuration() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())),
              trainingLevelPayload(
                  seeded.trainingLevelId(), 50, quoted("a"), quoted(null), false, "[]", 12)),
          HttpStatus.NO_CONTENT);

      assertThat(readDefinitionAsAdministrator(seeded.id()).<Number>read("$.estimated_duration"))
          .isEqualTo(FOUR_LEVEL_DURATION - 7 + 12);
    }

    @Test
    @DisplayName("hint penalties adding up to the maximal score are accepted")
    void updateTrainingLevel_hintPenaltiesEqualMaximalScore_returnsNoContent() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String hints = "[" + hintJson("First", 6, 0) + "," + hintJson("Second", 4, 1) + "]";

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())),
              trainingLevelPayload(
                  seeded.trainingLevelId(), 10, quoted("a"), quoted(null), false, hints, 9)),
          HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("hint penalties adding up past the maximal score are refused with status 422")
    void updateTrainingLevel_hintPenaltiesExceedMaximalScore_returnsUnprocessableContent() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String hints = "[" + hintJson("First", 6, 0) + "," + hintJson("Second", 5, 1) + "]";

      expectError(
          withJson(
              putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())),
              trainingLevelPayload(
                  seeded.trainingLevelId(), 10, quoted("a"), quoted(null), false, hints, 9)),
          HttpStatus.UNPROCESSABLE_CONTENT);

      assertThat(readLevelAsAdministrator(seeded.trainingLevelId()).<String>read("$.title"))
          .isEqualTo("Training level 1");
    }

    @Test
    @DisplayName("a level with variant answers stores the answer variable name and no answer")
    void updateTrainingLevel_variantAnswersWithVariableName_overwritesLevel() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())),
              trainingLevelPayload(
                  seeded.trainingLevelId(), 50, quoted(null), quoted("flag"), true, "[]", 9)),
          HttpStatus.NO_CONTENT);

      DocumentContext level = readLevelAsAdministrator(seeded.trainingLevelId());
      assertThat(level.<Boolean>read("$.variant_answers")).isTrue();
      assertThat(level.<String>read("$.answer_variable_name")).isEqualTo("flag");
      assertThat(level.<Object>read("$.answer")).isNull();
    }

    private static Stream<Arguments> inconsistentAnswerFields() {
      return Stream.of(
          Arguments.of("variant answers with a static answer", true, quoted("a"), quoted("flag")),
          Arguments.of("variant answers without a variable name", true, quoted(null), quoted(null)),
          Arguments.of(
              "variant answers with a blank variable name", true, quoted(null), quoted(" ")),
          Arguments.of("static answer with a variable name", false, quoted("a"), quoted("flag")),
          Arguments.of("static answer that is blank", false, quoted(" "), quoted(null)),
          Arguments.of("static answer that is missing", false, quoted(null), quoted(null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inconsistentAnswerFields")
    @DisplayName(
        "answer fields inconsistent with the variant answers choice are refused with status 400")
    void updateTrainingLevel_answerFieldsInconsistent_returnsBadRequest(
        String description, boolean variantAnswers, String answerJson, String variableNameJson) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(
          withJson(
              putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())),
              trainingLevelPayload(
                  seeded.trainingLevelId(),
                  50,
                  answerJson,
                  variableNameJson,
                  variantAnswers,
                  "[]",
                  9)),
          HttpStatus.BAD_REQUEST);

      assertThat(readLevelAsAdministrator(seeded.trainingLevelId()).<String>read("$.title"))
          .isEqualTo("Training level 1");
    }

    private static Stream<Arguments> invalidTrainingLevelFields() {
      return Stream.of(
          Arguments.of("maximal score above 100", "\"max_score\": 50", "\"max_score\": 101"),
          Arguments.of("maximal score below 0", "\"max_score\": 50", "\"max_score\": -1"),
          Arguments.of(
              "incorrect answer limit above 100",
              "\"incorrect_answer_limit\": 3",
              "\"incorrect_answer_limit\": 101"),
          Arguments.of(
              "incorrect answer limit below 0",
              "\"incorrect_answer_limit\": 3",
              "\"incorrect_answer_limit\": -1"),
          Arguments.of(
              "answer longer than 50 characters",
              "\"answer\": \"a\"",
              "\"answer\": \"" + "a".repeat(51) + "\""),
          Arguments.of("empty content", "\"content\": \"Updated content\"", "\"content\": \"\""),
          Arguments.of(
              "empty solution", "\"solution\": \"Updated solution\"", "\"solution\": \"\""),
          Arguments.of(
              "hint without title",
              "\"hints\": []",
              "\"hints\": [{\"title\": \"\", \"content\": \"c\", \"hint_penalty\": 1, \"order\": 0}]"),
          Arguments.of(
              "hint penalty above 100",
              "\"hints\": []",
              "\"hints\": [{\"title\": \"t\", \"content\": \"c\", \"hint_penalty\": 101, \"order\": 0}]"),
          Arguments.of(
              "hint without content",
              "\"hints\": []",
              "\"hints\": [{\"title\": \"t\", \"content\": \"\", \"hint_penalty\": 1, \"order\": 0}]"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTrainingLevelFields")
    @DisplayName("a body failing validation is refused with status 400")
    void updateTrainingLevel_invalidField_returnsBadRequest(
        String description, String original, String replacement) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String body =
          validPayload(UpdatableLevel.TRAINING, seeded.trainingLevelId())
              .replace(original, replacement);
      assertThat(body).contains(replacement);

      expectError(
          withJson(putAs(designer(), UpdatableLevel.TRAINING.path(seeded.id())), body),
          HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/info-levels")
  class UpdateInfoLevel {

    @Test
    @DisplayName("the level takes the submitted title and content and keeps its duration")
    void updateInfoLevel_validPayload_overwritesLevelKeepingDuration() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.INFO.path(seeded.id())),
              infoLevelPayload(seeded.infoLevelId(), "Fresh content")),
          HttpStatus.NO_CONTENT);

      DocumentContext level = readLevelAsAdministrator(seeded.infoLevelId());
      assertThat(level.<String>read("$.title")).isEqualTo("Updated info level");
      assertThat(level.<String>read("$.content")).isEqualTo("Fresh content");
      assertThat(level.<Number>read("$.estimated_duration")).isEqualTo(5);
      assertThat(level.<Number>read("$.order")).isEqualTo(0);
      assertThat(readDefinitionAsAdministrator(seeded.id()).<Number>read("$.estimated_duration"))
          .isEqualTo(FOUR_LEVEL_DURATION);
    }

    @Test
    @DisplayName("a body with empty content is refused with status 400")
    void updateInfoLevel_contentEmpty_returnsBadRequest() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(
          withJson(
              putAs(designer(), UpdatableLevel.INFO.path(seeded.id())),
              infoLevelPayload(seeded.infoLevelId(), "")),
          HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/assessment-levels")
  class UpdateAssessmentLevel {

    @Test
    @DisplayName(
        "the level takes the submitted questions and its maximal score is the sum of their points")
    void updateAssessmentLevel_questionnaire_storesQuestionsAndSumsPoints() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.ASSESSMENT.path(seeded.id())),
              assessmentLevelPayload(seeded.assessmentLevelId(), "QUESTIONNAIRE", 6, "")),
          HttpStatus.NO_CONTENT);

      DocumentContext level = readLevelAsAdministrator(seeded.assessmentLevelId());
      assertThat(level.<String>read("$.title")).isEqualTo("Updated assessment level");
      assertThat(level.<String>read("$.instructions")).isEqualTo("Updated instructions");
      assertThat(level.<String>read("$.assessment_type")).isEqualTo("QUESTIONNAIRE");
      assertThat(level.<Number>read("$.max_score")).isEqualTo(8);
      assertThat(level.<Number>read("$.order")).isEqualTo(3);
      assertThat(listAt(level, "$.questions[*].question_type")).containsExactly("EMI", "MCQ");
      assertThat(listAt(level, "$.questions[*].points")).containsExactly(5, 3);
      assertThat(listAt(level, "$.questions[0].extended_matching_options[*].text"))
          .containsExactly("First option", "Second option");
      assertThat(listAt(level, "$.questions[1].choices[*].correct")).containsExactly(true, false);
    }

    @Test
    @DisplayName("the definition's estimated duration follows the change of the level's duration")
    void updateAssessmentLevel_durationChanged_adjustsDefinitionEstimatedDuration() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.ASSESSMENT.path(seeded.id())),
              assessmentLevelPayload(seeded.assessmentLevelId(), "QUESTIONNAIRE", 10, "")),
          HttpStatus.NO_CONTENT);

      assertThat(readDefinitionAsAdministrator(seeded.id()).<Number>read("$.estimated_duration"))
          .isEqualTo(FOUR_LEVEL_DURATION - 4 + 10);
    }

    @Test
    @DisplayName("a test with the correct option of every statement is stored with that option")
    void updateAssessmentLevel_testWithCorrectOptions_storesCorrectOption() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.ASSESSMENT.path(seeded.id())),
              assessmentLevelPayload(
                  seeded.assessmentLevelId(), "TEST", 6, ", \"correct_option_order\": 1")),
          HttpStatus.NO_CONTENT);

      DocumentContext level = readLevelAsAdministrator(seeded.assessmentLevelId());
      assertThat(level.<String>read("$.assessment_type")).isEqualTo("TEST");
      assertThat(
              level.<Number>read(
                  "$.questions[0].extended_matching_statements[0].correct_option_order"))
          .isEqualTo(1);
    }

    @Test
    @DisplayName("a test with a statement lacking its correct option is refused with status 400")
    void updateAssessmentLevel_testStatementWithoutCorrectOption_returnsBadRequest() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(
          withJson(
              putAs(designer(), UpdatableLevel.ASSESSMENT.path(seeded.id())),
              assessmentLevelPayload(seeded.assessmentLevelId(), "TEST", 6, "")),
          HttpStatus.BAD_REQUEST);

      assertThat(readLevelAsAdministrator(seeded.assessmentLevelId()).<String>read("$.title"))
          .isEqualTo("Assessment level 3");
    }

    @Test
    @DisplayName("a questionnaire needs no correct option on its statements")
    void updateAssessmentLevel_questionnaireStatementWithoutCorrectOption_returnsNoContent() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(
              putAs(designer(), UpdatableLevel.ASSESSMENT.path(seeded.id())),
              assessmentLevelPayload(seeded.assessmentLevelId(), "QUESTIONNAIRE", 6, "")),
          HttpStatus.NO_CONTENT);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        delimiter = '|',
        value = {
          "missing assessment type|\"type\": \"QUESTIONNAIRE\",|",
          "missing instructions|\"instructions\": \"Updated instructions\",|",
          "question without text|\"text\": \"Pick one\"|\"text\": \"\""
        })
    @DisplayName("a body failing validation is refused with status 400")
    void updateAssessmentLevel_invalidBody_returnsBadRequest(
        String description, String original, String replacement) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String body =
          assessmentLevelPayload(seeded.assessmentLevelId(), "QUESTIONNAIRE", 6, "")
              .replace(original, replacement == null ? "" : replacement);

      expectError(
          withJson(putAs(designer(), UpdatableLevel.ASSESSMENT.path(seeded.id())), body),
          HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/levels")
  class UpdateLevels {

    private static final String LEVELS = DEFINITIONS + "/{definitionId}/levels";

    private String mixedPayload(SeededDefinition seeded) {
      return "["
          + String.join(
              ",",
              infoLevelPayload(seeded.infoLevelId(), "Fresh info"),
              validPayload(UpdatableLevel.TRAINING, seeded.trainingLevelId()),
              accessLevelPayload(seeded.accessLevelId()),
              assessmentLevelPayload(seeded.assessmentLevelId(), "QUESTIONNAIRE", 4, ""))
          + "]";
    }

    @Test
    @DisplayName("each level is overwritten according to its own type")
    void updateLevels_levelsOfEveryType_overwritesEachAccordingToItsType() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEmptyResponse(
          withJson(putAs(designer(), LEVELS, seeded.id()), mixedPayload(seeded)),
          HttpStatus.NO_CONTENT);

      DocumentContext body = readDefinitionAsAdministrator(seeded.id());
      assertThat(body.<String>read("$.levels[0].content")).isEqualTo("Fresh info");
      assertThat(body.<String>read("$.levels[1].title")).isEqualTo("Updated training level");
      assertThat(body.<String>read("$.levels[1].content")).isEqualTo("Updated content");
      assertThat(body.<String>read("$.levels[2].passkey")).isEqualTo("new-passkey");
      assertThat(body.<String>read("$.levels[2].cloud_content")).isEqualTo("New cloud content");
      assertThat(body.<String>read("$.levels[3].instructions")).isEqualTo("Updated instructions");
      assertThat(listAt(body, "$.levels[*].order")).containsExactly(0, 1, 2, 3);
      assertThat(body.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
    }

    @Test
    @DisplayName("a level of another definition aborts the whole request and changes nothing")
    void updateLevels_levelBelongsToOtherDefinition_returnsNotFoundAndChangesNothing() {
      SeededDefinition seeded = seedFourLevelDefinition("Own", TDState.UNRELEASED, CALLER_REF_ID);
      SeededDefinition other = seedFourLevelDefinition("Other", TDState.UNRELEASED, CALLER_REF_ID);
      String payload =
          "["
              + infoLevelPayload(seeded.infoLevelId(), "Fresh info")
              + ","
              + infoLevelPayload(other.infoLevelId(), "Foreign info")
              + "]";

      expectError(withJson(putAs(designer(), LEVELS, seeded.id()), payload), HttpStatus.NOT_FOUND);

      assertThat(readLevelAsAdministrator(seeded.infoLevelId()).<String>read("$.content"))
          .isEqualTo("Content of info level 0");
      assertThat(readLevelAsAdministrator(other.infoLevelId()).<String>read("$.content"))
          .isEqualTo("Content of info level 0");
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void updateLevels_unknownDefinition_returnsNotFound() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectEntityError(
          withJson(putAs(administrator(), LEVELS, 99_999L), mixedPayload(seeded)),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @Test
    @DisplayName("a released definition is refused with status 409")
    void updateLevels_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          withJson(putAs(designer(), LEVELS, seeded.id()), mixedPayload(seeded)),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("a definition with a training instance is refused with status 409")
    void updateLevels_definitionHasInstance_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          withJson(putAs(designer(), LEVELS, seeded.id()), mixedPayload(seeded)),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("a level failing validation is refused with status 400")
    void updateLevels_levelWithEmptyTitle_returnsBadRequest() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String payload =
          "["
              + infoLevelPayload(seeded.infoLevelId(), "Fresh info")
                  .replace("Updated info level", "")
              + "]";

      expectError(
          withJson(putAs(designer(), LEVELS, seeded.id()), payload), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("a test statement lacking its correct option is refused with status 400")
    void updateLevels_testStatementWithoutCorrectOption_returnsBadRequest() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      String payload =
          "[" + assessmentLevelPayload(seeded.assessmentLevelId(), "TEST", 6, "") + "]";

      expectError(
          withJson(putAs(designer(), LEVELS, seeded.id()), payload), HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void updateLevels_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          withJson(putAs(callerWithRoles(role), LEVELS, seeded.id()), mixedPayload(seeded)),
          HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/levels/{levelId}")
  class FindLevel {

    private static final String LEVEL = DEFINITIONS + "/levels/{levelId}";

    @Test
    @DisplayName("an info level is returned with its content")
    void findLevel_infoLevel_returnsInfoFields() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      DocumentContext body = respond(getAs(designer(), LEVEL, seeded.infoLevelId()), HttpStatus.OK);

      assertThat(body.<String>read("$.level_type")).isEqualTo("INFO_LEVEL");
      assertThat(body.<String>read("$.title")).isEqualTo("Info level 0");
      assertThat(body.<String>read("$.content")).isEqualTo("Content of info level 0");
      assertThat(body.<Number>read("$.estimated_duration")).isEqualTo(5);
      assertThat(body.<Number>read("$.order")).isEqualTo(0);
    }

    @Test
    @DisplayName("a training level is returned with its answer, solution and techniques")
    void findLevel_trainingLevel_returnsTrainingFields() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), LEVEL, seeded.trainingLevelId()), HttpStatus.OK);

      assertThat(body.<String>read("$.level_type")).isEqualTo("TRAINING_LEVEL");
      assertThat(body.<String>read("$.answer")).isEqualTo("answer-1");
      assertThat(body.<String>read("$.solution")).isEqualTo("Solution of training level 1");
      assertThat(body.<Number>read("$.max_score")).isEqualTo(100);
      assertThat(body.<Number>read("$.incorrect_answer_limit")).isEqualTo(10);
      assertThat(listAt(body, "$.mitre_techniques[*].technique_key")).containsExactly("T1059");
    }

    @Test
    @DisplayName("an access level is returned with its passkey and contents")
    void findLevel_accessLevel_returnsAccessFields() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), LEVEL, seeded.accessLevelId()), HttpStatus.OK);

      assertThat(body.<String>read("$.level_type")).isEqualTo("ACCESS_LEVEL");
      assertThat(body.<String>read("$.passkey")).isEqualTo("passkey-2");
      assertThat(body.<String>read("$.cloud_content")).isEqualTo("Cloud content 2");
      assertThat(body.<String>read("$.local_content")).isEqualTo("Local content 2");
    }

    @Test
    @DisplayName("an assessment level is returned with its instructions and type")
    void findLevel_assessmentLevel_returnsAssessmentFields() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), LEVEL, seeded.assessmentLevelId()), HttpStatus.OK);

      assertThat(body.<String>read("$.level_type")).isEqualTo("ASSESSMENT_LEVEL");
      assertThat(body.<String>read("$.instructions")).isEqualTo("Instructions 3");
      assertThat(body.<String>read("$.assessment_type")).isEqualTo("QUESTIONNAIRE");
      assertThat(listAt(body, "$.questions")).isEmpty();
    }

    @Test
    @DisplayName("an unknown level is answered with status 404")
    void findLevel_unknownLevel_returnsNotFound() {
      expectEntityError(
          getAs(administrator(), LEVEL, 99_999L), HttpStatus.NOT_FOUND, "AbstractLevel", 99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who is neither designer nor administrator is refused")
    void findLevel_callerIsNeitherDesignerNorAdministrator_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(getAs(callerWithRoles(role), LEVEL, seeded.infoLevelId()), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("POST /training-definitions/{definitionId}/levels/{levelType}")
  class CreateLevel {

    private static final String CREATE = DEFINITIONS + "/{definitionId}/levels/{levelType}";

    @ParameterizedTest(name = "{0}")
    @CsvSource({
      "INFO,INFO_LEVEL",
      "TRAINING,TRAINING_LEVEL",
      "ACCESS,ACCESS_LEVEL",
      "ASSESSMENT,ASSESSMENT_LEVEL"
    })
    @DisplayName("a level of the requested type is appended last")
    void createLevel_validType_returnsCreatedLevelAppendedLast(
        String levelType, String expectedType) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext created =
          respond(postAs(designer(), CREATE, seeded.id(), levelType), HttpStatus.CREATED);

      Long createdId = created.<Number>read("$.id").longValue();
      assertThat(created.<String>read("$.level_type")).isEqualTo(expectedType);
      assertThat(created.<Number>read("$.order")).isEqualTo(4);
      assertThat(created.<String>read("$.title")).isNotBlank();
      DocumentContext definition = readDefinitionAsAdministrator(seeded.id());
      assertThat(listAt(definition, "$.levels[*].level_type")).hasSize(5).endsWith(expectedType);
      assertThat(definition.<Number>read("$.levels[4].id").longValue()).isEqualTo(createdId);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"INFO", "TRAINING", "ACCESS", "ASSESSMENT"})
    @DisplayName("the estimated duration rises by the duration of the new level")
    void createLevel_validType_raisesEstimatedDurationByLevelDuration(String levelType) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      Long createdId =
          respond(postAs(designer(), CREATE, seeded.id(), levelType), HttpStatus.CREATED)
              .<Number>read("$.id")
              .longValue();

      int levelDuration =
          readLevelAsAdministrator(createdId).<Number>read("$.estimated_duration").intValue();
      assertThat(readDefinitionAsAdministrator(seeded.id()).<Number>read("$.estimated_duration"))
          .isEqualTo(FOUR_LEVEL_DURATION + levelDuration);
    }

    @Test
    @DisplayName("an administrator who does not author the definition appends a level")
    void createLevel_callerIsAdministratorWithoutAuthorship_returnsCreatedLevel() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      respond(postAs(administrator(), CREATE, seeded.id(), "INFO"), HttpStatus.CREATED);
    }

    @Test
    @DisplayName("an unrecognized level type is refused with status 400")
    void createLevel_unrecognizedType_returnsBadRequest() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);

      expectError(postAs(designer(), CREATE, seeded.id(), "BOGUS"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void createLevel_unknownDefinition_returnsNotFound() {
      expectEntityError(
          postAs(administrator(), CREATE, 99_999L, "INFO"),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @Test
    @DisplayName("a released definition is refused with status 409")
    void createLevel_definitionIsReleased_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.RELEASED, CALLER_REF_ID);

      expectEntityError(
          postAs(designer(), CREATE, seeded.id(), "INFO"),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @Test
    @DisplayName("a definition with a training instance is refused with status 409")
    void createLevel_definitionHasInstance_returnsConflict() {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, CALLER_REF_ID);
      seeder.instance(seeded.id(), inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          postAs(designer(), CREATE, seeded.id(), "INFO"),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          seeded.id());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void createLevel_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      SeededDefinition seeded = seedFourLevelDefinition(TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(postAs(callerWithRoles(role), CREATE, seeded.id(), "INFO"), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/{designers,organizers}")
  class ListRoleHolders {

    @ParameterizedTest(name = "{0}")
    @CsvSource({"designers,ROLE_TRAINING_DESIGNER", "organizers,ROLE_TRAINING_ORGANIZER"})
    @DisplayName(
        "the users the user-and-group service reports for the role are returned with the filters forwarded")
    void getRoleHolders_filtersAndPageGiven_forwardsThemAndReturnsUpstreamPage(
        String pathSegment, String roleType) {
      stubUpstreamUsers(USERS_BY_ROLE_PATH, 701L, 702L);

      DocumentContext body =
          respond(
              getAs(
                  designer(),
                  DEFINITIONS + "/{segment}?givenName=Ann&familyName=Lee&page=2&size=5",
                  pathSegment),
              HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].user_ref_id")).containsExactly(701, 702);
      assertThat(body.<String>read("$.content[0].full_name")).isEqualTo("Full 701");
      assertThat(body.<String>read("$.content[0].given_name")).isEqualTo("Given701");
      assertThat(body.<String>read("$.content[0].family_name")).isEqualTo("Family701");
      assertThat(body.<String>read("$.content[0].mail")).isEqualTo("user701@example.org");
      assertThat(body.<String>read("$.content[0].sub")).isEqualTo("user701@example.org");
      assertThat(body.<String>read("$.content[0].iss")).isEqualTo("http://issuer");
      assertThat(body.<Number>read("$.pagination.total_elements")).isEqualTo(2);
      externalServices.verify(
          1,
          getRequestedFor(urlPathEqualTo(USERS_BY_ROLE_PATH))
              .withQueryParam("roleType", equalTo(roleType))
              .withQueryParam("givenName", equalTo("Ann"))
              .withQueryParam("familyName", equalTo("Lee"))
              .withQueryParam("page", equalTo("2"))
              .withQueryParam("size", equalTo("5")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"designers,ROLE_TRAINING_DESIGNER", "organizers,ROLE_TRAINING_ORGANIZER"})
    @DisplayName("name filters are left out of the upstream request when absent")
    void getRoleHolders_noFilters_forwardsOnlyRoleAndPage(String pathSegment, String roleType) {
      stubUpstreamUsers(USERS_BY_ROLE_PATH, 701L);

      respond(getAs(administrator(), DEFINITIONS + "/{segment}", pathSegment), HttpStatus.OK);

      externalServices.verify(
          1,
          getRequestedFor(urlPathEqualTo(USERS_BY_ROLE_PATH))
              .withQueryParam("roleType", equalTo(roleType))
              .withQueryParam("givenName", absent())
              .withQueryParam("familyName", absent())
              .withQueryParam("page", equalTo("0"))
              .withQueryParam("size", equalTo("20")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"designers", "organizers"})
    @DisplayName(
        "a failing user-and-group service is reported as an error carrying the upstream failure")
    void getRoleHolders_upstreamFails_returnsErrorWithUpstreamDetail(String pathSegment) {
      externalServices.stubFor(get(urlPathEqualTo(USERS_BY_ROLE_PATH)).willReturn(serverError()));

      DocumentContext body =
          respond(
              getAs(designer(), DEFINITIONS + "/{segment}", pathSegment),
              HttpStatus.INTERNAL_SERVER_ERROR);

      assertThat(body.<Object>read("$.api_sub_error")).isNotNull();
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource(
        "cz.cyberrange.platform.training.rest.integration.TrainingDefinitionsIT#roleHolderEndpointsWithRefusedRoles")
    @DisplayName("a caller who is neither designer nor administrator is refused")
    void getRoleHolders_callerIsNeitherDesignerNorAdministrator_returnsForbidden(
        String pathSegment, RoleTypeSecurity role) {
      expectError(
          getAs(callerWithRoles(role), DEFINITIONS + "/{segment}", pathSegment),
          HttpStatus.FORBIDDEN);
    }
  }

  private static Stream<Arguments> roleHolderEndpointsWithRefusedRoles() {
    return Stream.of("designers", "organizers")
        .flatMap(
            pathSegment ->
                Stream.of(
                        RoleTypeSecurity.ROLE_TRAINING_ORGANIZER,
                        RoleTypeSecurity.ROLE_TRAINING_TRAINEE)
                    .map(role -> Arguments.of(pathSegment, role)));
  }

  @Nested
  @DisplayName("GET /training-definitions/{definitionId}/designers-not-in-training-definition")
  class ListDesignersNotInDefinition {

    private static final String DESIGNERS_NOT_IN =
        DEFINITIONS + "/{definitionId}/designers-not-in-training-definition";

    @Test
    @DisplayName("the designers the upstream service returns exclude the definition's authors")
    void getDesignersNotInDefinition_definitionWithAuthors_excludesAuthorsUpstream() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_ROLE_EXCLUDING_PATH, 801L);

      DocumentContext body =
          respond(
              getAs(
                  designer(),
                  DESIGNERS_NOT_IN + "?givenName=Ann&familyName=Lee&page=1&size=3",
                  definitionId),
              HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].user_ref_id")).containsExactly(801);
      externalServices.verify(
          1,
          getRequestedFor(urlPathEqualTo(USERS_BY_ROLE_EXCLUDING_PATH))
              .withQueryParam("roleType", equalTo("ROLE_TRAINING_DESIGNER"))
              .withQueryParam(
                  "ids",
                  matching(
                      CALLER_REF_ID
                          + ","
                          + OTHER_DESIGNER_REF_ID
                          + "|"
                          + OTHER_DESIGNER_REF_ID
                          + ","
                          + CALLER_REF_ID))
              .withQueryParam("givenName", equalTo("Ann"))
              .withQueryParam("familyName", equalTo("Lee"))
              .withQueryParam("page", equalTo("1"))
              .withQueryParam("size", equalTo("3")));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {
          "ROLE_TRAINING_DESIGNER",
          "ROLE_TRAINING_ORGANIZER",
          "ROLE_TRAINING_ADMINISTRATOR"
        })
    @DisplayName("a designer, an organizer or an administrator may list them")
    void getDesignersNotInDefinition_permittedRole_returnsPage(RoleTypeSecurity role) {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_ROLE_EXCLUDING_PATH, 801L);

      respond(getAs(callerWithRoles(role), DESIGNERS_NOT_IN, definitionId), HttpStatus.OK);
    }

    @Test
    @DisplayName(
        "an unknown definition is answered with status 404 without asking the upstream service")
    void getDesignersNotInDefinition_unknownDefinition_returnsNotFound() {
      expectEntityError(
          getAs(designer(), DESIGNERS_NOT_IN, 99_999L),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);

      externalServices.verify(0, getRequestedFor(urlPathEqualTo(USERS_BY_ROLE_EXCLUDING_PATH)));
    }

    @Test
    @DisplayName("a trainee is refused")
    void getDesignersNotInDefinition_callerIsTrainee_returnsForbidden() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(getAs(trainee(), DESIGNERS_NOT_IN, definitionId), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/{definitionId}/beta-testers")
  class ListBetaTesters {

    private static final String BETA_TESTERS = DEFINITIONS + "/{definitionId}/beta-testers";

    @Test
    @DisplayName("the organizers of the beta testing group are fetched from the upstream service")
    void getBetaTesters_groupWithOrganizers_returnsUpstreamUsersOfGroup() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID);
      seeder.betaTestingGroup(definitionId, ORGANIZER_REF_ID, TRAINEE_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, ORGANIZER_REF_ID, TRAINEE_REF_ID);

      DocumentContext body =
          respond(getAs(designer(), BETA_TESTERS + "?page=1&size=5", definitionId), HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].user_ref_id"))
          .containsExactlyInAnyOrder((int) ORGANIZER_REF_ID, (int) TRAINEE_REF_ID);
      assertThat(body.<Number>read("$.pagination.total_elements")).isEqualTo(2);
      externalServices.verify(
          1,
          getRequestedFor(urlPathEqualTo(USERS_BY_IDS_PATH))
              .withQueryParam(
                  "ids",
                  matching(
                      ORGANIZER_REF_ID
                          + ","
                          + TRAINEE_REF_ID
                          + "|"
                          + TRAINEE_REF_ID
                          + ","
                          + ORGANIZER_REF_ID))
              .withQueryParam("page", equalTo("1"))
              .withQueryParam("size", equalTo("5"))
              .withQueryParam("givenName", absent())
              .withQueryParam("familyName", absent()));
    }

    @Test
    @DisplayName(
        "a definition without a beta testing group yields an empty page without an upstream call")
    void getBetaTesters_noGroup_returnsEmptyPageWithoutUpstreamCall() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID);

      DocumentContext body = respond(getAs(designer(), BETA_TESTERS, definitionId), HttpStatus.OK);

      assertThat(listAt(body, "$.content")).isEmpty();
      assertThat(body.<Number>read("$.pagination.total_elements")).isEqualTo(0);
      externalServices.verify(0, getRequestedFor(urlPathEqualTo(USERS_BY_IDS_PATH)));
    }

    @Test
    @DisplayName(
        "a beta testing group without organizers yields an empty page without an upstream call")
    void getBetaTesters_emptyGroup_returnsEmptyPageWithoutUpstreamCall() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID);
      seeder.betaTestingGroup(definitionId);

      DocumentContext body = respond(getAs(designer(), BETA_TESTERS, definitionId), HttpStatus.OK);

      assertThat(listAt(body, "$.content")).isEmpty();
      assertThat(body.<Number>read("$.pagination.total_elements")).isEqualTo(0);
      externalServices.verify(0, getRequestedFor(urlPathEqualTo(USERS_BY_IDS_PATH)));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {
          "ROLE_TRAINING_DESIGNER",
          "ROLE_TRAINING_ORGANIZER",
          "ROLE_TRAINING_ADMINISTRATOR"
        })
    @DisplayName("a designer, an organizer or an administrator may list them")
    void getBetaTesters_permittedRole_returnsPage(RoleTypeSecurity role) {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      respond(getAs(callerWithRoles(role), BETA_TESTERS, definitionId), HttpStatus.OK);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void getBetaTesters_unknownDefinition_returnsNotFound() {
      expectEntityError(
          getAs(designer(), BETA_TESTERS, 99_999L),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @Test
    @DisplayName("a trainee is refused")
    void getBetaTesters_callerIsTrainee_returnsForbidden() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(getAs(trainee(), BETA_TESTERS, definitionId), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/{definitionId}/authors")
  class ListAuthors {

    private static final String AUTHORS = DEFINITIONS + "/{definitionId}/authors";

    @Test
    @DisplayName("the authors are fetched from the upstream service with the filters forwarded")
    void getAuthors_filtersGiven_forwardsThemAndReturnsUpstreamUsers() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);

      DocumentContext body =
          respond(
              getAs(
                  designer(),
                  AUTHORS + "?givenName=Ann&familyName=Lee&page=1&size=5",
                  definitionId),
              HttpStatus.OK);

      assertThat(listAt(body, "$.content[*].user_ref_id"))
          .containsExactlyInAnyOrder((int) CALLER_REF_ID, (int) OTHER_DESIGNER_REF_ID);
      externalServices.verify(
          1,
          getRequestedFor(urlPathEqualTo(USERS_BY_IDS_PATH))
              .withQueryParam(
                  "ids",
                  matching(
                      CALLER_REF_ID
                          + ","
                          + OTHER_DESIGNER_REF_ID
                          + "|"
                          + OTHER_DESIGNER_REF_ID
                          + ","
                          + CALLER_REF_ID))
              .withQueryParam("givenName", equalTo("Ann"))
              .withQueryParam("familyName", equalTo("Lee"))
              .withQueryParam("page", equalTo("1"))
              .withQueryParam("size", equalTo("5")));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {
          "ROLE_TRAINING_DESIGNER",
          "ROLE_TRAINING_ORGANIZER",
          "ROLE_TRAINING_ADMINISTRATOR"
        })
    @DisplayName("a designer, an organizer or an administrator may list them")
    void getAuthors_permittedRole_returnsPage(RoleTypeSecurity role) {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, OTHER_DESIGNER_REF_ID);

      respond(getAs(callerWithRoles(role), AUTHORS, definitionId), HttpStatus.OK);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void getAuthors_unknownDefinition_returnsNotFound() {
      expectEntityError(
          getAs(designer(), AUTHORS, 99_999L), HttpStatus.NOT_FOUND, "TrainingDefinition", 99_999L);
    }

    @Test
    @DisplayName("a trainee is refused")
    void getAuthors_callerIsTrainee_returnsForbidden() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(getAs(trainee(), AUTHORS, definitionId), HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/authors")
  class EditAuthors {

    private static final String AUTHORS = DEFINITIONS + "/{definitionId}/authors";

    @Test
    @DisplayName("an added user becomes an author")
    void editAuthors_userAdded_becomesAuthor() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, NEW_AUTHOR_REF_ID);

      expectEmptyResponse(
          putAs(designer(), AUTHORS + "?authorsAddition={added}", definitionId, NEW_AUTHOR_REF_ID),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId))
          .containsExactlyInAnyOrder(CALLER_REF_ID, NEW_AUTHOR_REF_ID);
    }

    @Test
    @DisplayName("an added user who already authors the definition is not added twice")
    void editAuthors_userAlreadyAuthor_leavesAuthorsUnchanged() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          putAs(
              designer(),
              AUTHORS + "?authorsAddition={added}",
              definitionId,
              OTHER_DESIGNER_REF_ID),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId))
          .containsExactlyInAnyOrder(CALLER_REF_ID, OTHER_DESIGNER_REF_ID);
    }

    @Test
    @DisplayName("a removed author stops being an author")
    void editAuthors_authorRemoved_stopsBeingAuthor() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          putAs(
              designer(),
              AUTHORS + "?authorsRemoval={removed}",
              definitionId,
              OTHER_DESIGNER_REF_ID),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId)).containsExactly(CALLER_REF_ID);
    }

    @Test
    @DisplayName("the caller stays an author even when listed for removal")
    void editAuthors_callerListedForRemoval_staysAuthor() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          putAs(
              designer(),
              AUTHORS + "?authorsRemoval={caller},{other}",
              definitionId,
              CALLER_REF_ID,
              OTHER_DESIGNER_REF_ID),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId)).containsExactly(CALLER_REF_ID);
    }

    @Test
    @DisplayName("users can be added and removed in one request")
    void editAuthors_additionAndRemovalTogether_appliesBoth() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, NEW_AUTHOR_REF_ID);

      expectEmptyResponse(
          putAs(
              designer(),
              AUTHORS + "?authorsAddition={added}&authorsRemoval={removed}",
              definitionId,
              NEW_AUTHOR_REF_ID,
              OTHER_DESIGNER_REF_ID),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId))
          .containsExactlyInAnyOrder(CALLER_REF_ID, NEW_AUTHOR_REF_ID);
    }

    @Test
    @DisplayName("a request naming nobody changes no author")
    void editAuthors_noUsersNamed_leavesAuthorsUnchanged() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(putAs(designer(), AUTHORS, definitionId), HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId))
          .containsExactlyInAnyOrder(CALLER_REF_ID, OTHER_DESIGNER_REF_ID);
    }

    @Test
    @DisplayName("the edit is recorded on the definition with the caller's full name")
    void editAuthors_authorRemoved_recordsCallerAsLastEditor() {
      Long definitionId =
          seeder.definition("Alpha", TDState.UNRELEASED, CALLER_REF_ID, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          putAs(
              designer(),
              AUTHORS + "?authorsRemoval={removed}",
              definitionId,
              OTHER_DESIGNER_REF_ID),
          HttpStatus.NO_CONTENT);

      DocumentContext body = readDefinitionAsAdministrator(definitionId);
      assertThat(body.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
      assertThat(body.<String>read("$.last_edited")).isNotEqualTo(SEEDED_DATE_TIME);
    }

    @Test
    @DisplayName("an administrator who does not author the definition edits its authors")
    void editAuthors_callerIsAdministratorWithoutAuthorship_changesAuthors() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
      stubUpstreamUsers(USERS_BY_IDS_PATH, NEW_AUTHOR_REF_ID);

      expectEmptyResponse(
          putAs(
              administrator(),
              AUTHORS + "?authorsAddition={added}",
              definitionId,
              NEW_AUTHOR_REF_ID),
          HttpStatus.NO_CONTENT);

      assertThat(seeder.authorUserRefIds(definitionId))
          .containsExactlyInAnyOrder(OTHER_DESIGNER_REF_ID, NEW_AUTHOR_REF_ID);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void editAuthors_unknownDefinition_returnsNotFound() {
      expectEntityError(
          putAs(administrator(), AUTHORS, 99_999L),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void editAuthors_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          putAs(
              callerWithRoles(role),
              AUTHORS + "?authorsRemoval={removed}",
              definitionId,
              OTHER_DESIGNER_REF_ID),
          HttpStatus.FORBIDDEN);

      assertThat(seeder.authorUserRefIds(definitionId)).containsExactly(OTHER_DESIGNER_REF_ID);
    }
  }

  @Nested
  @DisplayName("PUT /training-definitions/{definitionId}/states/{state}")
  class SwitchState {

    private static final String SWITCH = DEFINITIONS + "/{definitionId}/states/{state}";

    private Long seedDefinitionIn(TDState state) {
      return seeder.definition("Alpha", state, CALLER_REF_ID);
    }

    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({"UNRELEASED,RELEASED", "RELEASED,ARCHIVED", "RELEASED,UNRELEASED"})
    @DisplayName("the allowed moves change the state")
    void switchState_allowedMove_changesState(TDState from, String to) {
      Long definitionId = seedDefinitionIn(from);

      expectEmptyResponse(putAs(designer(), SWITCH, definitionId, to), HttpStatus.NO_CONTENT);

      assertThat(readDefinitionAsAdministrator(definitionId).<String>read("$.state")).isEqualTo(to);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"UNRELEASED", "RELEASED", "ARCHIVED"})
    @DisplayName("asking for the state the definition already holds changes nothing")
    void switchState_sameState_changesNothing(TDState state) {
      Long definitionId = seedDefinitionIn(state);

      expectEmptyResponse(
          putAs(designer(), SWITCH, definitionId, state.name()), HttpStatus.NO_CONTENT);

      assertThat(readDefinitionAsAdministrator(definitionId).<String>read("$.state"))
          .isEqualTo(state.name());
    }

    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({"UNRELEASED,ARCHIVED", "ARCHIVED,RELEASED", "ARCHIVED,UNRELEASED"})
    @DisplayName("every other move is refused with status 409 and leaves the state")
    void switchState_moveNotAllowed_returnsConflictAndKeepsState(TDState from, String to) {
      Long definitionId = seedDefinitionIn(from);

      expectEntityError(
          putAs(designer(), SWITCH, definitionId, to),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          definitionId);

      assertThat(readDefinitionAsAdministrator(definitionId).<String>read("$.state"))
          .isEqualTo(from.name());
    }

    @Test
    @DisplayName("a released definition with a training instance cannot return to unreleased")
    void switchState_releasedToUnreleasedWithInstance_returnsConflictAndKeepsState() {
      Long definitionId = seedDefinitionIn(TDState.RELEASED);
      seeder.instance(definitionId, inTheFuture(), ORGANIZER_REF_ID);

      expectEntityError(
          putAs(designer(), SWITCH, definitionId, "UNRELEASED"),
          HttpStatus.CONFLICT,
          "TrainingDefinition",
          definitionId);

      assertThat(readDefinitionAsAdministrator(definitionId).<String>read("$.state"))
          .isEqualTo("RELEASED");
    }

    @Test
    @DisplayName("the change is recorded on the definition with the caller's full name")
    void switchState_allowedMove_recordsCallerAsLastEditor() {
      Long definitionId = seedDefinitionIn(TDState.UNRELEASED);

      expectEmptyResponse(
          putAs(designer(), SWITCH, definitionId, "RELEASED"), HttpStatus.NO_CONTENT);

      DocumentContext body = readDefinitionAsAdministrator(definitionId);
      assertThat(body.<String>read("$.last_edited_by")).isEqualTo(CALLER_FULL_NAME);
      assertThat(body.<String>read("$.last_edited")).isNotEqualTo(SEEDED_DATE_TIME);
    }

    @Test
    @DisplayName("an administrator who does not author the definition changes its state")
    void switchState_callerIsAdministratorWithoutAuthorship_changesState() {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectEmptyResponse(
          putAs(administrator(), SWITCH, definitionId, "RELEASED"), HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("an unrecognized state is refused with status 400")
    void switchState_unrecognizedState_returnsBadRequest() {
      Long definitionId = seedDefinitionIn(TDState.UNRELEASED);

      expectError(putAs(designer(), SWITCH, definitionId, "BOGUS"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("an unknown definition is answered with status 404")
    void switchState_unknownDefinition_returnsNotFound() {
      expectEntityError(
          putAs(administrator(), SWITCH, 99_999L, "RELEASED"),
          HttpStatus.NOT_FOUND,
          "TrainingDefinition",
          99_999L);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = RoleTypeSecurity.class,
        names = {"ROLE_TRAINING_DESIGNER", "ROLE_TRAINING_ORGANIZER", "ROLE_TRAINING_TRAINEE"})
    @DisplayName("a caller who does not author the definition is refused")
    void switchState_callerIsNotAuthor_returnsForbidden(RoleTypeSecurity role) {
      Long definitionId = seeder.definition("Alpha", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);

      expectError(
          putAs(callerWithRoles(role), SWITCH, definitionId, "RELEASED"), HttpStatus.FORBIDDEN);

      assertThat(readDefinitionAsAdministrator(definitionId).<String>read("$.state"))
          .isEqualTo("UNRELEASED");
    }
  }

  @Nested
  @DisplayName("GET /training-definitions/{by-ids,levels/by-ids,hints/by-ids}")
  class FindByIds {

    private SeededDefinition participated;
    private Long levelWithHintsId;
    private List<Long> hintIds;
    private SeededDefinition unrelated;

    @BeforeEach
    void seedDefinitions() {
      participated =
          seedFourLevelDefinition("Participated", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
      levelWithHintsId =
          seeder.trainingLevelWithHints(participated.id(), 4, "First hint", "Second hint");
      hintIds = seeder.hintIds(levelWithHintsId);
      Long instanceId = seeder.instance(participated.id(), inThePast(), ORGANIZER_REF_ID);
      seeder.run(instanceId, participated.infoLevelId(), TRAINEE_REF_ID);
      unrelated = seedFourLevelDefinition("Unrelated", TDState.RELEASED, OTHER_DESIGNER_REF_ID);
    }

    private String csv(Long... ids) {
      return Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(","));
    }

    @Test
    @DisplayName(
        "an administrator receives the named definitions with their levels and skips unknown ids")
    void findDefinitionsByIds_callerIsAdministrator_returnsKnownDefinitionsWithLevels() {
      DocumentContext body =
          respond(
              getAs(
                  administrator(),
                  DEFINITIONS + "/by-ids?ids={ids}",
                  csv(participated.id(), unrelated.id(), 99_999L)),
              HttpStatus.OK);

      assertThat(listAt(body, "$[*].title")).containsExactlyInAnyOrder("Participated", "Unrelated");
      assertThat(listAt(body, "$[?(@.title=='Unrelated')].estimated_duration"))
          .containsExactly(FOUR_LEVEL_DURATION);
      assertThat(listAt(body, "$[?(@.title=='Unrelated')].levels[*].level_type"))
          .containsExactly("INFO_LEVEL", "TRAINING_LEVEL", "ACCESS_LEVEL", "ASSESSMENT_LEVEL");
      assertThat(listAt(body, "$[?(@.title=='Participated')].levels[*].id")).hasSize(5);
    }

    @Test
    @DisplayName("a trainee with a run in an instance of the definition receives it")
    void findDefinitionsByIds_callerHasRunInInstance_returnsDefinition() {
      stubCaller(TRAINEE_REF_ID);

      DocumentContext body =
          respond(
              getAs(trainee(), DEFINITIONS + "/by-ids?ids={ids}", participated.id()),
              HttpStatus.OK);

      assertThat(listAt(body, "$[*].title")).containsExactly("Participated");
    }

    @Test
    @DisplayName("an organizer of an instance of the definition receives it")
    void findDefinitionsByIds_callerOrganizesInstance_returnsDefinition() {
      stubCaller(ORGANIZER_REF_ID);

      respond(
          getAs(organizer(), DEFINITIONS + "/by-ids?ids={ids}", participated.id()), HttpStatus.OK);
    }

    @Test
    @DisplayName("a caller missing from one of the named definitions is refused")
    void findDefinitionsByIds_callerNotInEveryDefinition_returnsForbidden() {
      stubCaller(TRAINEE_REF_ID);

      expectError(
          getAs(
              trainee(), DEFINITIONS + "/by-ids?ids={ids}", csv(participated.id(), unrelated.id())),
          HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("missing ids are refused with status 400")
    void findDefinitionsByIds_idsMissing_returnsBadRequest() {
      expectError(getAs(administrator(), DEFINITIONS + "/by-ids"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("an administrator receives the named levels and skips unknown ids")
    void findLevelsByIds_callerIsAdministrator_returnsKnownLevels() {
      DocumentContext body =
          respond(
              getAs(
                  administrator(),
                  DEFINITIONS + "/levels/by-ids?ids={ids}",
                  csv(participated.infoLevelId(), participated.trainingLevelId(), 99_999L)),
              HttpStatus.OK);

      assertThat(listAt(body, "$[*].id"))
          .containsExactlyInAnyOrder(
              participated.infoLevelId().intValue(), participated.trainingLevelId().intValue());
      assertThat(listAt(body, "$[?(@.level_type=='INFO_LEVEL')].title"))
          .containsExactly("Info level 0");
      assertThat(listAt(body, "$[?(@.level_type=='TRAINING_LEVEL')].max_score"))
          .containsExactly(100);
      assertThat(listAt(body, "$[?(@.level_type=='TRAINING_LEVEL')].estimated_duration"))
          .containsExactly(7);
    }

    @Test
    @DisplayName("a trainee with a run in an instance of the level's definition receives the level")
    void findLevelsByIds_callerHasRunInInstance_returnsLevel() {
      stubCaller(TRAINEE_REF_ID);

      DocumentContext body =
          respond(
              getAs(
                  trainee(),
                  DEFINITIONS + "/levels/by-ids?ids={ids}",
                  participated.accessLevelId()),
              HttpStatus.OK);

      assertThat(listAt(body, "$[*].level_type")).containsExactly("ACCESS_LEVEL");
    }

    @Test
    @DisplayName("a caller missing from the definition of a named level is refused")
    void findLevelsByIds_callerNotInLevelDefinition_returnsForbidden() {
      stubCaller(TRAINEE_REF_ID);

      expectError(
          getAs(
              trainee(),
              DEFINITIONS + "/levels/by-ids?ids={ids}",
              csv(participated.infoLevelId(), unrelated.infoLevelId())),
          HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("missing level ids are refused with status 400")
    void findLevelsByIds_idsMissing_returnsBadRequest() {
      expectError(getAs(administrator(), DEFINITIONS + "/levels/by-ids"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("an administrator receives the named hints and skips unknown ids")
    void findHintsByIds_callerIsAdministrator_returnsKnownHints() {
      DocumentContext body =
          respond(
              getAs(
                  administrator(),
                  DEFINITIONS + "/hints/by-ids?ids={ids}",
                  csv(hintIds.get(0), hintIds.get(1), 99_999L)),
              HttpStatus.OK);

      assertThat(listAt(body, "$[*].title")).containsExactlyInAnyOrder("First hint", "Second hint");
      assertThat(listAt(body, "$[*].hint_penalty")).containsExactly(5, 5);
      assertThat(listAt(body, "$[*].id"))
          .containsExactlyInAnyOrder(hintIds.get(0).intValue(), hintIds.get(1).intValue());
    }

    @Test
    @DisplayName("a trainee with a run in an instance of the hint's definition receives the hint")
    void findHintsByIds_callerHasRunInInstance_returnsHint() {
      stubCaller(TRAINEE_REF_ID);

      DocumentContext body =
          respond(
              getAs(trainee(), DEFINITIONS + "/hints/by-ids?ids={ids}", hintIds.get(0)),
              HttpStatus.OK);

      assertThat(listAt(body, "$[*].id")).containsExactly(hintIds.get(0).intValue());
    }

    @Test
    @DisplayName("a caller missing from the definition of a named hint is refused")
    void findHintsByIds_callerNotInHintDefinition_returnsForbidden() {
      Long unrelatedHintLevel = seeder.trainingLevelWithHints(unrelated.id(), 4, "Hidden hint");
      stubCaller(TRAINEE_REF_ID);

      expectError(
          getAs(
              trainee(),
              DEFINITIONS + "/hints/by-ids?ids={ids}",
              csv(hintIds.get(0), seeder.hintIds(unrelatedHintLevel).get(0))),
          HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("missing hint ids are refused with status 400")
    void findHintsByIds_idsMissing_returnsBadRequest() {
      expectError(getAs(administrator(), DEFINITIONS + "/hints/by-ids"), HttpStatus.BAD_REQUEST);
    }
  }
}
