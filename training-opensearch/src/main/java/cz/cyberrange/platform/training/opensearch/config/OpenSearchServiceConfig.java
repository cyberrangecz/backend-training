package cz.cyberrange.platform.training.opensearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Configuration class joining all OpenSearch related configurations and component scans. */
@Configuration
@Import({ObjectMapperConfigOpenSearch.class, RestClientConfigOpenSearch.class})
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.opensearch",
    })
public class OpenSearchServiceConfig {

  /**
   * Creates the {@link OpenSearchClient} using the provided {@link RestClient} and {@link
   * ObjectMapper}.
   *
   * @param restClient the low-level {@link RestClient} used for HTTP communication
   * @param mapper the {@link ObjectMapper} used by {@link JacksonJsonpMapper} for JSON
   *     serialization
   * @return a configured {@link OpenSearchClient}
   */
  @Bean
  public OpenSearchClient openSearchClient(
      @Qualifier("openSearchRestClient") RestClient restClient,
      @Qualifier("openSearchObjectMapper") ObjectMapper mapper) {
    return new OpenSearchClient(
        new RestClientTransport(restClient, new JacksonJsonpMapper(mapper)));
  }
}
