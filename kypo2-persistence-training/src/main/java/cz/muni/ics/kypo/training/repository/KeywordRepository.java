package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface KeywordRepository  extends JpaRepository<Keyword, Long>, QuerydslPredicateExecutor<Keyword> {
  Optional<Keyword> findOneByKeywordHash(String keywordHash);
}
