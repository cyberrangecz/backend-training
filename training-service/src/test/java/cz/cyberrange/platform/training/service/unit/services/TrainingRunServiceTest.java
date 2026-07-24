package cz.cyberrange.platform.training.service.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.TooManyRequestsException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AnswerSelection;
import cz.cyberrange.platform.training.opensearch.events.training.model.EventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.ExtendedMatchingEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.FreeFormEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.MultipleChoiceEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TRAcquisitionLock;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TRAcquisitionLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.services.AuditEventsService;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = {TestDataFactory.class})
public class TrainingRunServiceTest {

  private TrainingRunService trainingRunService;
  @Autowired private TestDataFactory testDataFactory;

  @MockBean private SubmissionRepository submissionRepository;
  @MockBean private TRAcquisitionLockRepository trAcquisitionLockRepository;
  @MockBean private TrainingRunRepository trainingRunRepository;
  @MockBean private AuditEventsService auditEventService;
  @MockBean private AbstractLevelRepository abstractLevelRepository;
  @MockBean private TrainingInstanceRepository trainingInstanceRepository;
  @MockBean private UserRefRepository participantRefRepository;
  @MockBean private QuestionAnswerRepository questionAnswerRepository;
  @MockBean private HintRepository hintRepository;
  @MockBean private SandboxApiService sandboxApiService;
  @MockBean private SecurityService securityService;
  @MockBean private AnswersStorageApiService answersStorageApiService;
  @MockBean private CommandEventsService commandEventsService;
  @MockBean private TrainingEventsService trainingEventsService;

  private TrainingRun trainingRun1, trainingRun2;
  private TrainingLevel trainingLevel, trainingLevel2;
  private AssessmentLevel assessmentLevel;
  private InfoLevel infoLevel, infoLevel2;
  private Hint hint1, hint2;
  private TrainingInstance trainingInstance1, trainingInstance2;
  private UserRef participantRef;
  private SandboxInfo sandboxInfo;
  private TrainingDefinition trainingDefinition, trainingDefinition2;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    trainingRunService =
        new TrainingRunService(
            trainingRunRepository,
            abstractLevelRepository,
            trainingInstanceRepository,
            participantRefRepository,
            hintRepository,
            auditEventService,
            answersStorageApiService,
            securityService,
            questionAnswerRepository,
            sandboxApiService,
            trAcquisitionLockRepository,
            submissionRepository,
            commandEventsService,
            trainingEventsService);

    trainingDefinition = testDataFactory.getReleasedDefinition();
    trainingDefinition.setId(1L);
    trainingDefinition.setAuthors(new HashSet<>());

    trainingDefinition2 = testDataFactory.getUnreleasedDefinition();
    trainingDefinition2.setId(2L);

    trainingInstance2 = testDataFactory.getConcludedInstance();
    trainingInstance2.setId(2L);
    trainingInstance2.setTrainingDefinition(trainingDefinition2);

    trainingInstance1 = testDataFactory.getOngoingInstance();
    trainingInstance1.setId(1L);
    trainingInstance1.setTrainingDefinition(trainingDefinition);

    participantRef = new UserRef();
    participantRef.setId(1L);
    participantRef.setUserRefId(3L);

    hint1 = testDataFactory.getHint1();
    hint1.setId(1L);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setId(1L);
    trainingLevel.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
    trainingLevel.setOrder(0);
    trainingLevel.setTrainingDefinition(trainingDefinition);
    hint1.setTrainingLevel(trainingLevel);

    trainingLevel2 = testDataFactory.getNonPenalizedLevel();
    trainingLevel2.setId(1L);
    trainingLevel2.setOrder(0);
    trainingLevel2.setTrainingDefinition(trainingDefinition2);

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setId(2L);
    infoLevel.setOrder(1);
    infoLevel.setTrainingDefinition(trainingDefinition);

    infoLevel2 = testDataFactory.getInfoLevel2();
    infoLevel2.setId(2L);
    infoLevel2.setOrder(1);
    infoLevel2.setTrainingDefinition(trainingDefinition2);

    sandboxInfo = new SandboxInfo();
    sandboxInfo.setId("7L");
    sandboxInfo.setAllocationUnitId(7);

    trainingRun1 = testDataFactory.getRunningRun();
    trainingRun1.setId(1L);
    trainingRun1.setCurrentLevel(trainingLevel);
    trainingRun1.setParticipantRef(participantRef);
    trainingRun1.setTrainingInstance(trainingInstance1);
    trainingRun1.setTrainingInstance(trainingInstance1);

    trainingRun2 = testDataFactory.getRunningRun();
    trainingRun2.setId(2L);
    trainingRun2.setCurrentLevel(infoLevel);
    trainingRun2.setParticipantRef(participantRef);
    trainingRun2.setTrainingInstance(trainingInstance2);

