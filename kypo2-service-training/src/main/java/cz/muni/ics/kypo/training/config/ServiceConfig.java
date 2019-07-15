package cz.muni.ics.kypo.training.config;

import cz.muni.csirt.kypo.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Pavel Šeda
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
@Import({ElasticsearchServiceConfig.class, PersistenceConfig.class, ResourceServerSecurityConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service", "cz.muni.ics.kypo.training.config"})
public class ServiceConfig {


    @Autowired
    private RestTemplateHeaderModifierInterceptor restTemplateHeaderModifierInterceptor;

    @Value("${server.ssl.trust-store}")
    private String trustStore;
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    public ServiceConfig() {
    }

    @Bean
    public RestTemplate restTemplate() throws Exception{
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(new File(trustStore), trustStorePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(restTemplateHeaderModifierInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    /**
     * This configuration is necessary for sharing SecurityContext between worker threads (to pass SecurityContext to the @Async methods.)
     *
     * @return
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
