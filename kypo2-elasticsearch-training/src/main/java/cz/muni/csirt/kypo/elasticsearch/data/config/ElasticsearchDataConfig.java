package cz.muni.csirt.kypo.elasticsearch.data.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 
 * Install this project with following command (create kypo2_config.properties in /etc:
 * 
 * mvn clean install -Dpath.to.db.config.file=/etc/kypo2_config.properties
 * 
 * @author Pavel Šeda
 *
 */
@Configuration
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.data"})
public class ElasticsearchDataConfig {

	@Value("${elasticsearch.ipaddress}")
	private String ipaddress;
	@Value("${elasticsearch.protocol}")
	private String protocol;
	@Value("${elasticsearch.port1}")
	private int esPort1;
	@Value("${elasticsearch.port2}")
	private int esPort2;

	@Primary
	@Bean
	public RestClient lowLevelClient() {
          return RestClient
                  .builder(new HttpHost(ipaddress, esPort1, protocol), new HttpHost(ipaddress, esPort2, protocol))
                  .build();
	}

	@Bean
	public RestHighLevelClient highLevelClient() {
		return new RestHighLevelClient(lowLevelClient());
	}

	@Bean(name = "objMapperESClient")
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	// To resolve ${} in @Value
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		PropertySourcesPlaceholderConfigurer confPropertyPlaceholder = new PropertySourcesPlaceholderConfigurer();
		confPropertyPlaceholder.setIgnoreUnresolvablePlaceholders(true);
		return confPropertyPlaceholder;
	}
}
