package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

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
    private SecurityService securityService;

    private static final String ARCHIVED_OR_RELEASED = "Cannot edit released or archived training definition.";
    private static final String LEVEL_NOT_FOUND = "Level not found.";

    @Autowired
    public TrainingDefinitionServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository,
                                         AbstractLevelRepository abstractLevelRepository, InfoLevelRepository infoLevelRepository, GameLevelRepository gameLevelRepository,
                                         AssessmentLevelRepository assessmentLevelRepository, TrainingInstanceRepository trainingInstanceRepository,
                                         UserRefRepository userRefRepository, RestTemplate restTemplate, SecurityService securityService) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.userRefRepository = userRefRepository;
        this.restTemplate = restTemplate;
        this.securityService = securityService;
    }

    @Override
    public TrainingDefinition findById(Long id) {
        return trainingDefinitionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, id, "Training definition not found.")));
    }

    @Override
    public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.isAdmin()) {
            return trainingDefinitionRepository.findAll(predicate, pageable);
        }
        Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
        return trainingDefinitionRepository.findAll(predicate, pageable, loggedInUserId);
    }

    @Override
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

    @Override
    public TrainingDefinition create(TrainingDefinition trainingDefinition) {
        Assert.notNull(trainingDefinition, "Input training definition must not be null");

        addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinition);

        LOG.info("Training definition with id: {} created.", trainingDefinition.getId());
        return trainingDefinitionRepository.save(trainingDefinition);
    }

    @Override
    public void update(TrainingDefinition trainingDefinitionToUpdate) {
        Assert.notNull(trainingDefinitionToUpdate, "Input training definition must not be null");
        TrainingDefinition trainingDefinition = findById(trainingDefinitionToUpdate.getId());
        checkIfCanBeUpdated(trainingDefinition);
        addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinitionToUpdate);
        trainingDefinitionToUpdate.setEstimatedDuration(trainingDefinition.getEstimatedDuration());
        trainingDefinitionRepository.save(trainingDefinitionToUpdate);
        LOG.info("Training definition with id: {} updated.", trainingDefinitionToUpdate.getId());
    }

    @Override
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


    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<AbstractLevel> findAllLevelsFromDefinition(Long definitionId) {
        Assert.notNull(definitionId, "Definition id must not be null");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    }

    @Override
    public AbstractLevel findLevelById(Long levelId) {
        Assert.notNull(levelId, "Input level id must not be null.");
        return abstractLevelRepository.findByIdIncludinDefinition(levelId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", levelId.getClass(), levelId, "Level not found")));
    }

    @Override
    public List<TrainingInstance> findAllTrainingInstancesByTrainingDefinitionId(Long id) {
        Assert.notNull(id, "Input definition id must not be null");
        return trainingInstanceRepository.findAllByTrainingDefinitionId(id);
    }

    @Override
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
