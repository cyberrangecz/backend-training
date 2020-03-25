package cz.muni.ics.kypo.training.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.commons.security.enums.SpringProfiles;
import cz.muni.ics.kypo.training.exceptions.responsehandlers.JavaApiResponseErrorHandler;
import cz.muni.ics.kypo.training.exceptions.responsehandlers.PythonApiResponseErrorHandler;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.net.ssl.SSLContext;
import java.nio.charset.Charset;
import java.util.List;

/**
 * The type Service config.
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
@Import({ElasticsearchServiceConfig.class, PersistenceConfig.class, ResourceServerSecurityConfig.class, ObjectMappersConfiguration.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service", "cz.muni.ics.kypo.training.config"})
public class ServiceConfig {

    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;
    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;
    @Autowired
    private RestTemplateHeaderModifierInterceptor restTemplateHeaderModifierInterceptor;
    @Autowired
    @Qualifier("restTemplateObjectMapper")
    private ObjectMapper objectMapper;
    @Autowired
    private Environment env;

    /**
     * Instantiates a new Service config.
     */
    public ServiceConfig() {
    }

    /**
     * Python rest template rest template.
     *
     * @return the rest template
     * @throws Exception the exception
     */
    @Bean
    @Qualifier("pythonRestTemplate")
    public RestTemplate pythonRestTemplate() throws Exception {
        RestTemplate restTemplate = prepareRestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(kypoOpenStackURI));
        restTemplate.setErrorHandler(new PythonApiResponseErrorHandler(objectMapper));
        return restTemplate;
    }

    /**
     * Rest template rest template.
     *
     * @return the rest template
     * @throws Exception the exception
     */
    @Bean
    @Primary
    @Qualifier("javaRestTemplate")
    public RestTemplate javaRestTemplate() throws Exception {
        RestTemplate restTemplate = prepareRestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(userAndGroupURI));
        restTemplate.setErrorHandler(new JavaApiResponseErrorHandler(objectMapper));
        return restTemplate;
    }

    private RestTemplate prepareRestTemplate() throws Exception {
        RestTemplate restTemplate;
        if (List.of(env.getActiveProfiles()).contains(SpringProfiles.PROD)) {
            SSLContext sslContext = new SSLContextBuilder()
                    .setProtocol("TLSv1.2")
                    .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate = new RestTemplate(factory);
        } else {
            restTemplate = new RestTemplate();
        }
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(restTemplateHeaderModifierInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;

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
