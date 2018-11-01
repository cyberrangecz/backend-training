package cz.muni.ics.kypo.training.config;


import cz.muni.csirt.kypo.elasticsearch.service.TrainingElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.rest.config.WebConfigRestSecurityCommons;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Pavel Šeda
 *
 */
@Configuration
@Import({TrainingElasticsearchServiceConfig.class, WebConfigRestSecurityCommons.class, PersistenceConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class ServiceConfig {
}
