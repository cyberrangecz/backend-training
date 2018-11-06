package cz.muni.csirt.kypo.elasticsearch.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cz.muni.csirt.kypo.elasticsearch.data.config.ElasticsearchDataConfig;

/**
 * @author Pavel Šeda
 */
@Configuration
@Import({ElasticsearchDataConfig.class})
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.service"})
public class TrainingElasticsearchServiceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingElasticsearchServiceConfig.class);

}
