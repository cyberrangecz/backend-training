package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TrainingDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionService.class);
    @Value("${user-and-group-server.uri}")
    private String userAndGroupUrl;

    private TrainingDefinitionRepository trainingDefinitionRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private GameLevelRepository gameLevelRepository;
    private InfoLevelRepository infoLevelRepository;
    private AssessmentLevelRepository assessmentLevelRepository;
    private UserRefRepository userRefRepository;
    private SecurityService securityService;

    private static final String ARCHIVED_OR_RELEASED = "Cannot edit released or archived training definition.";
    private static final String LEVEL_NOT_FOUND = "Level not found.";

    @Autowired
    public TrainingDefinitionService(TrainingDefinitionRepository trainingDefinitionRepository,
                                     AbstractLevelRepository abstractLevelRepository, InfoLevelRepository infoLevelRepository, GameLevelRepository gameLevelRepository,
                                     AssessmentLevelRepository assessmentLevelRepository, TrainingInstanceRepository trainingInstanceRepository,
                                     UserRefRepository userRefRepository, SecurityService securityService) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.userRefRepository = userRefRepository;
        this.securityService = securityService;
    }

    /**
     * Finds specific Training Definition by id
     *
     * @param id of a Training Definition that would be returned
     * @return specific {@link TrainingDefinition} by id
     * @throws EntityNotFoundException training definition cannot be found
     */
    public TrainingDefinition findById(Long id) {
        return trainingDefinitionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, id, "Training definition not found.")));
    }

    /**
     * Find all Training Definitions by author if user is designer or all Training Definitions if user is admin.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return all {@link TrainingDefinition}s
     */
    public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.isAdmin()) {
            return trainingDefinitionRepository.findAll(predicate, pageable);
        }
        Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
        return trainingDefinitionRepository.findAll(predicate, pageable, loggedInUserId);
    }

    /**
     * Finds all Training Definitions accessible to users with the role of organizer.
     *
     * @param state represents a state of training definition if it is released or unreleased.
     * @param pageable pageable parameter with information about pagination.
     * @return all Training Definitions for organizers
     */
    public Page<TrainingDefinition> findAllForOrganizers(String state, Pageable pageable) {
        Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
        if(state != null && state.equals(TDState.RELEASED.name())) {
            return trainingDefinitionRepository.findAllForOrganizers(TDState.RELEASED, pageable);
        } else if (state != null && state.equals(TDState.UNRELEASED.name())) {
            if (securityService.isAdmin()) {
                if(state.equals(TDState.UNRELEASED.name())) {
                    return trainingDefinitionRepository.findAllForOrganizers(TDState.UNRELEASED, pageable);
                }
            } else if (securityService.isDesigner() && securityService.isOrganizer()) {
                return trainingDefinitionRepository.findAllForDesignersAndOrganizersUnreleased(loggedInUserId, pageable);
            } else {
                return trainingDefinitionRepository.findAllForOrganizersUnreleased(loggedInUserId, pageable);
            }
        }
        throw new InternalServerErrorException("It is required to provide training definition state that is RELEASED or UNRELEASED");
    }

    /**
     * creates new training definition
     *
     * @param trainingDefinition to be created
     * @return new {@link TrainingDefinition}
     */
    public TrainingDefinition create(TrainingDefinition trainingDefinition) {
        Assert.notNull(trainingDefinition, "Input training definition must not be null");

        addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinition);

        LOG.info("Training definition with id: {} created.", trainingDefinition.getId());
        return trainingDefinitionRepository.save(trainingDefinition);
    }

    /**
     * Updates given Training Definition
     *
     * @param trainingDefinitionToUpdate to be updated
     * @throws EntityNotFoundException training definition or one of the levels is not found.
     * @throws EntityConflictException released or archived training definition cannot be modified.
     */
    public void update(TrainingDefinition trainingDefinitionToUpdate) {
        Assert.notNull(trainingDefinitionToUpdate, "Input training definition must not be null");
        TrainingDefinition trainingDefinition = findById(trainingDefinitionToUpdate.getId());
        checkIfCanBeUpdated(trainingDefinition);
        addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinitionToUpdate);
        trainingDefinitionToUpdate.setEstimatedDuration(trainingDefinition.getEstimatedDuration());
        trainingDefinitionRepository.save(trainingDefinitionToUpdate);
        LOG.info("Training definition with id: {} updated.", trainingDefinitionToUpdate.getId());
    }

    /**
     * Creates new training definition by cloning existing one
     *
     * @param id    of definition to be cloned
     * @param title the title of the new cloned definition
     * @return cloned {@link TrainingDefinition}
     * @throws EntityNotFoundException training definition not found.
     * @throws EntityConflictException cannot clone unreleased training definition.
     */
    public TrainingDefinition clone(Long id, String title) {
        TrainingDefinition trainingDefinition = findById(id);
        TrainingDefinition clonedTrainingDefinition = new TrainingDefinition();
        BeanUtils.copyProperties(trainingDefinition, clonedTrainingDefinition);
        clonedTrainingDefinition.setId(null);
        clonedTrainingDefinition.setBetaTestingGroup(null);

        clonedTrainingDefinition.setTitle(title);
        clonedTrainingDefinition.setState(TDState.UNRELEASED);
        clonedTrainingDefinition.setAuthors(new HashSet<>());

        addLoggedInUserToTrainingDefinitionAsAuthor(clonedTrainingDefinition);
        clonedTrainingDefinition = trainingDefinitionRepository.save(clonedTrainingDefinition);
        cloneLevelsFromTrainingDefinition(trainingDefinition, clonedTrainingDefinition);

        LOG.info("Training definition with id: {} cloned.", trainingDefinition.getId());
        return clonedTrainingDefinition;
    }

    /**
     * Swaps between levels. Swap basically means swapping the order attribute between these two levels.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param swapLevelFrom - Id of a first level to be swapped.
     * @param swapLevelTo   - Id of a second level to be swapped.
     * @throws EntityNotFoundException training definition or one of the levels is not found.
     * @throws EntityConflictException released or archived training definition cannot be modified.
     */
    public void swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
        TrainingDefinition trainingDefinition = findById(definitionId);
        checkIfCanBeUpdated(trainingDefinition);
        AbstractLevel swapAbstractLevelFrom = abstractLevelRepository.findById(swapLevelFrom).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(AbstractLevel.class, "id", swapLevelFrom.getClass(), swapLevelFrom, LEVEL_NOT_FOUND)));
        AbstractLevel swapAbstractLevelTo = abstractLevelRepository.findById(swapLevelTo).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(AbstractLevel.class, "id", swapLevelTo.getClass(), swapLevelTo, LEVEL_NOT_FOUND)));
        int orderFromLevel = swapAbstractLevelFrom.getOrder();
        int orderToLevel = swapAbstractLevelTo.getOrder();
        swapAbstractLevelFrom.setOrder(orderToLevel);
        swapAbstractLevelTo.setOrder(orderFromLevel);

        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    }

    /**
     * Move level to the different position and modify orders of levels between moved level and new position.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param levelIdToBeMoved - id of the level to be moved to the new position
     * @param newPosition   - position where level will be moved
     * @throws EntityNotFoundException training definition or one of the levels is not found.
     * @throws EntityConflictException released or archived training definition cannot be modified.
     */
    public void moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
        Integer maxOrderOfLevel = abstractLevelRepository.getCurrentMaxOrder(definitionId);
        if (newPosition < 0) {
            newPosition = 0;
        } else if (newPosition > maxOrderOfLevel) {
            newPosition = maxOrderOfLevel;
        }
        TrainingDefinition trainingDefinition = findById(definitionId);
        checkIfCanBeUpdated(trainingDefinition);
        AbstractLevel levelToBeMoved = abstractLevelRepository.findById(levelIdToBeMoved).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(AbstractLevel.class, "id", levelIdToBeMoved.getClass(), levelIdToBeMoved, LEVEL_NOT_FOUND)));
        if (levelToBeMoved.getOrder() == newPosition) {
            return;
        } else if (levelToBeMoved.getOrder() < newPosition) {
            abstractLevelRepository.decreaseOrderOfLevels(definitionId, levelToBeMoved.getOrder() + 1, newPosition);
        } else {
            abstractLevelRepository.increaseOrderOfLevels(definitionId, newPosition, levelToBeMoved.getOrder() - 1);
        }
        levelToBeMoved.setOrder(newPosition);
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    }

    /**
     * Deletes specific training definition based on id
     *
     * @param definitionId of definition to be deleted
     * @throws EntityNotFoundException training definition or level is not found.
     * @throws EntityConflictException released training definition cannot be deleted.
     */
    public void delete(Long definitionId) {
        TrainingDefinition definition = findById(definitionId);
        if (definition.getState().equals(TDState.RELEASED))
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId, "Cannot delete released training definition."));
        if (trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId, "Cannot delete training definition with already created training instance. " +
                    "Remove training instance/s before deleting training definition."));
        }
        List<AbstractLevel> abstractLevels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
        abstractLevels.forEach(this::deleteLevel);
        trainingDefinitionRepository.delete(definition);
    }

    /**
     * Deletes specific level based on id
     *
     * @param definitionId - id of definition containing level to be deleted
     * @param levelId      - id of level to be deleted
     * @throws EntityNotFoundException training definition or level is not found.
     * @throws EntityConflictException level cannot be deleted in released or archived training definition.
     */
    public void deleteOneLevel(Long definitionId, Long levelId) {
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new EntityConflictException(new EntityErrorDetail(AbstractLevel.class, "id", levelId.getClass(), levelId, ARCHIVED_OR_RELEASED));
        Optional<AbstractLevel> abstractLevelToDelete = abstractLevelRepository.findById(levelId);
        if (abstractLevelToDelete.isPresent()) {
            trainingDefinition.setEstimatedDuration(trainingDefinition.getEstimatedDuration() - abstractLevelToDelete.get().getEstimatedDuration());
            int orderOfDeleted = abstractLevelToDelete.get().getOrder();
            deleteLevel(abstractLevelToDelete.get());
            List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
            for (AbstractLevel level : levels) {
                if (level.getOrder() > orderOfDeleted) level.setOrder(level.getOrder() - 1);
            }
        } else {
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", levelId.getClass(), levelId, LEVEL_NOT_FOUND));
        }
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    }

    /**
     * Updates game level in training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param gameLevelToUpdate    to be updated
     * @throws EntityNotFoundException training definition is not found.
     * @throws EntityConflictException level cannot be updated in released or archived training definition.
     */
    public void updateGameLevel(Long definitionId, GameLevel gameLevelToUpdate) {
        TrainingDefinition trainingDefinition = findById(definitionId);
        checkIfCanBeUpdated(trainingDefinition);
        if (!findLevelInDefinition(trainingDefinition, gameLevelToUpdate.getId()))
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", gameLevelToUpdate.getId().getClass(),
                    gameLevelToUpdate.getId(), "Level was not found in definition (id: " + definitionId + ")."));
        GameLevel gameLevel = gameLevelRepository.findById(gameLevelToUpdate.getId()).orElseThrow(() ->
                new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", gameLevelToUpdate.getId().getClass(),
                gameLevelToUpdate.getId(), LEVEL_NOT_FOUND)));
        gameLevelToUpdate.setOrder(gameLevel.getOrder());
        trainingDefinition.setEstimatedDuration(trainingDefinition.getEstimatedDuration() -
                gameLevel.getEstimatedDuration() + gameLevelToUpdate.getEstimatedDuration());
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
        gameLevelToUpdate.setTrainingDefinition(trainingDefinition);
        gameLevelRepository.save(gameLevelToUpdate);
    }

    /**
     * Updates info level in training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param infoLevelToUpdate    to be updated
     * @throws EntityNotFoundException training definition is not found.
     * @throws EntityConflictException level cannot be updated in released or archived training definition.
     */
    public void updateInfoLevel(Long definitionId, InfoLevel infoLevelToUpdate) {
        TrainingDefinition trainingDefinition = findById(definitionId);
        checkIfCanBeUpdated(trainingDefinition);
        if (!findLevelInDefinition(trainingDefinition, infoLevelToUpdate.getId()))
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", infoLevelToUpdate.getId().getClass(),
                    infoLevelToUpdate.getId(), "Level was not found in definition (id: " + definitionId + ")."));

        InfoLevel infoLevel = infoLevelRepository.findById(infoLevelToUpdate.getId()).orElseThrow(() ->
                new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", infoLevelToUpdate.getId().getClass(),
                        infoLevelToUpdate.getId(), LEVEL_NOT_FOUND)));
        infoLevelToUpdate.setOrder(infoLevel.getOrder());
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
        trainingDefinition.setEstimatedDuration(trainingDefinition.getEstimatedDuration() -
                infoLevel.getEstimatedDuration() + infoLevelToUpdate.getEstimatedDuration());
        infoLevelToUpdate.setTrainingDefinition(trainingDefinition);
        infoLevelRepository.save(infoLevelToUpdate);
    }

    /**
     * Updates assessment level in training definition
     *
     * @param definitionId    - id of training definition containing level to be updated
     * @param assessmentLevelToUpdate to be updated
     * @throws EntityNotFoundException training definition is not found.
     * @throws EntityConflictException level cannot be updated in released or archived training definition.
     */
    public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevelToUpdate) {
        TrainingDefinition trainingDefinition = findById(definitionId);
        checkIfCanBeUpdated(trainingDefinition);
        if (!findLevelInDefinition(trainingDefinition, assessmentLevelToUpdate.getId()))
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", assessmentLevelToUpdate.getId().getClass(),
                    assessmentLevelToUpdate.getId(), "Level was not found in definition (id: " + definitionId + ")."));

        AssessmentLevel assessmentLevel = assessmentLevelRepository.findById(assessmentLevelToUpdate.getId()).orElseThrow(() ->
                new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", assessmentLevelToUpdate.getId().getClass(),
                        assessmentLevelToUpdate.getId(), LEVEL_NOT_FOUND)));
        if (!assessmentLevelToUpdate.getQuestions().equals(assessmentLevel.getQuestions())) {
            AssessmentUtil.validQuestions(assessmentLevelToUpdate.getQuestions());
        }
        assessmentLevelToUpdate.setOrder(assessmentLevel.getOrder());
        trainingDefinition.setEstimatedDuration(trainingDefinition.getEstimatedDuration() -
                assessmentLevel.getEstimatedDuration() + assessmentLevelToUpdate.getEstimatedDuration());
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
        assessmentLevelToUpdate.setTrainingDefinition(trainingDefinition);
        assessmentLevelRepository.save(assessmentLevelToUpdate);
    }

    /**
     * Creates new game level
     *
     * @param definitionId - id of definition in which level will be created
     * @return new {@link GameLevel}
     * @throws EntityNotFoundException training definition is not found.
     * @throws EntityConflictException level cannot be created in released or archived training definition.
     */
    public GameLevel createGameLevel(Long definitionId) {
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                    "Cannot create level in released or archived training definition"));
        if (trainingInstanceRepository.existsAnyForTrainingDefinition(trainingDefinition.getId())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                    "Cannot update training definition with already created training instance. " +
                    "Remove training instance/s before updating training definition."));
        }

        GameLevel newGameLevel = new GameLevel();
        newGameLevel.setMaxScore(100);
        newGameLevel.setTitle("Title of game level");
        newGameLevel.setIncorrectFlagLimit(100);
        newGameLevel.setFlag("Secret flag");
        newGameLevel.setSolutionPenalized(true);
        newGameLevel.setSolution("Solution of the game should be here");
        newGameLevel.setContent("The test entry should be here");
        newGameLevel.setEstimatedDuration(1);
        newGameLevel.setOrder(getNextOrder(definitionId));
        newGameLevel.setTrainingDefinition(trainingDefinition);
        GameLevel gameLevel = gameLevelRepository.save(newGameLevel);
        trainingDefinition.setEstimatedDuration(trainingDefinition.getEstimatedDuration() + newGameLevel.getEstimatedDuration());
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
        LOG.info("Game level with id: {} created", gameLevel.getId());
        return gameLevel;
    }

    private int getNextOrder(Long definitionId) {
        return abstractLevelRepository.getCurrentMaxOrder(definitionId) + 1;
    }

    /**
     * Creates new info level
     *
     * @param definitionId - id of definition in which level will be created
     * @return new {@link InfoLevel}
     * @throws EntityNotFoundException training definition is not found.
     * @throws EntityConflictException level cannot be created in released or archived training definition.
     */
    public InfoLevel createInfoLevel(Long definitionId) {
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        checkIfCanBeUpdated(trainingDefinition);

        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setTitle("Title of info level");
        newInfoLevel.setContent("Content of info level should be here.");
        newInfoLevel.setOrder(getNextOrder(definitionId));
        newInfoLevel.setTrainingDefinition(trainingDefinition);
        InfoLevel infoLevel = infoLevelRepository.save(newInfoLevel);
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
        LOG.info("Info level with id: {} created.", infoLevel.getId());
        return infoLevel;
    }

    /**
     * Creates new assessment level
     *
     * @param definitionId - id of definition in which level will be created
     * @return new {@link AssessmentLevel}
     * @throws EntityNotFoundException training definition is not found.
     * @throws EntityConflictException level cannot be created in released or archived training definition.
     */
    public AssessmentLevel createAssessmentLevel(Long definitionId) {
        Assert.notNull(definitionId, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                    "Cannot create level in released or archived training definition"));
        if (trainingInstanceRepository.existsAnyForTrainingDefinition(trainingDefinition.getId())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                    "Cannot update training definition with already created training instance. " +
                            "Remove training instance/s before updating training definition."));
        }

        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setTitle("Title of assessment level");
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setInstructions("Instructions should be here");
        newAssessmentLevel.setQuestions("[{\"answer_required\":false,\"order\":0,\"penalty\":0,\"points\":0,\"text\":\"Example Question\",\"question_type\":\"FFQ\",\"correct_choices\":[]}]");
        newAssessmentLevel.setEstimatedDuration(1);
        newAssessmentLevel.setOrder(getNextOrder(definitionId));
        newAssessmentLevel.setTrainingDefinition(trainingDefinition);
        AssessmentLevel assessmentLevel = assessmentLevelRepository.save(newAssessmentLevel);
        trainingDefinition.setEstimatedDuration(trainingDefinition.getEstimatedDuration() + newAssessmentLevel.getEstimatedDuration());
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
        LOG.info("Assessment level with id: {} created.", assessmentLevel.getId());
        return assessmentLevel;
    }

    /**
     * Finds all levels from single definition
     *
     * @param definitionId of definition
     * @return list of {@link AbstractLevel} associated with training definition
     */
    public List<AbstractLevel> findAllLevelsFromDefinition(Long definitionId) {
        Assert.notNull(definitionId, "Definition id must not be null");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    }

    /**
     * Finds specific level by id
     *
     * @param levelId - id of wanted level
     * @return wanted {@link AbstractLevel}
     * @throws EntityNotFoundException level is not found.
     */
    public AbstractLevel findLevelById(Long levelId) {
        Assert.notNull(levelId, "Input level id must not be null.");
        return abstractLevelRepository.findByIdIncludinDefinition(levelId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", levelId.getClass(), levelId, "Level not found")));
    }

    /**
     * Find all training instances associated with training definition by id.
     *
     * @param id the id of training definition
     * @return the list of all {@link TrainingInstance}s associated with wanted {@link TrainingDefinition}
     */
    public List<TrainingInstance> findAllTrainingInstancesByTrainingDefinitionId(Long id) {
        Assert.notNull(id, "Input definition id must not be null");
        return trainingInstanceRepository.findAllByTrainingDefinitionId(id);
    }

    /**
     * Switch development state of definition from unreleased to released, or from released to archived or back to unreleased.
     *
     * @param definitionId - id of training definition
     * @param state        - new state of training definition
     */
    public void switchState(Long definitionId, cz.muni.ics.kypo.training.api.enums.TDState state) {
        TrainingDefinition trainingDefinition = findById(definitionId);
        if (trainingDefinition.getState().name().equals(state.name())) {
            return;
        }
        switch (trainingDefinition.getState()) {
            case UNRELEASED:
                if (state.equals(cz.muni.ics.kypo.training.api.enums.TDState.RELEASED))
                    trainingDefinition.setState(TDState.RELEASED);
                else
                    throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                            "Cannot switch from" + trainingDefinition.getState() + " to " + state));
                break;
            case RELEASED:
                if (state.equals(cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
                    trainingDefinition.setState(TDState.ARCHIVED);
                else if (state.equals(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED)) {
                    if (trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
                        throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                                "Cannot update training definition with already created training instance(s). " +
                                "Remove training instance(s) before changing the state from released to unreleased training definition."));
                    }
                    trainingDefinition.setState((TDState.UNRELEASED));
                }
                break;
            default:
                throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", definitionId.getClass(), definitionId,
                        "Cannot switch from " + trainingDefinition.getState() + " to " + state));
        }
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    }

    private boolean findLevelInDefinition(TrainingDefinition trainingDefinition, Long levelId) {
        Optional<AbstractLevel> abstractLevel = abstractLevelRepository.findLevelInDefinition(trainingDefinition.getId(), levelId);
        if (abstractLevel.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    private void cloneLevelsFromTrainingDefinition(TrainingDefinition trainingDefinition, TrainingDefinition clonedTrainingDefinition) {
        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinition.getId());
        if (levels == null || levels.size() == 0) {
            return;
        }
        levels.forEach(level -> {
            if (level instanceof AssessmentLevel) {
                AssessmentUtil.validQuestions(((AssessmentLevel) level).getQuestions());
                AssessmentLevel newAssessmentLevel = new AssessmentLevel();
                BeanUtils.copyProperties(level, newAssessmentLevel);
                newAssessmentLevel.setId(null);
                newAssessmentLevel.setTrainingDefinition(clonedTrainingDefinition);
                assessmentLevelRepository.save(newAssessmentLevel);
            }
            if (level instanceof InfoLevel) {
                InfoLevel newInfoLevel = new InfoLevel();
                BeanUtils.copyProperties(level, newInfoLevel);
                newInfoLevel.setId(null);
                newInfoLevel.setTrainingDefinition(clonedTrainingDefinition);
                infoLevelRepository.save(newInfoLevel);
            }
            if (level instanceof GameLevel) {
                GameLevel newGameLevel = new GameLevel();
                BeanUtils.copyProperties(level, newGameLevel);
                newGameLevel.setId(null);
                newGameLevel.setHints(null);
                Set<Hint> hints = new HashSet<>();
                for (Hint hint : ((GameLevel) level).getHints()) {
                    Hint newHint = new Hint();
                    BeanUtils.copyProperties(hint, newHint);
                    newHint.setId(null);
                    hints.add(newHint);
                }
                Set<Attachment> attachments = new HashSet<>();
                for (Attachment attachment : ((GameLevel) level).getAttachments()) {
                    Attachment newAttachment = new Attachment();
                    BeanUtils.copyProperties(attachment, newAttachment);
                    newAttachment.setId(null);
                    attachments.add(newAttachment);
                }
                newGameLevel.setHints(hints);
                newGameLevel.setAttachments(attachments);
                newGameLevel.setTrainingDefinition(clonedTrainingDefinition);
                gameLevelRepository.save(newGameLevel);
            }
        });
    }

    private void checkIfCanBeUpdated(TrainingDefinition trainingDefinition) {
        if (!trainingDefinition.getState().equals(TDState.UNRELEASED)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", trainingDefinition.getId().getClass(), trainingDefinition.getId(),
                    ARCHIVED_OR_RELEASED));
        }
        if (trainingInstanceRepository.existsAnyForTrainingDefinition(trainingDefinition.getId())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingDefinition.class, "id", trainingDefinition.getId().getClass(), trainingDefinition.getId(),
                    "Cannot update training definition with already created training instance. " +
                    "Remove training instance/s before updating training definition."));
        }

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

    private LocalDateTime getCurrentTimeInUTC() {
        return LocalDateTime.now(Clock.systemUTC());
    }

    private void addLoggedInUserToTrainingDefinitionAsAuthor(TrainingDefinition trainingDefinition) {
        Optional<UserRef> user = userRefRepository.findUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());
        if (user.isPresent()) {
            trainingDefinition.addAuthor(user.get());
        } else {
            UserRef newUser = securityService.createUserRefEntityByInfoFromUserAndGroup();
            trainingDefinition.addAuthor(newUser);
        }
        trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    }

}
