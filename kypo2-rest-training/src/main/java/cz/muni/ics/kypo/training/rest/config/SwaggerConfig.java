package cz.muni.ics.kypo.training.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;
import java.util.Set;

/**
 * Common configuration of Swagger for all projects that import this project.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConfig.class);
    private static final String NAME_OF_TOKEN = "bearer";
    private static final String NAME_OF_SECURITY_SCHEME = "KYPO";
    @Value("#{'${kypo.idp.4oauth.authorizationURIs}'.split(',')}")
    private List<String> authorizationURIs;
    @Value("#{'${kypo.idp.4oauth.client.clientIds}'.split(',')}")
    private List<String> clientIds;
    @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
    private Set<String> scopes;
    @Value("${swagger.enabled:true}")
    private boolean swaggerEnabled;

    /**
     * The Docket bean is configured to give more control over the API documentation generation process.
     *
     * @return the docket
     */
    @Bean
    public Docket api() {
        LOG.debug("SwaggerConfig -> api()");
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerEnabled)
                .groupName("public-api")
                .apiInfo(apiInfo()).useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(List.of(securityScheme()))
                .securityContexts(List.of(securityContext()));
    }

    private ApiInfo apiInfo() {
        LOG.debug("SwaggerConfig -> apiInfo()");
        return new ApiInfoBuilder()
                .title("REST API documentation")
                .description("Developed by CSIRT team")
                .termsOfServiceUrl("Licensed by CSIRT team")
                .build();
    }

    /**
     * Security configuration.
     *
     * @return the security configuration
     */
    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId(clientIds.get(0).trim())
                .scopeSeparator(" ")
                .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new ImplicitGrantBuilder()
                .loginEndpoint(new LoginEndpoint(authorizationURIs.get(0).trim()))
                .tokenName(NAME_OF_TOKEN)
                .build();

        return new OAuthBuilder().name(NAME_OF_SECURITY_SCHEME)
                .grantTypes(List.of(grantType))
                .scopes(List.of(scopes()))
                .build();
    }

    private AuthorizationScope[] scopes() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[scopes.size()];
        int i = 0;
        for (String scope : scopes) {
            authorizationScopes[i] = new AuthorizationScope(scope, "");
            i++;
        }
        return authorizationScopes;
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(List.of(new SecurityReference(NAME_OF_SECURITY_SCHEME, scopes())))
                .forPaths(PathSelectors.any())
                .build();
    }
}
