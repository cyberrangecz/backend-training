package cz.muni.csirt.kypo.elasticsearch.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cz.muni.csirt.kypo.elasticsearch.data.config.ElasticsearchDataConfig;

@Configuration
@Import({ElasticsearchDataConfig.class, ObjectMapperConfigElasticsearch.class})
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.service"})
public class ElasticsearchServiceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchServiceConfig.class);


}
