package cz.muni.ics.kypo.training.exceptions.responsehandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.exceptions.CustomRestTemplateException;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

/**
 * Handler used for errors returned from Python api.
 */
public class PythonApiResponseErrorHandler implements ResponseErrorHandler {

    private ObjectMapper mapper;

    /**
     * Instantiates a new Python api response error handler.
     *
     * @param mapper the mapper
     */
    public PythonApiResponseErrorHandler(ObjectMapper mapper) {
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
        PythonApiError pythonApiError = mapper.readValue(response.getBody(), PythonApiError.class);
        pythonApiError.setStatus(response.getStatusCode());
        throw new CustomRestTemplateException(pythonApiError);
    }
}
