package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import cz.muni.ics.kypo.training.persistence.model.Password;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long>, QuerydslPredicateExecutor<Password> {

    Optional<Password> findOneByPasswordHash(String passwordHash);
}
