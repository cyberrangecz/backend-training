package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupDTO;
import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts a beta testing group between its entity form and the DTOs exposed for it. Declared as
 * a {@code uses} target of {@link TrainingDefinitionMapper}, though that mapper only extracts the
 * group's id and never maps a nested {@code BetaTestingGroup} or {@code BetaTestingGroupDTO}
 * field, so no method here is reached from that composition.
 */
@Mapper(
    componentModel = "spring",
    uses = {UserRefMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BetaTestingGroupMapper extends ParentMapper {

  /**
   * Maps a beta testing group into a new entity. Its {@code organizersRefIds} carries member ids
   * while the entity's {@code organizers} holds member entities, so the field has no automatic
   * match and is left unset, along with the owning training definition; the primary key is
   * copied by the same-name match with the DTO's own {@code id}.
   *
   * @param dto the beta testing group to map
   * @return the mapped beta testing group
   */
  BetaTestingGroup mapToEntity(BetaTestingGroupDTO dto);

  /**
   * Maps a beta testing group to its DTO, copying the primary key and each organizer's cross
   * service reference id.
   *
   * @param entity the beta testing group to map
   * @return the mapped DTO
   */
  @Mapping(target = "organizersRefIds", source = "organizers")
  BetaTestingGroupDTO mapToDTO(BetaTestingGroup entity);

  /**
   * Collects the cross service reference id of each organizer.
   *
   * @param organizers the organizers to read, possibly null
   * @return one id per organizer, or an empty set when there are none
   */
  default Set<Long> mapOrganizersToRefIds(Set<UserRef> organizers) {
    if (organizers == null || organizers.isEmpty()) {
      return new HashSet<>();
    }
    return organizers.stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
  }

  /**
   * Maps a beta testing group update into a new entity, leaving its organizers unpopulated.
   *
   * @param dto the beta testing group update to map
   * @return the mapped beta testing group
   */
  BetaTestingGroup mapCreateToEntity(BetaTestingGroupUpdateDTO dto);

  List<BetaTestingGroup> mapToList(Collection<BetaTestingGroupDTO> dtos);

  List<BetaTestingGroupDTO> mapToListDTO(Collection<BetaTestingGroup> entities);

  Set<BetaTestingGroup> mapToSet(Collection<BetaTestingGroupDTO> dtos);

  Set<BetaTestingGroupDTO> mapToSetDTO(Collection<BetaTestingGroup> entities);

  default Optional<BetaTestingGroup> mapToEntityOptional(BetaTestingGroupDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<BetaTestingGroupDTO> mapToDTOOptional(BetaTestingGroup entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<BetaTestingGroupDTO> mapToPageDTO(Page<BetaTestingGroup> objects) {
    List<BetaTestingGroupDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default Page<BetaTestingGroup> mapToPage(Page<BetaTestingGroupDTO> objects) {
    List<BetaTestingGroup> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), objects.getTotalElements());
  }

  default PageResultResource<BetaTestingGroupDTO> mapToPageResultResource(
      Page<BetaTestingGroup> objects) {
    List<BetaTestingGroupDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
