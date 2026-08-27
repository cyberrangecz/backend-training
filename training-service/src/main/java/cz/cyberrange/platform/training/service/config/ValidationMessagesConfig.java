package cz.cyberrange.platform.training.service.config;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Wires validation failure messages to the bundled message files, so that a constraint's message
 * key resolves to readable text rather than being shown as the key.
 */
@Configuration
public class ValidationMessagesConfig {

  /**
   * Supplies the validation messages, read from the bundled locale files. A key with no entry
   * resolves to the key itself rather than failing, and the files are re-read on each lookup rather
   * than cached.
   *
   * @return the source validation messages are resolved through
   */
  @Bean
  public MessageSource messageSourceValidation() {
    final ReloadableResourceBundleMessageSource source =
        new ReloadableResourceBundleMessageSource();
    source.setBasename("classpath:locale/ValidationMessages");
    source.setUseCodeAsDefaultMessage(true);
    source.setDefaultEncoding("UTF-8");
    source.setCacheSeconds(0);
    return source;
  }

  /**
   * Supplies the validator the application validates with, resolving its failure messages through
   * the bundled message files. Being the primary validator, it is the one injected wherever no
   * qualifier names another.
   *
   * @return the validator
   */
  @Bean
  @Primary
  public LocalValidatorFactoryBean getValidator() {
    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSourceValidation());
    return bean;
  }

  /**
   * Prints available locales. It is useful to set up appropriate ValidationMessages.properties file
   * name, e.g. messages_en_US.properties
   *
   * <p>en_US
   *
   * <p>en -> language; US -> country
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    Locale[] locales = Locale.getAvailableLocales();
    Arrays.sort(locales, (l1, l2) -> l1.toString().compareTo(l2.toString()));
    for (Locale l : locales) {
      System.out.println(l.toString());
    }
  }
}
