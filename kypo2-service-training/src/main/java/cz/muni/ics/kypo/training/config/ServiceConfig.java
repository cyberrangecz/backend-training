package cz.muni.ics.kypo.training.config;

import cz.muni.ics.kypo.commons.config.WebConfigRestSecurityCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cz.muni.csirt.kypo.elasticsearch.service.audit.config.TrainingElasticsearchServiceConfig;

/**
 * @author Pavel Šeda
 *
 */
@Configuration
@Import({TrainingElasticsearchServiceConfig.class, WebConfigRestSecurityCommons.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class ServiceConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceConfig.class);

}
