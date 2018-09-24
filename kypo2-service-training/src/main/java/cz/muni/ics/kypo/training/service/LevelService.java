package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.model.AbstractLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 *
 * @author Boris Jadus
 *
 */
public interface LevelService {
		/**
		 * Finds specific level by id
		 *
		 * @param id of a level that would be returned
		 * @return specific level by id
		 */
		Optional<AbstractLevel> findById(long id);

		/**
		 * Find all Levels.
		 *
		 * @return all levels
		 */
		Page<AbstractLevel> findAll(Predicate predicate, Pageable pageable);

}
