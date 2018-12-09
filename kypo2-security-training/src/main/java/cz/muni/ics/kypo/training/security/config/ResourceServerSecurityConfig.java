package cz.muni.ics.kypo.training.security.config;

import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.mitre.oauth2.introspectingfilter.service.impl.StaticIntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Pavel Seda (441048) & Dominik Pilar
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.security"})
@PropertySource("file:${path.to.config.file}")
public class ResourceServerSecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Profile("PROD")
    @Component
    public class ProductionResourceServiceSecurityConfig extends ResourceServerConfigurerAdapter {

        @Value("${kypo.idp.4oauth.introspectionURI}")
        private String introspectionURI;
        @Value("${kypo.idp.4oauth.resource.clientId}")
        private String clientIdOfResource;
        @Value("${kypo.idp.4oauth.resource.clientSecret}")
        private String clientSecretResource;
        @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
        private Set<String> scopes;
        @Autowired
        private CustomAuthorityGranter.ProductionCustomAuthorityGranter customAuthorityGranter;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.tokenServices(tokenServices());
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .cors()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs/**", "/webjars/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated();
        }

        @Bean
        public ResourceServerTokenServices tokenServices() {
            IntrospectingTokenService tokenService = new IntrospectingTokenService();
            tokenService.setIntrospectionConfigurationService(introspectionConfigurationService());
            tokenService.setIntrospectionAuthorityGranter(customAuthorityGranter);
            return tokenService;
        }

        @Bean
        public StaticIntrospectionConfigurationService introspectionConfigurationService() {
            StaticIntrospectionConfigurationService introspectionService = new StaticIntrospectionConfigurationService();
            introspectionService.setIntrospectionUrl(introspectionURI);

            RegisteredClient client = new RegisteredClient();
            client.setClientId(clientIdOfResource);
            client.setClientSecret(clientSecretResource);
            client.setScope(scopes);
            introspectionService.setClientConfiguration(client);

            return introspectionService;
        }
    }

    @Profile("DEV")
    @Component
    public class DevResourceServiceSecurityConfig extends ResourceServerConfigurerAdapter {

        @Value("${kypo.idp.4oauth.introspectionURI}")
        private String introspectionURI;
        @Value("${kypo.idp.4oauth.resource.clientId}")
        private String clientIdOfResource;
        @Value("${kypo.idp.4oauth.resource.clientSecret}")
        private String clientSecretResource;
        @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
        private Set<String> scopes;
        @Autowired
        private CustomAuthorityGranter.DevCustomAuthorityGranter customAuthorityGranter;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.tokenServices(tokenServices());
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .cors()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs/**", "/webjars/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated();
        }

        @Bean
        public ResourceServerTokenServices tokenServices() {
            IntrospectingTokenService tokenService = new IntrospectingTokenService();
            tokenService.setIntrospectionConfigurationService(introspectionConfigurationService());
            tokenService.setIntrospectionAuthorityGranter(customAuthorityGranter);
            return tokenService;
        }

        @Bean
        public StaticIntrospectionConfigurationService introspectionConfigurationService() {
            StaticIntrospectionConfigurationService introspectionService = new StaticIntrospectionConfigurationService();
            introspectionService.setIntrospectionUrl(introspectionURI);

            RegisteredClient client = new RegisteredClient();
            client.setClientId(clientIdOfResource);
            client.setClientSecret(clientSecretResource);
            client.setScope(scopes);
            introspectionService.setClientConfiguration(client);

            return introspectionService;
        }
    }
}
