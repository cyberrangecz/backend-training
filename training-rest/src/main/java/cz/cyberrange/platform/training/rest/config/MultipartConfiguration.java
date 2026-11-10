package cz.cyberrange.platform.training.rest.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * Sets the multipart limits from the attachment budget of a definition. A file may be as large as
 * the budget, and a request may exceed it by a fixed margin for the multipart framing and the
 * {@code definition} part, so the exact budget check stays with the service. The {@code
 * spring.servlet.multipart.max-file-size} and {@code spring.servlet.multipart.max-request-size}
 * properties do not apply.
 */
@Configuration
public class MultipartConfiguration {

  private static final DataSize FRAMING_MARGIN = DataSize.ofMegabytes(1);

  /**
   * Builds the multipart configuration of the servlet container.
   *
   * @param budgetPerDefinition the cap on the total size of the files one definition links
   * @return the configuration limiting a file to the budget and a request to the budget plus the
   *     framing margin
   */
  @Bean
  MultipartConfigElement multipartConfigElement(
      @Value("${attachments.budget.per.definition}") DataSize budgetPerDefinition) {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(budgetPerDefinition);
    factory.setMaxRequestSize(
        DataSize.ofBytes(budgetPerDefinition.toBytes() + FRAMING_MARGIN.toBytes()));
    return factory.createMultipartConfig();
  }
}
