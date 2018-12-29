package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Boris Jadus
 */
@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long>, QuerydslPredicateExecutor<AccessToken> {

    Optional<AccessToken> findOneByAccessToken(String accessToken);
}
