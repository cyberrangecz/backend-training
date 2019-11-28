package cz.muni.ics.kypo.training.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.Map;

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
        Map<String, Object> json = mapper.readValue(response.getBody(), Map.class);
        throw new RestTemplateException((String) json.get("detail"), response.getStatusCode().toString());
    }
}
