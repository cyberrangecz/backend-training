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
 * Translates training definitions, instances and runs between their persisted form and the shapes
 * written into an export file or an archive, and reads an imported definition back into an entity.
 * Its implementation is generated at compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {EnumMapper.class, UserRefMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExportImportMapper extends ParentMapper {

  /**
   * Copies a definition's own attributes into its exported shape. The exported levels are left
   * empty, the entity holding no levels of its own, so the caller supplies them.
   *
   * @param entity the definition being exported
   * @return its exported shape, without levels
   */
  ExportTrainingDefinitionAndLevelsDTO mapToDTO(TrainingDefinition entity);

  /**
   * Turns an imported definition into an unpersisted entity. The submitted levels have no
   * counterpart on the entity and are dropped, so the caller creates each level separately.
   *
   * @param dto the definition submitted for import
   * @return the entity carrying its attributes, with no levels attached
   */
  TrainingDefinition mapToEntity(ImportTrainingDefinitionDTO dto);

  /**
   * Copies an instance into its archived shape, flattening its organizers to their cross-service
   * user reference ids and its training definition to that definition's id.
   *
   * @param entity the instance being archived
   * @return its archived shape
   */
  @Mapping(target = "organizersRefIds", source = "organizers")
  @Mapping(target = "definitionId", source = "trainingDefinition.id")
  TrainingInstanceArchiveDTO mapToDTO(TrainingInstance entity);

  /**
   * Copies a run's timing, state, event log reference and participant into its exported shape, the
   * participant carried whole rather than reduced to an id.
   *
   * @param entity the run being exported
   * @return its exported shape
   */
  TrainingRunExportDTO mapToDTO(TrainingRun entity);

  /**
   * Copies a run into its archived shape, reducing its participant to that participant's
   * cross-service user reference id. The instance the run belongs to is left unset, so the caller
   * supplies it.
   *
   * @param entity the run being archived
   * @return its archived shape, without the owning instance's id
   */
  @Mapping(target = "participantRefId", source = "participantRef.userRefId")
  TrainingRunArchiveDTO mapToArchiveDTO(TrainingRun entity);

  /**
   * Reduces user references to the ids by which other services know them.
   *
   * @param organizers the user references to reduce, which may be null
   * @return their cross-service user reference ids, empty when nothing was given
   */
  default Set<Long> mapOrganizersToRefIds(Set<UserRef> organizers) {
    if (organizers == null || organizers.isEmpty()) {
      return new HashSet<>();
    }
    return organizers.stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
  }
}
