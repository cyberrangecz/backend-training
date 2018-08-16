package cz.muni.ics.kypo.training.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mysema.commons.lang.Assert;
import cz.muni.ics.kypo.model.*;
import cz.muni.ics.kypo.repository.*;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.repository.TrainingDefinitionRepository;
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
  public Optional<TrainingDefinition> update(TrainingDefinition trainingDefinition) {
    LOG.debug("update({})", trainingDefinition);
    Assert.notNull(trainingDefinition, "Input training definition must not be null");
    TrainingDefinition tD = trainingDefinitionRepository.saveAndFlush(trainingDefinition);
    LOG.info("Training definition with id: " + trainingDefinition.getId() + " updated");
    return Optional.of(tD);
  }

  @Override
  public Optional<TrainingDefinition> clone(Long id) {
    LOG.debug("clone({})", id);
    try {
      TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(id).orElseThrow(() -> new ServiceLayerException());
      TrainingDefinition tD = new TrainingDefinition();
      BeanUtils.copyProperties(trainingDefinition, tD);
      tD.setId(null);
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

  private Long createLevels(Long id) {
    List<AbstractLevel> levels = new ArrayList<AbstractLevel>();
    while (id != null) {
      AbstractLevel nextLevel = abstractLevelRepository.findById(id).get();
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

  @Override
  public void swapLeft(Long definitionId, Long levelId) {
    TrainingDefinition trainingDefinition = findById(definitionId).orElseThrow(() -> new ServiceLayerException());
    AbstractLevel swapLevel = abstractLevelRepository.findById(trainingDefinition.getStartingLevel()).orElseThrow(() -> new ServiceLayerException());
    Long oneBeforeId = null;
    Long twoBeforeId = null;
    while (swapLevel.getId() != levelId){
      twoBeforeId = oneBeforeId;
      oneBeforeId = swapLevel.getId();
      swapLevel = abstractLevelRepository.findById(swapLevel.getNextLevel()).orElseThrow(() -> new ServiceLayerException());
    }
    try {
      AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId).orElseThrow(() -> new ServiceLayerException());
      oneBefore.setNextLevel(swapLevel.getNextLevel());
      swapLevel.setNextLevel(oneBeforeId);
      updateLevel(swapLevel);
      updateLevel(oneBefore);
      if (twoBeforeId != null){
        AbstractLevel twoBefore = abstractLevelRepository.findById(twoBeforeId).orElseThrow(() -> new ServiceLayerException());
        twoBefore.setNextLevel(swapLevel.getId());
        updateLevel(twoBefore);
      }
      if (oneBeforeId == trainingDefinition.getStartingLevel()){
        trainingDefinition.setStartingLevel(swapLevel.getId());
        update(trainingDefinition);
      }
    } catch (NullPointerException ex){
      throw new ServiceLayerException("Cant swap level");
    }
  }

  @Override
  public void swapRight(Long definitionId, Long levelId) {
    TrainingDefinition trainingDefinition = findById(definitionId).orElseThrow(() -> new ServiceLayerException());
    AbstractLevel swapLevel = abstractLevelRepository.findById(trainingDefinition.getStartingLevel()).orElseThrow(() -> new ServiceLayerException());
    Long oneBeforeId = null;
    while (swapLevel.getId() != levelId){
      oneBeforeId = swapLevel.getId();
      swapLevel = abstractLevelRepository.findById(swapLevel.getNextLevel()).orElseThrow(() -> new ServiceLayerException());
    }
    try {
      if (oneBeforeId !=null){
        AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId).orElseThrow(() -> new ServiceLayerException());
        oneBefore.setNextLevel(swapLevel.getNextLevel());
        updateLevel(oneBefore);
      }
      AbstractLevel nextLevel = abstractLevelRepository.findById(swapLevel.getNextLevel()).orElseThrow(() -> new ServiceLayerException());
      swapLevel.setNextLevel(nextLevel.getNextLevel());
      nextLevel.setNextLevel(swapLevel.getId());
      updateLevel(nextLevel);
      updateLevel(swapLevel);
      if (trainingDefinition.getStartingLevel() == levelId){
        trainingDefinition.setStartingLevel(nextLevel.getId());
        update(trainingDefinition);
      }
    } catch (NullPointerException ex) {
      throw new ServiceLayerException("Cant swap level");
    }


  }

  private void updateLevel(AbstractLevel level){
    if (level instanceof AssessmentLevel) {
      assessmentLevelRepository.saveAndFlush((AssessmentLevel) level);
    } else if (level instanceof InfoLevel) {
      infoLevelRepository.saveAndFlush((InfoLevel) level);
    } else {
      gameLevelRepository.saveAndFlush((GameLevel) level);
    }
  }

}
