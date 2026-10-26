package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exercises the {@code managed} flag of training instances through the secured application: how it
 * is reported, and how an update keeps, changes or refuses to change it.
 */
@Transactional
class TrainingInstanceManagedIT extends AbstractIntegrationTest {

  private static final String BASE_PATH = "/training-instances";
  private static final DateTimeFormatter ISO_UTC =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final String EMPTY_SEARCH_RESPONSE =
      "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,"
          + "\"failed\":0},\"hits\":{\"total\":{\"value\":0,\"relation\":\"eq\"},"
          + "\"max_score\":null,\"hits\":[]}}";
  private static final String RUNNING_INSTANCE_MANAGED_CHANGE_REASON =
      "Whether the running or finished instance is managed cannot be changed. Only title and end"
          + " time can be updated.";
  private static final long ORGANIZER_REF_ID = 1000L;

  private final JsonMapper jsonMapper = new JsonMapper();

  @Autowired private TrainingInstanceDataSeeder seeder;
  @Autowired private TrainingInstanceRepository trainingInstanceRepository;
  @Autowired private EntityManager entityManager;

  private TrainingInstance managedFutureInstance;
  private TrainingInstance unmanagedFutureInstance;
  private TrainingInstance managedRunningInstance;

  @BeforeEach
  void seedInstances() {
    UserRef organizer = seeder.userRef(ORGANIZER_REF_ID);
    TrainingDefinition definition = seeder.definition("Seed definition");
    managedFutureInstance =
        seeder.instance(
            definition,
            "Managed future instance",
            TrainingInstanceDataSeeder.now().plusDays(10),
            TrainingInstanceDataSeeder.now().plusDays(11),
            "managed-1234",
            null,
            organizer);
    unmanagedFutureInstance =
        seeder.instance(
            definition,
            "Unmanaged future instance",
            TrainingInstanceDataSeeder.now().plusDays(10),
            TrainingInstanceDataSeeder.now().plusDays(11),
            "unmanaged-1234",
            null,
            organizer);
    unmanagedFutureInstance.setManaged(false);
    trainingInstanceRepository.save(unmanagedFutureInstance);
    managedRunningInstance =
        seeder.instance(
            definition,
            "Managed running instance",
            TrainingInstanceDataSeeder.now().minusDays(1),
            TrainingInstanceDataSeeder.now().plusDays(1),
            "running-1234",
            null,
            organizer);
    stubLoggedInUser(ORGANIZER_REF_ID);
    externalServices.stubFor(
        WireMock.post(urlPathMatching("/.*_search")).willReturn(okJson(EMPTY_SEARCH_RESPONSE)));
  }

