package cz.muni.ics.kypo.training.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.events.game.GameStarted;
import cz.muni.ics.kypo.training.exceptions.NoAvailableSandboxException;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.TRState;
import cz.muni.ics.kypo.training.repository.*;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;


/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingRunServiceImpl implements TrainingRunService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingRunServiceImpl.class);
  private static final String SANDBOX_INFO_ENDPOINT = "kypo-openstack/api/v1/sandboxes?ids={ids}";
  @Value("${server.url}")
  private String serverUrl;

  private TrainingRunRepository trainingRunRepository;
  private AbstractLevelRepository abstractLevelRepository;
  private TrainingInstanceRepository trainingInstanceRepository;
  private ParticipantRefRepository participantRefRepository;
  private AuditService auditService;

  @Autowired
  public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
                                TrainingInstanceRepository trainingInstanceRepository, ParticipantRefRepository participantRefRepository, AuditService auditService) {
    this.trainingRunRepository = trainingRunRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.participantRefRepository = participantRefRepository;
    this.auditService = auditService;
  }


  @Override
  public Optional<TrainingRun> findById(Long id) {
    LOG.debug("findById({})", id);
    Objects.requireNonNull(id);
    try {
      return trainingRunRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return trainingRunRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingRun> findAllByParticipantRefId(Long participantRefId, Pageable pageable) {
    LOG.debug("findAllByParticipantRefId({})", participantRefId);
    Objects.requireNonNull(participantRefId);
    try {
      Page<TrainingRun> trainingRuns = trainingRunRepository.findAllByParticipantRefId(participantRefId, pageable);
      return trainingRuns;
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public TrainingRun create(TrainingRun trainingRun) {
    LOG.debug("create({})", trainingRun);
    Assert.notNull(trainingRun, "Input training run must not be empty.");
    TrainingRun tR = trainingRunRepository.save(trainingRun);
    LOG.info("Training run with id: " + tR.getId() + " created.");
    return tR;
  }

  @Override
  public Optional<AbstractLevel> getNextLevel(Long trainingRunId) {
    LOG.debug("getNextLevel({})", trainingRunId);
    Objects.requireNonNull(trainingRunId);
    TrainingRun trainingRun = findById(trainingRunId).orElseThrow(() ->
          new ServiceLayerException("Training run with id "+ trainingRunId + " not found."));
    Long nextLevelId = trainingRun.getCurrentLevel().getNextLevel();
    if(nextLevelId == null) {
      LOG.error("There is no next level.");
      throw new ServiceLayerException("There is no next level.");
    }
    AbstractLevel abstractLevel = abstractLevelRepository.findById(nextLevelId).orElseThrow(() ->
          new ServiceLayerException("Level with id " + nextLevelId + " not found."));
    trainingRun.setCurrentLevel(abstractLevel);
    trainingRunRepository.save(trainingRun);
    LOG.info("Participant with id " + trainingRun.getParticipantRef().getParticipantRefId() + " ends level " + trainingRun.getCurrentLevel().getTitle()
    + " and starts next level in order " + abstractLevel.getId() + " in training instance " + trainingRun.getTrainingInstance().getTitle() + ".");
    return Optional.of(abstractLevel);

  }

  @Override
  public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Long participantId, Pageable pageable) {
    LOG.debug("findAllByTrainingDefinitionAndParticipant({},{})", trainingDefinitionId, participantId);
    Objects.requireNonNull(trainingDefinitionId);
    Objects.requireNonNull(participantId);
    try {
      return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantRefId(trainingDefinitionId, participantId, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingRun> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
    LOG.debug("findAllByTrainingDefinition({},{})", trainingDefinitionId, pageable);
    Objects.requireNonNull(trainingDefinitionId);
    try {
      return trainingRunRepository.findAllByTrainingDefinitionId(trainingDefinitionId, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingRun> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
    LOG.debug("findAllByTrainingInstance({},{})", trainingInstanceId);
    Objects.requireNonNull(trainingInstanceId);
    try {
      return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public List<AbstractLevel> getLevels(Long levelId) {
    Objects.requireNonNull(levelId);
    List<AbstractLevel> levels = new ArrayList<>();
    AbstractLevel al = abstractLevelRepository.findById(levelId).get();
      while (al != null ) {
        levels.add(al);
        al = abstractLevelRepository.findById(al.getNextLevel()).get();
      }
      return levels;
    }


  @Override
  public Optional<AbstractLevel> accessTrainingRun(String password, Long participantId) {
    LOG.debug("accessTrainingRun({})", password);
    Assert.hasLength(password, "Password cannot be null or empty.");
    Objects.requireNonNull(participantId);
    List<TrainingInstance> trainingInstances = trainingInstanceRepository.findAll();
    for (TrainingInstance ti: trainingInstances) {

/*
      long id = ti.getId();
      long level = ti.getTrainingDefinition().getStartingLevel();
      long logicalTime = 0; // we do not know how to retrieve this value ???
      long playerId = participantId;
      GameStarted gs = new GameStarted();
      auditService.<GameStarted>save(gs);

*/
      //check hash of password not String
      if (ti.getKeyword().equals(password)) {
        Set<SandboxInstanceRef> sandboxInstancePool = ti.getSandboxInstanceRefs();
        Set<SandboxInstanceRef> allocatedSandboxInstances = trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(ti.getId());
        sandboxInstancePool.removeAll(allocatedSandboxInstances);

        if (!sandboxInstancePool.isEmpty()) {
          SandboxInstanceRef sandboxInstanceRef = getReadySandboxInstanceRef(sandboxInstancePool);
          TrainingRun trainingRun = new TrainingRun();
          AbstractLevel al = abstractLevelRepository.findById(ti.getTrainingDefinition().getStartingLevel()).get();
          trainingRun.setCurrentLevel(al);
          trainingRun.setParticipantRef(participantRefRepository.findByParticipantRefId(participantId)
                  .orElse(participantRefRepository.save(new ParticipantRef(participantId))));
          trainingRun.setTrainingInstance(ti);
          trainingRun.setState(TRState.NEW);
          trainingRun.setStartTime(LocalDateTime.now());
          trainingRun.setEndTime(ti.getEndTime());
          trainingRun.setSandboxInstanceRef(sandboxInstanceRef);
          create(trainingRun);
          return Optional.of(al);
        } else {
          LOG.error("No available sandbox for participant with id: "+ participantId + ".");
          throw new NoAvailableSandboxException("There is no available sandbox, wait a minute and try again.");
        }
      }
    }
    throw new ServiceLayerException("There is no training instance with password " + password + ".");
  }

  private SandboxInstanceRef getReadySandboxInstanceRef(Set<SandboxInstanceRef> sandboxInstancePool) {
    List<Long> idsOfNotAllocatedSandboxes = new ArrayList<>();
    for (SandboxInstanceRef sIR: sandboxInstancePool) {
      idsOfNotAllocatedSandboxes.add(sIR.getSandboxInstanceRef());
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    String listOfIds = idsOfNotAllocatedSandboxes.stream().map(Object::toString).collect(Collectors.joining(","));
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<List<SandboxInfo>> response = restTemplate.exchange(serverUrl + SANDBOX_INFO_ENDPOINT, HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>(){}, listOfIds);
    if (response.getStatusCode().isError()) {
      throw new ServiceLayerException();
    }
    List<SandboxInfo> sandboxInfoList = response.getBody();
    sandboxInfoList.removeIf(s -> s.getState().toString().equals("READY"));
    if (sandboxInfoList.isEmpty()) {
      throw new NoAvailableSandboxException("There is no available sandbox, wait a minute and try again.");
    } else {
      sandboxInstancePool.removeIf(sandboxInstanceRef -> sandboxInstanceRef.getSandboxInstanceRef() != sandboxInfoList.get(0).getId());
      return sandboxInstancePool.iterator().next();
    }

  }

  @Override
  public boolean isCorrectFlag(Long trainingRunId, String flag) {
    LOG.debug("isCorrectFlag({})", trainingRunId);
    TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId).orElseThrow(() ->
            new ServiceLayerException("Training run with id: " + trainingRunId + " not found." ));
    GameLevel gL = (GameLevel) trainingRun.getCurrentLevel();
    if(gL.getFlag().equals(flag)) {
      //event log Corrected Flag
      return true;
    } else {
      //event log Wrong Flag
      return false;
    }

  }

  @Override
  public String getSolution(Long trainingRunId) {
    LOG.debug("getSolution({})", trainingRunId);
    AbstractLevel level = findById(trainingRunId).orElseThrow(
            () -> new ServiceLayerException("Training run with id: " + trainingRunId + " not found.")).getCurrentLevel();
    if (level instanceof GameLevel) {
      return ((GameLevel) level).getSolution();
    } else {
      throw new ServiceLayerException("Current level is not game level and does not have solution.");
    }
  }

  @Override
  public Optional<Hint> getHint(Long trainingRunId, Long hintId) {
    LOG.debug("getHint({},{})", trainingRunId, hintId);
    Objects.requireNonNull(trainingRunId);
    Objects.requireNonNull(hintId);
    AbstractLevel level = findById(trainingRunId).orElseThrow(
            () -> new ServiceLayerException("Training run with id: " + trainingRunId + " not found.")).getCurrentLevel();
    if (level instanceof GameLevel) {
      for (Hint hint: ((GameLevel) level).getHints()) {
        if(hint.getId() == hintId) {
          return Optional.of(hint);
        }
      }
      return null;
    } else {
      throw new ServiceLayerException("Current level is not game level and does not have solution.");
    }
  }
}
