package cz.muni.ics.kypo.training.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.events.game.GameStarted;
import cz.muni.csirt.kypo.events.game.common.GameDetails;
import cz.muni.ics.kypo.training.exceptions.NoAvailableSandboxException;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.TRState;
import cz.muni.ics.kypo.training.repository.*;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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
	private RestTemplate restTemplate;

	@Autowired
	public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
			TrainingInstanceRepository trainingInstanceRepository, ParticipantRefRepository participantRefRepository, AuditService auditService,
			RestTemplate restTemplate) {
		this.trainingRunRepository = trainingRunRepository;
		this.abstractLevelRepository = abstractLevelRepository;
		this.trainingInstanceRepository = trainingInstanceRepository;
		this.participantRefRepository = participantRefRepository;
		this.auditService = auditService;
		this.restTemplate = restTemplate;
	}

	@Override
	public TrainingRun findById(Long id) {
		LOG.debug("findById({})", id);
		Objects.requireNonNull(id);
		return trainingRunRepository.findById(id).orElseThrow(() -> new ServiceLayerException("Training Run with id " + id + " not found."));
	}

	@Override
	@PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.model.enums.RoleType).ADMINISTRATOR)")
	public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
		LOG.debug("findAll({},{})", predicate, pageable);
		return trainingRunRepository.findAll(predicate, pageable);
	}

	@Override
	public Page<TrainingRun> findAllByParticipantRefLogin(Pageable pageable) {
		LOG.debug("findAllByParticipantRefId({})");
		Page<TrainingRun> trainingRuns = trainingRunRepository.findAllByParticipantRefLogin(getSubOfLoggedInUser(), pageable);
		return trainingRuns;
	}

	@Override
	public TrainingRun create(TrainingRun trainingRun) {
		LOG.debug("create({})", trainingRun);
		Assert.notNull(trainingRun, "Input training run must not be empty.");
		return trainingRunRepository.save(trainingRun);
	}

	@Override
	public AbstractLevel getNextLevel(Long trainingRunId) {
		LOG.debug("getNextLevel({})", trainingRunId);
		Assert.notNull(trainingRunId, "Input training run id must not be null.");
		TrainingRun trainingRun = findById(trainingRunId);
		Long nextLevelId = trainingRun.getCurrentLevel().getNextLevel();
		if (nextLevelId == null) {
			throw new ServiceLayerException("There is no next level.");
		}
		AbstractLevel abstractLevel = abstractLevelRepository.findById(nextLevelId)
				.orElseThrow(() -> new ServiceLayerException("Level with id " + nextLevelId + " not found."));
		trainingRun.setCurrentLevel(abstractLevel);
		trainingRunRepository.save(trainingRun);
		// event log LevelStarted
		return abstractLevel;
	}

	@Override
	public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
		LOG.debug("findAllByTrainingDefinitionAndParticipant({})", trainingDefinitionId);
		Assert.notNull(trainingDefinitionId, "Input training definition id must not be null.");
		return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantRefLogin(trainingDefinitionId, getSubOfLoggedInUser(),
				pageable);
	}

	@Override
	@PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.model.enums.RoleType).ADMINISTRATOR)")
	public Page<TrainingRun> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
		LOG.debug("findAllByTrainingDefinition({},{})", trainingDefinitionId, pageable);
		Assert.notNull(trainingDefinitionId, "Input training definition id must not be null.");
		return trainingRunRepository.findAllByTrainingDefinitionId(trainingDefinitionId, pageable);
	}

	@Override
	@PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.model.enums.RoleType).ORGANIZER)")
	public Page<TrainingRun> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
		LOG.debug("findAllByTrainingInstance({},{})", trainingInstanceId);
		Assert.notNull(trainingInstanceId, "Input training instance id must not be null.");
		return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
	}

	@Override
	public List<AbstractLevel> getLevels(Long levelId) {
		Assert.notNull(levelId, "Id of first level must not be null.");
		List<AbstractLevel> levels = new ArrayList<>();
		AbstractLevel al;
		do {
			al = abstractLevelRepository.findById(levelId)
					.orElseThrow(() -> new ServiceLayerException("Level with id: " + levelId + " not found."));
			levels.add(al);
		} while (al.getNextLevel() != null);
		return levels;
	}

	@Override
	public AbstractLevel accessTrainingRun(String password) {
		LOG.debug("accessTrainingRun({})", password);
		Assert.hasLength(password, "Password cannot be null or empty.");
		List<TrainingInstance> trainingInstances = trainingInstanceRepository.findAll();
		for (TrainingInstance ti : trainingInstances) {
			auditGameStartedAction(ti);

			// check hash of password not String
			if (new String(ti.getPassword()).equals(password)) {
				Set<SandboxInstanceRef> sandboxInstancePool = ti.getSandboxInstanceRefs();
				Set<SandboxInstanceRef> allocatedSandboxInstances = trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(ti.getId());
				sandboxInstancePool.removeAll(allocatedSandboxInstances);
				if (!sandboxInstancePool.isEmpty()) {
					SandboxInstanceRef sandboxInstanceRef = getReadySandboxInstanceRef(sandboxInstancePool);
					AbstractLevel al = abstractLevelRepository.findById(ti.getTrainingDefinition().getStartingLevel()).get();
					create(getNewTrainingRun(al, getSubOfLoggedInUser(), ti, TRState.NEW, LocalDateTime.now(), ti.getEndTime(), sandboxInstanceRef));
					return al;
				} else {
					throw new NoAvailableSandboxException("There is no available sandbox, wait a minute and try again.");
				}
			}
		}
		throw new ServiceLayerException("There is no training instance with password " + password + ".");
	}

	private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, String participantRefLogin, TrainingInstance trainingInstance,
			TRState state, LocalDateTime startTime, LocalDateTime endTime, SandboxInstanceRef sandboxInstanceRef) {
		TrainingRun tR = new TrainingRun();
		tR.setCurrentLevel(currentLevel);
		tR.setParticipantRef(participantRefRepository.findByParticipantRefLogin(participantRefLogin)
				.orElse(participantRefRepository.save(new ParticipantRef(participantRefLogin))));
		tR.setTrainingInstance(trainingInstance);
		tR.setState(state);
		tR.setStartTime(startTime);
		tR.setEndTime(endTime);
		tR.setSandboxInstanceRef(sandboxInstanceRef);
		return tR;
	}

	private SandboxInstanceRef getReadySandboxInstanceRef(Set<SandboxInstanceRef> sandboxInstancePool) {
		List<Long> idsOfNotAllocatedSandboxes = new ArrayList<>();
		for (SandboxInstanceRef sIR : sandboxInstancePool) {
			idsOfNotAllocatedSandboxes.add(sIR.getSandboxInstanceRef());
		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		String listOfIds = idsOfNotAllocatedSandboxes.stream().map(Object::toString).collect(Collectors.joining(","));
		ResponseEntity<List<SandboxInfo>> response = restTemplate.exchange(serverUrl + SANDBOX_INFO_ENDPOINT, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {}, listOfIds);
		if (response.getStatusCode().isError()) {
			throw new ServiceLayerException();
		}
		List<SandboxInfo> sandboxInfoList = response.getBody();
		sandboxInfoList.removeIf(s -> !s.getState().equals("READY"));
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
		Assert.notNull(trainingRunId, "Input training run id must not be null.");
		Assert.hasLength(flag, "Submitted flag must not be nul nor empty.");
		AbstractLevel level = findById(trainingRunId).getCurrentLevel();
		if (level instanceof GameLevel) {
			if (((GameLevel) level).getFlag().equals(flag)) {
				// event log Corrected Flag
				return true;
			} else {
				// event log Wrong Flag
				return false;
			}
		} else {
			throw new ServiceLayerException("Current level is not game level and does not have flag.");
		}
	}

	@Override
	public String getSolution(Long trainingRunId) {
		LOG.debug("getSolution({})", trainingRunId);
		Assert.notNull(trainingRunId, "Input trainign run id must not be null.");
		AbstractLevel level = findById(trainingRunId).getCurrentLevel();
		if (level instanceof GameLevel) {
			// event getSolution
			return ((GameLevel) level).getSolution();
		} else {
			throw new ServiceLayerException("Current level is not game level and does not have solution.");
		}
	}

	@Override
	public Hint getHint(Long trainingRunId, Long hintId) {
		LOG.debug("getHint({},{})", trainingRunId, hintId);
		Assert.notNull(trainingRunId, "Input training run id must not be null.");
		Assert.notNull(hintId, "Input hint id must not be null.");
		AbstractLevel level = findById(trainingRunId).getCurrentLevel();
		if (level instanceof GameLevel) {
			for (Hint hint : ((GameLevel) level).getHints()) {
				if (hint.getId() == hintId) {
					// event getHint()
					return hint;
				}
			}
			throw new ServiceLayerException("Hint with id " + hintId + " not found.");
		} else {
			throw new ServiceLayerException("Current level is not game level and does not contain hints.");
		}
	}

	@Override
	public int getLevelOrder(Long idOfFirstLevel, Long actualLevel) {
		LOG.debug("getLevelOrder({}, {})", idOfFirstLevel, actualLevel);
		Assert.notNull(idOfFirstLevel, "Input id of first level must not be null.");
		Assert.notNull(actualLevel, "Input id of actual level must not be null.");
		int order = 0;
		AbstractLevel abstractLevel = abstractLevelRepository.findById(idOfFirstLevel).get();
		while (abstractLevel != null) {
			if (abstractLevel.getId() == actualLevel) {
				return order;
			} else {
				abstractLevel = abstractLevelRepository.findById(idOfFirstLevel).get();
			}
		}
		throw new ServiceLayerException("Wrong parameters entered.");
	}

	public String getSubOfLoggedInUser() {
		OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
		return credentials.get("sub").getAsString();
	}

	private void auditGameStartedAction(TrainingInstance trainingInstance) {
		// String loggedInUser = getSubOfLoggedInUser();
		GameDetails gameDetails =
				new GameDetails(trainingInstance.getId(), trainingInstance.getTrainingDefinition().getStartingLevel(), 0L, "");
		auditService.<GameStarted>save(new GameStarted(gameDetails));
	}
}
