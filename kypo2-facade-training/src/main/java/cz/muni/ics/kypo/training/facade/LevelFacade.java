package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Boris Jaduš
 *
 */
public interface LevelFacade {

		/**
		 * finds specific level by id
		 *
		 * @param id of a level that would be returned
		 * @return specific level by id
		 */
		AbstractLevelDTO findById(Long id);

		/**
		 * Find all Levels.
		 *
		 * @return all levels
		 */
		PageResultResource<AbstractLevelDTO> findAll(Predicate predicate, Pageable pageable);

}
