package cz.muni.csirt.kypo.elasticsearch.service.audit.config;

import cz.muni.csirt.kypo.elasticsearch.data.config.ElasticsearchDataConfig;
import cz.muni.csirt.kypo.elasticsearch.service.eventvalidation.config.CacheConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;



/**
 * @author Pavel Šeda
 *
 */
@Configuration
@Import({ElasticsearchDataConfig.class, CacheConfig.class})
@ComponentScan(basePackages = {"cz.muni.csirt.kypo.elasticsearch.service.audit", "cz.muni.csirt.kypo.elasticsearch.service.eventvalidation"})
public class TrainingElasticsearchServiceConfig {

}
