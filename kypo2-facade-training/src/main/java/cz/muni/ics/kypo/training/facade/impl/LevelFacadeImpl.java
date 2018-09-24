package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.LevelFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.AbstractLevel;
import cz.muni.ics.kypo.training.service.LevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Boris Jaduš
 *
 */
@Service
@Transactional
public class LevelFacadeImpl implements LevelFacade {

		private static final Logger LOG = LoggerFactory.getLogger(LevelFacadeImpl.class);

		private LevelService levelService;
		private BeanMapping beanMapping;

		@Autowired
		public LevelFacadeImpl(LevelService levelService, BeanMapping beanMapping){
				this.levelService = levelService;
				this.beanMapping = beanMapping;
		}

		@Override public AbstractLevelDTO findById(Long id) {
				LOG.debug("findById({})", id);
				try {
						Objects.requireNonNull(id);
						Optional<AbstractLevel> lvl = levelService.findById(id);
						AbstractLevel level = lvl.orElseThrow(() -> new ServiceLayerException("Level with this id is not found"));
						return beanMapping.mapTo(level, AbstractLevelDTO.class);
				} catch (NullPointerException ex) {
						throw new FacadeLayerException("Given info ID is null.");
				} catch (ServiceLayerException ex) {
						throw new FacadeLayerException(ex.getLocalizedMessage());
				}
		}

		@Override public PageResultResource<AbstractLevelDTO> findAll(Predicate predicate, Pageable pageable) {
				LOG.debug("findAll({},{})", predicate, pageable);
				try {
						return beanMapping.mapToPageResultDTO(levelService.findAll(predicate, pageable), AbstractLevelDTO.class);
				} catch (ServiceLayerException ex) {
						throw new FacadeLayerException(ex.getLocalizedMessage());
				}		}
}
