package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

/**
 * The TrainingDefinitionMapper is an utility class to map items into data transfer objects. It
 * provides the implementation of mappings between Java bean type TrainingDefinitionMapper and DTOs
 * classes. Code is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {EnumMapper.class, UserRefMapper.class, BetaTestingGroupMapper.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TrainingDefinitionMapper extends ParentMapper {

  TrainingDefinition mapToEntity(TrainingDefinitionWithLevelsDTO dto);

  /**
   * Maps a training definition entity together with its levels to a {@link
   * TrainingDefinitionWithLevelsDTO}, flattening the beta testing group's identifier into {@code
   * betaTestingGroupId} and leaving {@code canBeArchived} unset.
   *
   * @param entity the training definition to map
   * @param levels the full levels of the definition, in the order they are presented in
   * @return the definition DTO carrying the given levels, without its archiving flag
   */
  @Mapping(target = "betaTestingGroupId", source = "entity.betaTestingGroup.id")
  @Mapping(target = "levels", source = "levels")
  @Mapping(target = "canBeArchived", ignore = true)
  TrainingDefinitionWithLevelsDTO mapToDTOWithLevels(
      TrainingDefinition entity, List<AbstractLevelDTO> levels);

  /**
   * Maps a training definition entity to a {@link TrainingDefinitionDTO}, flattening the beta
   * testing group's identifier into {@code betaTestingGroupId} and leaving {@code canBeArchived}
   * unset.
   *
   * @param entity the training definition to map
   * @return the definition DTO, without its archiving flag
   */
  @Named("trainingDefinitionToDTO")
  @Mapping(target = "betaTestingGroupId", source = "betaTestingGroup.id")
  @Mapping(target = "canBeArchived", ignore = true)
  TrainingDefinitionDTO mapToDTO(TrainingDefinition entity);

  /**
   * Maps a training definition entity together with its levels to a {@link
   * TrainingDefinitionBasicDTO}.
   *
   * @param entity the training definition to map
   * @param levels the levels of the definition, in the order they are presented in
   * @return the basic definition DTO carrying the given levels
   */
  @Mapping(target = "levels", source = "levels")
  TrainingDefinitionBasicDTO mapToBasicDTO(
      TrainingDefinition entity, List<AbstractLevelBasicDTO> levels);

  TrainingDefinitionInfoDTO mapToInfoDTO(TrainingDefinition entity);

  /**
   * Maps a training definition creation request into a new entity, leaving its beta testing group's
   * organizers unpopulated for the caller to assign.
   *
   * @param dto the creation request to map
   * @return the mapped training definition
   */
  TrainingDefinition mapCreateToEntity(TrainingDefinitionCreateDTO dto);

  /**
   * Maps a training definition update into a new entity, leaving its beta testing group's
   * organizers unpopulated for the caller to assign.
   *
   * @param dto the update to map
   * @return the mapped training definition
   */
  TrainingDefinition mapUpdateToEntity(TrainingDefinitionUpdateDTO dto);

  /**
   * Maps a page of training definitions to a page result resource holding their DTOs.
   *
   * @param objects the page of training definitions to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<TrainingDefinitionDTO> mapToPageResultResource(
      Page<TrainingDefinition> objects) {
    List<TrainingDefinitionDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  /**
   * Maps a page of training definitions to a page result resource holding their info DTOs.
   *
   * @param objects the page of training definitions to map
   * @return the mapped info DTOs alongside the page's pagination metadata
   */
  default PageResultResource<TrainingDefinitionInfoDTO> mapToPageResultResourceInfoDTO(
      Page<TrainingDefinition> objects) {
    List<TrainingDefinitionInfoDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToInfoDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
