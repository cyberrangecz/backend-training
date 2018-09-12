package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface PasswordRepository extends JpaRepository<Password, Long>, QuerydslPredicateExecutor<Password> {

	Optional<Password> findOneByPasswordHash(String passwordHash);
}
