package cz.muni.ics.kypo.training.exceptions.responsehandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.exceptions.CustomRestTemplateException;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

public class JavaApiResponseErrorHandler implements ResponseErrorHandler {

    private ObjectMapper mapper;

    /**
     * Instantiates a new Python api response error handler.
     *
     * @param mapper the mapper
     */
    public JavaApiResponseErrorHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (
                response.getStatusCode().series() == CLIENT_ERROR
                        || response.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
        if(responseBody.isBlank()) {
            throw new CustomRestTemplateException("Error from external microservice. No specific message provided.", response.getStatusCode());
        }
        JavaApiError javaApiError = mapper.readValue(responseBody, JavaApiError.class);
        throw new CustomRestTemplateException(javaApiError);
    }
}
