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

  TrainingInstance mapUpdateToEntity(TrainingInstanceUpdateDTO dto);

  TrainingInstance mapCreateToEntity(TrainingInstanceCreateDTO dto);

  TrainingInstance mapPartialUpdateToEntity(TrainingInstanceAssignPoolIdDTO dto);

  TrainingInstanceBasicInfoDTO mapToBasicDto(TrainingInstance dto);

  @IterableMapping(qualifiedByName = "trainingInstanceToBasicDTO")
  List<TrainingInstanceBasicDTO> mapToBasicDtoList(List<TrainingInstance> allByIds);

  @Mapping(
      target = "trainingDefinition",
      source = "trainingDefinition",
      qualifiedByName = "trainingDefinitionToDTO")
  @Mapping(target = "definitionId", source = "trainingDefinition.id")
  TrainingInstanceDTO mapToDTO(TrainingInstance entity);

  @Named("trainingInstanceToBasicDTO")
  @Mapping(target = "definitionId", source = "trainingDefinition.id")
  TrainingInstanceBasicDTO mapToBasicDTO(TrainingInstance entity);

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

  default PageResultResource<TrainingInstanceDTO> mapToPageResultResource(
      Page<TrainingInstance> objects) {
    List<TrainingInstanceDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  default PageResultResource<TrainingInstanceFindAllResponseDTO> mapToPageResultResourceBasicView(
      Page<TrainingInstance> objects) {
    List<TrainingInstanceFindAllResponseDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToFindAllViewDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
