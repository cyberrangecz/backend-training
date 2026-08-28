package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts a cheating detection between its entity form and {@link CheatingDetectionDTO}, mapping
 * its forbidden commands through {@link ForbiddenCommandMapper}
 */
@Mapper(
    componentModel = "spring",
    uses = {ForbiddenCommandMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheatingDetectionMapper extends ParentMapper {

  /**
   * Maps a cheating detection entity to a {@link CheatingDetectionDTO}, mapping its commands
   * through {@link ForbiddenCommandMapper} into {@code forbiddenCommands}.
   *
   * @param entity the cheating detection to map
   * @return the cheating detection DTO
   */
  @Mapping(target = "forbiddenCommands", source = "commands")
  CheatingDetectionDTO mapToDTO(CheatingDetection entity);

  /**
   * Maps a cheating detection DTO to a new entity, leaving {@code commands} unset; {@code
   * forbiddenCommands} carries no source counterpart on the entity side.
   *
   * @param dto the cheating detection DTO to map
   * @return the mapped cheating detection
   */
  CheatingDetection mapToEntity(CheatingDetectionDTO dto);

  List<CheatingDetection> mapToList(Collection<CheatingDetectionDTO> dtos);

  List<CheatingDetectionDTO> mapToListDTO(Collection<CheatingDetection> entities);

  Set<CheatingDetection> mapToSet(Collection<CheatingDetectionDTO> dtos);

  Set<CheatingDetectionDTO> mapToSetDTO(Collection<CheatingDetection> entities);

  default Page<CheatingDetectionDTO> mapToPageDTO(Page<CheatingDetection> objects) {
    List<CheatingDetectionDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default Page<CheatingDetection> mapToPage(Page<CheatingDetectionDTO> objects) {
    List<CheatingDetection> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  /**
   * Maps a page of cheating detections to a page result resource holding their DTOs.
   *
   * @param objects the page of cheating detections to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<CheatingDetectionDTO> mapToPageResultResource(
      Page<CheatingDetection> objects) {
    List<CheatingDetectionDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
