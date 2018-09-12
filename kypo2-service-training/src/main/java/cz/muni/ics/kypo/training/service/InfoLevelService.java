package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.InfoLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface InfoLevelService {

	/**
	 * Finds specific Info level by id
	 * 
	 * @param id of a Info level that would be returned
	 * @return specific info level by id
	 */
	Optional<InfoLevel> findById(long id);

	/**
	 * Find all Info Levels.
	 * 
	 * @return all info levels
	 */
	Page<InfoLevel> findAll(Predicate predicate, Pageable pageable);
}
