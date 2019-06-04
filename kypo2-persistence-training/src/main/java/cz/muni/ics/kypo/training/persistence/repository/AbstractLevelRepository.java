package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import java.util.List;
import java.util.Optional;

/**
 * @author Pavel Seda
 */
@Repository
public interface AbstractLevelRepository extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {

    @Query("SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order")
    List<AbstractLevel> findAllLevelsByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);

    @Query("SELECT COALESCE(MAX(l.order), -1) FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId")
    Integer getCurrentMaxOrder(@Param("trainingDefinitionId") Long trainingDefinitionId);

    @Query("SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.id = :levelId")
    Optional<AbstractLevel> findLevelInDefinition(@Param("trainingDefinitionId") Long trainingDefinitionId, @Param("levelId") Long levelId);

//    @EntityGraph(attributePaths = {"trainingDefinition","trainingDefinition.authors", "trainingDefinition.betaTestingGroup", "trainingDefinition.betaTestingGroup.organizers"})
    @Query("SELECT l FROM AbstractLevel l JOIN FETCH l.trainingDefinition td JOIN FETCH td.authors JOIN FETCH td.betaTestingGroup btg JOIN FETCH btg.organizers WHERE l.id = :levelId")
    Optional<AbstractLevel> findByIdIncludinDefinition(@Param("levelId") Long levelId);
}
