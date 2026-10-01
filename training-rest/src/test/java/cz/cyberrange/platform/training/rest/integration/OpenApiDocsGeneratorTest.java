package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.rest.config.OpenApiConfiguration;
import cz.cyberrange.platform.training.rest.config.WebConfigRestTraining;
import cz.cyberrange.platform.training.service.config.ObjectMappersConfiguration;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import cz.cyberrange.platform.training.service.facade.TrainingInstanceFacade;
import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import cz.cyberrange.platform.training.service.facade.detection.CheatingDetectionExportFacade;
import cz.cyberrange.platform.training.service.facade.detection.CheatingDetectionFacade;
import cz.cyberrange.platform.training.service.facade.detection.DetectionEventFacade;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Writes the springdoc OpenAPI document to the directory given by the {@code docs.output.directory}
 * system property, which the {@code docs} Maven profile sets. Skipped in regular test runs. Only
 * the web layer is started, with the facades mocked, as documenting the API does not call them.
 */
@SpringBootTest(classes = OpenApiDocsGeneratorTest.DocsApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@EnabledIfSystemProperty(named = "docs.output.directory", matches = ".+")
class OpenApiDocsGeneratorTest {

  private static final String FILE_NAME = "training-rest-swagger-docs.yaml";

  @MockBean private CheatingDetectionFacade cheatingDetectionFacade;
  @MockBean private DetectionEventFacade detectionEventFacade;
  @MockBean private CheatingDetectionExportFacade cheatingDetectionExportFacade;
  @MockBean private ExportImportFacade exportImportFacade;
  @MockBean private TrainingDefinitionFacade trainingDefinitionFacade;
  @MockBean private TrainingInstanceFacade trainingInstanceFacade;
  @MockBean private TrainingRunFacade trainingRunFacade;

  @Autowired private MockMvc mvc;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Test
  void generateOpenApiDocs() throws Exception {
    String yaml =
        mvc.perform(get(contextPath + "/v3/api-docs.yaml").contextPath(contextPath))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
    Path directory = Paths.get(System.getProperty("docs.output.directory"));
    Files.createDirectories(directory);
    Files.writeString(directory.resolve(FILE_NAME), yaml, StandardCharsets.UTF_8);
  }

  /** Web layer of {@code SpringBootRun}, without the service layer behind the facades. */
  @SpringBootConfiguration
  @EnableAutoConfiguration
  @EnableSpringDataWebSupport
  @Import({
    ObjectMappersConfiguration.class,
    WebConfigRestTraining.class,
    OpenApiConfiguration.class
  })
  @ComponentScan(
      basePackages = {
        "cz.cyberrange.platform.training.rest.controllers",
        "cz.cyberrange.platform.training.rest.utils.error"
      },
      excludeFilters =
          @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
  static class DocsApplication {}
}
