package cz.muni.ics.kypo.training.service.impl;

import java.util.*;

import cz.muni.ics.kypo.training.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.repository.*;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;

/**
 * @author Pavel Seda (441048)
 */
@Service
public class TrainingDefinitionServiceImpl implements TrainingDefinitionService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionServiceImpl.class);

  private TrainingDefinitionRepository trainingDefinitionRepository;

  private AbstractLevelRepository abstractLevelRepository;
  private GameLevelRepository gameLevelRepository;
  private InfoLevelRepository infoLevelRepository;
  private AssessmentLevelRepository assessmentLevelRepository;

  @Autowired
  public TrainingDefinitionServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository, AbstractLevelRepository abstractLevelRepository,
                                       InfoLevelRepository infoLevelRepository, GameLevelRepository gameLevelRepository,
                                       AssessmentLevelRepository assessmentLevelRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.gameLevelRepository = gameLevelRepository;
    this.infoLevelRepository = infoLevelRepository;
    this.assessmentLevelRepository = assessmentLevelRepository;
  }

  @Override
  public Optional<TrainingDefinition> findById(long id) {
    LOG.debug("findById({})", id);
    try {
      return trainingDefinitionRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return trainingDefinitionRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingDefinition> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
    LOG.debug("findAllBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
    try{
      return trainingDefinitionRepository.findAllBySandBoxDefinitionRefId(sandboxDefinitionId, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Optional<TrainingDefinition> create(TrainingDefinition trainingDefinition) {
    LOG.debug("create({})", trainingDefinition);
    Assert.notNull(trainingDefinition, "Input training definition must not be null");
    TrainingDefinition tD = trainingDefinitionRepository.save(trainingDefinition);
    LOG.info("Training definition with id: " + trainingDefinition.getId() + "created.");
    return Optional.of(tD);
  }

  @Override
  public void update(TrainingDefinition trainingDefinition) throws ServiceLayerException, CannotBeClonedException {
    LOG.debug("update({})", trainingDefinition);
    Assert.notNull(trainingDefinition, "Input training definition must not be null");
    TrainingDefinition tD = findById(trainingDefinition.getId())
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + trainingDefinition.getId() + ", not found"));
    if (!tD.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    trainingDefinitionRepository.save(trainingDefinition);
    LOG.info("Training definition with id: " + trainingDefinition.getId() + " updated");
  }

  @Override
  public Optional<TrainingDefinition> clone(Long id) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("clone({})", id);
    try {
      TrainingDefinition trainingDefinition = findById(id)
              .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + id + ", not found"));
      if (trainingDefinition.getState().equals(TDState.UNRELEASED))
        throw new CannotBeClonedException("Cant copy unreleased training definition");
      TrainingDefinition tD = new TrainingDefinition();
      BeanUtils.copyProperties(trainingDefinition, tD);
      tD.setId(null);
      tD.setTitle("Clone of " + tD.getTitle());
      tD.setState(TDState.UNRELEASED);
      if (tD.getStartingLevel() != null) {
        tD.setStartingLevel(createLevels(tD.getStartingLevel()));
      }
      tD = trainingDefinitionRepository.save(tD);
      LOG.info("Training definition with id: " + trainingDefinition.getId() + " cloned.");
      return Optional.of(tD);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public void swapLeft(Long definitionId, Long levelId) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("swapLeft({}, {})", definitionId, levelId);
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    AbstractLevel swapLevel = abstractLevelRepository.findById(trainingDefinition.getStartingLevel())
            .orElseThrow(() -> new ServiceLayerException("Level with id: "+ trainingDefinition.getStartingLevel() +", not found"));
    Long oneBeforeId = null;
    Long twoBeforeId = null;
    while (!swapLevel.getId().equals(levelId)) {
      twoBeforeId = oneBeforeId;
      oneBeforeId = swapLevel.getId();
      swapLevel = abstractLevelRepository.findById(swapLevel.getNextLevel())
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
    }
    AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId)
            .orElseThrow(() -> new ServiceLayerException("Cant swap left first level"));
    oneBefore.setNextLevel(swapLevel.getNextLevel());
    swapLevel.setNextLevel(oneBeforeId);
    updateLevel(swapLevel);
    updateLevel(oneBefore);
    if (twoBeforeId != null) {
      AbstractLevel twoBefore = abstractLevelRepository.findById(twoBeforeId)
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
      twoBefore.setNextLevel(swapLevel.getId());
      updateLevel(twoBefore);
    }
    if (oneBeforeId.equals(trainingDefinition.getStartingLevel())) {
      trainingDefinition.setStartingLevel(swapLevel.getId());
      update(trainingDefinition);
    }
  }

  @Override
  public void swapRight(Long definitionId, Long levelId) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("swapRight({}, {})", definitionId, levelId);
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    AbstractLevel swapLevel = abstractLevelRepository.findById(trainingDefinition.getStartingLevel())
            .orElseThrow(() -> new ServiceLayerException("Level with id: "+ trainingDefinition.getStartingLevel()+", not found"));
    Long oneBeforeId = null;
    while (!swapLevel.getId().equals(levelId)) {
      oneBeforeId = swapLevel.getId();
      swapLevel = abstractLevelRepository.findById(swapLevel.getNextLevel())
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
    }
    if (oneBeforeId != null) {
      AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId)
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
      oneBefore.setNextLevel(swapLevel.getNextLevel());
      updateLevel(oneBefore);
    }
    AbstractLevel nextLevel = abstractLevelRepository.findById(swapLevel.getNextLevel()).orElseThrow(() -> new ServiceLayerException("Cant swap right last level"));
    swapLevel.setNextLevel(nextLevel.getNextLevel());
    nextLevel.setNextLevel(swapLevel.getId());
    updateLevel(nextLevel);
    updateLevel(swapLevel);
    if (trainingDefinition.getStartingLevel().equals(levelId)) {
      trainingDefinition.setStartingLevel(nextLevel.getId());
      update(trainingDefinition);
    }
  }

  @Override
  public void delete(Long id) throws ServiceLayerException, CannotBeDeletedException {
    LOG.debug("delete({})", id);
    try {
      TrainingDefinition definition = findById(id)
              .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + id + ", not found"));
      if (definition.getState().equals(TDState.RELEASED))
        throw new CannotBeDeletedException("Cant delete released training definition");
      if (definition.getStartingLevel() != null) {
        Long levelId = definition.getStartingLevel();
        while (levelId != null) {
          AbstractLevel level = abstractLevelRepository.findById(levelId)
                  .orElseThrow(() -> new ServiceLayerException("Level not found"));
          levelId = level.getNextLevel();
          deleteLevel(level);
        }
      }
      trainingDefinitionRepository.delete(definition);
    } catch (NullPointerException ex) {
      throw new ServiceLayerException();
    }
  }

  @Override
  public void deleteOneLevel(Long definitionId, Long levelId) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("deleteOneLevel({}, {})", definitionId, levelId);
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    AbstractLevel level = abstractLevelRepository.findById(trainingDefinition.getStartingLevel())
            .orElseThrow(() -> new ServiceLayerException("Level with id: " + trainingDefinition.getStartingLevel() + ", not found"));
    Long oneIdBefore = null;
    while (!level.getId().equals(levelId)) {
      oneIdBefore = level.getId();
      level = abstractLevelRepository.findById(level.getNextLevel())
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
    }

    if (trainingDefinition.getStartingLevel().equals(level.getId())) {
      trainingDefinition.setStartingLevel(level.getNextLevel());
      trainingDefinitionRepository.save(trainingDefinition);
    } else {
      AbstractLevel oneBefore = abstractLevelRepository
              .findById(oneIdBefore).orElseThrow(() -> new ServiceLayerException("Level not found"));
      oneBefore.setNextLevel(level.getNextLevel());
      updateLevel(oneBefore);
    }
    deleteLevel(level);
  }

  @Override
  public void updateGameLevel(Long definitionId, GameLevel gameLevel) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("updateGameLevel({}, {})",definitionId, gameLevel);
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    if (!findLevelInDefinition(trainingDefinition, gameLevel.getId()))
      throw new CannotBeUpdatedException("Level was not found in definition");
    gameLevelRepository.save(gameLevel);
  }

  @Override
  public void updateInfoLevel(Long definitionId, InfoLevel infoLevel) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("updateInfoLevel({}, {})",definitionId, infoLevel);
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    if (!findLevelInDefinition(trainingDefinition, infoLevel.getId()))
      throw new CannotBeUpdatedException("Level was not found in definition");
    infoLevelRepository.save(infoLevel);
  }

  @Override
  public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("updateAssessmentLevel({}, {})",definitionId, assessmentLevel);
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant edit released or archived training definition");
    if (!findLevelInDefinition(trainingDefinition, assessmentLevel.getId()))
      throw new CannotBeUpdatedException("Level was not found in definition");
    assessmentLevelRepository.save(assessmentLevel);
  }

  @Override
  public Optional<GameLevel> createGameLevel(Long definitionId, GameLevel gameLevel) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("createGameLevel({}, {})", definitionId, gameLevel);
    Assert.notNull(definitionId, "Definition id must not be null");
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant create level in released or archived training definition");
    Assert.notNull(gameLevel, "Game level must not be null");
    GameLevel gL = gameLevelRepository.save(gameLevel);

    if (trainingDefinition.getStartingLevel() == null){
      trainingDefinition.setStartingLevel(gL.getId());
      update(trainingDefinition);
    } else {
      AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
      lastLevel.setNextLevel(gL.getId());
      updateLevel(lastLevel);
    }
    LOG.info("Game level with id: "+ gL.getId() +" created");
    return Optional.of(gL);
  }

  @Override
  public Optional<InfoLevel> createInfoLevel(Long definitionId, InfoLevel infoLevel) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("createInfoLevel({}, {})", definitionId, infoLevel);
    Assert.notNull(definitionId, "Definition id must not be null");
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant create level in released or archived training definition");
    Assert.notNull(infoLevel, "Info level must not be null");
    InfoLevel iL = infoLevelRepository.save(infoLevel);

    if (trainingDefinition.getStartingLevel() == null){
      trainingDefinition.setStartingLevel(iL.getId());
      update(trainingDefinition);
    } else {
      AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
      lastLevel.setNextLevel(iL.getId());
      updateLevel(lastLevel);
    }
    LOG.info("Info level with id: "+ iL.getId() +" created");
    return Optional.of(iL);
  }

  @Override
  public Optional<AssessmentLevel> createAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws ServiceLayerException, CannotBeUpdatedException {
    LOG.debug("createAssessmentLevel({}, {})", definitionId, assessmentLevel);
    Assert.notNull(definitionId, "Definition id must not be null");
    TrainingDefinition trainingDefinition = findById(definitionId)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + definitionId + ", not found"));
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new CannotBeUpdatedException("Cant create level in released or archived training definition");
    Assert.notNull(assessmentLevel, "Assessment level must not be null");
    AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel);

    if (trainingDefinition.getStartingLevel() == null){
      trainingDefinition.setStartingLevel(aL.getId());
      update(trainingDefinition);
    } else {
      AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
      lastLevel.setNextLevel(aL.getId());
      updateLevel(lastLevel);
    }
    LOG.info("Assessment level with id: "+ aL.getId() +" created");
    return Optional.of(aL);
  }

  @Override
  public ArrayList<AbstractLevel> findAllLevelsFromDefinition(Long id) {
    LOG.debug("findAllLevelsFromDefinition({})", id);
    Assert.notNull(id, "Definition id must not be null");
    TrainingDefinition trainingDefinition = findById(id)
            .orElseThrow(() -> new ServiceLayerException("Training definition with id: " + id + ", not found"));
    ArrayList<AbstractLevel> levels = new ArrayList<>();
    Long levelId = trainingDefinition.getStartingLevel();
    AbstractLevel level = null;
    while(levelId != null){
      level = abstractLevelRepository.findById(levelId)
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
      levels.add(level);
      levelId = level.getNextLevel();
    }
    return levels;
  }

  private AbstractLevel findLastLevel(Long levelId){
    AbstractLevel lastLevel = abstractLevelRepository.findById(levelId)
            .orElseThrow(() -> new ServiceLayerException("Level not found"));
    levelId = lastLevel.getNextLevel();
    while (levelId != null){
      lastLevel = abstractLevelRepository.findById(lastLevel.getNextLevel())
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
      levelId = lastLevel.getNextLevel();
    }
    return lastLevel;
  }

  private boolean findLevelInDefinition(TrainingDefinition definition, Long levelId) {
    Long nextId = definition.getStartingLevel();
    Boolean found = false;
    if (nextId.equals(levelId)) found = true;
    while(nextId != null && !found){
      AbstractLevel nextLevel = abstractLevelRepository.findById(nextId)
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
      if (nextLevel.getId().equals(levelId)) found = true;
      nextId = nextLevel.getNextLevel();
    }
    return found;
  }

  private Long createLevels(Long id) {
    List<AbstractLevel> levels = new ArrayList<AbstractLevel>();
    while (id != null) {
      AbstractLevel nextLevel = abstractLevelRepository.findById(id)
              .orElseThrow(() -> new ServiceLayerException("Level not found"));
      id = nextLevel.getNextLevel();
      levels.add(nextLevel);
    }
    Long newId = null;
    for (int i = levels.size() - 1; i >= 0; i--) {
      if (levels.get(i) instanceof AssessmentLevel) {
        AssessmentLevel newAL = new AssessmentLevel();
        BeanUtils.copyProperties(levels.get(i), newAL);
        newAL.setId(null);
        newAL.setNextLevel(newId);
        AssessmentLevel newLevel = assessmentLevelRepository.save(newAL);
        newId = newLevel.getId();
      } else if (levels.get(i) instanceof InfoLevel) {
        InfoLevel newIL = new InfoLevel();
        BeanUtils.copyProperties(levels.get(i), newIL);
        newIL.setId(null);
        newIL.setNextLevel(newId);
        InfoLevel newLevel = infoLevelRepository.save(newIL);
        newId = newLevel.getId();
      } else {
        GameLevel newGL = new GameLevel();
        BeanUtils.copyProperties(levels.get(i), newGL);
        newGL.setId(null);
        newGL.setNextLevel(newId);
        GameLevel newLevel = gameLevelRepository.save(newGL);
        newId = newLevel.getId();
      }
    }
    return newId;
  }

  private void deleteLevel(AbstractLevel level) {
    if (level instanceof AssessmentLevel) {
      assessmentLevelRepository.delete((AssessmentLevel) level);
    } else if (level instanceof InfoLevel) {
      infoLevelRepository.delete((InfoLevel) level);
    } else {
      gameLevelRepository.delete((GameLevel) level);
    }
  }

  private void updateLevel(AbstractLevel level) {
    if (level instanceof AssessmentLevel) {
      assessmentLevelRepository.save((AssessmentLevel) level);
    } else if (level instanceof InfoLevel) {
      infoLevelRepository.save((InfoLevel) level);
    } else {
      gameLevelRepository.save((GameLevel) level);
    }
  }
}
