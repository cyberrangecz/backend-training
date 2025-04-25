package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.TrainingInstanceLobbyDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {TeamMapper.class, UserRefMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingInstanceLobbyMapper {

    TrainingInstanceLobby mapToEntity(TrainingInstanceLobbyDTO dto);

    TrainingInstanceLobbyDTO mapToDTO(TrainingInstanceLobby entity);

    Set<TrainingInstanceLobby> mapToSet(Collection<TrainingInstanceLobby> entities);

    Set<TrainingInstanceLobbyDTO> mapToSetDTO(Set<TrainingInstanceLobby> entities);

}
