package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * The TrainingInstanceMapper is an utility class to map items into data transfer objects. It
 * provides the implementation of mappings between Java bean type TrainingInstanceMapper and DTOs
 * classes. Code is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {EnumMapper.class, TrainingDefinitionMapper.class, UserRefMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingInstanceMapper extends ParentMapper {
  TrainingInstance mapToEntity(TrainingInstanceDTO dto);

  /**
   * Maps a training instance update into a new entity, leaving its training definition association
   * unset; the facade resolves {@code trainingDefinitionId} via {@code
   * TrainingDefinitionService.findById} and assigns the definition itself.
   *
   * @param dto the update to map
   * @return the mapped training instance
   */
  TrainingInstance mapUpdateToEntity(TrainingInstanceUpdateDTO dto);

  /**
   * Maps a training instance creation request into a new entity, leaving its training definition
   * association unset; the facade resolves {@code trainingDefinitionId} and assigns the definition
   * itself.
   *
   * @param dto the creation request to map
   * @return the mapped training instance
   */
  TrainingInstance mapCreateToEntity(TrainingInstanceCreateDTO dto);

  TrainingInstance mapPartialUpdateToEntity(TrainingInstanceAssignPoolIdDTO dto);

  /**
   * Maps a training instance entity to a {@link TrainingInstanceBasicInfoDTO}, copying every field
   * the two share.
   *
   * @param dto the training instance entity to map
   * @return the basic info DTO
   */
  TrainingInstanceBasicInfoDTO mapToBasicDto(TrainingInstance dto);

  /**
   * Maps a list of training instance entities, in order, to {@link TrainingInstanceBasicDTO}
   * through {@link #mapToBasicDTO}.
   *
   * @param allByIds the training instances to map
   * @return the mapped basic DTOs, in the same order
   */
  @IterableMapping(qualifiedByName = "trainingInstanceToBasicDTO")
  List<TrainingInstanceBasicDTO> mapToBasicDtoList(List<TrainingInstance> allByIds);

  /**
   * Maps a training instance entity to a {@link TrainingInstanceDTO}, flattening the training
   * definition's identifier into {@code definitionId} and mapping the full definition through
   * {@link TrainingDefinitionMapper}. Leaves {@code sandboxesWithTrainingRun} at its default empty
   * list.
   *
   * @param entity the training instance to map
   * @return the instance DTO
   */
  @Mapping(
      target = "trainingDefinition",
      source = "trainingDefinition",
      qualifiedByName = "trainingDefinitionToDTO")
  @Mapping(target = "definitionId", source = "trainingDefinition.id")
  TrainingInstanceDTO mapToDTO(TrainingInstance entity);

  /**
   * Maps a training instance entity to a {@link TrainingInstanceBasicDTO}, flattening the training
   * definition's identifier into {@code definitionId}.
   *
   * @param entity the training instance to map
   * @return the basic instance DTO
   */
  @Named("trainingInstanceToBasicDTO")
  @Mapping(target = "definitionId", source = "trainingDefinition.id")
  TrainingInstanceBasicDTO mapToBasicDTO(TrainingInstance entity);

  /**
   * Maps a training instance entity to a {@link TrainingInstanceFindAllResponseDTO}, mapping the
   * full training definition through {@link TrainingDefinitionMapper}.
   *
   * @param entity the training instance to map
   * @return the find-all response DTO
   */
  @Mapping(
      target = "trainingDefinition",
      source = "trainingDefinition",
      qualifiedByName = "trainingDefinitionToDTO")
  TrainingInstanceFindAllResponseDTO mapToFindAllViewDTO(TrainingInstance entity);

  List<TrainingInstance> mapToList(Collection<TrainingInstanceDTO> dtos);

  List<TrainingInstanceDTO> mapToListDTO(Collection<TrainingInstance> entities);

  Set<TrainingInstance> mapToSet(Collection<TrainingInstanceDTO> dtos);

  Set<TrainingInstanceDTO> mapToSetDTO(Collection<TrainingInstance> entities);

  default Optional<TrainingInstance> mapToEntityOptional(TrainingInstanceDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<TrainingInstanceDTO> mapToDTOOptional(TrainingInstance entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<TrainingInstanceDTO> mapToPageDTO(Page<TrainingInstance> objects) {
    List<TrainingInstanceDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default Page<TrainingInstance> mapToPage(Page<TrainingInstanceDTO> objects) {
    List<TrainingInstance> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  /**
   * Maps a page of training instances to a page result resource holding their DTOs.
   *
   * @param objects the page of training instances to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<TrainingInstanceDTO> mapToPageResultResource(
      Page<TrainingInstance> objects) {
    List<TrainingInstanceDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  /**
   * Maps a page of training instances to a page result resource holding their find-all view DTOs.
   *
   * @param objects the page of training instances to map
   * @return the mapped view DTOs alongside the page's pagination metadata
   */
  default PageResultResource<TrainingInstanceFindAllResponseDTO> mapToPageResultResourceBasicView(
      Page<TrainingInstance> objects) {
    List<TrainingInstanceFindAllResponseDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToFindAllViewDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
