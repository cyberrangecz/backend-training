package cz.muni.csirt.kypo.elasticsearch.data.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Install this project with following command (create kypo2_config.properties in /etc:
 * <p>
 * mvn clean install -Dpath.to.db.config.file=/etc/kypo2_config.properties
 *
 * @author Pavel Šeda
 */
@Configuration
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.data"})
public class ElasticsearchDataConfig {

    @Value("${elasticsearch.ipaddress}")
    private String ipaddress;
    @Value("${elasticsearch.protocol}")
    private String protocol;
    @Value("${elasticsearch.port}")
    private int port;

    @Primary
    @Bean
    public RestClient lowLevelClient() {
        return RestClient
                .builder(new HttpHost(ipaddress, port, protocol))
                .build();
    }

    @Bean
    public RestHighLevelClient highLevelClient() {
        return new RestHighLevelClient(lowLevelClient());
    }

    // To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        PropertySourcesPlaceholderConfigurer confPropertyPlaceholder = new PropertySourcesPlaceholderConfigurer();
        confPropertyPlaceholder.setIgnoreUnresolvablePlaceholders(true);
        return confPropertyPlaceholder;
    }
}
