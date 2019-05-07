package cz.muni.ics.kypo.training.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Pavel Seda
 */
@Configuration
public class RestTemplateHeaderModifierInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = httpServletRequest.getHeader("Authorization");
        request.getHeaders().add("Authorization", token);
        return execution.execute(request, body);
    }
}