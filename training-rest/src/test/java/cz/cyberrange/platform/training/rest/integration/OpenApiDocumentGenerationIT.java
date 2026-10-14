package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Value;

/**
 * Writes the OpenAPI document of the running application to the directory given by the {@code
 * docs.output.directory} system property, which the {@code docs} Maven profile sets. Skipped in
 * regular test runs.
 */
@EnabledIfSystemProperty(named = "docs.output.directory", matches = ".+")
class OpenApiDocumentGenerationIT extends AbstractIntegrationTest {

  private static final String FILE_NAME = "training-rest-swagger-docs.yaml";

  @Value("${server.servlet.context-path}")
  private String contextPath;

  /**
   * Requests the YAML form of the runtime-generated OpenAPI document and writes it into the output
   * directory.
   *
   * @throws Exception when the document request fails or the file cannot be written
   */
  @Test
  void writesOpenApiDocument() throws Exception {
    String document =
        mockMvc
            .perform(
                get(contextPath + "/v3/api-docs.yaml")
                    .contextPath(contextPath)
                    .with(callerWithRoles()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

    Path directory = Paths.get(System.getProperty("docs.output.directory"));
    Files.createDirectories(directory);
    Files.writeString(directory.resolve(FILE_NAME), document, StandardCharsets.UTF_8);
  }
}
