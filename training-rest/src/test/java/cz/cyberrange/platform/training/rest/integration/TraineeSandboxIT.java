package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import cz.cyberrange.platform.training.api.responses.AllocationUnitInfo;
import cz.cyberrange.platform.training.api.responses.SandboxRequestInfo;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exercises the endpoints through which a trainee lists and removes the sandboxes they allocated
 * for themselves, with the sandbox service answered by the stub server.
 */
class TraineeSandboxIT extends AbstractIntegrationTest {

  private static final String SANDBOX = IntegrationTestInfrastructure.SANDBOX_SERVICE_PATH;
  private static final String BY_CREATOR_PATH = SANDBOX + "/sandbox-allocation-units/by-creator";
  private static final String USER_SANDBOXES_PATH = "/training-runs/user-sandboxes";
  private static final String READY_SANDBOX_UUID = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80";
  private static final int READY_UNIT_ID = 7;
  private static final int BUILDING_UNIT_ID = 8;
  private static final int FOREIGN_UNIT_ID = 99;

  private final JsonMapper jsonMapper = new JsonMapper();

  private final AllocationUnitInfo readyUnit =
      unitOf(READY_UNIT_ID, READY_SANDBOX_UUID, List.of("FINISHED", "FINISHED", "FINISHED"));
  private final AllocationUnitInfo buildingUnit =
      unitOf(BUILDING_UNIT_ID, null, List.of("FINISHED", "RUNNING", "IN_QUEUE"));

  @Test
  @DisplayName("getUserActiveSandboxes lists the own sandboxes with their removal permission")
  void getUserActiveSandboxes_ownSandboxes_listsThemWithRemovalPermission() throws Exception {
    stubOwnUnits(readyUnit, buildingUnit);

    perform(get("/training-runs/user-active-sandboxes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(READY_UNIT_ID))
        .andExpect(jsonPath("$[0].sandbox_id").value(READY_SANDBOX_UUID))
        .andExpect(jsonPath("$[0].allow_remove").value(true))
        .andExpect(jsonPath("$[1].id").value(BUILDING_UNIT_ID))
        .andExpect(jsonPath("$[1].allow_remove").value(false));
  }

  @Test
  @DisplayName("getUserActiveSandboxes without sandboxes is empty")
  void getUserActiveSandboxes_withoutSandboxes_isEmpty() throws Exception {
    stubOwnUnits();

    perform(get("/training-runs/user-active-sandboxes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("cleanup by allocation removes a ready sandbox")
  void cleanupByAllocation_readySandbox_isNoContent() throws Exception {
    stubOwnUnits(readyUnit);
    stubCleanup(READY_UNIT_ID, 204);

    perform(post(USER_SANDBOXES_PATH + "/by-allocation/{unitId}/cleanup", READY_UNIT_ID))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("cleanup by sandbox id removes a ready sandbox")
  void cleanupBySandboxId_readySandbox_isNoContent() throws Exception {
    stubOwnUnits(readyUnit);
    stubCleanup(READY_UNIT_ID, 204);

    perform(post(USER_SANDBOXES_PATH + "/{sandboxId}/cleanup", READY_SANDBOX_UUID))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("cleanup by allocation of a foreign unit is forbidden")
  void cleanupByAllocation_foreignUnit_isForbidden() throws Exception {
    stubOwnUnits(readyUnit);

    perform(post(USER_SANDBOXES_PATH + "/by-allocation/{unitId}/cleanup", FOREIGN_UNIT_ID))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value("FORBIDDEN"));
  }

  @Test
  @DisplayName("cleanup by sandbox id of a foreign sandbox is forbidden")
  void cleanupBySandboxId_foreignSandbox_isForbidden() throws Exception {
    stubOwnUnits(readyUnit);

    perform(
            post(
                USER_SANDBOXES_PATH + "/{sandboxId}/cleanup",
                "00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("cleanup while the first stage runs is a bad request")
  void cleanupByAllocation_firstStageRunning_isBadRequest() throws Exception {
    stubOwnUnits(buildingUnit);
    externalServices.stubFor(
        WireMock.post(cleanupPath(BUILDING_UNIT_ID))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(
                        "{\"detail\":\"Cleanup while the first stage is running is not"
                            + " allowed.\"}")));

    perform(post(USER_SANDBOXES_PATH + "/by-allocation/{unitId}/cleanup", BUILDING_UNIT_ID))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
  }

  @Test
  @DisplayName("cleanup of an already removed unit is no content")
  void cleanupByAllocation_alreadyRemovedUnit_isNoContent() throws Exception {
    stubOwnUnits(readyUnit);
    externalServices.stubFor(
        WireMock.post(cleanupPath(READY_UNIT_ID))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"detail\":\"No such unit.\"}")));

    perform(post(USER_SANDBOXES_PATH + "/by-allocation/{unitId}/cleanup", READY_UNIT_ID))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("cleanup with a non-numeric allocation id is a bad request")
  void cleanupByAllocation_nonNumericAllocationId_isBadRequest() throws Exception {
    perform(post(USER_SANDBOXES_PATH + "/by-allocation/{unitId}/cleanup", "abc"))
        .andExpect(status().isBadRequest());
  }

  private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request.with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_TRAINEE)));
  }

  private void stubOwnUnits(AllocationUnitInfo... units) throws Exception {
    externalServices.stubFor(
        WireMock.get(urlPathEqualTo(BY_CREATOR_PATH))
            .willReturn(okJson(jsonMapper.writeValueAsString(List.of(units)))));
  }

  private void stubCleanup(int unitId, int responseStatus) {
    externalServices.stubFor(
        WireMock.post(cleanupPath(unitId)).willReturn(aResponse().withStatus(responseStatus)));
  }

  private static UrlPathPattern cleanupPath(int unitId) {
    return urlPathEqualTo(SANDBOX + "/sandbox-allocation-units/" + unitId + "/cleanup-request");
  }

  private static AllocationUnitInfo unitOf(int id, String sandboxId, List<String> stages) {
    SandboxRequestInfo allocationRequest = new SandboxRequestInfo();
    allocationRequest.setId(id);
    allocationRequest.setAllocationUnitId(id);
    allocationRequest.setStages(stages);

    AllocationUnitInfo unit = new AllocationUnitInfo();
    unit.setId(id);
    unit.setPoolId(1L);
    unit.setSandboxId(sandboxId);
    unit.setAllocationRequest(allocationRequest);
    return unit;
  }
}
