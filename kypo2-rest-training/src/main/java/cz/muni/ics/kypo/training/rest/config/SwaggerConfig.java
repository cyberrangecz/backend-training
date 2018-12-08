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

import java.util.Arrays;
import java.util.Set;

/**
 * @author Pavel Seda & Jan Duda
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${kypo.idp.4oauth.authorizationURI}")
    private String authorizationURI;

    @Value("${kypo.idp.4oauth.client.clientId}")
    private String clientIdOfClient;

    @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
    private Set<String> scopes;

    private static final String NAME_OF_TOKEN = "bearer";

    private static final String NAME_OF_SECURITY_SCHEME = "KYPO";

    @Bean
    public Docket api() {
        LOG.debug("SwaggerConfig -> api()");
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("public-api")
                .apiInfo(apiInfo()).useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Arrays.asList(securityScheme()))
                .securityContexts(Arrays.asList(securityContext()));
    }

    private ApiInfo apiInfo() {
        LOG.debug("SwaggerConfig -> apiInfo()");
        return new ApiInfoBuilder()
                .title("REST API documentation")
                .description("Developed By CSIRT team")
                .termsOfServiceUrl("Licensed by CSIRT team")
                .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId(clientIdOfClient)
                .scopeSeparator(" ")
                .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new ImplicitGrantBuilder()
                .loginEndpoint(new LoginEndpoint(authorizationURI))
                .tokenName(NAME_OF_TOKEN)
                .build();

        return new OAuthBuilder().name(NAME_OF_SECURITY_SCHEME)
                .grantTypes(Arrays.asList(grantType))
                .scopes(Arrays.asList(scopes()))
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
                .securityReferences(
                        Arrays.asList(new SecurityReference(NAME_OF_SECURITY_SCHEME, scopes())))
                .forPaths(PathSelectors.any())
                .build();
    }
}
