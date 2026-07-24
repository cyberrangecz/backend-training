package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.enums.CommandType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.enums.TRState;
import org.mapstruct.Mapper;

/**
 * Dedicated mapper for cross-module enum conversions between API and persistence layers. Kept
 * separate to prevent MapStruct ambiguity when multiple mappers share the same conversion
 * signatures via a common parent interface.
 */
@Mapper(componentModel = "spring")
public interface EnumMapper {

  default cz.cyberrange.platform.training.persistence.model.enums.TDState mapTDState(
      TDState apiState) {
    if (apiState == null) {
      return null;
    }
    return cz.cyberrange.platform.training.persistence.model.enums.TDState.valueOf(apiState.name());
  }

  default TDState mapTDState(
      cz.cyberrange.platform.training.persistence.model.enums.TDState persistenceState) {
    if (persistenceState == null) {
      return null;
    }
    return TDState.valueOf(persistenceState.name());
  }

  default cz.cyberrange.platform.training.persistence.model.enums.TRState mapTRState(
      TRState apiState) {
    if (apiState == null) {
      return null;
    }
    return cz.cyberrange.platform.training.persistence.model.enums.TRState.valueOf(apiState.name());
  }

  default TRState mapTRState(
      cz.cyberrange.platform.training.persistence.model.enums.TRState persistenceState) {
    if (persistenceState == null) {
      return null;
    }
    return TRState.valueOf(persistenceState.name());
  }

  default cz.cyberrange.platform.training.persistence.model.enums.CommandType mapCommandType(
      CommandType apiType) {
    if (apiType == null) {
      return null;
    }
    return cz.cyberrange.platform.training.persistence.model.enums.CommandType.valueOf(
        apiType.name());
  }

  default CommandType mapCommandType(
      cz.cyberrange.platform.training.persistence.model.enums.CommandType persistenceType) {
    if (persistenceType == null) {
      return null;
    }
    return CommandType.valueOf(persistenceType.name());
  }
}
