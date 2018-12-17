package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring", uses = {TrainingInstanceMapper.class})
public interface UserRefMapper extends ParentMapper{
    UserRef mapToEntity(UserRefDTO dto);

    UserRefDTO mapToDTO(UserRef entity);

    List<UserRef> mapToList(Collection<UserRefDTO> dtos);

    List<UserRefDTO> mapToListDTO(Collection<UserRef> entities);

    Set<UserRef> mapToSet(Collection<UserRefDTO> dtos);

    Set<UserRefDTO> mapToSetDTO(Collection<UserRef> entities);

    default Optional<UserRef> mapToOptional(UserRefDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<UserRefDTO> mapToOptional(UserRef entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<UserRefDTO> mapToPageDTO(Page<UserRef> objects){
        List<UserRefDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<UserRef> mapToPage(Page<UserRefDTO> objects){
        List<UserRef> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<UserRefDTO> mapToPageResultResource(Page<UserRef> objects){
        List<UserRefDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
