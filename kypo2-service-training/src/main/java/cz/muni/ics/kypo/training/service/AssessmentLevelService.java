package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Optional;

public interface AssessmentLevelService {

    /**
     * Gets assessment with given id from database.
     * @param id of the assessment to be loaded
     * @return assessment with given id
     * @throws ServiceLayerException
     */
    Optional<AssessmentLevel> findById(long id);

    /**
     * Find all Assessment Levels.
     *
     * @return all a ssessment levels
     */
    Page<AssessmentLevel> findAll(Predicate predicate, Pageable pageable);
}
