package cz.cyberrange.platform.training.api.dto.trainingdefinition;

/** A training definition response carrying the number of instances created from the definition. */
public interface InstanceCountHolder {

  /**
   * Gets the id of the training definition the response describes.
   *
   * @return the training definition id
   */
  Long getId();

  /**
   * Sets the number of training instances created from the definition.
   *
   * @param instanceCount the number of instances, past ones included
   */
  void setInstanceCount(long instanceCount);
}
