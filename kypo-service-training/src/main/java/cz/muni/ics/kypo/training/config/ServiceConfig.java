package cz.muni.ics.kypo.training.config;

import cz.muni.csirt.kypo.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.commons.startup.config.MicroserviceRegistrationConfiguration;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The type Service config.
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
@Import({MicroserviceRegistrationConfiguration.class,
        ElasticsearchServiceConfig.class,
        PersistenceConfig.class,
        ResourceServerSecurityConfig.class,
        WebClientConfig.class
})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service", "cz.muni.ics.kypo.training.config", "cz.muni.ics.kypo.training.startup"})
public class ServiceConfig {
    
    private Environment env;

    /**
     * Instantiates a new Service config.
     */
    @Autowired
    public ServiceConfig(Environment env) {
        this.env = env;
    }

    /**
     * This configuration is necessary for sharing SecurityContext between worker threads (to pass SecurityContext to the @Async methods.)
     *
     * @return method invoking factory bean
     */
    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments((Object[]) new String[]{SecurityContextHolder.MODE_INHERITABLETHREADLOCAL});
        return methodInvokingFactoryBean;
    }

}
