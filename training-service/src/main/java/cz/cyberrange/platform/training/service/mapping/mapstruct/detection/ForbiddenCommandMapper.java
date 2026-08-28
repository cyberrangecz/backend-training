package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommand;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts a forbidden command between its entity form and {@link ForbiddenCommandDTO}, flattening
 * the owning cheating detection's identifier into {@code cheatingDetectionId}
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ForbiddenCommandMapper extends ParentMapper {

  /**
   * Maps a forbidden command DTO to a new entity, leaving its owning cheating detection association
   * unset; {@code cheatingDetectionId} carries no source counterpart on the entity side.
   *
   * @param dto the forbidden command DTO to map
   * @return the mapped forbidden command
   */
  ForbiddenCommand mapToEntity(ForbiddenCommandDTO dto);

  /**
   * Maps a forbidden command entity to a {@link ForbiddenCommandDTO}, flattening the owning
   * cheating detection's identifier into {@code cheatingDetectionId}.
   *
   * @param entity the forbidden command to map
   * @return the forbidden command DTO
   */
  @Mapping(target = "cheatingDetectionId", source = "cheatingDetection.id")
  ForbiddenCommandDTO mapToDTO(ForbiddenCommand entity);

  List<ForbiddenCommand> mapToList(Collection<ForbiddenCommandDTO> dtos);

  List<ForbiddenCommandDTO> mapToListDTO(Collection<ForbiddenCommand> entities);

  default Page<ForbiddenCommand> mapToPage(Page<ForbiddenCommandDTO> objects) {
    List<ForbiddenCommand> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of forbidden commands to a page result resource holding their DTOs.
   *
   * @param objects the page of forbidden commands to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<ForbiddenCommandDTO> mapToPageResultResource(
      Page<ForbiddenCommand> objects) {
    List<ForbiddenCommandDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
