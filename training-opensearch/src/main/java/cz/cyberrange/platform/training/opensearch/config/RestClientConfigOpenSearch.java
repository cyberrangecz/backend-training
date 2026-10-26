package cz.cyberrange.platform.training.opensearch.config;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson3.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that creates the HTTP transport the OpenSearch client sends its requests
 * through
 */
@Configuration
public class RestClientConfigOpenSearch {

  @Value("${opensearch.protocol}")
  private String protocol;

  @Value("${opensearch.host}")
  private String host;

  @Value("${opensearch.port}")
  private int port;

  /**
   * Creates the transport to the configured OpenSearch node, reading and writing documents with the
   * given mapper.
   *
   * @param mapper the mapper OpenSearch documents are serialized with
   * @return a configured {@link OpenSearchTransport} instance
   */
  @Bean("openSearchTransport")
  public OpenSearchTransport openSearchTransport(
      @Qualifier("openSearchObjectMapper") tools.jackson.databind.ObjectMapper mapper) {
    return ApacheHttpClient5TransportBuilder.builder(new HttpHost(protocol, host, port))
        .setMapper(new JacksonJsonpMapper(mapper))
        .build();
  }
}
