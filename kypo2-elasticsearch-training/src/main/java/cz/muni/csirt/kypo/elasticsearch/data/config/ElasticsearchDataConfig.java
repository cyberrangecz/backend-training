package cz.muni.csirt.kypo.elasticsearch.data.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The type Elasticsearch data config.
 */
@Configuration
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.data"})
public class ElasticsearchDataConfig {

    @Value("${elasticsearch.protocol}")
    private String protocol;
    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private int port;

    @Bean("kypoRestHighLevelClient")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, port, protocol)));
    }

}
