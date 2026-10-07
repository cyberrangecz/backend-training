package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Regenerates the checked-in OpenAPI document from the running application, so the document
 * describes exactly the controllers and configuration the service ships with.
 */
class OpenApiDocumentGenerationIT extends AbstractIntegrationTest {

  private static final Path OUTPUT_FILE =
      Paths.get(
              System.getProperty("user.dir"), "..", "doc-files", "training-rest-swagger-docs.yaml")
          .normalize();

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
            .perform(get("/v3/api-docs.yaml").with(callerWithRoles()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

    Files.writeString(OUTPUT_FILE, document, StandardCharsets.UTF_8);
  }
}
