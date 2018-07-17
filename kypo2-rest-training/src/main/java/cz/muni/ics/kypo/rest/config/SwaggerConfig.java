package cz.muni.ics.kypo.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * <p>
 * That is the configuration for documentation generated with the Swagger_2 framework.
 * </p>
 * 
 * <p>
 * Swagger documentation is accessible at the following link:
 * http://localhost:8080/{API-ROOT-CONTEXT}/swagger-ui.html (localhost example)
 * </p>
 * 
 * @author Pavel Šeda
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket api() {
    // @formatter:off
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("public-api")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())   
                .paths(PathSelectors.any())
                .build();
    // @formatter:on
  }

  private ApiInfo apiInfo() {
    // @formatter:off
        return new ApiInfoBuilder()
                .title("REST API documentation")
                .description("Developed By CSIRT team")
                .termsOfServiceUrl("Licensed by CSIRT team")
                .build();
    // @formatter:on
  }

}
