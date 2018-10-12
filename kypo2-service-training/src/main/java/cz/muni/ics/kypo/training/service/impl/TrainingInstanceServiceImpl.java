package cz.muni.ics.kypo.training.service.impl;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.Password;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.repository.PasswordRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;


/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingInstanceServiceImpl implements TrainingInstanceService {

	private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceServiceImpl.class);
	@Value("${server.url}")
	private String serverUrl;

	private TrainingInstanceRepository trainingInstanceRepository;
	private PasswordRepository passwordRepository;
	private RestTemplate restTemplate;

	@Autowired
	public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository, PasswordRepository passwordRepository,
			RestTemplate restTemplate) {
		this.trainingInstanceRepository = trainingInstanceRepository;
		this.passwordRepository = passwordRepository;
		this.restTemplate = restTemplate;
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
            .orElseThrow(() -> new ServiceLayerException("Training instance with id: "+ trainingInstance.getId() +", not found.", ErrorCode.RESOURCE_NOT_FOUND));
    LocalDateTime currentDate = LocalDateTime.now();
    if (!currentDate.isBefore(trainingInstance.getStartTime()))
      throw new ServiceLayerException("Starting time of instance must be in future", ErrorCode.RESOURCE_CONFLICT);
    trainingInstance.setPasswordHash(tI.getPasswordHash());
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
      throw new ServiceLayerException("Only finished instances can be deleted.", ErrorCode.RESOURCE_CONFLICT);
    trainingInstanceRepository.delete(trainingInstance);
    LOG.info("Training instance with id: " + id + "deleted.");
  }

  @Override
  public String generatePassword(TrainingInstance trainingInstance, String password) {
    String newPasswordHash = "";
    String newPassword = "";
		boolean generated = false;
		while (!generated){
			String numPart = RandomStringUtils.random(4, false, true);
			newPassword = password +"-"+ numPart;
			newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
			Optional<Password> pW = passwordRepository.findOneByPasswordHash(newPasswordHash);
			if (!pW.isPresent()) generated = true;
		}
		Password newPasswordInstance = new Password();
		newPasswordInstance.setPasswordHash(newPasswordHash);
		passwordRepository.saveAndFlush(newPasswordInstance);

		trainingInstance.setPasswordHash(newPasswordHash);
		trainingInstanceRepository.save(trainingInstance);
    return newPassword;
  }

	@Override
	public ResponseEntity<Void> allocateSandboxes(Long instanceId) throws ServiceLayerException {
		LOG.debug("allocateSandboxes({})", instanceId);
		HttpHeaders httpHeaders = new HttpHeaders();
		TrainingInstance trainingInstance = findById(instanceId);
		int count = trainingInstance.getPoolSize();
		Long sandboxId = trainingInstance.getTrainingDefinition().getSandBoxDefinitionRef().getId();
		String url = "kypo-openstack/api/v1/sandbox-definitions/"+ sandboxId +"/build/"+ count;
		return restTemplate.exchange(serverUrl + url, HttpMethod.POST, new HttpEntity<>(httpHeaders), Void.class);
	}
}
