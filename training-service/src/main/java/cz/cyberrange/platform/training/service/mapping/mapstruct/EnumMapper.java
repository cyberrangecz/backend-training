package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.enums.CommandType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.enums.TRState;
import org.mapstruct.Mapper;

/**
 * Dedicated mapper for cross-module enum conversions between API and persistence layers. Kept
 * separate to prevent MapStruct ambiguity when multiple mappers share the same conversion
 * signatures via a common parent interface.
 *
 * <p>{@link #mapTDState} and {@link #mapTRState} are each reached automatically wherever a
 * {@code uses} caller maps a same-named {@code state} field between the persistence and API
 * enums of that name; neither is called by name anywhere. Both overloads of {@link
 * #mapCommandType} carry no such caller: no mapper that maps a {@code CommandType} field declares
 * this interface in its {@code uses}.
 */
@Mapper(componentModel = "spring")
public interface EnumMapper {

  /**
   * Converts the API training definition state to its persistence counterpart by matching
   * constant name.
   *
   * @param apiState the state to convert, or null
   * @return the matching persistence state, or null when the argument is null
   */
  default cz.cyberrange.platform.training.persistence.model.enums.TDState mapTDState(
      TDState apiState) {
    if (apiState == null) {
      return null;
    }
    return cz.cyberrange.platform.training.persistence.model.enums.TDState.valueOf(apiState.name());
  }

  /**
   * Converts the persistence training definition state to its API counterpart by matching
   * constant name.
   *
   * @param persistenceState the state to convert, or null
   * @return the matching API state, or null when the argument is null
   */
  default TDState mapTDState(
      cz.cyberrange.platform.training.persistence.model.enums.TDState persistenceState) {
    if (persistenceState == null) {
      return null;
    }
    return TDState.valueOf(persistenceState.name());
  }

  /**
   * Converts the API training run state to its persistence counterpart by matching constant name.
   *
   * @param apiState the state to convert, or null
   * @return the matching persistence state, or null when the argument is null
   */
  default cz.cyberrange.platform.training.persistence.model.enums.TRState mapTRState(
      TRState apiState) {
    if (apiState == null) {
      return null;
    }
    return cz.cyberrange.platform.training.persistence.model.enums.TRState.valueOf(apiState.name());
  }

  /**
   * Converts the persistence training run state to its API counterpart by matching constant name.
   *
   * @param persistenceState the state to convert, or null
   * @return the matching API state, or null when the argument is null
   */
  default TRState mapTRState(
      cz.cyberrange.platform.training.persistence.model.enums.TRState persistenceState) {
    if (persistenceState == null) {
      return null;
    }
    return TRState.valueOf(persistenceState.name());
  }

  /**
   * Converts the API command type to its persistence counterpart by matching constant name.
   *
   * @param apiType the command type to convert, or null
   * @return the matching persistence command type, or null when the argument is null
   */
  default cz.cyberrange.platform.training.persistence.model.enums.CommandType mapCommandType(
      CommandType apiType) {
    if (apiType == null) {
      return null;
    }
    return cz.cyberrange.platform.training.persistence.model.enums.CommandType.valueOf(
        apiType.name());
  }

  /**
   * Converts the persistence command type to its API counterpart by matching constant name.
   *
   * @param persistenceType the command type to convert, or null
   * @return the matching API command type, or null when the argument is null
   */
  default CommandType mapCommandType(
      cz.cyberrange.platform.training.persistence.model.enums.CommandType persistenceType) {
    if (persistenceType == null) {
      return null;
    }
    return CommandType.valueOf(persistenceType.name());
  }
}
