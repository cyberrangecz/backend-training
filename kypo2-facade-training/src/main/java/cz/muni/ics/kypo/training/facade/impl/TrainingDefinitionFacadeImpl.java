package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.LevelType;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingDefinitionFacadeImpl implements TrainingDefinitionFacade {

	private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionFacadeImpl.class);

	private TrainingDefinitionService trainingDefinitionService;
	private BeanMapping beanMapping;

	@Autowired
	public TrainingDefinitionFacadeImpl(TrainingDefinitionService trainingDefinitionService, BeanMapping beanMapping) {
		this.trainingDefinitionService = trainingDefinitionService;
		this.beanMapping = beanMapping;
	}

  @Override
  @Transactional(readOnly = true)
  public TrainingDefinitionDTO findById(long id) {
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      ArrayList<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(id);
      TrainingDefinitionDTO trainingDefinitionDTO = beanMapping.mapTo(trainingDefinitionService.findById(id), TrainingDefinitionDTO.class);


			Set<BasicLevelInfoDTO> levelInfoDTOs = new HashSet<>();
			for (int i = 0; i < levels.size(); i++) {
				BasicLevelInfoDTO basicLevelInfoDTO = new BasicLevelInfoDTO();
				basicLevelInfoDTO.setId(levels.get(i).getId());
				basicLevelInfoDTO.setOrder(i);
				basicLevelInfoDTO.setTitle(levels.get(i).getTitle());
				if (levels.get(i) instanceof GameLevel)
					basicLevelInfoDTO.setLevelType(LevelType.GAME);
				else if (levels.get(i) instanceof AssessmentLevel)
					basicLevelInfoDTO.setLevelType(LevelType.ASSESSMENT);
				else
					basicLevelInfoDTO.setLevelType(LevelType.INFO);
				levelInfoDTOs.add(basicLevelInfoDTO);
			}

      trainingDefinitionDTO.setBasicLevelInfoDTOs(levelInfoDTOs);
      return trainingDefinitionDTO;
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(trainingDefinitionService.findAll(predicate, pageable), TrainingDefinitionDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }


	@Override
	public PageResultResource<TrainingDefinitionDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
		LOG.debug("findAllBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
		try {
			return beanMapping.mapToPageResultDTO(trainingDefinitionService.findAllBySandboxDefinitionId(sandboxDefinitionId, pageable),
					TrainingDefinitionDTO.class);
		} catch (ServiceLayerException ex) {
			throw new FacadeLayerException(ex.getLocalizedMessage());
		}
	}

  @Override
  @Transactional
  public TrainingDefinitionCreateDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
    LOG.debug("create({})", trainingDefinition);
    try{
      Objects.requireNonNull(trainingDefinition);
      TrainingDefinition newTR = trainingDefinitionService.create(beanMapping.mapTo(trainingDefinition, TrainingDefinition.class));
      return beanMapping.mapTo(newTR, TrainingDefinitionCreateDTO.class);
    } catch(ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void update(TrainingDefinitionUpdateDTO trainingDefinition) throws FacadeLayerException {
    LOG.debug("update({})", trainingDefinition);
    try {
      Objects.requireNonNull(trainingDefinition);
      trainingDefinitionService.update(beanMapping.mapTo(trainingDefinition, TrainingDefinition.class));
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public TrainingDefinitionDTO clone(Long id) throws FacadeLayerException {
    LOG.debug("clone({})", id);
    try {
      Objects.requireNonNull(id);
      return beanMapping.mapTo(trainingDefinitionService.clone(id), TrainingDefinitionDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void swapLeft(Long definitionId, Long levelId) throws FacadeLayerException {
    LOG.debug("swapLeft({},{})", definitionId, levelId);
    try{
      Objects.requireNonNull(definitionId);
      Objects.requireNonNull(levelId);
      trainingDefinitionService.swapLeft(definitionId,levelId);
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void swapRight(Long definitionId, Long levelId) throws FacadeLayerException {
    LOG.debug("swapRight({},{})", definitionId, levelId);
    try{
      Objects.requireNonNull(definitionId);
      Objects.requireNonNull(levelId);
      trainingDefinitionService.swapRight(definitionId,levelId);
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void delete(Long id) throws FacadeLayerException {
    LOG.debug("delete({})", id);
    try{
      Objects.requireNonNull(id);
      trainingDefinitionService.delete(id);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException {
    LOG.debug("deleteOneLevel({}, {})", definitionId, levelId);
    try {
      Objects.requireNonNull(definitionId);
      Objects.requireNonNull(levelId);
      trainingDefinitionService.deleteOneLevel(definitionId, levelId);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) throws FacadeLayerException {
    LOG.debug("updateGameLevel({}, {})", definitionId, gameLevel);
    try {
      Objects.requireNonNull(gameLevel);
      Objects.requireNonNull(definitionId);
      trainingDefinitionService.updateGameLevel(definitionId,beanMapping.mapTo(gameLevel, GameLevel.class));
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) throws FacadeLayerException {
    LOG.debug("updateInfoLevel({}, {})", definitionId, infoLevel);
    try {
      Objects.requireNonNull(infoLevel);
      Objects.requireNonNull(definitionId);
      trainingDefinitionService.updateInfoLevel(definitionId, beanMapping.mapTo(infoLevel, InfoLevel.class));
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel) throws FacadeLayerException {
    LOG.debug("updateAssessmentLevel({}, {})", definitionId, assessmentLevel);
    try {
      Objects.requireNonNull(assessmentLevel);
      Objects.requireNonNull(definitionId);
      trainingDefinitionService.updateAssessmentLevel(definitionId, beanMapping.mapTo(assessmentLevel, AssessmentLevel.class));
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public InfoLevelCreateDTO createInfoLevel(Long definitionId) throws FacadeLayerException {
    LOG.debug("createInfoLevel({})", definitionId);
    try{
      Objects.requireNonNull(definitionId);
      InfoLevel newInfoLevel = trainingDefinitionService.createInfoLevel(definitionId);
      return beanMapping.mapTo(newInfoLevel, InfoLevelCreateDTO.class);
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public GameLevelCreateDTO createGameLevel(Long definitionId) throws FacadeLayerException {
    LOG.debug("createGameLevel({})", definitionId);
    try{
      Objects.requireNonNull(definitionId);
      GameLevel newGameLevel = trainingDefinitionService.createGameLevel(definitionId);
      return beanMapping.mapTo(newGameLevel, GameLevelCreateDTO.class);
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex);
    }
  }

	@Override
	@Transactional
	public AssessmentLevelCreateDTO createAssessmentLevel(Long definitionId) throws FacadeLayerException {
		LOG.debug("assessmentInfoLevel({})", definitionId);
		try{
			Objects.requireNonNull(definitionId);
			AssessmentLevel newAssessmentLevel = trainingDefinitionService.createAssessmentLevel(definitionId);
			return beanMapping.mapTo(newAssessmentLevel, AssessmentLevelCreateDTO.class);
		} catch (ServiceLayerException ex){
			throw new FacadeLayerException(ex);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AbstractLevelDTO findLevelById(Long levelId) throws FacadeLayerException {
		LOG.debug("findLevelById({})", levelId);
		try{
			AbstractLevel aL = trainingDefinitionService.findLevelById(levelId);
			if(aL instanceof GameLevel) {
				return beanMapping.mapTo(aL, GameLevelDTO.class);
			} else if (aL instanceof AssessmentLevel) {
				return beanMapping.mapTo(aL, AssessmentLevelDTO.class);
			} else {
				return beanMapping.mapTo(aL, InfoLevelDTO.class);
			}
		} catch (ServiceLayerException ex){
			throw new FacadeLayerException(ex.getLocalizedMessage());
		}
	}
}
