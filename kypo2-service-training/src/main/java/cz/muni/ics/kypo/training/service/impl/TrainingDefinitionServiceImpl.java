package cz.muni.ics.kypo.training.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Seda (441048)
 */
@Service
public class TrainingDefinitionServiceImpl implements TrainingDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionServiceImpl.class);

    private TrainingDefinitionRepository trainingDefinitionRepository;
    private TrainingInstanceRepository trainingInstanceRepository;

    private AbstractLevelRepository abstractLevelRepository;
    private GameLevelRepository gameLevelRepository;
    private InfoLevelRepository infoLevelRepository;
    private AssessmentLevelRepository assessmentLevelRepository;
    private AuthorRefRepository authorRefRepository;
    private SandboxDefinitionRefRepository sandboxDefinitionRefRepository;
    private static final String ARCHIVED_OR_RELEASED = "Cannot edit released or archived training definition.";
    private static final String LEVEL_NOT_FOUND = "Level not found.";

    @Autowired
    public TrainingDefinitionServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository,
                                         AbstractLevelRepository abstractLevelRepository, InfoLevelRepository infoLevelRepository, GameLevelRepository gameLevelRepository,
                                         AssessmentLevelRepository assessmentLevelRepository, TrainingInstanceRepository trainingInstanceRepository, @Lazy
                                         AuthorRefRepository authorRefRepository, SandboxDefinitionRefRepository sandboxDefinitionRefRepository) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.authorRefRepository = authorRefRepository;
        this.sandboxDefinitionRefRepository = sandboxDefinitionRefRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    public TrainingDefinition findById(Long id) {
        LOG.debug("findById({})", id);
        return trainingDefinitionRepository.findById(id).orElseThrow(
                () -> new ServiceLayerException("Training definition with id: " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).DESIGNER})")
    public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAll({},{})", predicate, pageable);
        if(isAdmin()) {
            return trainingDefinitionRepository.findAll(predicate, pageable);
        }
        return trainingDefinitionRepository.findAllByLoggedInAuthor(getSubOfLoggedInUser(), pageable);

    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("sub").getAsString();
    }
    private boolean isAdmin() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA: authentication.getUserAuthentication().getAuthorities()) {
            if(gA.getAuthority().equals("ADMINISTRATOR")) return true;
        }
        return false;
    }

    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).DESIGNER})")
    public TrainingDefinition create(TrainingDefinition trainingDefinition) {
        LOG.debug("create({})", trainingDefinition);
        Assert.notNull(trainingDefinition, "Input training definition must not be null");
        TrainingDefinition tD = trainingDefinitionRepository.save(trainingDefinition);
        LOG.info("Training definition with id: {} created.", trainingDefinition.getId());
        return tD;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public Page<TrainingDefinition> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
        LOG.debug("findAllBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
        return trainingDefinitionRepository.findAllBySandBoxDefinitionRefId(sandboxDefinitionId, pageable);

    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinition.id)")
    public void update(TrainingDefinition trainingDefinition) {
        LOG.debug("update({})", trainingDefinition);
        Assert.notNull(trainingDefinition, "Input training definition must not be null");
        TrainingDefinition tD = findById(trainingDefinition.getId());
        if (!tD.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);

        trainingDefinition.setStartingLevel(tD.getStartingLevel());
        trainingDefinitionRepository.save(trainingDefinition);
        LOG.info("Training definition with id: {} updated.", trainingDefinition.getId());
    }

    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).DESIGNER})")
    public TrainingDefinition clone(Long id) {
        LOG.debug("clone({})", id);
        TrainingDefinition trainingDefinition = findById(id);
        if (trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot copy unreleased training definition.", ErrorCode.RESOURCE_CONFLICT);
        TrainingDefinition tD = new TrainingDefinition();
        BeanUtils.copyProperties(trainingDefinition, tD);
        tD.setId(null);
        tD.setTitle("Clone of " + tD.getTitle());
        tD.setState(TDState.UNRELEASED);
        if (tD.getStartingLevel() != null) {
            tD.setStartingLevel(createLevels(tD.getStartingLevel()));
        }
        tD = trainingDefinitionRepository.save(tD);
        LOG.info("Training definition with id: {} cloned.", trainingDefinition.getId());
        return tD;

    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
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
            update(trainingDefinition);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
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
        if (oneBeforeId != null) {
            AbstractLevel oneBefore = abstractLevelRepository.findById(oneBeforeId)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            oneBefore.setNextLevel(swapLevel.getNextLevel());
            updateLevel(oneBefore);
        }
        AbstractLevel nextLevel = abstractLevelRepository.findById(swapLevel.getNextLevel())
                .orElseThrow(() -> new ServiceLayerException("Cannot swap right last level.", ErrorCode.RESOURCE_CONFLICT));
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
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    public void delete(Long id) {
        LOG.debug("delete({})", id);

        TrainingDefinition definition = findById(id);
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
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void deleteOneLevel(Long definitionId, Long levelId) {
        LOG.debug("deleteOneLevel({}, {})", definitionId, levelId);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
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
        deleteLevel(level);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void updateGameLevel(Long definitionId, GameLevel gameLevel) {
        LOG.debug("updateGameLevel({}, {})", definitionId, gameLevel);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (!findLevelInDefinition(trainingDefinition, gameLevel.getId()))
            throw new ServiceLayerException("Level was not found in definition.", ErrorCode.RESOURCE_NOT_FOUND);

        GameLevel gL = gameLevelRepository.findById(gameLevel.getId()).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + gameLevel.getId() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        gameLevel.setNextLevel(gL.getNextLevel());
        gameLevelRepository.save(gameLevel);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void updateInfoLevel(Long definitionId, InfoLevel infoLevel) {
        LOG.debug("updateInfoLevel({}, {})", definitionId, infoLevel);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (!findLevelInDefinition(trainingDefinition, infoLevel.getId()))
            throw new ServiceLayerException("Level was not found in definition.", ErrorCode.RESOURCE_NOT_FOUND);

        InfoLevel iL = infoLevelRepository.findById(infoLevel.getId()).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + infoLevel.getId() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        infoLevel.setNextLevel(iL.getNextLevel());
        infoLevelRepository.save(infoLevel);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) {
        LOG.debug("updateAssessmentLevel({}, {})", definitionId, assessmentLevel);
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException(ARCHIVED_OR_RELEASED, ErrorCode.RESOURCE_CONFLICT);
        if (!findLevelInDefinition(trainingDefinition, assessmentLevel.getId()))
            throw new ServiceLayerException("Level was not found in definition", ErrorCode.RESOURCE_NOT_FOUND);

        AssessmentLevel aL = assessmentLevelRepository.findById(assessmentLevel.getId()).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + assessmentLevel.getId() + ", not found.",
                        ErrorCode.RESOURCE_NOT_FOUND));
        assessmentLevel.setNextLevel(aL.getNextLevel());
        if(!assessmentLevel.getQuestions().equals(aL.getQuestions())) {
            validQuestions(assessmentLevel.getQuestions());
        }
        assessmentLevelRepository.save(assessmentLevel);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public GameLevel createGameLevel(Long definitionId) {
        LOG.debug("createGameLevel({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot create level in released or archived training definition", ErrorCode.RESOURCE_CONFLICT);

        GameLevel newGameLevel = new GameLevel();
        newGameLevel.setMaxScore(100);
        newGameLevel.setTitle("New Game Level");
        newGameLevel.setIncorrectFlagLimit(5);
        newGameLevel.setFlag("");
        newGameLevel.setSolutionPenalized(true);
        newGameLevel.setSolution("");
        newGameLevel.setContent("");
        GameLevel gL = gameLevelRepository.save(newGameLevel);

        if (trainingDefinition.getStartingLevel() == null) {
            trainingDefinition.setStartingLevel(gL.getId());
            update(trainingDefinition);
        } else {
            AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
            lastLevel.setNextLevel(gL.getId());
            updateLevel(lastLevel);
        }
        LOG.info("Game level with id: {} created", gL.getId());
        return gL;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public InfoLevel createInfoLevel(Long definitionId) {
        LOG.debug("createInfoLevel({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot create level in released or archived training definition", ErrorCode.RESOURCE_CONFLICT);

        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setTitle("New Info Level");
        newInfoLevel.setContent("");
        newInfoLevel.setMaxScore(0);
        InfoLevel iL = infoLevelRepository.save(newInfoLevel);

        if (trainingDefinition.getStartingLevel() == null) {
            trainingDefinition.setStartingLevel(iL.getId());
            update(trainingDefinition);
        } else {
            AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
            lastLevel.setNextLevel(iL.getId());
            updateLevel(lastLevel);
        }
        LOG.info("Info level with id: {} created.", iL.getId());
        return iL;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public AssessmentLevel createAssessmentLevel(Long definitionId) {
        LOG.debug("createAssessmentLevel({})", definitionId);
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new ServiceLayerException("Cannot create level in released or archived training definition.", ErrorCode.RESOURCE_CONFLICT);

        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setTitle("New Assessment Level");
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setInstructions("");
        newAssessmentLevel.setQuestions("[]");
        AssessmentLevel aL = assessmentLevelRepository.save(newAssessmentLevel);

        if (trainingDefinition.getStartingLevel() == null) {
            trainingDefinition.setStartingLevel(aL.getId());
            update(trainingDefinition);
        } else {
            AbstractLevel lastLevel = findLastLevel(trainingDefinition.getStartingLevel());
            lastLevel.setNextLevel(aL.getId());
            updateLevel(lastLevel);
        }
        LOG.info("Assessment level with id: {} created.", aL.getId());
        return aL;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    public List<AbstractLevel> findAllLevelsFromDefinition(Long id) {
        LOG.debug("findAllLevelsFromDefinition({})", id);
        Assert.notNull(id, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(id);
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
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).DESIGNER})")
    public AbstractLevel findLevelById(Long levelId) {
        LOG.debug("findLevelById({})", levelId);
        Assert.notNull(levelId, "Input level id must not be null.");
        return abstractLevelRepository.findById(levelId)
                .orElseThrow(() -> new ServiceLayerException("Level with id: " + levelId + ", not found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public List<TrainingInstance> findAllTrainingInstancesByTrainingDefinitionId(Long id) {
        Assert.notNull(id, "Input definition id must not be null");
        return trainingInstanceRepository.findAllByTrainingDefinitionId(id);
    }

    @Override
    public AuthorRef findAuthorRefById(Long id) throws ServiceLayerException {
        return authorRefRepository.findById(id).orElseThrow(
                () -> new ServiceLayerException("Author ref with id" + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public SandboxDefinitionRef findSandboxDefinitionRefById(Long id) throws ServiceLayerException {
        return sandboxDefinitionRefRepository.findById(id).orElseThrow(
                () -> new ServiceLayerException("Sandbox definition ref with id" + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
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

    private boolean findLevelInDefinition(TrainingDefinition definition, Long levelId) {
        Long nextId = definition.getStartingLevel();
        Boolean found = false;
        if (nextId.equals(levelId))
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

    private void validQuestions(String questions) {
        try {
            JsonNode n = JsonLoader.fromString(questions);
            final JsonNode jsonSchema = JsonLoader.fromResource("/questions-schema.json");
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonValidator v = factory.getValidator();
            ProcessingReport report = v.validate(jsonSchema, n);
            if (!report.toString().contains("success")) {
                throw new IllegalArgumentException("Given questions are not not valid .\n" +  report.iterator().next());
            }

        } catch (IOException | ProcessingException ex) {
            throw new ServiceLayerException(ex.getMessage(), ErrorCode.UNEXPECTED_ERROR);
        }
    }
}
