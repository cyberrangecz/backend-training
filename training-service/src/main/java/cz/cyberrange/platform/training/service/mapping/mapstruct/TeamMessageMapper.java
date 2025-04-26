package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamMessageDTO;
import cz.cyberrange.platform.training.persistence.model.TeamMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMessageMapper {

    TeamMessageDTO mapToDTO(TeamMessage entity);

    List<TeamMessageDTO> mapToListDTO(Collection<TeamMessage> entity);

}
