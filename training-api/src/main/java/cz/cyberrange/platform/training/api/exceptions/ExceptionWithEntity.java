package cz.cyberrange.platform.training.api.exceptions;

/**
 * Base class for an exception that carries structured detail about the entity involved. A
 * subclass's {@code @ResponseStatus} and, where read by {@code CustomRestExceptionHandlerTraining},
 * its {@link #getEntityErrorDetail()} value determine how the training-rest error handler renders
 * the response.
 */
public abstract class ExceptionWithEntity extends RuntimeException {
  private EntityErrorDetail entityErrorDetail;

  protected ExceptionWithEntity() {
    super();
  }

  /**
   * Attaches {@code entityErrorDetail} to the exception, filling in its reason from {@link
   * #createDefaultReason} when the caller left it unset
   */
  protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail) {
    this.entityErrorDetail = entityErrorDetail;
    if (entityErrorDetail.getReason() == null) {
      this.entityErrorDetail.setReason(createDefaultReason(this.entityErrorDetail));
    }
  }

  /**
   * Attaches {@code entityErrorDetail} and {@code cause} to the exception, filling in the detail's
   * reason from {@link #createDefaultReason} when the caller left it unset
   */
  protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(cause);
    this.entityErrorDetail = entityErrorDetail;
    if (entityErrorDetail.getReason() == null) {
      this.entityErrorDetail.setReason(createDefaultReason(this.entityErrorDetail));
    }
  }

  protected ExceptionWithEntity(Throwable cause) {
    super(cause);
  }

  /**
   * Returns the entity detail attached to this exception, or {@code null} when it was constructed
   * without one
   */
  public EntityErrorDetail getEntityErrorDetail() {
    return entityErrorDetail;
  }

  /**
   * Computes the reason to attach to {@code entityErrorDetail} when its own reason is unset,
   * derived from the other detail already present on it.
   *
   * @param entityErrorDetail the detail being completed
   * @return the reason to record on the detail
   */
  protected abstract String createDefaultReason(EntityErrorDetail entityErrorDetail);
}
