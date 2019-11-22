package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainingDefinitionRepositoryCustom {

    Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable, Long loggedInUserId);

}
