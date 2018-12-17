package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.persistence.model.AuthorRef;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring",
        nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public interface AuthorRefMapper extends ParentMapper {
    AuthorRef mapToEntity(AuthorRefDTO dto);

    AuthorRefDTO mapToDTO(AuthorRef entity);

    List<AuthorRef> mapToList(Collection<AuthorRefDTO> dtos);

    List<AuthorRefDTO> mapToListDTO(Collection<AuthorRef> entities);

    Set<AuthorRef> mapToSet(Collection<AuthorRefDTO> dtos);

    Set<AuthorRefDTO> mapToSetDTO(Collection<AuthorRef> entities);

    default Optional<AuthorRef> mapToOptional(AuthorRefDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<AuthorRefDTO> mapToOptional(AuthorRef entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<AuthorRefDTO> mapToPageDTO(Page<AuthorRef> objects){
        List<AuthorRefDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<AuthorRef> mapToPage(Page<AuthorRefDTO> objects){
        List<AuthorRef> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<AuthorRefDTO> mapToPageResultResource(Page<AuthorRef> objects){
        List<AuthorRefDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