    assessmentLevel = testDataFactory.getTest();
    assessmentLevel.setId(3L);
  }

  @Test
  public void getTrainingRunById() {
    given(trainingRunRepository.findById(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));

    TrainingRun t = trainingRunService.findById(trainingRun1.getId());
    assertEquals(t.getId(), trainingRun1.getId());
    assertEquals(t.getState(), trainingRun1.getState());

    then(trainingRunRepository).should().findById(trainingRun1.getId());
  }

  @Test
  public void getTrainingRunByIdNotFound() {
    Long id = 6L;
    assertThrows(EntityNotFoundException.class, () -> trainingRunService.findById(id));
  }

  @Test
  public void findByIdWithLevel() {
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));

    Optional<TrainingRun> optionalTrainingRun =
        trainingRunRepository.findByIdWithLevel(trainingRun1.getId());
    assertTrue(optionalTrainingRun.isPresent());
    assertTrue(optionalTrainingRun.get().getCurrentLevel() instanceof TrainingLevel);
  }

  @Test
  public void findByIdLevelNotFound() {
    given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> trainingRunService.findByIdWithLevel(100L));
  }

  @Test
  public void findAll() {
    List<TrainingRun> expected = new ArrayList<>();
    expected.add(trainingRun1);
    expected.add(trainingRun2);

    Page<TrainingRun> p = new PageImpl<>(expected);
    PathBuilder<TrainingRun> t = new PathBuilder<>(TrainingRun.class, "trainingRun");
    Predicate predicate = t.isNotNull();
    given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

    Page<TrainingRun> pr = trainingRunService.findAll(predicate, PageRequest.of(0, 2));
    assertEquals(2, pr.getTotalElements());
  }

  @Test
  public void findAllEmpty() {
    Page<TrainingRun> p = new PageImpl<>(new ArrayList<>());
    PathBuilder<TrainingRun> t = new PathBuilder<>(TrainingRun.class, "trainingRun");
    Predicate predicate = t.isNotNull();
    given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

    Page<TrainingRun> pr = trainingRunRepository.findAll(predicate, PageRequest.of(0, 2));
    assertEquals(0, pr.getTotalElements());
  }

  @Test
  public void deleteFinishedTrainingRun() {
    trainingRun1.setState(TRState.FINISHED);
    given(trainingRunRepository.findById(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    trainingRunService.deleteTrainingRun(trainingRun1.getId(), false, false);

    then(trAcquisitionLockRepository)
        .should()
        .deleteByParticipantRefIdAndTrainingInstanceId(
            trainingRun1.getParticipantRef().getUserRefId(),
            trainingRun1.getTrainingInstance().getId());
    then(trainingRunRepository).should().delete(trainingRun1);
  }

  @Test
  public void deleteRunningTrainingRun() {
    trainingRun1.setState(TRState.RUNNING);
    given(trainingRunRepository.findById(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.deleteTrainingRun(trainingRun1.getId(), false, false));

    then(trAcquisitionLockRepository)
        .should(never())
        .deleteByParticipantRefIdAndTrainingInstanceId(
            trainingRun1.getParticipantRef().getUserRefId(),
            trainingRun1.getTrainingInstance().getId());
    then(trainingRunRepository).should(never()).delete(trainingRun1);
  }

  @Test
  public void deleteRunningTrainingRunForce() {
    trainingRun1.setState(TRState.RUNNING);
    given(trainingRunRepository.findById(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    trainingRunService.deleteTrainingRun(trainingRun1.getId(), true, false);

    then(trAcquisitionLockRepository)
        .should()
        .deleteByParticipantRefIdAndTrainingInstanceId(
            trainingRun1.getParticipantRef().getUserRefId(),
            trainingRun1.getTrainingInstance().getId());
    then(trainingRunRepository).should().delete(trainingRun1);
  }

  @Test
  public void deleteTrainingRunNotFound() {
    given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> trainingRunService.deleteTrainingRun(trainingRun1.getId(), true, false));

    then(trAcquisitionLockRepository)
        .should(never())
        .deleteByParticipantRefIdAndTrainingInstanceId(
            trainingRun1.getParticipantRef().getUserRefId(),
            trainingRun1.getTrainingInstance().getId());
    then(trainingRunRepository).should(never()).delete(trainingRun1);
  }

  @Test
  public void existsAnyForTrainingInstance() {
    given(
            trainingRunRepository.existsAnyForTrainingInstance(
                trainingRun1.getTrainingInstance().getId()))
        .willReturn(true);
    boolean result =
        trainingRunService.existsAnyForTrainingInstance(trainingRun1.getTrainingInstance().getId());
    assertTrue(result);
  }

  // TODO fix test

  //    @Test
  //    public void findAllByParticipantUserRefId() {
  //        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1,
  // trainingRun2));
  //
  // given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
  //        given(trainingRunRepository.findAllByParticipantRefId(eq(participantRef.getUserRefId()),
  // any(Predicate.class), any(PageRequest.class))).willReturn(expectedPage);
  //        Page<TrainingRun> resultPage =
  // trainingRunService.findAllByParticipantRefUserRefId(any(Predicate.class), any(Pageable.class));
  //
  //        assertEquals(expectedPage, resultPage);
  //
  // then(trainingRunRepository).should().findAllByParticipantRefId(participantRef.getUserRefId(),
  // any(Predicate.class), PageRequest.of(0, 2));
  //    }

  @Test
  public void findAllByTrainingInstanceId() {
    given(trainingRunRepository.findAllByTrainingInstanceId(trainingInstance1.getId()))
        .willReturn(Set.of(trainingRun1, trainingRun2));
    Set<TrainingRun> resultSet =
        trainingRunService.findAllByTrainingInstanceId(trainingRun1.getTrainingInstance().getId());
    assertEquals(Set.of(trainingRun1, trainingRun2), resultSet);
  }

  @Test
  public void moveToNextLevel() {
    List<AbstractLevel> levels = new ArrayList<>();
    levels.add(trainingLevel);
    levels.add(infoLevel);
    trainingRun1.setLevelAnswered(true);
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(any(Long.class)))
        .willReturn(levels);
    given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

    AbstractLevel resultAbstractLevel =
        trainingRunService.moveToNextLevel(trainingRun1.getId()).getCurrentLevel();

    assertEquals(trainingRun1.getCurrentLevel().getId(), resultAbstractLevel.getId());
    assertEquals(trainingRun1.getMaxLevelScore(), infoLevel.getMaxScore());
    assertTrue(
        trainingRun1.isLevelAnswered()); // because next level is info and it is always set to true
    then(trainingRunRepository).should().findByIdWithLevel(trainingRun1.getId());
    then(trainingRunRepository).should().save(trainingRun1);
  }

  @Test
  public void getNextLevelTrainingRunNotFound() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> trainingRunService.moveToNextLevel(trainingRun1.getId()));
  }

  @Test
  public void getNextLevelNotAnswered() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.moveToNextLevel(trainingRun1.getId()));
  }

  @Test
  public void getNextLevelNoNextLevel() {
    trainingRun2.setLevelAnswered(true);
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun2));
    given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
    assertThrows(
        EntityNotFoundException.class,
        () -> trainingRunService.moveToNextLevel(trainingRun2.getId()));
  }

  @Test
  public void findAllByTrainingDefinitionAndParticipant() {
    Page<TrainingRun> expectedPage = new PageImpl<>(Collections.singletonList(trainingRun2));
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
    given(
            trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(
                any(Long.class), eq(participantRef.getUserRefId()), any(Pageable.class)))
        .willReturn(expectedPage);

    Page<TrainingRun> resultPage =
        trainingRunService.findAllByTrainingDefinitionAndParticipant(
            trainingDefinition2.getId(), PageRequest.of(0, 2));

    assertEquals(expectedPage, resultPage);
    then(trainingRunRepository)
        .should()
        .findAllByTrainingDefinitionIdAndParticipantUserRefId(
            trainingDefinition2.getId(), participantRef.getUserRefId(), PageRequest.of(0, 2));
  }

  @Test
  public void findAllByTrainingDefinition() {
    Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1, trainingRun2));
    given(
            trainingRunRepository.findAllByTrainingDefinitionId(
                any(Long.class), any(PageRequest.class)))
        .willReturn(expectedPage);
    Page<TrainingRun> resultPage =
        trainingRunService.findAllByTrainingDefinition(
            trainingDefinition.getId(), PageRequest.of(0, 2));
    assertEquals(expectedPage, resultPage);
    then(trainingRunRepository)
        .should()
        .findAllByTrainingDefinitionId(trainingDefinition.getId(), PageRequest.of(0, 2));
  }

  @Test
  public void getLevels() {
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinition.getId()))
        .willReturn(List.of(trainingLevel, trainingLevel2, infoLevel));
    List<AbstractLevel> result = trainingRunService.getLevels(trainingDefinition.getId());
    assertEquals(List.of(trainingLevel, trainingLevel2, infoLevel), result);
  }

  @Test
  public void createTrainingRun() throws Exception {
    given(
            abstractLevelRepository.findFirstLevelByTrainingDefinitionId(
                eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
        .willReturn(List.of(trainingLevel));
    given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
        .willReturn(Optional.of(participantRef));
    given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

    TrainingRun trainingRun =
        trainingRunService.createTrainingRun(trainingInstance1, participantRef.getUserRefId());
    then(trainingRunRepository).should().save(any(TrainingRun.class));
    assertEquals(trainingRun1, trainingRun);
  }

  @Test
  public void createTrainingRunNewParticipant() throws Exception {
    UserRef newParticipant = new UserRef();
    newParticipant.setUserRefId(participantRef.getUserRefId());
    sandboxInfo.setLockId(1);
    given(
            abstractLevelRepository.findFirstLevelByTrainingDefinitionId(
                eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
        .willReturn(List.of(trainingLevel));
    given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
        .willReturn(Optional.empty());
    given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(newParticipant.getUserRefId());
    given(participantRefRepository.save(newParticipant)).willReturn(participantRef);

    TrainingRun trainingRun =
        trainingRunService.createTrainingRun(trainingInstance1, participantRef.getUserRefId());
    then(trainingRunRepository).should().save(any(TrainingRun.class));
    then(participantRefRepository).should().createOrGet(participantRef.getUserRefId());
    assertEquals(trainingRun1, trainingRun);
  }

  @Test
  public void createTrainingRunNoStartingLevel() {
    trainingInstance1.setTrainingDefinition(trainingDefinition);
    given(
            abstractLevelRepository.findFirstLevelByTrainingDefinitionId(
                eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
        .willReturn(List.of());
    assertThrows(
        EntityNotFoundException.class,
        () ->
            trainingRunService.createTrainingRun(trainingInstance1, participantRef.getUserRefId()));
  }

  @Test
  public void createTrainingRunUserManagementError() {
    given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
        .willReturn(Optional.empty());
    willThrow(
            new MicroserviceApiException(
                HttpStatus.CONFLICT,
                JavaApiError.of("Error when calling user " + "managements service")))
        .given(securityService)
        .getUserRefIdFromUserAndGroup();
  }

  @Test
  public void findRunningTrainingRunOfUser() {
    given(
            trainingRunRepository.findRunningTrainingRunOfUser(
                trainingInstance1.getAccessToken(), participantRef.getUserRefId()))
        .willReturn(Optional.of(trainingRun1));
    Optional<TrainingRun> result =
        trainingRunService.findRunningTrainingRunOfUser(
            trainingInstance1.getAccessToken(), participantRef.getUserRefId());
    assertTrue(result.isPresent());
  }

  @Test
  public void trAcquisitionLockToPreventManyRequestsFromSameUser() {
    TRAcquisitionLock trAcquisitionLock =
        new TRAcquisitionLock(
            participantRef.getUserRefId(), trainingInstance1.getId(), LocalDateTime.now());
    trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(
        participantRef.getUserRefId(),
        trainingInstance1.getId(),
        trainingInstance1.getAccessToken());
    then(trAcquisitionLockRepository).should().saveAndFlush(trAcquisitionLock);
  }

  @Test
  public void trAcquisitionLockToPreventManyRequestsFromSameUserAlreadyExists() {
    TRAcquisitionLock trAcquisitionLock =
        new TRAcquisitionLock(
            participantRef.getUserRefId(), trainingInstance1.getId(), LocalDateTime.now());
    willThrow(DataIntegrityViolationException.class)
        .given(trAcquisitionLockRepository)
        .saveAndFlush(trAcquisitionLock);
    assertThrows(
        TooManyRequestsException.class,
        () ->
            trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(
                participantRef.getUserRefId(),
                trainingInstance1.getId(),
                trainingInstance1.getAccessToken()));
  }

  @Test
  public void deleteTrAcquisitionLockToPreventManyRequestsFromSameUser() {
    trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(
        participantRef.getId(), trainingInstance1.getId());
    then(trAcquisitionLockRepository)
        .should()
        .deleteByParticipantRefIdAndTrainingInstanceId(
            participantRef.getId(), trainingInstance1.getId());
  }

  @Test
  public void assignSandbox() throws Exception {
    trainingRun1.setSandboxInstanceRefId(null);
    trainingRun1.setSandboxInstanceAllocationId(null);
    given(
            sandboxApiService.getAndLockSandbox(
                anyLong(), eq(trainingRun1.getTrainingInstance().getAccessToken())))
        .willReturn(sandboxInfo);
    trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
    then(trainingRunRepository).should().save(trainingRun1);
    assertEquals(sandboxInfo.getId(), trainingRun1.getSandboxInstanceRefId());
    assertEquals(sandboxInfo.getAllocationUnitId(), trainingRun1.getSandboxInstanceAllocationId());
  }

  @Test
  public void assignSandboxNoAvailable() throws Exception {
    trainingRun1.setSandboxInstanceRefId(null);
    trainingRun1.setSandboxInstanceAllocationId(null);
    willThrow(
            new ForbiddenException(
                "There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes."))
        .given(sandboxApiService)
        .getAndLockSandbox(anyLong(), eq(trainingRun1.getTrainingInstance().getAccessToken()));
    assertThrows(
        ForbiddenException.class,
        () ->
            trainingRunService.assignSandbox(
                trainingRun1, trainingRun1.getTrainingInstance().getPoolId()));
    then(trainingRunRepository).should(never()).save(trainingRun1);
  }

  @Test
  public void assignSandboxMicroserviceException() throws Exception {
    trainingRun1.setSandboxInstanceRefId(null);
    trainingRun1.setSandboxInstanceAllocationId(null);
    willThrow(
            new MicroserviceApiException(
                "Error",
                new CustomWebClientException(
                    HttpStatus.NOT_FOUND, PythonApiError.of("Some error"))))
        .given(sandboxApiService)
        .getAndLockSandbox(anyLong(), eq(trainingRun1.getTrainingInstance().getAccessToken()));
    assertThrows(
        MicroserviceApiException.class,
        () ->
            trainingRunService.assignSandbox(
                trainingRun1, trainingRun1.getTrainingInstance().getPoolId()));
    then(trainingRunRepository).should(never()).save(trainingRun1);
  }

  @Test
  public void resumeTrainingRun() {
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRun1.getId());

    assertEquals(trainingRun.getId(), trainingRun1.getId());
    assertTrue(trainingRun.getCurrentLevel() instanceof TrainingLevel);
  }

  @Test
  public void resumeTrainingRunWithNotFound() {
    trainingRun1.setSandboxInstanceRefId(null);
    trainingRun1.setSandboxInstanceAllocationId(null);
    given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> trainingRunService.resumeTrainingRun(trainingRun1.getId()));
  }

  @Test
  public void resumeTrainingRunFinished() {
    trainingRun1.setState(TRState.FINISHED);
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.resumeTrainingRun(trainingRun1.getId()));
  }

  @Test
  public void resumeTrainingRunTrainingInstanceFinished() {
    trainingRun1
        .getTrainingInstance()
        .setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(2));
    trainingRun1
        .getTrainingInstance()
        .setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1));
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.resumeTrainingRun(trainingRun1.getId()));
  }

  @Test
  public void resumeTrainingRunDeletedSandbox() {
    trainingRun1.setSandboxInstanceRefId(null);
    trainingRun1.setSandboxInstanceAllocationId(null);
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.resumeTrainingRun(trainingRun1.getId()));
  }

  @Test
  public void isCorrectAnswer() {
    int scoreBefore = trainingRun1.getTotalTrainingScore() + trainingRun1.getTotalAssessmentScore();
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    boolean isCorrect =
        trainingRunService.isCorrectAnswer(trainingRun1.getId(), trainingLevel.getAnswer());
    assertTrue(isCorrect);
    assertEquals(
        scoreBefore + (trainingRun1.getMaxLevelScore() - trainingRun1.getCurrentPenalty()),
        trainingRun1.getTotalTrainingScore() + trainingRun1.getTotalAssessmentScore());
    assertTrue(trainingRun1.isLevelAnswered());
  }

  @Test
  public void isCorrectAnswerNotCorrect() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    boolean isCorrect = trainingRunService.isCorrectAnswer(trainingRun1.getId(), "wrong answer");
    assertFalse(isCorrect);
    assertFalse(trainingRun1.isLevelAnswered());
  }

  @Test
  public void isCorrectAnswerOfNonGameLevel() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId()))
        .willReturn(Optional.of(trainingRun2));
    assertThrows(
        BadRequestException.class,
        () -> trainingRunService.isCorrectAnswer(trainingRun2.getId(), "answer"));
  }

  @Test
  public void getRemainingAttempts() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.ofNullable(trainingRun1));
    int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
    assertEquals(
        trainingLevel.getIncorrectAnswerLimit() - trainingRun1.getIncorrectAnswerCount(), attempts);
  }

  @Test
  public void getSolution() {
    trainingRun1.setTotalTrainingScore(40);
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    String solution = trainingRunService.getSolution(trainingRun1.getId());
    assertEquals(solution, trainingLevel.getSolution());
    assertFalse(trainingRun1.isLevelAnswered());
  }

  @Test
  public void getSolutionAlreadyTaken() {
    trainingRun1.setSolutionTaken(true);
    given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId()))
        .willReturn(Optional.of(trainingRun1));
    String solution = trainingRunService.getSolution(trainingRun1.getId());
    assertEquals(solution, trainingLevel.getSolution());
    assertFalse(trainingRun1.isLevelAnswered());
  }

  @Test
  public void getSolutionNonGameLevel() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId()))
        .willReturn(Optional.of(trainingRun2));
    assertThrows(
        BadRequestException.class, () -> trainingRunService.getSolution(trainingRun2.getId()));
  }

  @Test
  public void getHint() {
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    given(hintRepository.findById(any(Long.class))).willReturn(Optional.of(hint1));
    Hint resultHint1 = trainingRunService.getHint(trainingRun1.getId(), hint1.getId());
    assertEquals(hint1, resultHint1);
    assertEquals(hint1.getHintPenalty(), (Integer) trainingRun1.getCurrentPenalty());
  }

  @Test
  public void getHintonGameLevel() {
    given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId()))
        .willReturn(Optional.of(trainingRun2));
    assertThrows(
        BadRequestException.class,
        () -> trainingRunService.getHint(trainingRun2.getId(), hint1.getId()));
  }

  @Test
  public void testFinishTrainingRun() {
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun2));
    given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
    trainingRunService.finishTrainingRun(trainingRun2.getId());
    then(trAcquisitionLockRepository)
        .should()
        .deleteByParticipantRefIdAndTrainingInstanceId(
            trainingRun2.getParticipantRef().getUserRefId(),
            trainingRun2.getTrainingInstance().getId());
    assertEquals(trainingRun2.getState(), TRState.FINISHED);
  }

  @Test
  public void testFinishTrainingRunNonLastLevel() {
    trainingRun1.setLevelAnswered(true);
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.finishTrainingRun(trainingRun1.getId()));
  }

  @Test
  public void testFinishTrainingRunNotAnsweredLevel() {
    given(trainingRunRepository.findByIdWithLevel(any(Long.class)))
        .willReturn(Optional.of(trainingRun1));
    assertThrows(
        EntityConflictException.class,
        () -> trainingRunService.finishTrainingRun(trainingRun1.getId()));
  }

  private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
    ClientResponse clientResponse =
        ClientResponse.create(HttpStatus.OK)
            .body(convertObjectToJsonBytes(body))
            .header(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    return Mono.just(clientResponse);
  }

  private static String convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }

  @Test
  public void questionnaireEventAnswersCarryNoCorrectnessAndZeroPoints() {
    Question freeForm = assessmentQuestion(101L, 0, QuestionType.FFQ);
    Question multipleChoice = assessmentQuestion(102L, 1, QuestionType.MCQ);
    multipleChoice.setChoices(
        new ArrayList<>(List.of(choice("Alpha", 0), choice("Beta", 1), choice("Gamma", 2))));
    Question extendedMatching = assessmentQuestion(103L, 2, QuestionType.EMI);
    extendedMatching.setExtendedMatchingStatements(
        new ArrayList<>(List.of(statement(0), statement(1))));

    AssessmentLevel level = new AssessmentLevel();
    level.setAssessmentType(AssessmentType.QUESTIONNAIRE);
    level.setQuestions(new ArrayList<>(List.of(freeForm, multipleChoice, extendedMatching)));

    TrainingRun run = testDataFactory.getRunningRun();
    run.setId(51L);
    run.setCurrentLevel(level);
    run.setLevelAnswered(false);
    given(trainingRunRepository.findByIdWithLevel(51L)).willReturn(Optional.of(run));

    Map<Long, QuestionAnswerDTO> submission = new LinkedHashMap<>();
    submission.put(101L, answerDto(101L, Set.of("my free answer"), null));
    submission.put(102L, answerDto(102L, Set.of("Gamma", "Alpha"), null));
    submission.put(103L, answerDto(103L, null, Map.of(0, 3, 1, 5)));

    trainingRunService.evaluateResponsesToAssessment(51L, submission);

    ArgumentCaptor<List<EventAnswer>> captor = ArgumentCaptor.forClass(List.class);
    then(auditEventService).should().auditAssessmentAnswersAction(eq(run), captor.capture());
    List<EventAnswer> answers = captor.getValue();

    assertEquals(3, answers.size());
    for (EventAnswer answer : answers) {
      assertNull(answer.getCorrect());
      assertEquals(0, answer.getPointsGained());
    }

    FreeFormEventAnswer freeFormAnswer = (FreeFormEventAnswer) answers.get(0);
    assertEquals("my free answer", freeFormAnswer.getAnswer().getValue());
    assertNull(freeFormAnswer.getAnswer().getCorrect());

    MultipleChoiceEventAnswer multipleChoiceAnswer = (MultipleChoiceEventAnswer) answers.get(1);
    assertEquals(
        List.of(0, 2),
        multipleChoiceAnswer.getSelectedOptions().stream().map(AnswerSelection::getValue).toList());
    multipleChoiceAnswer.getSelectedOptions().forEach(option -> assertNull(option.getCorrect()));

    ExtendedMatchingEventAnswer extendedMatchingAnswer =
        (ExtendedMatchingEventAnswer) answers.get(2);
    assertEquals(3, extendedMatchingAnswer.getPairs().get(0).getValue());
    assertEquals(5, extendedMatchingAnswer.getPairs().get(1).getValue());
    assertNull(extendedMatchingAnswer.getPairs().get(0).getCorrect());
    assertNull(extendedMatchingAnswer.getPairs().get(1).getCorrect());
  }

  @Test
  public void testEventAnswersCarryPerSelectionCorrectnessAndNetSignedPoints() {
    Question freeForm = assessmentQuestion(201L, 0, QuestionType.FFQ);
    freeForm.setPoints(10);
    freeForm.setPenalty(3);
    freeForm.setChoices(new ArrayList<>(List.of(choice("right", 0))));

    Question multipleChoice = assessmentQuestion(202L, 1, QuestionType.MCQ);
    multipleChoice.setPoints(5);
    multipleChoice.setPenalty(2);
    multipleChoice.setChoices(
        new ArrayList<>(
            List.of(choice("Alpha", 0, true), choice("Beta", 1, false), choice("Gamma", 2, true))));

    Question extendedMatching = assessmentQuestion(203L, 2, QuestionType.EMI);
    extendedMatching.setPoints(8);
    extendedMatching.setPenalty(4);
    extendedMatching.setExtendedMatchingStatements(
        new ArrayList<>(List.of(statement(0, 3), statement(1, 5))));

    Question correctZeroPoints = assessmentQuestion(204L, 3, QuestionType.FFQ);
    correctZeroPoints.setPoints(0);
    correctZeroPoints.setPenalty(1);
    correctZeroPoints.setChoices(new ArrayList<>(List.of(choice("ok", 0))));

    AssessmentLevel level = new AssessmentLevel();
    level.setAssessmentType(AssessmentType.TEST);
    level.setQuestions(
        new ArrayList<>(List.of(freeForm, multipleChoice, extendedMatching, correctZeroPoints)));

    TrainingRun run = testDataFactory.getRunningRun();
    run.setId(52L);
    run.setCurrentLevel(level);
    run.setLevelAnswered(false);
    given(trainingRunRepository.findByIdWithLevel(52L)).willReturn(Optional.of(run));

    Map<Long, QuestionAnswerDTO> submission = new LinkedHashMap<>();
    submission.put(201L, answerDto(201L, Set.of("right"), null));
    submission.put(202L, answerDto(202L, Set.of("Alpha"), null));
    submission.put(203L, answerDto(203L, null, Map.of(0, 3, 1, 9)));
    submission.put(204L, answerDto(204L, Set.of("ok"), null));

    trainingRunService.evaluateResponsesToAssessment(52L, submission);

    ArgumentCaptor<List<EventAnswer>> captor = ArgumentCaptor.forClass(List.class);
    then(auditEventService).should().auditAssessmentAnswersAction(eq(run), captor.capture());
    List<EventAnswer> answers = captor.getValue();

    assertEquals(4, answers.size());

    FreeFormEventAnswer freeFormAnswer = (FreeFormEventAnswer) answers.get(0);
    assertEquals(Boolean.TRUE, freeFormAnswer.getCorrect());
    assertEquals(10, freeFormAnswer.getPointsGained());
    assertEquals("right", freeFormAnswer.getAnswer().getValue());
    assertEquals(Boolean.TRUE, freeFormAnswer.getAnswer().getCorrect());

    MultipleChoiceEventAnswer multipleChoiceAnswer = (MultipleChoiceEventAnswer) answers.get(1);
    assertEquals(Boolean.FALSE, multipleChoiceAnswer.getCorrect());
    assertEquals(-2, multipleChoiceAnswer.getPointsGained());
    assertEquals(1, multipleChoiceAnswer.getSelectedOptions().size());
    assertEquals(0, multipleChoiceAnswer.getSelectedOptions().get(0).getValue());
    assertEquals(Boolean.TRUE, multipleChoiceAnswer.getSelectedOptions().get(0).getCorrect());

    ExtendedMatchingEventAnswer extendedMatchingAnswer =
        (ExtendedMatchingEventAnswer) answers.get(2);
    assertEquals(Boolean.FALSE, extendedMatchingAnswer.getCorrect());
    assertEquals(-4, extendedMatchingAnswer.getPointsGained());
    assertEquals(3, extendedMatchingAnswer.getPairs().get(0).getValue());
    assertEquals(Boolean.TRUE, extendedMatchingAnswer.getPairs().get(0).getCorrect());
    assertEquals(9, extendedMatchingAnswer.getPairs().get(1).getValue());
    assertEquals(Boolean.FALSE, extendedMatchingAnswer.getPairs().get(1).getCorrect());

    FreeFormEventAnswer zeroPointAnswer = (FreeFormEventAnswer) answers.get(3);
    assertEquals(Boolean.TRUE, zeroPointAnswer.getCorrect());
    assertEquals(0, zeroPointAnswer.getPointsGained());
    assertEquals(Boolean.TRUE, zeroPointAnswer.getAnswer().getCorrect());
  }

  @Test
  public void evaluateResponsesToAssessmentBuildsTypedEventAnswersInLevelOrder() {
    Question freeForm = assessmentQuestion(101L, 0, QuestionType.FFQ);
    Question multipleChoice = assessmentQuestion(102L, 1, QuestionType.MCQ);
    multipleChoice.setChoices(
        new ArrayList<>(List.of(choice("Alpha", 0), choice("Beta", 1), choice("Gamma", 2))));
    Question extendedMatching = assessmentQuestion(103L, 2, QuestionType.EMI);
    extendedMatching.setExtendedMatchingStatements(
        new ArrayList<>(List.of(statement(0), statement(1))));
    Question unansweredOptional = assessmentQuestion(104L, 3, QuestionType.FFQ);
    unansweredOptional.setAnswerRequired(false);

    AssessmentLevel level = new AssessmentLevel();
    level.setAssessmentType(AssessmentType.QUESTIONNAIRE);
    level.setQuestions(
        new ArrayList<>(List.of(freeForm, multipleChoice, extendedMatching, unansweredOptional)));

    TrainingRun run = testDataFactory.getRunningRun();
    run.setId(50L);
    run.setCurrentLevel(level);
    run.setLevelAnswered(false);
    given(trainingRunRepository.findByIdWithLevel(50L)).willReturn(Optional.of(run));

    Map<Long, QuestionAnswerDTO> submission = new LinkedHashMap<>();
    submission.put(101L, answerDto(101L, Set.of("my free answer"), null));
    submission.put(102L, answerDto(102L, Set.of("Gamma", "Alpha"), null));
    Map<Integer, Integer> pairs = new LinkedHashMap<>();
    pairs.put(1, 5);
    pairs.put(0, 3);
    submission.put(103L, answerDto(103L, null, pairs));

    trainingRunService.evaluateResponsesToAssessment(50L, submission);

    ArgumentCaptor<List<EventAnswer>> captor = ArgumentCaptor.forClass(List.class);
    then(auditEventService).should().auditAssessmentAnswersAction(eq(run), captor.capture());
    List<EventAnswer> answers = captor.getValue();

    assertEquals(3, answers.size());

    assertInstanceOf(FreeFormEventAnswer.class, answers.get(0));
    FreeFormEventAnswer freeFormAnswer = (FreeFormEventAnswer) answers.get(0);
    assertEquals(101L, freeFormAnswer.getQuestionId());
    assertEquals("my free answer", freeFormAnswer.getAnswer().getValue());

    assertInstanceOf(MultipleChoiceEventAnswer.class, answers.get(1));
    MultipleChoiceEventAnswer multipleChoiceAnswer = (MultipleChoiceEventAnswer) answers.get(1);
    assertEquals(102L, multipleChoiceAnswer.getQuestionId());
    assertEquals(
        List.of(0, 2),
        multipleChoiceAnswer.getSelectedOptions().stream().map(AnswerSelection::getValue).toList());

    assertInstanceOf(ExtendedMatchingEventAnswer.class, answers.get(2));
    ExtendedMatchingEventAnswer extendedMatchingAnswer =
        (ExtendedMatchingEventAnswer) answers.get(2);
    assertEquals(103L, extendedMatchingAnswer.getQuestionId());
    assertEquals(List.of(0, 1), new ArrayList<>(extendedMatchingAnswer.getPairs().keySet()));
    assertEquals(3, extendedMatchingAnswer.getPairs().get(0).getValue());
    assertEquals(5, extendedMatchingAnswer.getPairs().get(1).getValue());
  }

  private Question assessmentQuestion(Long id, int order, QuestionType questionType) {
    Question question = new Question();
    question.setId(id);
    question.setOrder(order);
    question.setQuestionType(questionType);
    question.setText("Question " + id);
    question.setAnswerRequired(true);
    return question;
  }

  private QuestionChoice choice(String text, int order) {
    QuestionChoice choice = new QuestionChoice();
    choice.setText(text);
    choice.setOrder(order);
    return choice;
  }

  private QuestionChoice choice(String text, int order, boolean correct) {
    QuestionChoice choice = this.choice(text, order);
    choice.setCorrect(correct);
    return choice;
  }

  private ExtendedMatchingStatement statement(int order) {
    ExtendedMatchingStatement statement = new ExtendedMatchingStatement();
    statement.setOrder(order);
    return statement;
  }

  private ExtendedMatchingStatement statement(int order, int optionOrder) {
    ExtendedMatchingStatement statement = this.statement(order);
    ExtendedMatchingOption option = new ExtendedMatchingOption();
    option.setOrder(optionOrder);
    statement.setExtendedMatchingOption(option);
    return statement;
  }

  private QuestionAnswerDTO answerDto(
      Long questionId, Set<String> answers, Map<Integer, Integer> extendedMatchingPairs) {
    QuestionAnswerDTO dto = new QuestionAnswerDTO();
    dto.setQuestionId(questionId);
    dto.setAnswers(answers);
    dto.setExtendedMatchingPairs(extendedMatchingPairs);
    return dto;
  }

  @AfterEach
  public void after() {
    reset(trainingRunRepository);
  }
}
