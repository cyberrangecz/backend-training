package cz.muni.ics.kypo.training.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import com.mysema.commons.lang.Assert;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.model.Password;
import cz.muni.ics.kypo.training.repository.PasswordRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
  private PasswordRepository passwordRepository;

  @Autowired
  public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository, PasswordRepository passwordRepository) {
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.passwordRepository = passwordRepository;
  }


  @Override
  public TrainingInstance findById(long id) {
    LOG.debug("findById({})", id);
    return trainingInstanceRepository.findById(id).orElseThrow(() -> new ServiceLayerException("Training instance with id: " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
  }

  @Override
  public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    return trainingInstanceRepository.findAll(predicate, pageable);
  }

  @Override
  public TrainingInstance create(TrainingInstance trainingInstance) {
    LOG.debug("create({})", trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "created.");
    return tI;
  }

  @Override
  public void update(TrainingInstance trainingInstance) throws ServiceLayerException{
    LOG.debug("update({})", trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    TrainingInstance tI = trainingInstanceRepository.findById(trainingInstance.getId())
            .orElseThrow(() -> new ServiceLayerException("Training instance with id: "+ trainingInstance.getId() +", not found", ErrorCode.RESOURCE_NOT_FOUND));
    LocalDateTime currentDate = LocalDateTime.now();
    if (!currentDate.isBefore(trainingInstance.getStartTime()))
      throw new ServiceLayerException("Starting time of instance must be in future", ErrorCode.CANNOT_BE_MODIFIED);
    trainingInstanceRepository.save(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "updated.");
  }

  @Override
  public void delete(Long id) throws ServiceLayerException{
    LOG.debug("delete({})", id);
    Assert.notNull(id, "Input training instance id must not be null");
    TrainingInstance trainingInstance = trainingInstanceRepository.findById(id)
            .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + id + ", not found", ErrorCode.RESOURCE_NOT_FOUND));
    LocalDateTime currentDate = LocalDateTime.now();
    if (!currentDate.isAfter(trainingInstance.getEndTime()))
      throw new ServiceLayerException("Only finished instances can be deleted", ErrorCode.CANNOT_BE_MODIFIED);
    trainingInstanceRepository.delete(trainingInstance);
    LOG.info("Training instance with id: " + id + "created.");
  }

  @Override
  public char[] generatePassword() throws ServiceLayerException {
    String newPassword = RandomStringUtils.random(6, true, true);
    String newPasswordHash = DigestUtils.sha256Hex(newPassword);

    Optional<Password> password = passwordRepository.findOneByPasswordHash(newPasswordHash);
    if (password.isPresent()) throw new ServiceLayerException("Password already exists", ErrorCode.PASSWORD_ALREADY_EXISTS);
    Password newPasswordInstance = new Password();
    newPasswordInstance.setPasswordHash(newPasswordHash);
    passwordRepository.save(newPasswordInstance);

    return newPassword.toCharArray();
  }


}
