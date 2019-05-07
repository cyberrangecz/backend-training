package cz.muni.ics.kypo.training.config;

import cz.muni.csirt.kypo.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfig;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Šeda
 */
@Configuration
@EnableAsync
@Import({ElasticsearchServiceConfig.class, PersistenceConfig.class, ResourceServerSecurityConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class ServiceConfig {
    @Autowired
    private HttpServletRequest servletRequest;

    public ServiceConfig() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
//        interceptors.add(new RestTemplateHeaderModifierInterceptor());
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    @Bean(name="processExecutor")
    public TaskExecutor workExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
        threadPoolTaskExecutor.setCorePoolSize(2);
        threadPoolTaskExecutor.setMaxPoolSize(2);
        threadPoolTaskExecutor.setQueueCapacity(50);
        threadPoolTaskExecutor.afterPropertiesSet();
//        threadPoolTaskExecutor.setTaskDecorator(new ContextCopyingDecorator());
        return threadPoolTaskExecutor;
    }

//    static class ContextCopyingDecorator implements TaskDecorator {
//        @Nonnull
//        @Override
//        public Runnable decorate(@Nonnull Runnable runnable) {
//            RequestAttributes context =
//                    RequestContextHolder.currentRequestAttributes();
//            Map<String, String> contextMap = MDC.getCopyOfContextMap();
//            return () -> {
//                try {
//                    RequestContextHolder.setRequestAttributes(context);
//                    MDC.setContextMap(contextMap);
//                    runnable.run();
//                } finally {
//                    MDC.clear();
//                    RequestContextHolder.resetRequestAttributes();
//                }
//            };
//        }
//    }
//
//    public class RestTemplateHeaderModifierInterceptor implements ClientHttpRequestInterceptor {
//
//        @Override
//        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//            String token = servletRequest.getHeader("Authorization");
//            request.getHeaders().add("Authorization", token);
//            return execution.execute(request, body);
//        }
//    }
}
