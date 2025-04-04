package cz.cyberrange.platform.training.persistence.repository;


import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Set;


/**
 * The JPA repository interface to manage {@link TrainingInstanceLobby} instances.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, QuerydslPredicateExecutor<Team> {


    Set<Team> getTeamsByMembersContains(UserRef member);

    Set<Team> getTeamsByTrainingInstance_Id(Long id);

    boolean existsByNameAndTrainingInstance_Id(String name, Long trainingInstanceId);
}