  @Test
  @DisplayName("findAllTrainingInstances reports the managed flag of every instance")
  void findAllTrainingInstances_instancesOfBothKinds_reportsManagedFlag() throws Exception {
    stubLoggedInUser(DEFAULT_USER_REF_ID);

    perform(
            get(BASE_PATH).param("size", "100"),
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(
            jsonPath("$.content[?(@.id == %d)].managed", managedFutureInstance.getId())
                .value(true))
        .andExpect(
            jsonPath("$.content[?(@.id == %d)].managed", unmanagedFutureInstance.getId())
                .value(false));
  }

  @Test
  @DisplayName("findTrainingInstanceById reports the managed flag of the instance")
  void findTrainingInstanceById_instancesOfBothKinds_reportsManagedFlag() throws Exception {
    perform(
            get(BASE_PATH + "/{id}", managedFutureInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.managed").value(true));
    perform(
            get(BASE_PATH + "/{id}", unmanagedFutureInstance.getId()),
            RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.managed").value(false));
  }

  @Test
  @DisplayName("updateTrainingInstance without managed keeps the flag of a future instance")
  void updateTrainingInstance_withoutManagedOnFutureInstance_keepsManaged() throws Exception {
    perform(put(BASE_PATH), updateBody(managedFutureInstance, null))
        .andExpect(status().isOk());

    assertThat(storedManaged(managedFutureInstance)).isTrue();
  }

  @Test
  @DisplayName("updateTrainingInstance without managed keeps the flag of a running instance")
  void updateTrainingInstance_withoutManagedOnRunningInstance_keepsManaged() throws Exception {
    perform(put(BASE_PATH), updateBody(managedRunningInstance, null))
        .andExpect(status().isOk());

    assertThat(storedManaged(managedRunningInstance)).isTrue();
  }

  @Test
  @DisplayName("updateTrainingInstance with another managed value changes a future instance")
  void updateTrainingInstance_changedManagedOnFutureInstance_changesManaged() throws Exception {
    perform(put(BASE_PATH), updateBody(managedFutureInstance, false))
        .andExpect(status().isOk());

    assertThat(storedManaged(managedFutureInstance)).isFalse();
  }

  @Test
  @DisplayName("updateTrainingInstance with the same managed value on a running instance succeeds")
  void updateTrainingInstance_sameManagedOnRunningInstance_isAccepted() throws Exception {
    perform(put(BASE_PATH), updateBody(managedRunningInstance, true))
        .andExpect(status().isOk());

    assertThat(storedManaged(managedRunningInstance)).isTrue();
  }

  @Test
  @DisplayName("updateTrainingInstance with another managed value on a running instance conflicts")
  void updateTrainingInstance_changedManagedOnRunningInstance_returnsConflict() throws Exception {
    perform(put(BASE_PATH), updateBody(managedRunningInstance, false))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value("CONFLICT"))
        .andExpect(jsonPath("$.entity_error_detail.entity").value("TrainingInstance"))
        .andExpect(
            jsonPath("$.entity_error_detail.identifier_value")
                .value(managedRunningInstance.getId().intValue()))
        .andExpect(
            jsonPath("$.entity_error_detail.reason").value(RUNNING_INSTANCE_MANAGED_CHANGE_REASON));

    assertThat(storedManaged(managedRunningInstance)).isTrue();
  }

  @Test
  @DisplayName("An update request omitting managed deserializes it to null")
  void updateRequest_omittingManaged_deserializesToNull() throws Exception {
    String requestBody = jsonMapper.writeValueAsString(updateBody(managedFutureInstance, null));

    TrainingInstanceUpdateDTO parsed =
        jsonMapper.readValue(requestBody, TrainingInstanceUpdateDTO.class);

    assertThat(parsed.getManaged()).isNull();
  }

  private Map<String, Object> updateBody(TrainingInstance instance, Boolean managed) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", instance.getId());
    body.put("title", instance.getTitle());
    String accessToken = instance.getAccessToken();
    body.put("access_token", accessToken.substring(0, accessToken.lastIndexOf('-')));
    body.put("start_time", isoUtc(instance.getStartTime()));
    body.put("end_time", isoUtc(instance.getEndTime()));
    body.put("training_definition_id", instance.getTrainingDefinition().getId());
    body.put("local_environment", instance.isLocalEnvironment());
    body.put("show_stepper_bar", true);
    body.put("backward_mode", false);
    if (managed != null) {
      body.put("managed", managed);
    }
    return body;
  }

  private ResultActions perform(MockHttpServletRequestBuilder request, RoleTypeSecurity role)
      throws Exception {
    return mockMvc.perform(request.with(callerWithRoles(role)));
  }

  private ResultActions perform(
      MockHttpServletRequestBuilder request, Map<String, Object> jsonBody)
      throws Exception {
    return perform(
        request
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(jsonBody)),
        RoleTypeSecurity.ROLE_TRAINING_ORGANIZER);
  }

  private boolean storedManaged(TrainingInstance instance) {
    entityManager.flush();
    entityManager.clear();
    return trainingInstanceRepository.findById(instance.getId()).orElseThrow().isManaged();
  }

  private static String isoUtc(LocalDateTime dateTime) {
    return ISO_UTC.format(dateTime);
  }
}
