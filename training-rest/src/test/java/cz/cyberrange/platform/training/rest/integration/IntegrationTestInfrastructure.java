package cz.cyberrange.platform.training.rest.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import cz.cyberrange.platform.training.persistence.FastPostgreSQLContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Supplies the external systems an integration test context runs against: a PostgreSQL container
 * the datasource connects to, and one HTTP stub server standing in for user-and-group, the sandbox
 * service, answers-storage and OpenSearch. Both live as long as the cached application context.
 */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestInfrastructure {

  private static final String MICROSERVICE_REGISTRATION_PATH = "/microservices";
  static final String USER_AND_GROUP_PATH = "/user-and-group";
  static final String SANDBOX_SERVICE_PATH = "/sandbox-service";
  static final String ANSWERS_STORAGE_PATH = "/answers-storage";

  /**
   * Starts the PostgreSQL container whose connection details replace the configured datasource.
   *
   * @return the container the application's datasource connects to
   */
  @Bean
  @ServiceConnection
  PostgreSQLContainer postgreSQLContainer() {
    return new FastPostgreSQLContainer();
  }

  /**
   * Starts the stub server on a free port, accepting the microservice registration the application
   * sends to user-and-group while starting.
   *
   * @return the running stub server
   */
  @Bean(destroyMethod = "stop")
  WireMockServer externalServicesStub() {
    WireMockServer server = new WireMockServer(options().dynamicPort());
    server.start();
    server.stubFor(
        post(urlPathEqualTo(USER_AND_GROUP_PATH + MICROSERVICE_REGISTRATION_PATH))
            .willReturn(aResponse().withStatus(201)));
    return server;
  }

  /**
   * Points the URI of every external service at the stub server, each under its own path prefix,
   * and the OpenSearch node at the stub server's root.
   *
   * @param externalServicesStub the stub server to point at
   * @return the registrar adding those properties
   */
  @Bean
  DynamicPropertyRegistrar externalServiceProperties(WireMockServer externalServicesStub) {
    return registry -> {
      registry.add(
          "user-and-group-server.uri", () -> externalServicesStub.baseUrl() + USER_AND_GROUP_PATH);
      registry.add(
          "sandbox-service.uri", () -> externalServicesStub.baseUrl() + SANDBOX_SERVICE_PATH);
      registry.add(
          "answers-storage.uri", () -> externalServicesStub.baseUrl() + ANSWERS_STORAGE_PATH);
      registry.add("opensearch.port", externalServicesStub::port);
    };
  }
}
