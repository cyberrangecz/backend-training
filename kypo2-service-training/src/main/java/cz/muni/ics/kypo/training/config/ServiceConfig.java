package cz.muni.ics.kypo.training.config;

import cz.muni.csirt.kypo.elasticsearch.service.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;

import cz.muni.ics.kypo.training.security.config.ResourceServerSecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Pavel Šeda
 */
@Configuration
@Import({ElasticsearchServiceConfig.class, PersistenceConfig.class, ResourceServerSecurityConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class ServiceConfig {
}
