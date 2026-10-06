package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.opensearch.config.ObjectMapperConfigOpenSearch;
import cz.cyberrange.platform.training.rest.config.OpenApiConfiguration;
import cz.cyberrange.platform.training.rest.controllers.CheatingDetectionsRestController;
import cz.cyberrange.platform.training.rest.controllers.ExportImportRestController;
import cz.cyberrange.platform.training.rest.controllers.TrainingDefinitionsRestController;
import cz.cyberrange.platform.training.rest.controllers.TrainingInstancesRestController;
import cz.cyberrange.platform.training.rest.controllers.TrainingRunsRestController;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Boots every training-rest controller together with springdoc's auto-configuration and regenerates
 * the checked-in OpenAPI document from the resulting application context.
 */
@SpringBootTest(
    classes = {
      CheatingDetectionsRestController.class,
      ExportImportRestController.class,
      TrainingDefinitionsRestController.class,
      TrainingInstancesRestController.class,
      TrainingRunsRestController.class,
      OpenApiConfiguration.class,
      ObjectMapperConfigOpenSearch.class,
      OpenApiDocumentGenerationIT.SupplementaryBeansTestConfiguration.class,
      IntegrationTestApplication.class
    })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc(addFilters = false)
class OpenApiDocumentGenerationIT {

  private static final Path OUTPUT_FILE =
      Paths.get(
              System.getProperty("user.dir"), "..", "doc-files", "training-rest-swagger-docs.yaml")
          .normalize();

  @Autowired private MockMvc mockMvc;

  /**
   * Requests the YAML form of the runtime-generated OpenAPI document and overwrites the checked-in
   * copy consumed by other projects.
   *
   * @throws Exception when the document request fails or the file cannot be written
   */
  @Test
  void regeneratesOpenApiDocument() throws Exception {
    String document =
        mockMvc
            .perform(get("/v3/api-docs.yaml"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

    Files.writeString(OUTPUT_FILE, document, StandardCharsets.UTF_8);
  }

  /**
   * Supplies the OpenSearch event query services and the default-level startup loader that facades
   * under test depend on but the shared test configuration does not scan, alongside the {@link
   * OpenSearchClient} the event query services are built from.
   */
  @Configuration
  @ComponentScan(
      basePackages = {
        "cz.cyberrange.platform.training.opensearch.events.commands.query",
        "cz.cyberrange.platform.training.opensearch.events.training.query",
        "cz.cyberrange.platform.training.service.startup"
      })
  static class SupplementaryBeansTestConfiguration {

    /**
     * Builds the OpenSearch client the event query services issue their searches through.
     *
     * @param transport the transport the test context registers for OpenSearch
     * @return the client wired to the given transport
     */
    @Bean
    OpenSearchClient openSearchClient(
        @Qualifier("openSearchTransport") OpenSearchTransport transport) {
      return new OpenSearchClient(transport);
    }
  }
}
