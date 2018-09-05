package cz.muni.ics.kypo.training.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.mysema.commons.lang.Assert;
import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.model.Keyword;
import cz.muni.ics.kypo.training.repository.KeywordRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingInstanceServiceImpl implements TrainingInstanceService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceServiceImpl.class);

  private TrainingInstanceRepository trainingInstanceRepository;
  private KeywordRepository keywordRepository;

  @Autowired
  public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository, KeywordRepository keywordRepository) {
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.keywordRepository = keywordRepository;
  }


  @Override
  public Optional<TrainingInstance> findById(long id) {
    LOG.debug("findById({})", id);
    try {
      return trainingInstanceRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return trainingInstanceRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Optional<TrainingInstance> create(TrainingInstance trainingInstance) {
    LOG.debug("create({})", trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "created.");
    return Optional.of(tI);
  }

  @Override
  public Optional<TrainingInstance> update(TrainingInstance trainingInstance) {
    LOG.debug("update({})", trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    TrainingInstance tI = trainingInstanceRepository.saveAndFlush(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "updated.");
    return Optional.of(tI);
  }

  @Override
  public void delete(Long id) throws CannotBeDeletedException, ServiceLayerException{
    LOG.debug("delete({})", id);
    Assert.notNull(id, "Input training instance id must not be null");
    TrainingInstance trainingInstance = trainingInstanceRepository.findById(id)
            .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + id + ", not found"));
    LocalDateTime currentDate = LocalDateTime.now();
    if (!currentDate.isAfter(trainingInstance.getEndTime())) throw new CannotBeDeletedException("Only finished instances can be deleted");
    trainingInstanceRepository.delete(trainingInstance);
    LOG.info("Training instance with id: " + id + "created.");
  }

  @Override
  public char[] generateKeyword() throws ServiceLayerException {
    String newKeyword = RandomStringUtils.random(6, true, true);
    String newKeywordHash = DigestUtils.sha256Hex(newKeyword);

    Optional<Keyword> keyword = keywordRepository.findOneByKeywordHash(newKeywordHash);
    if (keyword.isPresent()) throw new ServiceLayerException("Keyword already exists");
    Keyword newKeywordInstance = new Keyword();
    newKeywordInstance.setKeywordHash(newKeywordHash);
    keywordRepository.save(newKeywordInstance);

    return newKeyword.toCharArray();
  }


}
