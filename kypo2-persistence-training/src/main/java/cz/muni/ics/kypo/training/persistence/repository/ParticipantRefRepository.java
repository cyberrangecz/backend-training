package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.ParticipantRef;

import java.util.Optional;

/**
 * @author Pavel Seda
 */
@Repository
public interface ParticipantRefRepository extends JpaRepository<ParticipantRef, Long>, QuerydslPredicateExecutor<ParticipantRef> {

    Optional<ParticipantRef> findByParticipantRefLogin(String participantRefLogin);
}
