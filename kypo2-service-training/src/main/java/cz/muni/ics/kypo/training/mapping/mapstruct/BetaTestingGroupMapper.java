package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.RestResponses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * The BetaTestingGroupMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type BetaTestingGroupMapper and
 * DTOs classes. Code is generated during compile time.
 *
 * @author Boris Jadus
 */
@Mapper(componentModel = "spring", uses = {UserRefMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BetaTestingGroupMapper extends ParentMapper {
    BetaTestingGroup mapToEntity(BetaTestingGroupDTO dto);

    BetaTestingGroupDTO mapToDTO(BetaTestingGroup entity);

    BetaTestingGroup mapCreateToEntity(BetaTestingGroupUpdateDTO dto);

    List<BetaTestingGroup> mapToList(Collection<BetaTestingGroupDTO> dtos);

    List<BetaTestingGroupDTO> mapToListDTO(Collection<BetaTestingGroup> entities);

    Set<BetaTestingGroup> mapToSet(Collection<BetaTestingGroupDTO> dtos);

    Set<BetaTestingGroupDTO> mapToSetDTO(Collection<BetaTestingGroup> entities);

    default Optional<BetaTestingGroup> mapToOptional(BetaTestingGroupDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<BetaTestingGroupDTO> mapToOptional(BetaTestingGroup entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<BetaTestingGroupDTO> mapToPageDTO(Page<BetaTestingGroup> objects){
        List<BetaTestingGroupDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<BetaTestingGroup> mapToPage(Page<BetaTestingGroupDTO> objects){
        List<BetaTestingGroup> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<BetaTestingGroupDTO> mapToPageResultResource(Page<BetaTestingGroup> objects){
        List<BetaTestingGroupDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
