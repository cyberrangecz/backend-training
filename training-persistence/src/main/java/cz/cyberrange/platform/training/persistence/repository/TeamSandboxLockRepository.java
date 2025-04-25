package cz.cyberrange.platform.training.persistence.repository;


import cz.cyberrange.platform.training.persistence.model.TeamSandboxLock;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;


/**
 * The JPA repository interface to manage {@link TrainingInstanceLobby} instances.
 */
@Repository
public interface TeamSandboxLockRepository extends JpaRepository<TeamSandboxLock, Long>, QuerydslPredicateExecutor<TeamSandboxLock> {

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO team_sandbox_lock (team_id) VALUES (:teamId)")
    void createLock(Long id);

}
