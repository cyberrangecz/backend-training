package cz.muni.csirt.kypo.elasticsearch.service.config;

import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cz.muni.csirt.kypo.elasticsearch.data.config.ElasticsearchDataConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.List;

/**
 * The type Elasticsearch service config.
 */
@Configuration
@Import({ElasticsearchDataConfig.class, ObjectMapperConfigElasticsearch.class})
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.service"})
public class ElasticsearchServiceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchServiceConfig.class);

    @Value("${elasticsearch.protocol:http}")
    private String elasticsearchProtocol;
    @Value("${elasticsearch.ipaddress:localhost}")
    private String elasticsearchIpAddress;
    @Value("${elasticsearch.port:9200}")
    private String elasticsearchPort;

    @Bean
    @Qualifier("elasticsearchWebClient")
    public WebClient elasticsearchWebClient() {
        return WebClient.builder()
                .baseUrl(elasticsearchProtocol + "://" + elasticsearchIpAddress + ":" + elasticsearchPort)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();
    }
}
