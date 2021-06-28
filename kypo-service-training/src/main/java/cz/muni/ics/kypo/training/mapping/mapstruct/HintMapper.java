package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.export.HintExportDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.persistence.model.Hint;
import cz.muni.ics.kypo.training.persistence.model.HintInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * The HintMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type HintMapper and
 * DTOs classes. Code is generated during compile time.
 *
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HintMapper extends ParentMapper {

    Hint mapToEntity(HintDTO dto);

    HintDTO mapToDTO(Hint entity);

    @Mapping(source = "hintId", target = "id")
    @Mapping(source = "hintContent", target = "content")
    @Mapping(source = "hintTitle", target = "title")
    TakenHintDTO mapToDTO(HintInfo hintInfo);

    HintExportDTO mapToHintExportDTO(Hint entity);

    List<Hint> mapToList(Collection<HintDTO> dtos);

    List<HintDTO> mapToListDTO(Collection<Hint> entities);

    Set<Hint> mapToSet(Collection<HintDTO> dtos);

    Set<HintDTO> mapToSetDTO(Collection<Hint> entities);

    default Optional<Hint> mapToOptional(HintDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<HintDTO> mapToOptional(Hint entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<HintDTO> mapToPageDTO(Page<Hint> objects) {
        List<HintDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<Hint> mapToPage(Page<HintDTO> objects) {
        List<Hint> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<HintDTO> mapToPageResultResource(Page<Hint> objects) {
        List<HintDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
