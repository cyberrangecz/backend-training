package cz.muni.ics.kypo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cz.muni.csirt.kypo.elasticsearch.service.audit.config.TrainingElasticsearchServiceConfig;

@Configuration
@Import({PersistenceConfig.class, TrainingElasticsearchServiceConfig.class})
//@ComponentScan(basePackages = {"cz.muni.ics.kypo.service"})
public class ServiceConfig {

}
