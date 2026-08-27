package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.export.UserRefExportDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts a {@code UserRef} row between its entity form and its DTOs. Declared as a {@code uses}
 * target of {@code TrainingInstanceMapper}, {@code TrainingDefinitionMapper},
 * {@code BetaTestingGroupMapper} and {@code ExportImportMapper}, but none of those mappers
 * delegates a {@code UserRef}/{@code UserRefDTO} field mapping to a method of this interface —
 * the two that touch a {@code UserRef}-shaped field ({@code BetaTestingGroupMapper}, {@code
 * ExportImportMapper}) resolve it with their own default method instead — so no method here is
 * reached from any of those compositions, nor injected and called directly anywhere.
 */
@Mapper(
    componentModel = "spring",
    uses = {TrainingInstanceMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRefMapper extends ParentMapper {

  /**
   * Maps a user reference into a new entity, copying only {@code userRefId}: it is the sole field
   * the DTO and the entity share by name. The primary key and the entity's association sets are
   * left unset.
   *
   * @param dto the user reference to map
   * @return the mapped user reference entity
   */
  UserRef mapToEntity(UserRefDTO dto);

  /**
   * Maps a user reference to its DTO, copying only {@code userRefId}: it is the sole field the
   * entity and the DTO share by name. Every other DTO field is left unset, since the entity
   * carries no matching property; those fields are populated only when a user-and-group service
   * response is deserialized directly into a {@code UserRefDTO}.
   *
   * @param entity the user reference to map
   * @return the mapped DTO
   */
  UserRefDTO mapToDTO(UserRef entity);

  /**
   * Maps each user reference into a new entity, as {@link #mapToEntity(UserRefDTO)}.
   *
   * @param dtos the user references to map
   * @return the mapped entities
   */
  List<UserRef> mapToList(Collection<UserRefDTO> dtos);

  /**
   * Maps each user reference to its DTO, as {@link #mapToDTO(UserRef)}.
   *
   * @param entities the user references to map
   * @return the mapped DTOs
   */
  List<UserRefDTO> mapToListDTO(Collection<UserRef> entities);

  /**
   * Maps each user reference into a new entity, as {@link #mapToEntity(UserRefDTO)}.
   *
   * @param dtos the user references to map
   * @return the mapped entities
   */
  Set<UserRef> mapToSet(Collection<UserRefDTO> dtos);

  /**
   * Maps each user reference to its DTO, as {@link #mapToDTO(UserRef)}.
   *
   * @param entities the user references to map
   * @return the mapped DTOs
   */
  Set<UserRefDTO> mapToSetDTO(Collection<UserRef> entities);

  /**
   * Maps each user reference to its export DTO, copying {@code userRefFullName},
   * {@code userRefGivenName}, {@code userRefFamilyName}, {@code iss} and {@code userRefId}.
   * {@code userRefLogin} carries no matching source field, since the DTO's own login-shaped
   * property is named {@code userRefSub}, and is left unset.
   *
   * @param userRefDTOs the user references to map
   * @return the exported user references
   */
  List<UserRefExportDTO> mapUserRefExportDTOToUserRefDTO(Collection<UserRefDTO> userRefDTOs);

  default Optional<UserRef> mapToEntityOptional(UserRefDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<UserRefDTO> mapToDTOOptional(UserRef entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<UserRefDTO> mapToPageDTO(Page<UserRef> objects) {
    List<UserRefDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default Page<UserRef> mapToPage(Page<UserRefDTO> objects) {
    List<UserRef> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default PageResultResource<UserRefDTO> mapToPageResultResource(Page<UserRef> objects) {
    List<UserRefDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
