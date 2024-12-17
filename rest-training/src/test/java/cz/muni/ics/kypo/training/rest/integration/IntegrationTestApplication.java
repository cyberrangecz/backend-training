package cz.muni.ics.kypo.training.rest.integration;

import cz.muni.ics.kypo.training.rest.integration.config.RestConfigTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RestConfigTest.class)
public class IntegrationTestApplication {
}
