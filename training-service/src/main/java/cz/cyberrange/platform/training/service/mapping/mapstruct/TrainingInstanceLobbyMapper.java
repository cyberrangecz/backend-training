package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.TrainingInstanceLobbyDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {TeamMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingInstanceLobbyMapper {

    TrainingInstanceLobby mapToEntity(TrainingInstanceLobbyDTO dto);

    TrainingInstanceLobbyDTO mapToDTO(TrainingInstanceLobby entity);

    List<TrainingInstanceLobbyDTO> mapToListDTO(List<TrainingInstanceLobby> entity);

    List<TrainingInstanceLobby> mapToList(List<TrainingInstanceLobbyDTO> dto);

    Set<TrainingInstanceLobby> mapToSet(Collection<TrainingInstanceLobby> entities);

    Set<TrainingInstanceLobbyDTO> mapToSetDTO(Set<TrainingInstanceLobby> entities);

}
