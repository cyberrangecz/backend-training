package cz.cyberrange.platform.training.opensearch.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Configuration class joining all OpenSearch related configurations and component scans */
@Configuration
@Import({ObjectMapperConfigOpenSearch.class, RestClientConfigOpenSearch.class})
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.opensearch",
    })
public class OpenSearchServiceConfig {

  /**
   * Creates the {@link OpenSearchClient} sending its requests through the given transport.
   *
   * @param transport the {@link OpenSearchTransport} carrying requests to the OpenSearch node
   * @return a configured {@link OpenSearchClient}
   */
  @Bean
  public OpenSearchClient openSearchClient(
      @Qualifier("openSearchTransport") OpenSearchTransport transport) {
    return new OpenSearchClient(transport);
  }
}
