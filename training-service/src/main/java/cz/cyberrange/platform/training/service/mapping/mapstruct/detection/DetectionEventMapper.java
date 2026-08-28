package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.AbstractDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AnswerSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.LocationSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.MinimalSolveTimeDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.NoCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.TimeProximityDetectionEventDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Maps cheating detection findings between their persistence subtype hierarchy, rooted at {@link
 * AbstractDetectionEvent}, and their DTO subtype hierarchy, rooted at {@link
 * AbstractDetectionEventDTO}. Each finding kind is mapped through its own pair of methods; the
 * mapper performs no dispatch by itself, so a caller holding a value as its base type must pick the
 * matching kind-specific method itself to carry the kind's own fields across.
 */
@Mapper(
    componentModel = "spring",
    uses = {DetectionEventParticipant.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectionEventMapper extends ParentMapper {
  // ANSWER SIMILARITY
  AnswerSimilarityDetectionEvent mapToEntity(AnswerSimilarityDetectionEventDTO dto);

  /**
   * Maps an answer similarity detection event entity to a {@link
   * AnswerSimilarityDetectionEventDTO}, matching every inherited and own field by name.
   *
   * @param entity the answer similarity finding to map
   * @return the answer similarity finding DTO
   */
  AnswerSimilarityDetectionEventDTO mapToAnswerSimilarityDetectionEventDTO(
      AnswerSimilarityDetectionEvent entity);

  // LOCATION SIMILARITY
  LocationSimilarityDetectionEvent mapToEntity(LocationSimilarityDetectionEventDTO dto);

  /**
   * Maps a location similarity detection event entity to a {@link
   * LocationSimilarityDetectionEventDTO}, matching every inherited and own field by name.
   *
   * @param entity the location similarity finding to map
   * @return the location similarity finding DTO
   */
  LocationSimilarityDetectionEventDTO mapToLocationSimilarityDetectionEventDTO(
      LocationSimilarityDetectionEvent entity);

  // TIME PROXIMITY
  TimeProximityDetectionEvent mapToEntity(TimeProximityDetectionEventDTO dto);

  /**
   * Maps a time proximity detection event entity to a {@link TimeProximityDetectionEventDTO},
   * matching every inherited and own field by name.
   *
   * @param entity the time proximity finding to map
   * @return the time proximity finding DTO
   */
  TimeProximityDetectionEventDTO mapToTimeProximityDetectionEventDTO(
      TimeProximityDetectionEvent entity);

  // MINIMAL SOLVE TIME
  MinimalSolveTimeDetectionEvent mapToEntity(MinimalSolveTimeDetectionEventDTO dto);

  /**
   * Maps a minimal solve time detection event entity to a {@link
   * MinimalSolveTimeDetectionEventDTO}, matching every inherited and own field by name.
   *
   * @param entity the minimal solve time finding to map
   * @return the minimal solve time finding DTO
   */
  MinimalSolveTimeDetectionEventDTO mapToMinimalSolveTimeDetectionEventDTO(
      MinimalSolveTimeDetectionEvent entity);

  // FORBIDDEN COMMANDS
  ForbiddenCommandsDetectionEvent mapToEntity(ForbiddenCommandsDetectionEventDTO dto);

  /**
   * Maps a forbidden commands detection event entity to a {@link
   * ForbiddenCommandsDetectionEventDTO}, matching every inherited and own field by name.
   *
   * @param entity the forbidden commands finding to map
   * @return the forbidden commands finding DTO
   */
  ForbiddenCommandsDetectionEventDTO mapToForbiddenCommandsDetectionEventDTO(
      ForbiddenCommandsDetectionEvent entity);

  // NO COMMANDS
  NoCommandsDetectionEvent mapToEntity(NoCommandsDetectionEventDTO dto);

  /**
   * Maps a no commands detection event entity to a {@link NoCommandsDetectionEventDTO}, matching
   * every inherited field by name; neither type declares fields of its own.
   *
   * @param entity the no commands finding to map
   * @return the no commands finding DTO
   */
  NoCommandsDetectionEventDTO mapToNoCommandsDetectionEventDTO(NoCommandsDetectionEvent entity);

  // ABSTRACT
  /**
   * Maps a detection event entity to a plain {@link AbstractDetectionEventDTO}, matching only the
   * fields declared on the base types. Passing a concrete subtype instance still yields a plain
   * {@link AbstractDetectionEventDTO}: the fields the subtype adds are not carried across, since
   * this method dispatches on the parameter's declared type, not its runtime type.
   *
   * @param entity the detection event to map
   * @return the base detection event DTO
   */
  AbstractDetectionEventDTO mapToDTO(AbstractDetectionEvent entity);

  List<AbstractDetectionEventDTO> mapToList(Collection<AbstractDetectionEventDTO> dtos);

  List<AbstractDetectionEventDTO> mapToListDTO(Collection<AbstractDetectionEventDTO> entities);

  Set<AbstractDetectionEventDTO> mapToSet(Collection<AbstractDetectionEventDTO> dtos);

  Set<AbstractDetectionEventDTO> mapToSetDTO(Collection<AbstractDetectionEventDTO> entities);

  default Page<AbstractDetectionEventDTO> mapToPageDTO(Page<AbstractDetectionEventDTO> objects) {
    List<AbstractDetectionEventDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default Page<AbstractDetectionEventDTO> mapToPage(Page<AbstractDetectionEventDTO> objects) {
    List<AbstractDetectionEventDTO> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of detection events to a page result resource holding their DTOs.
   *
   * @param objects the page of detection events to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<AbstractDetectionEventDTO> mapToPageResultResource(
      Page<AbstractDetectionEvent> objects) {
    List<AbstractDetectionEventDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
