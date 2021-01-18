package cz.muni.ics.kypo.training.persistence.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDTO;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class TestDataFactory {

    private SimpleModule simpleModule = new SimpleModule("SimpleModule").addSerializer(new LocalDateTimeUTCSerializer());
    private ObjectMapper mapper = new ObjectMapper().registerModule( new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(simpleModule)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private AssessmentLevel test = generateAssessmentLevel("Test", 50, 10L,
            1, "List of questions", "List of instructions", AssessmentType.TEST);
    private AssessmentLevel questionnaire = generateAssessmentLevel("Questionnaire", 0, 15L,
            2, "List of other questions", "List of some instructions", AssessmentType.QUESTIONNAIRE);
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO = generateAssessmentLevelUpdateDTO("New Assessment Title",
            19, "New questions", "New instructions", cz.muni.ics.kypo.training.api.enums.AssessmentType.QUESTIONNAIRE, 9);
    private AssessmentLevelImportDTO assessmentLevelImportDTO = generateAssessmentLevelImportDTO("Assessment level import", 15,
            "Questions import", "Instructions import", cz.muni.ics.kypo.training.api.enums.AssessmentType.TEST, 22);
    private AssessmentLevelDTO assessmentLevelDTO = generateAssessmentLevelDTO("Assessment DTO", 100, 12, "DTO questions",
            "DTO instructions", cz.muni.ics.kypo.training.api.enums.AssessmentType.TEST);

    private GameLevel penalizedLevel = generateGameLevel("Penalized Game Level", 100, 20L,
            3, "SecretFlag", "Content of penalized level", "Solution of penalized level",
            true, 5);
    private GameLevel nonPenalizedLevel = generateGameLevel("Non-Penalized Game Level", 50, 5L,
            4, "PublicFlag", "Content of non-penalized level", "Solution of non-penalized level",
            false, 99);
    private GameLevelUpdateDTO gameLevelUpdateDTO = generateGameLevelUpdateDTO("New Game Title", 99, "newFlag",
            "New Content", "New solution", true, 66, 6);
    private GameLevelImportDTO gameLevelImportDTO = generateGameLevelImportDTO("Game level import", 12, "ImportFlag", "Game level import content",
            "import solution", true, 9, 100);
    private GameLevelDTO gameLevelDTO = generateGameLevelDTO("DTO flag", "DTO game content", "DTO soulution", true, 8, "DTO game level",
            80, 25);

    private InfoLevel infoLevel1 = generateInfoLevel("Info level 1", 7L, 5, "Information");
    private InfoLevel infoLevel2 = generateInfoLevel("Info level 2", 9L, 6, "Content");
    private InfoLevelUpdateDTO infoLevelUpdateDTO = generateInfoLevelUpdateDTO("New Info Title", "New Info Content");
    private InfoLevelImportDTO infoLevelImportDTO = generateInfoLevelImportDTO("Info level import", 5, "Info level import content");
    private InfoLevelDTO infoLevelDTO = generateInfoLevelDTO("Info DTO", 3, "DTO content");

    private AbstractLevelDTO abstractLevelDTO = generateAbstractLevelDTO("AbstractLevelDTO", 8, LevelType.GAME_LEVEL, 8);
    private BasicLevelInfoDTO basicGameLevelInfoDTO = generateBasicLevelInfoDTO("Basic Game Level info", LevelType.GAME_LEVEL);
    private BasicLevelInfoDTO basicInfoLevelInfoDTO = generateBasicLevelInfoDTO("Basic Info Level info", LevelType.INFO_LEVEL);

    private AccessToken accessToken1 = generateAccessToken("test-0000");
    private AccessToken accessToken2 = generateAccessToken("token-9999");

    private Hint hint1 = generateHint("Hint 1", "Hint1 content", 25, 0);
    private Hint hint2 = generateHint("Hint 2", "Hint2 content", 50, 1);
    private Hint hint3 = generateHint("Hint 3", "Hint3 content", 75, 2);
    private HintDTO hintDTO = generateHintDTO("HintDTO", "Hint DTO content", 15);
    private HintImportDTO hintImportDTO = generateHintImportDTO("Hint Import", "Hint import content", 50);

    private TrainingDefinition unreleasedDefinition = generateTrainingDefinition("Unreleased definition", "Unreleased description",
            new String[]{"p1", "p2"}, new String[]{"o1", "o2"}, TDState.UNRELEASED, true,
            LocalDateTime.now(Clock.systemUTC()).minusHours(1));
    private TrainingDefinition releasedDefinition = generateTrainingDefinition("Released definition", "Released description",
            new String[]{"p3", "p4"}, new String[]{"o3"}, TDState.RELEASED, true,
            LocalDateTime.now(Clock.systemUTC()).minusHours(5));
    private TrainingDefinition archivedDefinition = generateTrainingDefinition("Archived definition", "Archived description",
            new String[]{"p5"}, new String[]{"o4", "o5", "o6"}, TDState.ARCHIVED, false,
            LocalDateTime.now(Clock.systemUTC()).minusHours(10));
    private TrainingDefinitionDTO unreleasedDefinitionDTO = generateTrainingDefinitionDTO(unreleasedDefinition);
    private TrainingDefinitionDTO releasedDefinitionDTO = generateTrainingDefinitionDTO(releasedDefinition);
    private TrainingDefinitionDTO archivedDefinitionDTO = generateTrainingDefinitionDTO(archivedDefinition);
    private TrainingDefinitionInfoDTO unreleasedDefinitionInfoDTO = generateTrainingDefinitionInfoDTO(unreleasedDefinition);
    private TrainingDefinitionInfoDTO releasedDefinitionInfoDTO = generateTrainingDefinitionInfoDTO(releasedDefinition);
    private TrainingDefinitionInfoDTO archivedDefinitionInfoDTO = generateTrainingDefinitionInfoDTO(archivedDefinition);
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO = generateTrainingDefinitionCreateDTO("Training definition create DTO",
            "Creation of definition", new String[]{"p8", "p9"}, new String[]{"o8", "o9"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED,
            true);
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO = generateTrainingDefinitionUpdateDTO("Training definition updaet DTO",
            "Update of definition", new String[]{"p6", "p7"}, new String[]{"o6", "o7"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED,
            false, 7L);
    private ImportTrainingDefinitionDTO importTrainingDefinitionDTO = generateImportTrainingDefinitionDTO("Imported definition", "Imported description",
            new String[]{"ip1", "ip2"}, new String[]{"io1", "io2"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED, true);
    private TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = generateTrainingDefinitionByIdDTO("TDbyId", "Definition by id",  new String[]{"p8", "p9"},
            new String[]{"o8", "o9"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED,false, false,
            20L, LocalDateTime.now(Clock.systemUTC()).minusHours(15));

    private TrainingInstance futureInstance = generateTrainingInstance(LocalDateTime.now(Clock.systemUTC()).plusHours(10),
            LocalDateTime.now(Clock.systemUTC()).plusHours(20), "Future Instance", 1L, "future-1111");
    private TrainingInstance ongoingInstance = generateTrainingInstance(LocalDateTime.now(Clock.systemUTC()).minusHours(10),
            LocalDateTime.now(Clock.systemUTC()).plusHours(10), "Ongoing Instance", 2L, "ongoing-2222");
    private TrainingInstance concludedInstance = generateTrainingInstance(LocalDateTime.now(Clock.systemUTC()).minusHours(20),
            LocalDateTime.now(Clock.systemUTC()).minusHours(5), "Concluded Instance", 3L, "concluded-3333");
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO = generateTrainingInstanceCreateDTO(LocalDateTime.now(Clock.systemUTC()).plusHours(15),
            LocalDateTime.now(Clock.systemUTC()).plusHours(22), "Create Instance", "create");
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO = generateTrainingInstanceUpdateDTO(LocalDateTime.now(Clock.systemUTC()).plusHours(5),
            LocalDateTime.now(Clock.systemUTC()).plusHours(7), "Update Instance", "update");
    private TrainingInstanceDTO trainingInstanceDTO = generateTrainingInstanceDTO(LocalDateTime.now(Clock.systemUTC()).plusHours(11),
            LocalDateTime.now(Clock.systemUTC()).plusHours(22), "Instance DTO", "DTO-5555", 10L);
    private TrainingInstanceArchiveDTO trainingInstanceArchiveDTO = generateTrainingInstanceArchiveDTO(LocalDateTime.now(Clock.systemUTC()).minusHours(20),
            LocalDateTime.now(Clock.systemUTC()).minusHours(10), "Archived instance", "archived-6666");

    private TrainingRun runningRun = generateTrainingRun(LocalDateTime.now(Clock.systemUTC()).minusHours(2), LocalDateTime.now(Clock.systemUTC()).plusHours(2),
            "logReference1", TRState.RUNNING, 2, true, 1L, 55,
            200, false, 2L, 20);
    private TrainingRun finishedRun = generateTrainingRun(LocalDateTime.now(Clock.systemUTC()).minusHours(10), LocalDateTime.now(Clock.systemUTC()).minusHours(5),
            "logReference2", TRState.FINISHED, 4, false, 3L, 80, 300, true, 4L, 0);
    private TrainingRun archivedRun = generateTrainingRun(LocalDateTime.now(Clock.systemUTC()).minusHours(20), LocalDateTime.now(Clock.systemUTC()).minusHours(10),
            "logReference3", TRState.ARCHIVED, 0, false, 5L, 500, 600, true, 6L, 0);
    private TrainingRunByIdDTO trainingRunByIdDTO = generateTrainingRunByIdDTO(LocalDateTime.now(Clock.systemUTC()).minusHours(2), LocalDateTime.now(Clock.systemUTC()).plusHours(2),
            "logReference1", cz.muni.ics.kypo.training.api.enums.TRState.RUNNING, 5L);
    private TrainingRunDTO trainingRunDTO = generateTrainingRunDTO(LocalDateTime.now(Clock.systemUTC()).minusHours(9), LocalDateTime.now(Clock.systemUTC()).minusHours(5),
            "logReference1", cz.muni.ics.kypo.training.api.enums.TRState.FINISHED, 7L);
    private AccessedTrainingRunDTO accessedTrainingRunDTO = generateAccessedTrainingRunDTO("Accessed run", LocalDateTime.now(Clock.systemUTC()).minusHours(8), LocalDateTime.now(Clock.systemUTC()).minusHours(4), 5,
            6, Actions.RESUME);

    private PoolInfoDTO poolInfoDTO = generatePoolInfoDTO(1L, 1L, 5L, 10L, 5L, "sha", "revSha");
    private SandboxInfo sandboxInfo = generateSandboxInfo(1L, 1, 4);
    private SandboxPoolInfo sandboxPoolInfo = generateSandboxPoolInfo(1L, 1L, 10L, 5L);
    private LockedPoolInfo lockedPoolInfo = generateLockedPoolInfo(1L, 1L);

    private UserRefDTO userRefDTO1 = generateUserRefDTO(10L, "Michael Bolt", "Bolt", "Michael", "mail1@muni.cz", "https://oidc.muni.cz/oidc", null);
    private UserRefDTO userRefDTO2 = generateUserRefDTO(12L, "Peter Most", "Most", "Peter", "mail2@muni.cz", "https://oidc.muni.cz/oidc", null);
    private UserRefDTO userRefDTO3 = generateUserRefDTO(14L, "John Nevel", "Nevel", "John", "mail38@muni.cz", "https://oidc.muni.cz/oidc", null);
    private UserRefDTO userRefDTO4 = generateUserRefDTO(17L, "Ted Mosby", "Mosby", "Ted", "mail4@muni.cz", "https://oidc.muni.cz/oidc", null);

    private UserRef userRef1 = generateUserRef( 10L);
    private UserRef userRef2 = generateUserRef(12L);
    private UserRef userRef3 = generateUserRef(14L);
    private UserRef userRef4 = generateUserRef(17L);

    public AssessmentLevel getTest(){
        return clone(test, AssessmentLevel.class);
    }

    public AssessmentLevel getQuestionnaire(){
        return clone(questionnaire, AssessmentLevel.class);
    }

    public GameLevel getPenalizedLevel(){
        return clone(penalizedLevel, GameLevel.class);
    }

    public GameLevel getNonPenalizedLevel(){
        return clone(nonPenalizedLevel, GameLevel.class);
    }

    public InfoLevel getInfoLevel1(){
        return clone(infoLevel1, InfoLevel.class);
    }

    public InfoLevel getInfoLevel2(){
        return clone(infoLevel2, InfoLevel.class);
    }

    public AccessToken getAccessToken1(){
        return clone(accessToken1, AccessToken.class);
    }

    public AccessToken getAccessToken2(){
        return clone(accessToken2, AccessToken.class);
    }

    public Hint getHint1(){
        return clone(hint1, Hint.class);
    }

    public Hint getHint2(){
        return clone(hint2, Hint.class);
    }

    public Hint getHint3(){
        return clone(hint3, Hint.class);
    }

    public TrainingDefinition getUnreleasedDefinition(){
        return clone(unreleasedDefinition, TrainingDefinition.class);
    }

    public TrainingDefinition getReleasedDefinition(){
        return clone(releasedDefinition, TrainingDefinition.class);
    }

    public TrainingDefinition getArchivedDefinition() {
        return clone(archivedDefinition, TrainingDefinition.class);
    }

    public TrainingDefinitionDTO getUnreleasedDefinitionDTO(){
        return clone(unreleasedDefinitionDTO, TrainingDefinitionDTO.class);
    }

    public TrainingDefinitionDTO getReleasedDefinitionDTO(){
        return clone(releasedDefinitionDTO, TrainingDefinitionDTO.class);
    }

    public TrainingDefinitionDTO getArchivedDefinitionDTO(){
        return clone(archivedDefinitionDTO, TrainingDefinitionDTO.class);
    }

    public TrainingDefinitionInfoDTO getUnreleasedDefinitionInfoDTO(){
        return clone(unreleasedDefinitionInfoDTO, TrainingDefinitionInfoDTO.class);
    }

    public TrainingDefinitionInfoDTO getReleasedDefinitionInfoDTO(){
        return clone(releasedDefinitionInfoDTO, TrainingDefinitionInfoDTO.class);
    }

    public TrainingDefinitionInfoDTO getArchivedDefinitionInfoDTO(){
        return clone(archivedDefinitionInfoDTO, TrainingDefinitionInfoDTO.class);
    }

    public TrainingInstance getFutureInstance(){
        return clone(futureInstance, TrainingInstance.class);
    }

    public TrainingInstance getOngoingInstance(){
        return clone(ongoingInstance, TrainingInstance.class);
    }

    public TrainingInstance getConcludedInstance(){
        return clone(concludedInstance, TrainingInstance.class);
    }

    public TrainingRun getRunningRun(){
        return clone(runningRun, TrainingRun.class);
    }

    public TrainingRun getFinishedRun(){
        return clone(finishedRun, TrainingRun.class);
    }

    public TrainingRun getArchivedRun(){
        return clone(archivedRun, TrainingRun.class);
    }

    public TrainingDefinitionCreateDTO getTrainingDefinitionCreateDTO() {
        return clone(trainingDefinitionCreateDTO, TrainingDefinitionCreateDTO.class);
    }

    public TrainingDefinitionUpdateDTO getTrainingDefinitionUpdateDTO() {
        return clone(trainingDefinitionUpdateDTO, TrainingDefinitionUpdateDTO.class);
    }

    public TrainingInstanceCreateDTO getTrainingInstanceCreateDTO() {
        return clone(trainingInstanceCreateDTO, TrainingInstanceCreateDTO.class);
    }

    public TrainingInstanceUpdateDTO getTrainingInstanceUpdateDTO() {
        return clone(trainingInstanceUpdateDTO, TrainingInstanceUpdateDTO.class);
    }

    public AssessmentLevelUpdateDTO getAssessmentLevelUpdateDTO() {
        return clone(assessmentLevelUpdateDTO, AssessmentLevelUpdateDTO.class);
    }

    public TrainingRunByIdDTO getTrainingRunByIdDTO(){
        return clone(trainingRunByIdDTO, TrainingRunByIdDTO.class);
    }


    public TrainingRunDTO getTrainingRunDTO(){
        return clone(trainingRunDTO, TrainingRunDTO.class);
    }

    public GameLevelUpdateDTO getGameLevelUpdateDTO(){
        return clone(gameLevelUpdateDTO, GameLevelUpdateDTO.class);
    }

    public InfoLevelUpdateDTO getInfoLevelUpdateDTO(){
        return clone(infoLevelUpdateDTO, InfoLevelUpdateDTO.class);
    }

    public InfoLevelImportDTO getInfoLevelImportDTO(){
        return clone(infoLevelImportDTO, InfoLevelImportDTO.class);
    }

    public GameLevelImportDTO getGameLevelImportDTO(){
        return clone(gameLevelImportDTO, GameLevelImportDTO.class);
    }

    public AssessmentLevelImportDTO getAssessmentLevelImportDTO(){
        return clone(assessmentLevelImportDTO, AssessmentLevelImportDTO.class);
    }

    public ImportTrainingDefinitionDTO getImportTrainingDefinitionDTO(){
        return clone(importTrainingDefinitionDTO, ImportTrainingDefinitionDTO.class);
    }

    public TrainingDefinitionByIdDTO getTrainingDefinitionByIdDTO(){
        return clone(trainingDefinitionByIdDTO, TrainingDefinitionByIdDTO.class);
    }

    public AbstractLevelDTO getAbstractLevelDTO() {
        return clone(abstractLevelDTO, AbstractLevelDTO.class);
    }

    public BasicLevelInfoDTO getBasicGameLevelInfoDTO(){
        return clone(basicGameLevelInfoDTO, BasicLevelInfoDTO.class);
    }
    public BasicLevelInfoDTO getBasicInfoLevelInfoDTO(){
        return clone(basicInfoLevelInfoDTO, BasicLevelInfoDTO.class);
    }

    public TrainingInstanceDTO getTrainingInstanceDTO(){
        return clone(trainingInstanceDTO, TrainingInstanceDTO.class);
    }

    public InfoLevelDTO getInfoLevelDTO(){
        return clone(infoLevelDTO, InfoLevelDTO.class);
    }

    public GameLevelDTO getGameLevelDTO(){
        return clone(gameLevelDTO, GameLevelDTO.class);
    }

    public AssessmentLevelDTO getAssessmentLevelDTO(){
        return clone(assessmentLevelDTO, AssessmentLevelDTO.class);
    }

    public AccessedTrainingRunDTO getAccessedTrainingRunDTO(){
        return clone(accessedTrainingRunDTO, AccessedTrainingRunDTO.class);
    }

    public HintDTO getHintDTO(){
        return clone(hintDTO, HintDTO.class);
    }

    public HintImportDTO getHintImportDTO(){
        return clone(hintImportDTO, HintImportDTO.class);
    }

    public TrainingInstanceArchiveDTO getTrainingInstanceArchiveDTO(){
        return clone(trainingInstanceArchiveDTO, TrainingInstanceArchiveDTO.class);
    }

    public PoolInfoDTO getPoolInfoDTO(){
        return clone(poolInfoDTO, PoolInfoDTO.class);
    }

    public SandboxInfo getSandboxInfo(){
        return clone(sandboxInfo, SandboxInfo.class);
    }

    public SandboxPoolInfo getSandboxPoolInfo(){
        return clone(sandboxPoolInfo, SandboxPoolInfo.class);
    }

    public LockedPoolInfo getLockedPoolInfo(){
        return clone(lockedPoolInfo, LockedPoolInfo.class);
    }

    public UserRefDTO getUserRefDTO1() { return clone(userRefDTO1, UserRefDTO.class);}
    public UserRefDTO getUserRefDTO2() { return clone(userRefDTO2, UserRefDTO.class);}
    public UserRefDTO getUserRefDTO3() { return clone(userRefDTO3, UserRefDTO.class);}
    public UserRefDTO getUserRefDTO4() { return clone(userRefDTO4, UserRefDTO.class);}


    public UserRef getUserRef1() { return clone(userRef1, UserRef.class);}
    public UserRef getUserRef2() { return clone(userRef2, UserRef.class);}
    public UserRef getUserRef3() { return clone(userRef3, UserRef.class);}
    public UserRef getUserRef4() { return clone(userRef4, UserRef.class);}

    private AssessmentLevel generateAssessmentLevel(String title, int maxScore, long estimatedDuration, int order,
                                                    String questions, String instructions, AssessmentType assessmentType){
        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setTitle(title);
        newAssessmentLevel.setMaxScore(maxScore);
        newAssessmentLevel.setEstimatedDuration(estimatedDuration);
        newAssessmentLevel.setOrder(order);
        newAssessmentLevel.setQuestions(questions);
        newAssessmentLevel.setInstructions(instructions);
        newAssessmentLevel.setAssessmentType(assessmentType);
        return newAssessmentLevel;
    }

    private GameLevel generateGameLevel(String title, int maxScore, long estimatedDuration, int order, String flag,
                                        String content, String solution, boolean solutionPenalized, int incorrectFlagLimit){
        GameLevel newGameLevel = new GameLevel();
        newGameLevel.setTitle(title);
        newGameLevel.setMaxScore(maxScore);
        newGameLevel.setEstimatedDuration(estimatedDuration);
        newGameLevel.setOrder(order);
        newGameLevel.setFlag(flag);
        newGameLevel.setContent(content);
        newGameLevel.setSolution(solution);
        newGameLevel.setSolutionPenalized(solutionPenalized);
        newGameLevel.setIncorrectFlagLimit(incorrectFlagLimit);
        return newGameLevel;
    }

    private InfoLevel generateInfoLevel(String title, long estimatedDuration, int order, String content){
        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setTitle(title);
        newInfoLevel.setMaxScore(0);
        newInfoLevel.setEstimatedDuration(estimatedDuration);
        newInfoLevel.setOrder(order);
        newInfoLevel.setContent(content);
        return newInfoLevel;
    }

    private AccessToken generateAccessToken(String accessToken){
        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setAccessToken(accessToken);
        return newAccessToken;
    }

    private Hint generateHint(String title, String content, Integer hintPenalty, int order){
        Hint newHint = new Hint();
        newHint.setTitle(title);
        newHint.setContent(content);
        newHint.setHintPenalty(hintPenalty);
        newHint.setOrder(order);
        return newHint;
    }

    private TrainingDefinition generateTrainingDefinition(String title, String description, String[] prerequisites,
                                                          String[] outcomes, TDState state, boolean showStepperBar,
                                                          LocalDateTime lastEdited){
        TrainingDefinition newTrainingDefinition = new TrainingDefinition();
        newTrainingDefinition.setTitle(title);
        newTrainingDefinition.setDescription(description);
        newTrainingDefinition.setPrerequisities(prerequisites);
        newTrainingDefinition.setOutcomes(outcomes);
        newTrainingDefinition.setState(state);
        newTrainingDefinition.setShowStepperBar(showStepperBar);
        newTrainingDefinition.setLastEdited(lastEdited);
        return newTrainingDefinition;
    }

    private TrainingDefinitionDTO generateTrainingDefinitionDTO(TrainingDefinition trainingDefinition){
        TrainingDefinitionDTO trainingDefinitionDTO = new TrainingDefinitionDTO();
        trainingDefinitionDTO.setTitle(trainingDefinition.getTitle());
        trainingDefinitionDTO.setDescription(trainingDefinition.getDescription());
        trainingDefinitionDTO.setPrerequisities(trainingDefinition.getPrerequisities());
        trainingDefinitionDTO.setOutcomes(trainingDefinition.getOutcomes());
        trainingDefinitionDTO.setState(mapToTDState(trainingDefinition.getState()));
        trainingDefinitionDTO.setShowStepperBar(trainingDefinition.isShowStepperBar());
        trainingDefinitionDTO.setLastEdited(trainingDefinition.getLastEdited());
        return trainingDefinitionDTO;
    }

    private TrainingDefinitionInfoDTO generateTrainingDefinitionInfoDTO(TrainingDefinition trainingDefinition){
        TrainingDefinitionInfoDTO trainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        trainingDefinitionInfoDTO.setTitle(trainingDefinition.getTitle());
        trainingDefinitionInfoDTO.setState(mapToTDState(trainingDefinition.getState()));
        return trainingDefinitionInfoDTO;
    }

    private TrainingInstance generateTrainingInstance(LocalDateTime starTime, LocalDateTime endTime, String title,
                                                      Long poolId, String accessToken){
        TrainingInstance newTrainingInstance = new TrainingInstance();
        newTrainingInstance.setStartTime(starTime);
        newTrainingInstance.setEndTime(endTime);
        newTrainingInstance.setTitle(title);
        newTrainingInstance.setPoolId(poolId);
        newTrainingInstance.setAccessToken(accessToken);
        return newTrainingInstance;
    }

    private TrainingRun generateTrainingRun(LocalDateTime startTime, LocalDateTime endTime, String eventLogReference, TRState state,
                                    int incorrectFlagCount, boolean solutionTaken, Long SBIRefId, int totalScore, int maxScore, boolean levelAnswered, Long previousSBIRefId, int currentPenalty){
        TrainingRun newTrainingRun = new TrainingRun();
        newTrainingRun.setStartTime(startTime);
        newTrainingRun.setEndTime(endTime);
        newTrainingRun.setEventLogReference(eventLogReference);
        newTrainingRun.setState(state);
        newTrainingRun.setIncorrectFlagCount(incorrectFlagCount);
        newTrainingRun.setSolutionTaken(solutionTaken);
        newTrainingRun.setSandboxInstanceRefId(SBIRefId);
        newTrainingRun.setTotalScore(totalScore);
        newTrainingRun.setMaxLevelScore(maxScore);
        newTrainingRun.setLevelAnswered(levelAnswered);
        newTrainingRun.setPreviousSandboxInstanceRefId(previousSBIRefId);
        newTrainingRun.setCurrentPenalty(currentPenalty);
        return newTrainingRun;
    }

    private TrainingDefinitionCreateDTO generateTrainingDefinitionCreateDTO(String title, String description, String[] prerequisites,
                                                                      String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                      boolean showStepperBar){
        TrainingDefinitionCreateDTO trainingDefinitionCreateDTO = new TrainingDefinitionCreateDTO();
        trainingDefinitionCreateDTO.setTitle(title);
        trainingDefinitionCreateDTO.setDescription(description);
        trainingDefinitionCreateDTO.setPrerequisities(prerequisites);
        trainingDefinitionCreateDTO.setOutcomes(outcomes);
        trainingDefinitionCreateDTO.setState(state);
        trainingDefinitionCreateDTO.setShowStepperBar(showStepperBar);
        return trainingDefinitionCreateDTO;
    }

    private TrainingDefinitionUpdateDTO generateTrainingDefinitionUpdateDTO(String title, String description, String[] prerequisites,
                                                                            String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                            boolean showStepperBar, Long SDRefId){
        TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();
        trainingDefinitionUpdateDTO.setTitle(title);
        trainingDefinitionUpdateDTO.setDescription(description);
        trainingDefinitionUpdateDTO.setPrerequisities(prerequisites);
        trainingDefinitionUpdateDTO.setOutcomes(outcomes);
        trainingDefinitionUpdateDTO.setState(state);
        trainingDefinitionUpdateDTO.setShowStepperBar(showStepperBar);
        return trainingDefinitionUpdateDTO;
    }

    private TrainingInstanceCreateDTO generateTrainingInstanceCreateDTO(LocalDateTime startTime, LocalDateTime endTime,
                                                                        String title, String accessToken){
        TrainingInstanceCreateDTO trainingInstanceCreateDTO = new TrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setStartTime(startTime);
        trainingInstanceCreateDTO.setEndTime(endTime);
        trainingInstanceCreateDTO.setTitle(title);
        trainingInstanceCreateDTO.setAccessToken(accessToken);
        return trainingInstanceCreateDTO;
    }

    private TrainingInstanceUpdateDTO generateTrainingInstanceUpdateDTO(LocalDateTime startTime, LocalDateTime endTime,
                                                                        String title, String accessToken){
        TrainingInstanceUpdateDTO trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setStartTime(startTime);
        trainingInstanceUpdateDTO.setEndTime(endTime);
        trainingInstanceUpdateDTO.setTitle(title);
        trainingInstanceUpdateDTO.setAccessToken(accessToken);
        return trainingInstanceUpdateDTO;
    }

    private GameLevelUpdateDTO generateGameLevelUpdateDTO(String title, int maxScore, String flag, String content, String solution,
                                                          boolean solutionPenalized, int estimatedDuration, int incorrectFlagLimit){
        GameLevelUpdateDTO gameLevelUpdateDTO = new GameLevelUpdateDTO();
        gameLevelUpdateDTO.setTitle(title);
        gameLevelUpdateDTO.setMaxScore(maxScore);
        gameLevelUpdateDTO.setFlag(flag);
        gameLevelUpdateDTO.setContent(content);
        gameLevelUpdateDTO.setSolution(solution);
        gameLevelUpdateDTO.setSolutionPenalized(solutionPenalized);
        gameLevelUpdateDTO.setEstimatedDuration(estimatedDuration);
        gameLevelUpdateDTO.setIncorrectFlagLimit(incorrectFlagLimit);
        return gameLevelUpdateDTO;
    }

    private AssessmentLevelUpdateDTO generateAssessmentLevelUpdateDTO(String title, int maxScore, String question, String instructions,
                                                                      cz.muni.ics.kypo.training.api.enums.AssessmentType type, int estimatedDuration){
        AssessmentLevelUpdateDTO assessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();
        assessmentLevelUpdateDTO.setTitle(title);
        assessmentLevelUpdateDTO.setMaxScore(maxScore);
        assessmentLevelUpdateDTO.setQuestions(question);
        assessmentLevelUpdateDTO.setInstructions(instructions);
        assessmentLevelUpdateDTO.setType(type);
        assessmentLevelUpdateDTO.setEstimatedDuration(estimatedDuration);
        return assessmentLevelUpdateDTO;
    }

    private InfoLevelUpdateDTO generateInfoLevelUpdateDTO(String title, String content){
        InfoLevelUpdateDTO infoLevelUpdateDTO = new InfoLevelUpdateDTO();
        infoLevelUpdateDTO.setTitle(title);
        infoLevelUpdateDTO.setContent(content);
        return infoLevelUpdateDTO;
    }

    private InfoLevelImportDTO generateInfoLevelImportDTO(String title, Integer estimatedDuration, String content){
        InfoLevelImportDTO infoLevelImportDTO = new InfoLevelImportDTO();
        infoLevelImportDTO.setTitle(title);
        infoLevelImportDTO.setContent(content);
        infoLevelImportDTO.setLevelType(LevelType.INFO_LEVEL);
        infoLevelImportDTO.setEstimatedDuration(estimatedDuration);
        return infoLevelImportDTO;
    }

    private AssessmentLevelImportDTO generateAssessmentLevelImportDTO(String title, Integer estimatedDuration, String questions, String instructions,
                                                                      cz.muni.ics.kypo.training.api.enums.AssessmentType type, int maxScore){
        AssessmentLevelImportDTO assessmentLevelImportDTO = new AssessmentLevelImportDTO();
        assessmentLevelImportDTO.setTitle(title);
        assessmentLevelImportDTO.setEstimatedDuration(estimatedDuration);
        assessmentLevelImportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assessmentLevelImportDTO.setQuestions(questions);
        assessmentLevelImportDTO.setInstructions(instructions);
        assessmentLevelImportDTO.setAssessmentType(type);
        assessmentLevelImportDTO.setMaxScore(maxScore);
        return assessmentLevelImportDTO;
    }

    private GameLevelImportDTO generateGameLevelImportDTO(String title, Integer estimatedDuration, String flag, String content, String solution, boolean solutionPenalized,
                                                          int incorrectFlagLimit, int maxScore){
        GameLevelImportDTO gameLevelImportDTO = new GameLevelImportDTO();
        gameLevelImportDTO.setTitle(title);
        gameLevelImportDTO.setEstimatedDuration(estimatedDuration);
        gameLevelImportDTO.setLevelType(LevelType.GAME_LEVEL);
        gameLevelImportDTO.setFlag(flag);
        gameLevelImportDTO.setContent(content);
        gameLevelImportDTO.setSolution(solution);
        gameLevelImportDTO.setSolutionPenalized(solutionPenalized);
        gameLevelImportDTO.setIncorrectFlagLimit(incorrectFlagLimit);
        gameLevelImportDTO.setMaxScore(maxScore);
        return gameLevelImportDTO;
    }

    private ImportTrainingDefinitionDTO generateImportTrainingDefinitionDTO(String title, String description, String[] prerequisites,
                                                                            String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                            boolean showStepperBar){
        ImportTrainingDefinitionDTO importTrainingDefinitionDTO = new ImportTrainingDefinitionDTO();
        importTrainingDefinitionDTO.setTitle(title);
        importTrainingDefinitionDTO.setDescription(description);
        importTrainingDefinitionDTO.setPrerequisities(prerequisites);
        importTrainingDefinitionDTO.setOutcomes(outcomes);
        importTrainingDefinitionDTO.setState(state);
        importTrainingDefinitionDTO.setShowStepperBar(showStepperBar);
        return importTrainingDefinitionDTO;
    }

    private TrainingDefinitionByIdDTO generateTrainingDefinitionByIdDTO(String title, String description, String[] prerequisites,
                                                                        String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                        boolean showStepperBar, boolean canBeArchived, long estimatedDuration,
                                                                        LocalDateTime lastEdited){
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = new TrainingDefinitionByIdDTO();
        trainingDefinitionByIdDTO.setTitle(title);
        trainingDefinitionByIdDTO.setDescription(description);
        trainingDefinitionByIdDTO.setPrerequisities(prerequisites);
        trainingDefinitionByIdDTO.setOutcomes(outcomes);
        trainingDefinitionByIdDTO.setState(state);
        trainingDefinitionByIdDTO.setShowStepperBar(showStepperBar);
        trainingDefinitionByIdDTO.setCanBeArchived(canBeArchived);
        trainingDefinitionByIdDTO.setEstimatedDuration(estimatedDuration);
        trainingDefinitionByIdDTO.setLastEdited(lastEdited);
        return trainingDefinitionByIdDTO;
    }

    private AbstractLevelDTO generateAbstractLevelDTO(String title, int maxScore, LevelType type, int estimatedDuration){
        AbstractLevelDTO abstractLevelDTO = new AbstractLevelDTO();
        abstractLevelDTO.setTitle(title);
        abstractLevelDTO.setMaxScore(maxScore);
        abstractLevelDTO.setLevelType(type);
        abstractLevelDTO.setEstimatedDuration(estimatedDuration);
        return abstractLevelDTO;
    }

    private BasicLevelInfoDTO generateBasicLevelInfoDTO(String title, LevelType levelType){
        BasicLevelInfoDTO basicLevelInfoDTO = new BasicLevelInfoDTO();
        basicLevelInfoDTO.setTitle(title);
        basicLevelInfoDTO.setLevelType(levelType);
        return basicLevelInfoDTO;
    }

    private TrainingInstanceDTO generateTrainingInstanceDTO(LocalDateTime start, LocalDateTime end, String title,
                                                            String accessToken, Long poolId){
        TrainingInstanceDTO trainingInstanceDTO = new TrainingInstanceDTO();
        trainingInstanceDTO.setStartTime(start);
        trainingInstanceDTO.setEndTime(end);
        trainingInstanceDTO.setTitle(title);
        trainingInstanceDTO.setAccessToken(accessToken);
        trainingInstanceDTO.setPoolId(poolId);
        return trainingInstanceDTO;
    }

    private TrainingRunByIdDTO generateTrainingRunByIdDTO(LocalDateTime start, LocalDateTime end, String logReference, cz.muni.ics.kypo.training.api.enums.TRState state,
                                                          Long SBIId){
        TrainingRunByIdDTO trainingRunByIdDTO = new TrainingRunByIdDTO();
        trainingRunByIdDTO.setStartTime(start);
        trainingRunByIdDTO.setEndTime(end);
        trainingRunByIdDTO.setEventLogReference(logReference);
        trainingRunByIdDTO.setState(state);
        trainingRunByIdDTO.setSandboxInstanceRefId(SBIId);
        return trainingRunByIdDTO;
    }

    private TrainingRunDTO generateTrainingRunDTO(LocalDateTime start, LocalDateTime end, String logReference, cz.muni.ics.kypo.training.api.enums.TRState state,
                                                  Long SBIId){
        TrainingRunDTO trainingRunDTO = new TrainingRunDTO();
        trainingRunDTO.setStartTime(start);
        trainingRunDTO.setEndTime(end);
        trainingRunDTO.setEventLogReference(logReference);
        trainingRunDTO.setState(state);
        trainingRunDTO.setSandboxInstanceRefId(SBIId);
        return trainingRunDTO;
    }

    private InfoLevelDTO generateInfoLevelDTO(String title, int estimatedDuration, String content){
        InfoLevelDTO infoLevelDTO = new InfoLevelDTO();
        infoLevelDTO.setTitle(title);
        infoLevelDTO.setMaxScore(0);
        infoLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        infoLevelDTO.setEstimatedDuration(estimatedDuration);
        infoLevelDTO.setContent(content);
        return infoLevelDTO;
    }

    private AssessmentLevelDTO generateAssessmentLevelDTO(String title, int maxScore, int estimatedDuration,
                                                          String questions, String instructions, cz.muni.ics.kypo.training.api.enums.AssessmentType assessmentType){
        AssessmentLevelDTO assessmentLevelDTO = new AssessmentLevelDTO();
        assessmentLevelDTO.setTitle(title);
        assessmentLevelDTO.setMaxScore(maxScore);
        assessmentLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assessmentLevelDTO.setEstimatedDuration(estimatedDuration);
        assessmentLevelDTO.setQuestions(questions);
        assessmentLevelDTO.setInstructions(instructions);
        assessmentLevelDTO.setAssessmentType(assessmentType);
        return assessmentLevelDTO;
    }

    private GameLevelDTO generateGameLevelDTO(String flag, String content, String solution, boolean solutionPenalized, int flagLimit, String title, int maxScore, int estimatedDuration){
        GameLevelDTO gameLevelDTO = new GameLevelDTO();
        gameLevelDTO.setFlag(flag);
        gameLevelDTO.setContent(content);
        gameLevelDTO.setSolution(solution);
        gameLevelDTO.setSolutionPenalized(solutionPenalized);
        gameLevelDTO.setIncorrectFlagLimit(flagLimit);
        gameLevelDTO.setTitle(title);
        gameLevelDTO.setMaxScore(maxScore);
        gameLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        gameLevelDTO.setEstimatedDuration(estimatedDuration);
        return gameLevelDTO;
    }

    private AccessedTrainingRunDTO generateAccessedTrainingRunDTO(String title, LocalDateTime start, LocalDateTime end, int currentLevelOrder,
                                                                  int numberOfLevels, Actions possibleAction){
        AccessedTrainingRunDTO accessedTrainingRunDTO = new AccessedTrainingRunDTO();
        accessedTrainingRunDTO.setTitle(title);
        accessedTrainingRunDTO.setTrainingInstanceStartDate(start);
        accessedTrainingRunDTO.setTrainingInstanceEndDate(end);
        accessedTrainingRunDTO.setCurrentLevelOrder(currentLevelOrder);
        accessedTrainingRunDTO.setNumberOfLevels(numberOfLevels);
        accessedTrainingRunDTO.setPossibleAction(possibleAction);
        return accessedTrainingRunDTO;
    }

    private HintDTO generateHintDTO(String title, String content, Integer penalty){
        HintDTO hintDTO = new HintDTO();
        hintDTO.setTitle(title);
        hintDTO.setContent(content);
        hintDTO.setHintPenalty(penalty);
        return hintDTO;
    }

    private HintImportDTO generateHintImportDTO(String title, String content, Integer penalty){
        HintImportDTO hintImportDTO = new HintImportDTO();
        hintImportDTO.setTitle(title);
        hintImportDTO.setContent(content);
        hintImportDTO.setHintPenalty(penalty);
        return hintImportDTO;
    }

    private TrainingInstanceArchiveDTO generateTrainingInstanceArchiveDTO(LocalDateTime start, LocalDateTime end, String title, String accessToken){
        TrainingInstanceArchiveDTO trainingInstanceArchiveDTO = new TrainingInstanceArchiveDTO();
        trainingInstanceArchiveDTO.setStartTime(start);
        trainingInstanceArchiveDTO.setEndTime(end);
        trainingInstanceArchiveDTO.setTitle(title);
        trainingInstanceArchiveDTO.setAccessToken(accessToken);
        return trainingInstanceArchiveDTO;
    }

    private PoolInfoDTO generatePoolInfoDTO(Long id, Long definitionId, Long lockId, Long maxSize, Long size, String sha, String revSha){
        PoolInfoDTO poolInfoDTO = new PoolInfoDTO();
        poolInfoDTO.setId(id);
        poolInfoDTO.setDefinitionId(definitionId);
        poolInfoDTO.setLockId(lockId);
        poolInfoDTO.setMaxSize(maxSize);
        poolInfoDTO.setSize(size);
        poolInfoDTO.setRev(sha);
        poolInfoDTO.setRevSha(revSha);
        return poolInfoDTO;
    }

    private SandboxInfo generateSandboxInfo(Long id, Integer lockId, Integer allocationUnit){
        SandboxInfo sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(id);
        sandboxInfo.setAllocationUnitId(allocationUnit);
        sandboxInfo.setLockId(lockId);
        return sandboxInfo;
    }

    private SandboxPoolInfo generateSandboxPoolInfo(Long id, Long definitionId, Long maxSize, Long size){
        SandboxPoolInfo sandboxPoolInfo = new SandboxPoolInfo();
        sandboxPoolInfo.setId(id);
        sandboxPoolInfo.setDefinitionId(definitionId);
        sandboxPoolInfo.setMaxSize(maxSize);
        sandboxPoolInfo.setSize(size);
        return sandboxPoolInfo;
    }

    private LockedPoolInfo generateLockedPoolInfo(Long id, Long poolId){
        LockedPoolInfo lockedPoolInfo = new LockedPoolInfo();
        lockedPoolInfo.setId(id);
        lockedPoolInfo.setPoolId(poolId);
        return lockedPoolInfo;
    }

    private UserRef generateUserRef(Long userRefId) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userRefId);
        return userRef;
    }

    private UserRefDTO generateUserRefDTO(Long userRefId, String fullName, String familyName, String givenName, String sub, String iss, byte[] picture) {
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefId(userRefId);
        userRefDTO.setUserRefFullName(fullName);
        userRefDTO.setUserRefFamilyName(familyName);
        userRefDTO.setUserRefGivenName(givenName);
        userRefDTO.setUserRefSub(sub);
        userRefDTO.setIss(iss);
        userRefDTO.setPicture(picture);
        return userRefDTO;
    }

    private <T> T clone(Object object, Class<T> tClass){
        try {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            String json = mapper.writeValueAsString(object);
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private cz.muni.ics.kypo.training.api.enums.TDState mapToTDState(TDState state) {
        switch (state) {
            case UNRELEASED: return cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED;
            case RELEASED: return cz.muni.ics.kypo.training.api.enums.TDState.RELEASED;
            case ARCHIVED: return cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED;
            case PRIVATED: return cz.muni.ics.kypo.training.api.enums.TDState.PRIVATED;
        }
        return null;
    }
}
