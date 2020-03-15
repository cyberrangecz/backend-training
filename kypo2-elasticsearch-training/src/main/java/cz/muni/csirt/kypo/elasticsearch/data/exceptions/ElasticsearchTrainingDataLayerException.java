package cz.muni.csirt.kypo.elasticsearch.data.exceptions;

/**
 * The type Elasticsearch training data layer exception.
 */
public class ElasticsearchTrainingDataLayerException extends RuntimeException {

    /**
     * Instantiates a new Elasticsearch training data layer exception.
     */
    public ElasticsearchTrainingDataLayerException() {
    }

    /**
     * Instantiates a new Elasticsearch training data layer exception.
     *
     * @param message the message
     */
    public ElasticsearchTrainingDataLayerException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Elasticsearch training data layer exception.
     *
     * @param message the message
     * @param ex      the exception
     */
    public ElasticsearchTrainingDataLayerException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Instantiates a new Elasticsearch training data layer exception.
     *
     * @param ex the exception
     */
    public ElasticsearchTrainingDataLayerException(Throwable ex) {
        super(ex);
    }

}
