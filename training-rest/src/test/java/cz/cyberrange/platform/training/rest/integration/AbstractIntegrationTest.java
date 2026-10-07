package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.github.tomakehurst.wiremock.WireMockServer;
import cz.cyberrange.platform.training.rest.config.SpringBootRun;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Base of every integration test: the whole application against PostgreSQL migrated by Flyway and
 * validated by Hibernate, with external services answered by {@link #externalServices}. Every
 * subclass shares one cached context, so the container and the stub server start once per JVM. Each
 * test starts with only the logged-in user lookup stubbed, answering {@link #DEFAULT_USER_REF_ID}.
 */
@SpringBootTest(
    classes = SpringBootRun.class,
    properties = {
      "spring.flyway.enabled=true",
      "spring.jpa.hibernate.ddl-auto=validate",
      "server.port=8083",
      "microservice.name=training",
      "crczp.identity.providers[0].issuer=http://localhost/integration-test-issuer"
    })
@AutoConfigureMockMvc
@Import(IntegrationTestInfrastructure.class)
public abstract class AbstractIntegrationTest {

  protected static final long DEFAULT_USER_REF_ID = 1L;
  protected static final String LOGGED_IN_USER_PATH =
      IntegrationTestInfrastructure.USER_AND_GROUP_PATH + "/users/info";

  @Autowired protected MockMvc mockMvc;
  @Autowired protected WireMockServer externalServices;

  /** Clears the stubs and recorded requests of the previous test and stubs the default user. */
  @BeforeEach
  void resetExternalServices() {
    externalServices.resetAll();
    stubLoggedInUser(DEFAULT_USER_REF_ID);
  }

  /**
   * Makes user-and-group report the given user, named {@code User <userRefId>}, as the caller of
   * every subsequent request.
   *
   * @param userRefId cross-service id of the user to report
   */
  protected void stubLoggedInUser(long userRefId) {
    externalServices.stubFor(
        get(urlPathEqualTo(LOGGED_IN_USER_PATH))
            .willReturn(
                okJson(
                    "{\"user_ref_id\": %d, \"full_name\": \"User %d\"}"
                        .formatted(userRefId, userRefId))));
  }

  /**
   * Matches the rejection of a request carrying no bearer token: 401, or 403 when a state-changing
   * request is refused for lacking a CSRF token first.
   *
   * @return the matcher accepting either status
   */
  protected static ResultMatcher rejectedAsUnauthenticated() {
    return result ->
        assertThat(result.getResponse().getStatus())
            .isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
  }

  /**
   * Authenticates a request as a bearer token holder granted the given roles.
   *
   * @param roles the roles the caller holds
   * @return the post-processor placing that authentication on the request
   */
  protected static RequestPostProcessor callerWithRoles(RoleTypeSecurity... roles) {
    return jwt()
        .authorities(
            Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toArray(GrantedAuthority[]::new));
  }
}
