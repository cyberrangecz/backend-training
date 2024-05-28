package cz.muni.ics.kypo.training.mapping.mapstruct.detection;

import cz.muni.ics.kypo.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.mapping.mapstruct.ParentMapper;
import cz.muni.ics.kypo.training.persistence.model.detection.DetectedForbiddenCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectedForbiddenCommandMapper extends ParentMapper {

    DetectedForbiddenCommand mapToEntity(DetectedForbiddenCommandDTO dto);

    DetectedForbiddenCommandDTO mapToDTO(DetectedForbiddenCommand entity);

    List<DetectedForbiddenCommand> mapToList(Collection<DetectedForbiddenCommandDTO> dtos);

    List<DetectedForbiddenCommandDTO> mapToListDTO(Collection<DetectedForbiddenCommand> entities);

    default Page<DetectedForbiddenCommand> mapToPage(Page<DetectedForbiddenCommandDTO> objects) {
        List<DetectedForbiddenCommand> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<DetectedForbiddenCommandDTO> mapToPageResultResource(Page<DetectedForbiddenCommand> objects) {
        List<DetectedForbiddenCommandDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}