package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.ParticipantRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRefRepository extends JpaRepository<ParticipantRef, Long>, QuerydslPredicateExecutor<ParticipantRef> {

    Optional<ParticipantRef> findByParticipantRefLogin(String participantRefLogin);
}
