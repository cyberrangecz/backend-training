package cz.muni.ics.kypo.training.service.impl;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsAdminOrDesignerOrOrganizer;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.*;

/**
 * @author Pavel Seda (441048)
 */
@Service
public class TrainingDefinitionServiceImpl implements TrainingDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionServiceImpl.class);
    @Value("${user-and-group-server.uri}")
    private String userAndGroupUrl;


    private RestTemplate restTemplate;
    private TrainingDefinitionRepository trainingDefinitionRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private GameLevelRepository gameLevelRepository;
    private InfoLevelRepository infoLevelRepository;
    private AssessmentLevelRepository assessmentLevelRepository;
    private UserRefRepository userRefRepository;

    private static final String ARCHIVED_OR_RELEASED = "Cannot edit released or archived training definition.";
    private static final String LEVEL_NOT_FOUND = "Level not found.";

    @Autowired
    public TrainingDefinitionServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository,
                                         AbstractLevelRepository abstractLevelRepository, InfoLevelRepository infoLevelRepository, GameLevelRepository gameLevelRepository,
                                         AssessmentLevelRepository assessmentLevelRepository, TrainingInstanceRepository trainingInstanceRepository,
                                         UserRefRepository userRefRepository, RestTemplate restTemplate) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.userRefRepository = userRefRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" + "or @securityService.isDesignerOfGivenTrainingDefinition(#id)" +
            "or @securityService.isInBetaTestingGroup(#id)")
    public TrainingDefinition findById(Long id) {
        LOG.debug("findById({})", id);
        return trainingDefinitionRepository.findById(id).orElseThrow(
                () -> new ServiceLayerException("Training definition with id: " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @IsAdminOrDesignerOrOrganizer
    public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAllTrainingDefinitions({},{})", predicate, pageable);
        if (isAdmin()) {
            return trainingDefinitionRepository.findAll(predicate, pageable);
        } else if (isDesigner() && isOrganizer()) {
            return trainingDefinitionRepository.findAllForDesignersAndOrganizers(getSubOfLoggedInUser(), pageable);
        } else if (isDesigner()) {
            return trainingDefinitionRepository.findAllByLoggedInUser(getSubOfLoggedInUser(), pageable);
        } else {
            return trainingDefinitionRepository.findAllForOrganizers(getSubOfLoggedInUser(), pageable);
        }
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("sub").getAsString();
    }

    private String getFullNameOfLoggedInUser(){
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("name").getAsString();
    }

    private boolean isAdmin() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name())) return true;
        }
        return false;
    }

    private boolean isDesigner() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name())) return true;
        }
        return false;
    }

    private boolean isOrganizer() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name())) return true;
        }
        return false;
    }

    @Override
    @IsDesignerOrAdmin
    public TrainingDefinition create(TrainingDefinition trainingDefinition) {
        LOG.debug("create({})", trainingDefinition);
        Assert.notNull(trainingDefinition, "Input training definition must not be null");
        String userSub = getSubOfLoggedInUser();

        Optional<UserRef> user = userRefRepository.findUserByUserRefLogin(userSub);
        if (user.isPresent()) {
            trainingDefinition.addAuthor(user.get());
        } else {
            UserRef newUser = new UserRef();
            newUser.setUserRefLogin(userSub);
            newUser.setUserRefFullName(getFullNameOfLoggedInUser());
            trainingDefinition.addAuthor(newUser);
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());

        LOG.info("Training definition with id: {} created.", trainingDefinition.getId());
        return trainingDefinitionRepository.save(trainingDefinition);
    }

    @Override
    @IsDesignerOrAdmin
    public Page<TrainingDefinition> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
        LOG.debug("findAllBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
        return trainingDefinitionRepository.findAllBySandBoxDefinitionRefId(sandboxDefinitionId, pageable);

    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionToUpdate.id)")
    public void update(TrainingDefinition trainingDefinitionToUpdate) {
        LOG.debug("update({})", trainingDefinitionToUpdate);
        Assert.notNull(trainingDefinitionToUpdate, "Input training definition must not be null");
        TrainingDefinition trainingDefinition = findById(trainingDefinitionToUpdate.getId());
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED)) {
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        }
        if(trainingInstanceRepository.existsAnyForTrainingDefinition(trainingDefinition.getId())) {
            throw new ServiceLayerException("Cannot update training definition with already created training instance. " +
                    "Remove training instance/s before updating training definition.", ErrorCode.RESOURCE_CONFLICT);
        }
        String userSub = getSubOfLoggedInUser();
        Optional<UserRef> user = userRefRepository.findUserByUserRefLogin(userSub);
        if (user.isPresent()) {
            trainingDefinitionToUpdate.addAuthor(user.get());
        } else {
            UserRef newUser = new UserRef();
            newUser.setUserRefLogin(userSub);
            newUser.setUserRefFullName(getFullNameOfLoggedInUser());
            trainingDefinitionToUpdate.addAuthor(newUser);
        }
        trainingDefinitionToUpdate.setLastEdited(LocalDateTime.now());
        trainingDefinitionToUpdate.setStartingLevel(trainingDefinition.getStartingLevel());
        trainingDefinitionRepository.save(trainingDefinitionToUpdate);
        LOG.info("Training definition with id: {} updated.", trainingDefinitionToUpdate.getId());
    }

    @Override
    @IsDesignerOrAdmin
    public TrainingDefinition clone(Long id) {
        LOG.debug("clone({})", id);
        TrainingDefinition trainingDefinition = findById(id);
        TrainingDefinition clonedTrainingDefinition = new TrainingDefinition();
        BeanUtils.copyProperties(trainingDefinition, clonedTrainingDefinition);
        clonedTrainingDefinition.setId(null);

        BetaTestingGroup vG = new BetaTestingGroup();
        BeanUtils.copyProperties(clonedTrainingDefinition.getBetaTestingGroup(), vG);
        vG.setId(null);

        clonedTrainingDefinition.setBetaTestingGroup(vG);
        clonedTrainingDefinition.setTitle("Clone of " + clonedTrainingDefinition.getTitle());
        clonedTrainingDefinition.setState(TDState.UNRELEASED);
        if (clonedTrainingDefinition.getStartingLevel() != null) {
            clonedTrainingDefinition.setStartingLevel(createLevels(clonedTrainingDefinition.getStartingLevel()));
        }
        clonedTrainingDefinition.setAuthors(new HashSet<>());
        Optional<UserRef> user = userRefRepository.findUserByUserRefLogin(getSubOfLoggedInUser());
        if (user.isPresent()) {
            clonedTrainingDefinition.addAuthor(user.get());
        } else {
            UserRef newUser = new UserRef();
            newUser.setUserRefLogin(getSubOfLoggedInUser());
            newUser.setUserRefFullName(getFullNameOfLoggedInUser());
            clonedTrainingDefinition.addAuthor(newUser);
        }
        clonedTrainingDefinition.setLastEdited(LocalDateTime.now());
        clonedTrainingDefinition = trainingDefinitionRepository.save(clonedTrainingDefinition);
        LOG.info("Training definition with id: {} cloned.", trainingDefinition.getId());
        return clonedTrainingDefinition;

    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void swapLeft(Long definitionId, Long levelId) {
        LOG.debug("swapLeft({}, {})", definitionId, levelId);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        AbstractLevel swapLevel = abstractLevelRepository.findById(trainingDefinition.getStartingLevel())
                .orElseThrow(() -> new ServiceLayerException("Level with id: " + trainingDefinition.getStartingLevel() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        Long oneBeforeId = null;
        Long twoBeforeId = null;
        while (!swapLevel.getId().equals(levelId)) {
            twoBeforeId = oneBeforeId;
            oneBeforeId = swapLevel.getId();
            swapLevel = abstractLevelRepository.findById(swapLevel.getNextLevel())
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        }
        if (oneBeforeId == null) {
            throw new ServiceLayerException("Cannot swap left first level.", ErrorCode.RESOURCE_CONFLICT);
        }
        AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId)
                .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        oneBefore.setNextLevel(swapLevel.getNextLevel());
        swapLevel.setNextLevel(oneBeforeId);
        updateLevel(swapLevel);
        updateLevel(oneBefore);
        if (twoBeforeId != null) {
            AbstractLevel twoBefore = abstractLevelRepository.findById(twoBeforeId)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            twoBefore.setNextLevel(swapLevel.getId());
            updateLevel(twoBefore);
        }
        if (oneBeforeId.equals(trainingDefinition.getStartingLevel())) {
            trainingDefinition.setStartingLevel(swapLevel.getId());
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void swapRight(Long definitionId, Long levelId) {
        LOG.debug("swapRight({}, {})", definitionId, levelId);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        AbstractLevel swapLevel = abstractLevelRepository.findById(trainingDefinition.getStartingLevel())
                .orElseThrow(() -> new ServiceLayerException("Level with id: " + trainingDefinition.getStartingLevel() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));

        Long oneBeforeId = null;
        while (!swapLevel.getId().equals(levelId)) {
            oneBeforeId = swapLevel.getId();
            swapLevel = abstractLevelRepository.findById(swapLevel.getNextLevel())
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        }
        if (swapLevel.getNextLevel() == null) throw new ServiceLayerException("Cannot swap right last level.", ErrorCode.RESOURCE_CONFLICT);
        if (oneBeforeId != null) {
            AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            oneBefore.setNextLevel(swapLevel.getNextLevel());
            updateLevel(oneBefore);
        }
        AbstractLevel nextLevel = abstractLevelRepository.findById(swapLevel.getNextLevel())
                .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        swapLevel.setNextLevel(nextLevel.getNextLevel());
        nextLevel.setNextLevel(swapLevel.getId());
        updateLevel(nextLevel);
        updateLevel(swapLevel);
        if (trainingDefinition.getStartingLevel().equals(levelId)) {
            trainingDefinition.setStartingLevel(nextLevel.getId());
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void delete(Long definitionId) {
        LOG.debug("delete({})", definitionId);

        TrainingDefinition definition = findById(definitionId);
        if (definition.getState().equals(TDState.RELEASED))
            throw new ServiceLayerException("Cannot delete released training definition.", ErrorCode.RESOURCE_CONFLICT);
        if (definition.getStartingLevel() != null) {
            Long levelId = definition.getStartingLevel();
            while (levelId != null) {
                AbstractLevel level = abstractLevelRepository.findById(levelId)
                        .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
                levelId = level.getNextLevel();
                deleteLevel(level);
            }
        }
        trainingDefinitionRepository.delete(definition);

    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void deleteOneLevel(Long definitionId, Long levelId) {
        LOG.debug("deleteOneLevel({}, {})", definitionId, levelId);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (trainingDefinition.getStartingLevel() == null) throw new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
        AbstractLevel level = abstractLevelRepository.findById(trainingDefinition.getStartingLevel())
                .orElseThrow(() -> new ServiceLayerException("Level with id: " + trainingDefinition.getStartingLevel() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        Long oneIdBefore = null;
        while (!level.getId().equals(levelId)) {
            oneIdBefore = level.getId();
            level = abstractLevelRepository.findById(level.getNextLevel())
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        }

        if (trainingDefinition.getStartingLevel().equals(level.getId())) {
            trainingDefinition.setStartingLevel(level.getNextLevel());
            trainingDefinitionRepository.save(trainingDefinition);
        } else {
            AbstractLevel oneBefore = abstractLevelRepository.findById(oneIdBefore)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            oneBefore.setNextLevel(level.getNextLevel());
            updateLevel(oneBefore);
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
        deleteLevel(level);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void updateGameLevel(Long definitionId, GameLevel gameLevelToUpdate) {
        LOG.debug("updateGameLevel({}, {})", definitionId, gameLevelToUpdate);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (!findLevelInDefinition(trainingDefinition, gameLevelToUpdate.getId()))
            throw new ServiceLayerException("Level was not found in definition.", ErrorCode.RESOURCE_NOT_FOUND);

        GameLevel gameLevel = gameLevelRepository.findById(gameLevelToUpdate.getId()).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + gameLevelToUpdate.getId() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        gameLevelToUpdate.setNextLevel(gameLevel.getNextLevel());
        trainingDefinition.setLastEdited(LocalDateTime.now());
        gameLevelRepository.save(gameLevelToUpdate);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void updateInfoLevel(Long definitionId, InfoLevel infoLevelToUpdate) {
        LOG.debug("updateInfoLevel({}, {})", definitionId, infoLevelToUpdate);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (!findLevelInDefinition(trainingDefinition, infoLevelToUpdate.getId()))
            throw new ServiceLayerException("Level was not found in definition.", ErrorCode.RESOURCE_NOT_FOUND);

        InfoLevel infoLevel = infoLevelRepository.findById(infoLevelToUpdate.getId()).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + infoLevelToUpdate.getId() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        infoLevelToUpdate.setNextLevel(infoLevel.getNextLevel());
        trainingDefinition.setLastEdited(LocalDateTime.now());
        infoLevelRepository.save(infoLevelToUpdate);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevelToUpdate) {
        LOG.debug("updateAssessmentLevel({}, {})", definitionId, assessmentLevelToUpdate);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (!findLevelInDefinition(trainingDefinition, assessmentLevelToUpdate.getId()))
            throw new ServiceLayerException("Level was not found in definition", ErrorCode.RESOURCE_NOT_FOUND);

        AssessmentLevel assessmentLevel = assessmentLevelRepository.findById(assessmentLevelToUpdate.getId()).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + assessmentLevelToUpdate.getId() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        assessmentLevelToUpdate.setNextLevel(assessmentLevel.getNextLevel());
        if (!assessmentLevelToUpdate.getQuestions().equals(assessmentLevel.getQuestions())) {
            AssessmentUtil.validQuestions(assessmentLevelToUpdate.getQuestions());
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
        assessmentLevelRepository.save(assessmentLevelToUpdate);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public GameLevel createGameLevel(Long definitionId) {
        LOG.debug("createGameLevel({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot create level in released or archived training definition", ErrorCode.RESOURCE_CONFLICT);

        GameLevel newGameLevel = new GameLevel();
        newGameLevel.setMaxScore(100);
        newGameLevel.setTitle("Title of game level");
        newGameLevel.setIncorrectFlagLimit(5);
        newGameLevel.setFlag("Secret flag");
        newGameLevel.setSolutionPenalized(true);
        newGameLevel.setSolution("Solution of the game should be here");
        newGameLevel.setContent("The test entry should be here");
        newGameLevel.setEstimatedDuration(1);
        GameLevel gameLevel = gameLevelRepository.save(newGameLevel);

        if (trainingDefinition.getStartingLevel() == null) {
            trainingDefinition.setStartingLevel(gameLevel.getId());
            update(trainingDefinition);
        } else {
            AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
            lastLevel.setNextLevel(gameLevel.getId());
            updateLevel(lastLevel);
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
        LOG.info("Game level with id: {} created", gameLevel.getId());
        return gameLevel;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public InfoLevel createInfoLevel(Long definitionId) {
        LOG.debug("createInfoLevel({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot create level in released or archived training definition", ErrorCode.RESOURCE_CONFLICT);

        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setTitle("Title of info level");
        newInfoLevel.setContent("Content of info level should be here.");
        InfoLevel infoLevel = infoLevelRepository.save(newInfoLevel);

        if (trainingDefinition.getStartingLevel() == null) {
            trainingDefinition.setStartingLevel(infoLevel.getId());
            update(trainingDefinition);
        } else {
            AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
            lastLevel.setNextLevel(infoLevel.getId());
            updateLevel(lastLevel);
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
        LOG.info("Info level with id: {} created.", infoLevel.getId());
        return infoLevel;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public AssessmentLevel createAssessmentLevel(Long definitionId) {
        LOG.debug("createAssessmentLevel({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot create level in released or archived training definition.", ErrorCode.RESOURCE_CONFLICT);

        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setTitle("Title of assessment level");
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setInstructions("Instructions should be here");
        newAssessmentLevel.setQuestions("[]");
        AssessmentLevel assessmentLevel = assessmentLevelRepository.save(newAssessmentLevel);

        if (trainingDefinition.getStartingLevel() == null) {
            trainingDefinition.setStartingLevel(assessmentLevel.getId());
            update(trainingDefinition);
        } else {
            AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
            lastLevel.setNextLevel(assessmentLevel.getId());
            updateLevel(lastLevel);
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
        LOG.info("Assessment level with id: {} created.", assessmentLevel.getId());
        return assessmentLevel;
    }

    @Override
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR," +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<AbstractLevel> findAllLevelsFromDefinition(Long definitionId) {
        LOG.debug("findAllLevelsFromDefinition({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        List<AbstractLevel> levels = new ArrayList<>();
        Long levelId = trainingDefinition.getStartingLevel();
        AbstractLevel level = null;
        while (levelId != null) {
            level = abstractLevelRepository.findById(levelId)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            levels.add(level);
            levelId = level.getNextLevel();
        }
        return levels;
    }

    @Override
    @IsDesignerOrAdmin
    public AbstractLevel findLevelById(Long levelId) {
        LOG.debug("findLevelById({})", levelId);
        Assert.notNull(levelId, "Input level id must not be null.");
        return abstractLevelRepository.findById(levelId)
                .orElseThrow(() -> new ServiceLayerException("Level with id: " + levelId + ", not found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @IsAdminOrDesignerOrOrganizer
    public List<TrainingInstance> findAllTrainingInstancesByTrainingDefinitionId(Long id) {
        Assert.notNull(id, "Input definition id must not be null");
        return trainingInstanceRepository.findAllByTrainingDefinitionId(id);
    }

    @Override
    @IsAdminOrDesignerOrOrganizer
    public UserRef findUserRefByLogin(String login) {
        return userRefRepository.findUserByUserRefLogin(login).orElseThrow(
                () -> new ServiceLayerException("UserRef with login " + login + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @IsDesignerOrAdmin
    public List<UserInfoDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String url = userAndGroupUrl + "/roles/users" + "?roleType=" + roleType
                + "&page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize() + "&fields=content[login,full_name]";
        ResponseEntity<PageResultResource<UserInfoDTO>> usersResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<PageResultResource<UserInfoDTO>>() {
                });
        if (usersResponse.getStatusCode().isError() || usersResponse.getBody() == null) {
            throw new ServiceLayerException("Error while obtaining info about users in designers groups.", ErrorCode.UNEXPECTED_ERROR);
        }
        return usersResponse.getBody().getContent();
    }

    @Override
    @TransactionalWO
    public UserRef createUserRef(UserRef userRefToCreate) {
        LOG.debug("createUserRef({})", userRefToCreate.getUserRefLogin());
        Assert.notNull(userRefToCreate, "User ref must not be null");
        UserRef userRef = userRefRepository.save(userRefToCreate);
        LOG.info("User ref with login: {} created.", userRef.getUserRefLogin());
        return userRef;
    }

    @Override
    @TransactionalWO
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
        "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void switchState(Long definitionId, cz.muni.ics.kypo.training.api.enums.TDState state) {
        LOG.debug("unreleaseDefinition({})", definitionId);
        TrainingDefinition trainingDefinition = findById(definitionId);

        switch (trainingDefinition.getState()){
            case UNRELEASED:
                if (state.equals(cz.muni.ics.kypo.training.api.enums.TDState.RELEASED)) trainingDefinition.setState(TDState.RELEASED);
                else throw new ServiceLayerException("Cannot switch from" + trainingDefinition.getState() + " to "+ state, ErrorCode.RESOURCE_CONFLICT);
                break;
            case RELEASED:
                if (state.equals(cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED)) trainingDefinition.setState(TDState.ARCHIVED);
                else if (state.equals(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED)){
                    if(trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
                        throw new ServiceLayerException("Cannot update training definition with already created training instance. " +
                            "Remove training instance/s before updating training definition.", ErrorCode.RESOURCE_CONFLICT);
                    }
                    trainingDefinition.setState(TDState.UNRELEASED);
                } else throw new ServiceLayerException("Cannot switch from" + trainingDefinition.getState() + " to "+ state, ErrorCode.RESOURCE_CONFLICT);
                break;
            default:
                throw new ServiceLayerException("Cannot switch from" + trainingDefinition.getState() + " to "+ state, ErrorCode.RESOURCE_CONFLICT);
        }
        trainingDefinition.setLastEdited(LocalDateTime.now());
    }

    private AbstractLevel findLastLevel(Long levelId) {
        AbstractLevel lastLevel = abstractLevelRepository.findById(levelId)
                .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        levelId = lastLevel.getNextLevel();
        while (levelId != null) {
            lastLevel = abstractLevelRepository.findById(lastLevel.getNextLevel())
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            levelId = lastLevel.getNextLevel();
        }
        return lastLevel;
    }

    private boolean findLevelInDefinition(TrainingDefinition trainingDefinition, Long levelId) {
        Long nextId = trainingDefinition.getStartingLevel();
        Boolean found = false;
        if (nextId == levelId)
            found = true;

        while (nextId != null && !found) {
            AbstractLevel nextLevel = abstractLevelRepository.findById(nextId)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            if (nextLevel.getId().equals(levelId))
                found = true;
            nextId = nextLevel.getNextLevel();
        }
        return found;
    }

    private Long createLevels(Long id) {
        List<AbstractLevel> levels = new ArrayList<>();
        while (id != null) {
            AbstractLevel nextLevel = abstractLevelRepository.findById(id)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            id = nextLevel.getNextLevel();
            levels.add(nextLevel);
        }
        Long newId = null;
        for (int i = levels.size() - 1; i >= 0; i--) {
            //TODO clone post and pre hook ?
            if (levels.get(i) instanceof AssessmentLevel) {
                AssessmentLevel newAssessmentLevel = new AssessmentLevel();
                BeanUtils.copyProperties(levels.get(i), newAssessmentLevel);
                newAssessmentLevel.setId(null);
                newAssessmentLevel.setNextLevel(newId);
                newAssessmentLevel.setSnapshotHook(null);
                newId = assessmentLevelRepository.save(newAssessmentLevel).getId();
            } else if (levels.get(i) instanceof InfoLevel) {
                InfoLevel newInfoLevel = new InfoLevel();
                BeanUtils.copyProperties(levels.get(i), newInfoLevel);
                newInfoLevel.setId(null);
                newInfoLevel.setNextLevel(newId);
                newInfoLevel.setSnapshotHook(null);
                newId = infoLevelRepository.save(newInfoLevel).getId();
            } else {
                GameLevel newGameLevel = new GameLevel();
                BeanUtils.copyProperties(levels.get(i), newGameLevel);
                newGameLevel.setId(null);
                newGameLevel.setNextLevel(newId);
                newGameLevel.setSnapshotHook(null);
                newId = gameLevelRepository.save(newGameLevel).getId();
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
