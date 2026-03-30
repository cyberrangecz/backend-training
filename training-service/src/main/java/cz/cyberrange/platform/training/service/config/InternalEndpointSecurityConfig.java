package cz.cyberrange.platform.training.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Permits unauthenticated access to internal service-to-service endpoints (e.g. single-sandbox cleanup
 * pool-ids). These paths are protected by internal checks (e.g. X-Internal-Secret when configured)
 * and must not require JWT so that sandbox-service can call them without user context.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InternalEndpointSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers().antMatchers(
                        "/training-instances/internal/**",
                        "/training/api/v1/training-instances/internal/**")
                .and()
                .authorizeRequests().anyRequest().permitAll();
    }
}
