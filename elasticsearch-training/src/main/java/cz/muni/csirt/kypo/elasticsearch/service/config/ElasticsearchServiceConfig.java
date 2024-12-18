package cz.muni.csirt.kypo.elasticsearch.service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * The type Elasticsearch service config.
 */
@Configuration
@Import(ObjectMapperConfigElasticsearch.class)
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.service"})
public class ElasticsearchServiceConfig {


}
