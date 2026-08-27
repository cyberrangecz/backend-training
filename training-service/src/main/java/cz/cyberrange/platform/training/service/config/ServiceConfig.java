package cz.cyberrange.platform.training.service.config;

import cz.cyberrange.platform.commons.security.config.ResourceServerSecurityConfig;
import cz.cyberrange.platform.commons.startup.config.MicroserviceRegistrationConfiguration;
import cz.cyberrange.platform.training.opensearch.config.OpenSearchServiceConfig;
import cz.cyberrange.platform.training.persistence.config.PersistenceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Assembles the service layer: it registers the services, this configuration package and the
 * startup components, enables asynchronous method execution, and pulls in persistence, audit
 * storage, security and the external service clients.
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
@Import({
  MicroserviceRegistrationConfiguration.class,
  OpenSearchServiceConfig.class,
  PersistenceConfig.class,
  ResourceServerSecurityConfig.class,
  WebClientConfig.class
})
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.service.services",
      "cz.cyberrange.platform.training.service.config",
      "cz.cyberrange.platform.training.service.startup"
    })
public class ServiceConfig {

  private Environment env;

  @Autowired
  public ServiceConfig(Environment env) {
    this.env = env;
  }

  /**
   * Switches the security context to be inherited by a thread from the thread that started it, so
   * that a method running asynchronously still sees the caller's authentication instead of none.
   *
   * @return the bean whose creation performs that switch
   */
  @Bean
  public MethodInvokingFactoryBean methodInvokingFactoryBean() {
    MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
    methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
    methodInvokingFactoryBean.setTargetMethod("setStrategyName");
    methodInvokingFactoryBean.setArguments(
        (Object[]) new String[] {SecurityContextHolder.MODE_INHERITABLETHREADLOCAL});
    return methodInvokingFactoryBean;
  }
}
