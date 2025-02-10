package cz.cyberrange.platform.training.elasticsearch.service.exceptions;

/**
 * The type Elasticsearch training service layer exception.
 */
public class ElasticsearchTrainingServiceLayerException extends RuntimeException {

    /**
     * Instantiates a new Elasticsearch training service layer exception.
     */
    public ElasticsearchTrainingServiceLayerException() {
    }

    /**
     * Instantiates a new Elasticsearch training service layer exception.
     *
     * @param message the message
     */
    public ElasticsearchTrainingServiceLayerException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Elasticsearch training service layer exception.
     *
     * @param message the message
     * @param ex      the exception
     */
    public ElasticsearchTrainingServiceLayerException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Instantiates a new Elasticsearch training service layer exception.
     *
     * @param ex the exception
     */
    public ElasticsearchTrainingServiceLayerException(Throwable ex) {
        super(ex);
    }
}
