package cz.cyberrange.platform.training.service.unit.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.cyberrange.platform.training.api.dto.CorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TraineeSandboxDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunBasicDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError;
import cz.cyberrange.platform.training.api.responses.SandboxAnswersInfo;
import cz.cyberrange.platform.training.api.responses.VariantAnswer;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.*;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TraineeSandboxService;
import cz.cyberrange.platform.training.service.services.TraineeSandboxService.SandboxReadiness;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      EnumMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      UserRefMapperImpl.class,
      LevelMapperImpl.class,
      HintMapperImpl.class,
      TrainingRunMapperImpl.class,
      BetaTestingGroupMapperImpl.class,
      QuestionMapperImpl.class,
      AttachmentMapperImpl.class,
      MitreTechniqueMapperImpl.class
    })
public class TrainingRunFacadeTest {

  @Autowired TrainingRunMapperImpl trainingRunMapper;
  @Autowired LevelMapper levelMapper;
  @Autowired HintMapperImpl hintMapper;
  @Autowired TestDataFactory testDataFactory;

  private TrainingRunFacade trainingRunFacade;

  @MockBean private TrainingRunService trainingRunService;
  @MockBean private SecurityService securityService;
  @MockBean private UserService userService;
  @MockBean private AnswersStorageApiService answersStorageApiService;
  @MockBean private TraineeSandboxService traineeSandboxService;

  private TrainingRun trainingRun1, trainingRun2;
  private Hint hint;
  private TrainingLevel trainingLevel;
  private InfoLevel infoLevel;
  private AssessmentLevel assessmentLevel;
  private UserRefDTO participantRefDTO;
  private UserRef participant;
  private TrainingInstance trainingInstance;
  private TrainingDefinition trainingDefinition;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    trainingRunFacade =
        new TrainingRunFacade(
            trainingRunService,
            answersStorageApiService,
            securityService,
            userService,
            trainingRunMapper,
            levelMapper,
            hintMapper,
            traineeSandboxService);

    participant = new UserRef();
    participant.setUserRefId(5L);
    participant.setId(1L);

    trainingRun1 = testDataFactory.getRunningRun();
    trainingRun1.setId(1L);
    trainingRun1.setParticipantRef(participant);

    hint = testDataFactory.getHint1();
    hint.setId(1L);

    participantRefDTO = new UserRefDTO();
    participantRefDTO.setUserRefSub("mail@test.cz");
    participantRefDTO.setUserRefId(2L);

    trainingRun2 = testDataFactory.getFinishedRun();
    trainingRun2.setId(2L);

    trainingDefinition = testDataFactory.getReleasedDefinition();
    trainingDefinition.setId(1L);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setId(1L);
    trainingLevel.setTrainingDefinition(trainingDefinition);
    trainingLevel.setOrder(0);
    trainingRun1.setCurrentLevel(trainingLevel);

    assessmentLevel = testDataFactory.getTest();
    assessmentLevel.setId(2L);
    assessmentLevel.setOrder(1);
    assessmentLevel.setTrainingDefinition(trainingDefinition);

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setId(3L);
    infoLevel.setOrder(2);
    infoLevel.setTrainingDefinition(trainingDefinition);

