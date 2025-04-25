package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
import cz.cyberrange.platform.training.persistence.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRefMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {

    Team mapToEntity(TeamDTO dto);

    TeamDTO mapToDTO(Team entity);

    List<TeamDTO> mapToListDTO(Collection<Team> entity);

    List<Team> mapToList(Collection<TeamDTO> dto);

}
