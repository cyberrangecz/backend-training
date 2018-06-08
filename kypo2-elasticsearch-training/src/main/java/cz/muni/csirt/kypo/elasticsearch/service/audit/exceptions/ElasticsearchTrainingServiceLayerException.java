package cz.muni.csirt.kypo.elasticsearch.service.audit.exceptions;

/**
 * @author Pavel Šeda (441048)
 *
 */
public class ElasticsearchTrainingServiceLayerException extends RuntimeException {

  public ElasticsearchTrainingServiceLayerException() {}

  public ElasticsearchTrainingServiceLayerException(String message) {
    super(message);
  }

  public ElasticsearchTrainingServiceLayerException(String message, Throwable ex) {
    super(message, ex);
  }

  public ElasticsearchTrainingServiceLayerException(Throwable ex) {
    super(ex);
  }
}
