package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TeamMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TeamMessageRepository extends JpaRepository<TeamMessage, Long> {

    List<TeamMessage> findAllByTeam_IdAndTimeAfter(Long teamId, LocalDateTime time);

    void deleteTeamMessageByTeam(Team team);
}