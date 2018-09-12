package cz.muni.csirt.kypo.elasticsearch.data.exceptions;

/**
 * @author Pavel Šeda (441048)
 *
 */
public class ElasticsearchTrainingDataLayerException extends RuntimeException {

	public ElasticsearchTrainingDataLayerException() {}

	public ElasticsearchTrainingDataLayerException(String message) {
		super(message);
	}

	public ElasticsearchTrainingDataLayerException(String message, Throwable ex) {
		super(message, ex);
	}

	public ElasticsearchTrainingDataLayerException(Throwable ex) {
		super(ex);
	}

}
