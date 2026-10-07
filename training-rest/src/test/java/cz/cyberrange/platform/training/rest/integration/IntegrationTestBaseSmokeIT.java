package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Checks that the integration test base starts a migrated, secured and reachable application. */
class IntegrationTestBaseSmokeIT extends AbstractIntegrationTest {

  private static final String TRAINING_DEFINITIONS_PATH = "/training-definitions";

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void flywayAppliesEveryMigration() {
    List<String> appliedVersions =
        jdbcTemplate.queryForList(
            "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank",
            String.class);

    assertThat(appliedVersions).containsExactly("1.0.0", "3.0.0");
  }

  @Test
  void authenticatedRequestReachesController() throws Exception {
    mockMvc
        .perform(
            get(TRAINING_DEFINITIONS_PATH)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());

    externalServices.verify(
        getRequestedFor(urlPathEqualTo(LOGGED_IN_USER_PATH))
            .withHeader("Authorization", equalTo("Bearer token")));
  }

  @Test
  void unauthenticatedRequestIsRejected() throws Exception {
    mockMvc.perform(get(TRAINING_DEFINITIONS_PATH)).andExpect(status().isUnauthorized());
  }
}
