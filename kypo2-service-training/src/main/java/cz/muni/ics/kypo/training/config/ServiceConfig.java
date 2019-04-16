package cz.muni.ics.kypo.training.config;

import cz.muni.csirt.kypo.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * @author Pavel Šeda
 */
@Configuration
@EnableAsync
@Import({ElasticsearchServiceConfig.class, PersistenceConfig.class, ResourceServerSecurityConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class ServiceConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rT = new RestTemplate();
        rT.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return rT;
    }
}
