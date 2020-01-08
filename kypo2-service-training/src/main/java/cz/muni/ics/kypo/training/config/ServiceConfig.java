package cz.muni.ics.kypo.training.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.commons.security.enums.SpringProfiles;
import cz.muni.ics.kypo.training.exceptions.PythonApiResponseErrorHandler;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
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

@Configuration
@EnableAsync(proxyTargetClass = true)
@Import({ElasticsearchServiceConfig.class, PersistenceConfig.class, ResourceServerSecurityConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service", "cz.muni.ics.kypo.training.config"})
public class ServiceConfig {


    @Autowired
    private RestTemplateHeaderModifierInterceptor restTemplateHeaderModifierInterceptor;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${server.ssl.trust-store: #{null}}")
    private String trustStore;
    @Value("${server.ssl.trust-store-password: #{null}}")
    private String trustStorePassword;
    @Autowired
    private Environment env;


    public ServiceConfig() {
    }

    @Bean
    @Qualifier("pythonRestTemplate")
    public RestTemplate pythonRestTemplate() throws Exception{
        RestTemplate restTemplate = prepareRestTemplate();
        restTemplate.setErrorHandler(new PythonApiResponseErrorHandler(objectMapper));
        return restTemplate;
    }

    @Bean
    public RestTemplate restTemplate() throws Exception{
        return prepareRestTemplate();
    }

    private RestTemplate prepareRestTemplate() throws Exception{
        RestTemplate restTemplate;
        if(List.of(env.getActiveProfiles()).contains(SpringProfiles.PROD)) {
            if(trustStore != null && trustStorePassword != null) {
                SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(new File(trustStore), trustStorePassword.toCharArray())
                        .setProtocol("TLSv1.2")
                        .build();
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
                HttpClient httpClient = HttpClients.custom()
                        .setSSLSocketFactory(socketFactory)
                        .build();
                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
                restTemplate = new RestTemplate(factory);
            }
            else {
                throw new ExceptionInInitializerError("Path to trust store and trust store password must be defined.");
            }
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
