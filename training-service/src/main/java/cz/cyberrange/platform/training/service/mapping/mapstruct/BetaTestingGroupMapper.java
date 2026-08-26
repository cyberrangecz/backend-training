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
 * The BetaTestingGroupMapper is an utility class to map items into data transfer objects. It
 * provides the implementation of mappings between Java bean type BetaTestingGroupMapper and DTOs
 * classes. Code is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {UserRefMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BetaTestingGroupMapper extends ParentMapper {
  BetaTestingGroup mapToEntity(BetaTestingGroupDTO dto);

  @Mapping(target = "organizersRefIds", source = "organizers")
  BetaTestingGroupDTO mapToDTO(BetaTestingGroup entity);

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
