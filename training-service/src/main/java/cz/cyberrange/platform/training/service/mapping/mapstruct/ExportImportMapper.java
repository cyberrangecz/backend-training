package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.cyberrange.platform.training.api.dto.archive.TrainingRunArchiveDTO;
import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.dto.export.TrainingRunExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * The ExportImportMapper is an utility class to map items into data transfer objects. It provides
 * the implementation of mappings between Java bean type ExportImportMapper and DTOs classes. Code
 * is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {EnumMapper.class, UserRefMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExportImportMapper extends ParentMapper {

  ExportTrainingDefinitionAndLevelsDTO mapToDTO(TrainingDefinition entity);

  TrainingDefinition mapToEntity(ImportTrainingDefinitionDTO dto);

  @Mapping(target = "organizersRefIds", source = "organizers")
  TrainingInstanceArchiveDTO mapToDTO(TrainingInstance entity);

  TrainingRunExportDTO mapToDTO(TrainingRun entity);

  @Mapping(target = "participantRefId", source = "participantRef.userRefId")
  TrainingRunArchiveDTO mapToArchiveDTO(TrainingRun entity);

  default Set<Long> mapOrganizersToRefIds(Set<UserRef> organizers) {
    if (organizers == null || organizers.isEmpty()) {
      return new HashSet<>();
    }
    return organizers.stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
  }
}