    trainingInstance = testDataFactory.getConcludedInstance();
    trainingInstance.setId(1L);
    trainingInstance.setTrainingDefinition(trainingDefinition);
    trainingRun1.setTrainingInstance(trainingInstance);
  }

  @Test
  public void findTrainingRunById() {
    given(trainingRunService.findById(any(Long.class))).willReturn(trainingRun1);
    given(userService.getUserRefDTOByUserRefId(anyLong())).willReturn(participantRefDTO);
    trainingRunFacade.findById(trainingRun1.getId());
    then(trainingRunService).should().findById(trainingRun1.getId());
  }

  @Test
  public void findAllTrainingRuns() {
    List<TrainingRun> expected = new ArrayList<>();
    expected.add(trainingRun1);
    expected.add(trainingRun2);

    Page<TrainingRun> p = new PageImpl<TrainingRun>(expected);

    PathBuilder<TrainingRun> tR = new PathBuilder<TrainingRun>(TrainingRun.class, "trainingRun");
    Predicate predicate = tR.isNotNull();

    given(trainingRunService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);
  }

  @Test
  public void isCorrectAnswerBeforeSolutionTaken() {
    given(trainingRunService.isCorrectAnswer(trainingRun1.getId(), "answer")).willReturn(true);
    given(trainingRunService.getRemainingAttempts(trainingRun1.getId())).willReturn(1);
    IsCorrectAnswerDTO correctAnswerDTO =
        trainingRunFacade.isCorrectAnswer(trainingRun1.getId(), "answer");
    assertTrue(correctAnswerDTO.isCorrect());
    assertEquals(1, correctAnswerDTO.getRemainingAttempts());
  }

  @Test
  public void isCorrectAnswerAfterSolutionTaken() {
    given(trainingRunService.isCorrectAnswer(trainingRun1.getId(), "answer")).willReturn(false);
    IsCorrectAnswerDTO correctAnswerDTO =
        trainingRunFacade.isCorrectAnswer(trainingRun1.getId(), "answer");
    assertFalse(correctAnswerDTO.isCorrect());
    assertEquals(0, correctAnswerDTO.getRemainingAttempts());
  }

  @Test
  public void accessTrainingRun() {
    given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString()))
        .willReturn(trainingInstance);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
    given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong()))
        .willReturn(Optional.empty());
    given(trainingRunService.createTrainingRun(trainingInstance, 1L)).willReturn(trainingRun1);
    trainingRunFacade.accessTrainingRun("password");
    then(trainingRunService)
        .should()
        .trAcquisitionLockToPreventManyRequestsFromSameUser(
            1l, trainingInstance.getId(), "password");
    then(trainingRunService).should().createTrainingRun(trainingInstance, 1L);
  }

  @Test
  public void accessRunningTrainingRun() {
    given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString()))
        .willReturn(trainingInstance);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
    given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong()))
        .willReturn(Optional.of(trainingRun1));
    given(trainingRunService.resumeTrainingRun(anyLong())).willReturn(trainingRun1);
    trainingRunFacade.accessTrainingRun("password");
    then(trainingRunService).should().resumeTrainingRun(anyLong());
  }

  @Test
  public void accessTrainingRunAllocatesOwnSandbox() {
    List<TraineeSandboxDTO> ownSandboxes = List.of(new TraineeSandboxDTO());
    givenTraineeAllocation();
    given(trainingRunService.findUnfinishedTrainingRunOfUser("password", 1L))
        .willReturn(Optional.empty());
    given(traineeSandboxService.canProvideSandbox("sub", trainingInstance.getPoolId()))
        .willReturn(true);
    given(trainingRunService.createTrainingRun(trainingInstance, 1L)).willReturn(trainingRun1);
    given(traineeSandboxService.prepareSandbox(trainingRun1, "sub"))
        .willReturn(SandboxReadiness.NOT_READY);
    given(traineeSandboxService.findOwnSandboxes("sub")).willReturn(ownSandboxes);

    AccessTrainingRunDTO result = trainingRunFacade.accessTrainingRun("password");

    then(traineeSandboxService).should().lockUser(1L);
    then(trainingRunService)
        .should()
        .trAcquisitionLockToPreventManyRequestsFromSameUser(
            1L, trainingInstance.getId(), "password");
    then(traineeSandboxService).should().prepareSandbox(trainingRun1, "sub");
    then(trainingRunService).should(never()).auditTrainingRunStarted(any(TrainingRun.class));
    assertEquals(trainingRun1.getId(), result.getTrainingRunID());
    assertNull(result.getAbstractLevelDTO());
    assertFalse(result.isAllowAllocate());
    assertEquals(ownSandboxes, result.getActiveSandboxes());
  }

  @Test
  public void accessTrainingRunKeepsAllocationWhenListingSandboxesFails() {
    givenTraineeAllocation();
    given(trainingRunService.findUnfinishedTrainingRunOfUser("password", 1L))
        .willReturn(Optional.empty());
    given(traineeSandboxService.canProvideSandbox("sub", trainingInstance.getPoolId()))
        .willReturn(true);
    given(trainingRunService.createTrainingRun(trainingInstance, 1L)).willReturn(trainingRun1);
    given(traineeSandboxService.prepareSandbox(trainingRun1, "sub"))
        .willReturn(SandboxReadiness.NOT_READY);
    given(traineeSandboxService.findOwnSandboxes("sub"))
        .willThrow(
            new MicroserviceApiException(
                HttpStatus.BAD_GATEWAY, PythonApiError.of("Sandbox service is down.")));

    AccessTrainingRunDTO result = trainingRunFacade.accessTrainingRun("password");

    then(traineeSandboxService).should().prepareSandbox(trainingRun1, "sub");
    then(trainingRunService)
        .should(never())
        .deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(anyLong(), anyLong());
    assertTrue(result.getActiveSandboxes().isEmpty());
  }

  @Test
  public void accessTrainingRunWithActiveSandboxElsewhere() {
    givenTraineeAllocation();
    given(trainingRunService.findUnfinishedTrainingRunOfUser("password", 1L))
        .willReturn(Optional.empty());
    given(traineeSandboxService.canProvideSandbox("sub", trainingInstance.getPoolId()))
        .willReturn(false);

    AccessTrainingRunDTO result = trainingRunFacade.accessTrainingRun("password");

    then(trainingRunService).should(never()).createTrainingRun(any(), anyLong());
    then(traineeSandboxService).should(never()).prepareSandbox(any(), anyString());
    assertNull(result.getTrainingRunID());
    assertFalse(result.isAllowAllocate());
  }

  @Test
  public void accessTrainingRunWhileOwnSandboxIsBuilt() {
    givenTraineeAllocation();
    given(trainingRunService.findUnfinishedTrainingRunOfUser("password", 1L))
        .willReturn(Optional.of(trainingRun1));
    given(traineeSandboxService.prepareSandbox(trainingRun1, "sub"))
        .willReturn(SandboxReadiness.NOT_READY);

    AccessTrainingRunDTO result = trainingRunFacade.accessTrainingRun("password");

    then(trainingRunService).should(never()).resumeTrainingRun(anyLong());
    assertEquals(trainingRun1.getId(), result.getTrainingRunID());
    assertNull(result.getAbstractLevelDTO());
    assertFalse(result.isAllowAllocate());
  }

  @Test
  public void accessTrainingRunStartsRunOnFirstOwnSandbox() {
    givenTraineeAllocation();
    given(trainingRunService.findUnfinishedTrainingRunOfUser("password", 1L))
        .willReturn(Optional.of(trainingRun1));
    given(traineeSandboxService.prepareSandbox(trainingRun1, "sub"))
        .willReturn(SandboxReadiness.FIRST_READY);

    AccessTrainingRunDTO result = trainingRunFacade.accessTrainingRun("password");

    then(trainingRunService).should().auditTrainingRunStarted(trainingRun1);
    then(trainingRunService).should(never()).resumeTrainingRun(anyLong());
    assertNotNull(result.getAbstractLevelDTO());
    assertTrue(result.isAllowAllocate());
  }

  @Test
  public void accessTrainingRunResumesRunInReadyOwnSandbox() {
    givenTraineeAllocation();
    given(trainingRunService.findUnfinishedTrainingRunOfUser("password", 1L))
        .willReturn(Optional.of(trainingRun1));
    given(traineeSandboxService.prepareSandbox(trainingRun1, "sub"))
        .willReturn(SandboxReadiness.READY);
    given(trainingRunService.resumeTrainingRun(trainingRun1.getId())).willReturn(trainingRun1);

    AccessTrainingRunDTO result = trainingRunFacade.accessTrainingRun("password");

    then(trainingRunService).should().resumeTrainingRun(trainingRun1.getId());
    then(trainingRunService).should(never()).auditTrainingRunStarted(any(TrainingRun.class));
    assertNotNull(result.getAbstractLevelDTO());
  }

  @Test
  public void accessTrainingRunAfterInstanceEnded() {
    givenTraineeAllocation();
    trainingInstance.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusMinutes(1));

    assertThrows(
        EntityConflictException.class, () -> trainingRunFacade.accessTrainingRun("password"));

    then(traineeSandboxService).should(never()).prepareSandbox(any(), anyString());
  }

  @Test
  public void resumeTrainingRunWithoutOwnSandbox() {
    givenResumeWithTraineeAllocation(SandboxReadiness.NONE);

    AccessTrainingRunDTO result = trainingRunFacade.resumeTrainingRun(trainingRun1.getId());

    then(traineeSandboxService).should().lockUser(participant.getUserRefId());
    then(traineeSandboxService).should().reload(trainingRun1);
    then(trainingRunService).should().checkTrainingRunResumable(trainingRun1);
    then(trainingRunService).should(never()).resumeTrainingRun(anyLong());
    assertNull(result.getAbstractLevelDTO());
    assertTrue(result.isAllowAllocate());
  }

  @Test
  public void resumeTrainingRunWhileOwnSandboxIsBuilt() {
    givenResumeWithTraineeAllocation(SandboxReadiness.NOT_READY);

    AccessTrainingRunDTO result = trainingRunFacade.resumeTrainingRun(trainingRun1.getId());

    assertNull(result.getAbstractLevelDTO());
    assertFalse(result.isAllowAllocate());
  }

  @Test
  public void resumeTrainingRunStartsRunOnFirstOwnSandbox() {
    givenResumeWithTraineeAllocation(SandboxReadiness.FIRST_READY);

    AccessTrainingRunDTO result = trainingRunFacade.resumeTrainingRun(trainingRun1.getId());

    then(trainingRunService).should().auditTrainingRunStarted(trainingRun1);
    then(trainingRunService).should(never()).resumeTrainingRun(anyLong());
    assertNotNull(result.getAbstractLevelDTO());
  }

  @Test
  public void finishTrainingRunRemovesOwnSandbox() {
    given(trainingRunService.finishTrainingRun(trainingRun1.getId())).willReturn(trainingRun1);
    given(traineeSandboxService.isEnabledFor(trainingInstance)).willReturn(true);

    trainingRunFacade.finishTrainingRun(trainingRun1.getId());

    then(traineeSandboxService).should().removeSandboxOfFinishedRun(trainingRun1);
  }

  private void givenResumeWithTraineeAllocation(SandboxReadiness readiness) {
    given(traineeSandboxService.isEnabled()).willReturn(true);
    given(traineeSandboxService.isEnabledFor(trainingInstance)).willReturn(true);
    given(trainingRunService.findById(trainingRun1.getId())).willReturn(trainingRun1);
    given(traineeSandboxService.refreshSandbox(trainingRun1)).willReturn(readiness);
  }

  @Test
  public void findOwnSandboxesWhenTraineeAllocationDisabled() {
    assertTrue(trainingRunFacade.findOwnSandboxes().isEmpty());
    then(traineeSandboxService).should(never()).findOwnSandboxes(anyString());
  }

  private void givenTraineeAllocation() {
    given(trainingRunService.getTrainingInstanceForParticularAccessToken("password"))
        .willReturn(trainingInstance);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
    given(securityService.getOidcSub()).willReturn("sub");
    given(traineeSandboxService.isEnabled()).willReturn(true);
    given(traineeSandboxService.isEnabledFor(trainingInstance)).willReturn(true);
    trainingInstance.setEndTime(LocalDateTime.now(Clock.systemUTC()).plusHours(1));
  }

  @Test
  public void deleteTrainingRun() {
    given(trainingRunService.deleteTrainingRun(trainingRun1.getId(), true, true))
        .willReturn(trainingRun1);
    trainingRunFacade.deleteTrainingRun(trainingRun1.getId(), true);
    then(trainingRunService).should().deleteTrainingRun(trainingRun1.getId(), true, true);
  }

  @Test
  public void getNextLevel() {
    given(trainingRunService.moveToNextLevel(trainingRun1.getId())).willReturn(trainingRun1);
    trainingRunFacade.getNextLevel(trainingRun1.getId());
    then(trainingRunService).should().moveToNextLevel(trainingRun1.getId());
  }

  @Test
  public void getHint() {
    given(trainingRunService.getHint(anyLong(), anyLong())).willReturn(hint);
    trainingRunFacade.getHint(1L, 1L);
    then(trainingRunService).should().getHint(1L, 1L);
  }

  @Test
  public void getSolution() {
    given(trainingRunService.getSolution(1L)).willReturn("game solution");
    trainingRunFacade.getSolution(trainingRun1.getId());
    then(trainingRunService).should().getSolution(1L);
  }

  @Test
  public void finishTrainingRun() {
    given(trainingRunService.finishTrainingRun(trainingRun1.getId())).willReturn(trainingRun1);
    trainingRunFacade.finishTrainingRun(trainingRun1.getId());
    then(trainingRunService).should().finishTrainingRun(trainingRun1.getId());
  }

  @Test
  public void getParticipant() {
    given(trainingRunService.findById(trainingRun1.getId())).willReturn(trainingRun1);
    given(userService.getUserRefDTOByUserRefId(participant.getUserRefId()))
        .willReturn(participantRefDTO);
    UserRefDTO foundParticipantRefDTO = trainingRunFacade.getParticipant(trainingRun1.getId());
    assertEquals(participantRefDTO, foundParticipantRefDTO);
  }

  @Test
  public void getCorrectAnswers() {
    TrainingLevel trainingLevelVariantAnswer = testDataFactory.getNonPenalizedLevel();
    trainingLevelVariantAnswer.setId(6L);
    trainingLevelVariantAnswer.setVariantAnswers(true);
    trainingLevelVariantAnswer.setAnswerVariableName("username");
    trainingLevelVariantAnswer.setAnswer(null);
    VariantAnswer variantAnswer = new VariantAnswer();
    variantAnswer.setAnswerVariableName("username");
    variantAnswer.setAnswerContent("john");
    SandboxAnswersInfo sandboxAnswersInfo = new SandboxAnswersInfo();
    sandboxAnswersInfo.setSandboxRefId(trainingRun1.getSandboxInstanceRefId());
    sandboxAnswersInfo.setVariantAnswers(new ArrayList<>(List.of(variantAnswer)));

    List<AbstractLevel> levels =
        new ArrayList<>(
            List.of(trainingLevel, infoLevel, assessmentLevel, trainingLevelVariantAnswer));
    given(trainingRunService.findByIdWithLevel(trainingRun1.getId())).willReturn(trainingRun1);
    given(trainingRunService.getLevels(trainingDefinition.getId())).willReturn(levels);
    given(answersStorageApiService.getAnswersBySandboxId(trainingRun1.getSandboxInstanceRefId()))
        .willReturn(sandboxAnswersInfo);
    List<CorrectAnswerDTO> correctAnswers =
        trainingRunFacade.getCorrectAnswers(trainingRun1.getId());

    assertEquals(2, correctAnswers.size());
    assertTrue(
        correctAnswers.containsAll(
            List.of(
                getCorrectAnswerDTO(trainingLevel, null),
                getCorrectAnswerDTO(
                    trainingLevelVariantAnswer, variantAnswer.getAnswerContent()))));
  }

  private CorrectAnswerDTO getCorrectAnswerDTO(
      TrainingLevel trainingLevel, String correctVariantAnswer) {
    CorrectAnswerDTO answerDTO = new CorrectAnswerDTO();
    answerDTO.setLevelId(trainingLevel.getId());
    answerDTO.setLevelTitle(trainingLevel.getTitle());
    answerDTO.setLevelOrder(trainingLevel.getOrder());
    answerDTO.setCorrectAnswer(
        correctVariantAnswer == null ? trainingLevel.getAnswer() : correctVariantAnswer);
    answerDTO.setVariableName(trainingLevel.getAnswerVariableName());
    return answerDTO;
  }

  @Test
  public void findTrainingRunsByIdsAsPrivilegedCallerReturnsUnmaskedSandboxReference() {
    trainingRun1.setSandboxInstanceRefId("sandbox-own-instance");
    UserRef otherParticipant = new UserRef();
    otherParticipant.setId(2L);
    otherParticipant.setUserRefId(77L);
    trainingRun2.setParticipantRef(otherParticipant);
    trainingRun2.setSandboxInstanceRefId("sandbox-other-instance");
    List<Long> ids = List.of(trainingRun1.getId(), trainingRun2.getId());

    given(trainingRunService.findAllByIds(ids)).willReturn(List.of(trainingRun1, trainingRun2));
    given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(true);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participant.getUserRefId());

    List<TrainingRunBasicDTO> result = trainingRunFacade.findTrainingRunsByIds(ids);

    assertEquals(trainingRun1.getSandboxInstanceRefId(), result.get(0).getSandboxInstanceRefId());
    assertEquals(trainingRun2.getSandboxInstanceRefId(), result.get(1).getSandboxInstanceRefId());
  }

  @Test
  public void
      findTrainingRunsByIdsAsNonPrivilegedCallerMasksOtherParticipantsSandboxReferenceStably() {
    trainingRun1.setSandboxInstanceRefId("sandbox-own-instance");
    UserRef otherParticipant = new UserRef();
    otherParticipant.setId(2L);
    otherParticipant.setUserRefId(77L);
    trainingRun2.setParticipantRef(otherParticipant);
    trainingRun2.setSandboxInstanceRefId("sandbox-other-instance");
    List<Long> ids = List.of(trainingRun1.getId(), trainingRun2.getId());

    given(trainingRunService.findAllByIds(ids)).willReturn(List.of(trainingRun1, trainingRun2));
    given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(false);
    given(securityService.isOrganizerOfGivenTrainingRuns(ids)).willReturn(false);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participant.getUserRefId());

    List<TrainingRunBasicDTO> firstResult = trainingRunFacade.findTrainingRunsByIds(ids);
    List<TrainingRunBasicDTO> secondResult = trainingRunFacade.findTrainingRunsByIds(ids);

    assertEquals(
        trainingRun1.getSandboxInstanceRefId(), firstResult.get(0).getSandboxInstanceRefId());
    assertNotEquals(
        trainingRun2.getSandboxInstanceRefId(), firstResult.get(1).getSandboxInstanceRefId());
    assertEquals(
        firstResult.get(1).getSandboxInstanceRefId(),
        secondResult.get(1).getSandboxInstanceRefId());
  }

  @Test
  public void findTrainingRunsByIdsWithAbsentSandboxReferenceKeepsItAbsentForNonPrivilegedCaller() {
    trainingRun1.setSandboxInstanceRefId(null);
    List<Long> ids = List.of(trainingRun1.getId());

    given(trainingRunService.findAllByIds(ids)).willReturn(List.of(trainingRun1));
    given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(false);
    given(securityService.isOrganizerOfGivenTrainingRuns(ids)).willReturn(false);
    given(securityService.getUserRefIdFromUserAndGroup())
        .willReturn(participant.getUserRefId() + 1);

    List<TrainingRunBasicDTO> result = trainingRunFacade.findTrainingRunsByIds(ids);

    assertNull(result.get(0).getSandboxInstanceRefId());
  }
}
