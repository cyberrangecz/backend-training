package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.GameLevelFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.service.GameLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class GameLevelFacadeImpl implements GameLevelFacade {

	private static final Logger LOG = LoggerFactory.getLogger(GameLevelFacadeImpl.class);

	private GameLevelService gameLevelService;
	private BeanMapping beanMapping;

	@Autowired
	public GameLevelFacadeImpl(GameLevelService gameLevelService, BeanMapping beanMapping) {
		this.gameLevelService = gameLevelService;
		this.beanMapping = beanMapping;
	}

	@Override
	@Transactional(readOnly = true)
	public GameLevelDTO findById(long id) {
		LOG.debug("findById({})", id);
		try {
			GameLevel gameLevel = gameLevelService.findById(id).get();
			return beanMapping.mapTo(gameLevel, GameLevelDTO.class);
		} catch (ServiceLayerException ex) {
			throw new FacadeLayerException(ex);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PageResultResource<GameLevelDTO> findAll(Predicate predicate, Pageable pageable) {
		LOG.debug("findAll({},{})", predicate, pageable);
		try {
			return beanMapping.mapToPageResultDTO(gameLevelService.findAll(predicate, pageable), GameLevelDTO.class);
		} catch (ServiceLayerException ex) {
			throw new FacadeLayerException(ex);
		}
	}
}
