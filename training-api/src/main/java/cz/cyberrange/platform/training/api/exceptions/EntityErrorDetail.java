package cz.cyberrange.platform.training.api.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Structured detail about the entity involved in an {@link ExceptionWithEntity}. The training-rest
 * error handler reads it off the exception and serializes it into the error response body; a {@link
 * cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError} deserialized from another
 * Java microservice's error response can likewise carry one, though nothing in this codebase reads
 * that copy back out.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityErrorDetail {
  @ApiModelProperty(value = "Class of the entity.", example = "IDMGroup")
  private String entity;

  @ApiModelProperty(value = "Identifier of the entity.", example = "id")
  private String identifier;

  @ApiModelProperty(value = "Value of the identifier.", example = "1")
  private Object identifierValue;

  @ApiModelProperty(
      value = "Detailed message of the exception",
      example = "Group with same name already exists.")
  private String reason;

  public EntityErrorDetail() {}

  public EntityErrorDetail(@NotBlank String reason) {
    this.reason = reason;
  }

  /** Attaches the entity's simple class name to {@code reason}, leaving the identifier unset */
  public EntityErrorDetail(@NotNull Class<?> entityClass, @NotBlank String reason) {
    this(reason);
    this.entity = entityClass.getSimpleName();
  }

  /**
   * Records the entity's simple class name, a caller-chosen identifier label, and its value
   * alongside an explicit reason, skipping the reason this type would otherwise derive from {@link
   * ExceptionWithEntity#createDefaultReason}
   */
  public EntityErrorDetail(
      @NotNull Class<?> entityClass,
      @NotBlank String identifier,
      @NotNull Class<?> identifierClass,
      @NotNull Object identifierValue,
      @NotBlank String reason) {
    this(entityClass, reason);
    this.identifier = identifier;
    this.identifierValue = identifierClass.cast(identifierValue);
  }

  /**
   * Records the entity's simple class name, a caller-chosen identifier label, and its value,
   * leaving the reason unset so it is derived later from {@link
   * ExceptionWithEntity#createDefaultReason}
   */
  public EntityErrorDetail(
      @NotNull Class<?> entityClass,
      @NotBlank String identifier,
      @NotNull Class<?> identifierClass,
      @NotNull Object identifierValue) {
    this.entity = entityClass.getSimpleName();
    this.identifier = identifier;
    this.identifierValue = identifierClass.cast(identifierValue);
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(@NotBlank String entity) {
    this.entity = entity;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(@NotBlank String identifier) {
    this.identifier = identifier;
  }

  public Object getIdentifierValue() {
    return identifierValue;
  }

  public void setIdentifierValue(@NotNull Object identifierValue) {
    this.identifierValue = identifierValue;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(@NotBlank String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityErrorDetail entity = (EntityErrorDetail) o;
    return Objects.equals(getEntity(), entity.getEntity())
        && Objects.equals(getIdentifier(), entity.getIdentifier())
        && Objects.equals(getIdentifierValue(), entity.getIdentifierValue())
        && Objects.equals(getReason(), entity.getReason());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEntity(), getIdentifier(), getIdentifierValue(), getReason());
  }

  @Override
  public String toString() {
    return "EntityErrorDetail{"
        + "entity='"
        + entity
        + '\''
        + ", identifier='"
        + identifier
        + '\''
        + ", identifierValue="
        + identifierValue
        + ", reason='"
        + reason
        + '\''
        + '}';
  }
}
