package cz.muni.ics.kypo.training.exceptions;

public class RestTemplateException extends RuntimeException {
    private String statusCode;

    public RestTemplateException(){

    }

    public RestTemplateException(String message, String statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RestTemplateException(String message, Throwable ex, String statusCode) {
        super(message, ex);
        this.statusCode = statusCode;
    }

    public RestTemplateException(Throwable ex) {
        super(ex);
    }

    public String getStatusCode() {
        return statusCode;
    }
}
