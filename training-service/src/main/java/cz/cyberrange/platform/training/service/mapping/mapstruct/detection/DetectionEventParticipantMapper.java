package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts a detection event participant between its entity form and {@link
 * DetectionEventParticipantDTO}, matching every field by name except {@code cheatingDetectionId},
 * which carries no counterpart on the DTO side
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectionEventParticipantMapper extends ParentMapper {
  /**
   * Maps a detection event participant DTO to a new entity, matching every field by name except
   * {@code cheatingDetectionId}, which carries no source counterpart on the DTO side.
   *
   * @param dto the detection event participant DTO to map
   * @return the mapped detection event participant
   */
  DetectionEventParticipant mapToEntity(DetectionEventParticipantDTO dto);

  /**
   * Maps a detection event participant entity to a {@link DetectionEventParticipantDTO}, matching
   * every field by name; the entity's {@code cheatingDetectionId} carries no counterpart on the DTO
   * side.
   *
   * @param entity the detection event participant to map
   * @return the detection event participant DTO
   */
  DetectionEventParticipantDTO mapToDTO(DetectionEventParticipant entity);

  List<DetectionEventParticipant> mapToList(Collection<DetectionEventParticipantDTO> dtos);

  List<DetectionEventParticipantDTO> mapToListDTO(Collection<DetectionEventParticipant> entities);

  default Page<DetectionEventParticipant> mapToPage(Page<DetectionEventParticipantDTO> objects) {
    List<DetectionEventParticipant> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of detection event participants to a page result resource holding their DTOs.
   *
   * @param objects the page of detection event participants to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<DetectionEventParticipantDTO> mapToPageResultResource(
      Page<DetectionEventParticipant> objects) {
    List<DetectionEventParticipantDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
