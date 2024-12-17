package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.traininglevel.ReferenceSolutionNodeDTO;
import cz.muni.ics.kypo.training.persistence.model.ReferenceSolutionNode;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReferenceSolutionNodeMapper extends ParentMapper {

    ReferenceSolutionNodeMapper INSTANCE = Mappers.getMapper(ReferenceSolutionNodeMapper.class);


    List<ReferenceSolutionNode> mapToList(List<ReferenceSolutionNodeDTO> dtos);

    ReferenceSolutionNode mapToEntity(ReferenceSolutionNodeDTO dto);

    List<ReferenceSolutionNodeDTO> mapToListDTO(List<ReferenceSolutionNode> entities);

    Set<ReferenceSolutionNodeDTO> mapToSetDTO(Set<ReferenceSolutionNode> entities);

    ReferenceSolutionNodeDTO mapToDTO(ReferenceSolutionNode entity);
}
