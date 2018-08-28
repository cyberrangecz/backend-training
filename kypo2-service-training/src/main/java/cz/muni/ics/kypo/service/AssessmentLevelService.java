package cz.muni.ics.kypo.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.AssessmentLevel;
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
    public Optional<AssessmentLevel> findById(long id);

    /**
     * Find all Assessment Levels.
     *
     * @return all a ssessment levels
     */
    public Page<AssessmentLevel> findAll(Predicate predicate, Pageable pageable);

    /**
     * Creates given assessment in database.
     * @param assessment assessment to be created
     * @throws ServiceLayerException
     */
    Optional<AssessmentLevel> create(AssessmentLevel assessment);

    /**
     * Updates given assessment in database.
     * @param assessment assessment to be updated
     * @throws ServiceLayerException
     */
   // void update(AssessmentLevel assessment);

}
