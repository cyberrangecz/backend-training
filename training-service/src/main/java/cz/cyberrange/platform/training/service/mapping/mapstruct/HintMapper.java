package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.export.HintExportDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.hint.TakenHintDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.HintInfo;
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
 * Converts a training level's hint between its entity form, its editing and export DTOs, and the
 * snapshot a training run keeps of a hint once taken
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HintMapper extends ParentMapper {

  /**
   * Maps a hint into a new entity, copying id, content, penalty and order; title falls back to an
   * empty string when the DTO carries none. The owning training level carries no matching source
   * field and is left unset.
   *
   * @param dto the hint to map
   * @return the mapped hint entity
   */
  @Mapping(target = "title", source = "title", defaultValue = "")
  Hint mapToEntity(HintDTO dto);

  /**
   * Maps a hint to its DTO, copying id, content, penalty and order; title falls back to an empty
   * string when the entity carries none.
   *
   * @param entity the hint to map
   * @return the mapped DTO
   */
  @Mapping(target = "title", source = "title", defaultValue = "")
  HintDTO mapToDTO(Hint entity);

  /**
   * Maps a hint to the DTO exposing only its id, title and penalty, without the advice text.
   *
   * @param entity the hint to map
   * @return the mapped basic DTO
   */
  @Named("hintToBasicDTO")
  HintBasicDTO mapToBasicDTO(Hint entity);

  /**
   * Maps each hint to its basic DTO, as {@link #mapToBasicDTO(Hint)}.
   *
   * @param entities the hints to map
   * @return the mapped basic DTOs, in the given order
   */
  @IterableMapping(qualifiedByName = "hintToBasicDTO")
  List<HintBasicDTO> mapToBasicDtoList(List<Hint> entities);

  /**
   * Maps each hint to its basic DTO, as {@link #mapToBasicDTO(Hint)}.
   *
   * @param entities the hints to map
   * @return the mapped basic DTOs
   */
  @Named("hintsToBasicDtoSet")
  @IterableMapping(qualifiedByName = "hintToBasicDTO")
  Set<HintBasicDTO> mapToBasicDtoSet(Collection<Hint> entities);

  /**
   * Maps a taken hint snapshot to the DTO handed to the trainee who took it, copying its hint id,
   * title, content and order. The penalty is not carried, since {@link HintInfo} does not record
   * one.
   *
   * @param hintInfo the taken hint snapshot to map
   * @return the mapped DTO
   */
  @Mapping(source = "hintId", target = "id")
  @Mapping(source = "hintContent", target = "content")
  @Mapping(source = "hintTitle", target = "title")
  TakenHintDTO mapToDTO(HintInfo hintInfo);

  /**
   * Maps a hint to its export DTO, copying title, content, penalty and order.
   *
   * @param entity the hint to map
   * @return the exported hint
   */
  HintExportDTO mapToHintExportDTO(Hint entity);

  List<Hint> mapToList(Collection<HintDTO> dtos);

  List<HintDTO> mapToListDTO(Collection<Hint> entities);

  Set<Hint> mapToSet(Collection<HintDTO> dtos);

  Set<HintDTO> mapToSetDTO(Collection<Hint> entities);

  /**
   * Maps each taken hint snapshot to its DTO, as {@link #mapToDTO(HintInfo)}.
   *
   * @param entities the taken hint snapshots to map
   * @return the mapped DTOs
   */
  Set<TakenHintDTO> mapToSetInfoDTO(Collection<HintInfo> entities);

  default Optional<Hint> mapToEntityOptional(HintDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<HintDTO> mapToDTOOptional(Hint entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<HintDTO> mapToPageDTO(Page<Hint> objects) {
    List<HintDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default Page<Hint> mapToPage(Page<HintDTO> objects) {
    List<Hint> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  /**
   * Maps a page of hints to a page result resource holding their DTOs.
   *
   * @param objects the page of hints to map
   * @return the mapped DTOs alongside the page's pagination metadata
   */
  default PageResultResource<HintDTO> mapToPageResultResource(Page<Hint> objects) {
    List<HintDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
