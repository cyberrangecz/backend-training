package cz.cyberrange.platform.training.elasticsearch.service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * The type Elasticsearch service config.
 */
@Configuration
@Import(ObjectMapperConfigElasticsearch.class)
@ComponentScan(basePackages = {"cz.cyberrange.platform.training.elasticsearch.service"})
public class ElasticsearchServiceConfig {


}
