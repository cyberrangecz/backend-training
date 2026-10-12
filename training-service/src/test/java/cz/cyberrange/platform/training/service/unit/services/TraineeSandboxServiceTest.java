package cz.cyberrange.platform.training.service.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import cz.cyberrange.platform.training.api.dto.run.TraineeSandboxDTO;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.TooManyRequestsException;
import cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError;
import cz.cyberrange.platform.training.api.responses.AllocationUnitInfo;
import cz.cyberrange.platform.training.api.responses.SandboxRequestInfo;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.services.TraineeSandboxService;
import cz.cyberrange.platform.training.service.services.TraineeSandboxService.SandboxReadiness;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TraineeSandboxServiceTest {

  private static final String USER_SUB = "trainee-sub";
  private static final String SANDBOX_ID = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80";
  private static final List<String> FINISHED = List.of("FINISHED", "FINISHED", "FINISHED");
  private static final List<String> BUILDING = List.of("FINISHED", "RUNNING", "IN_QUEUE");
  private static final List<String> FAILED = List.of("FINISHED", "FAILED", "FAILED");

  @Mock private SandboxApiService sandboxApiService;
  @Mock private TrainingRunRepository trainingRunRepository;
  @Mock private UserRefRepository userRefRepository;

  private TraineeSandboxService traineeSandboxService;
  private TrainingInstance trainingInstance;
  private TrainingRun trainingRun;

  @BeforeEach
  void init() {
    traineeSandboxService =
        new TraineeSandboxService(
            sandboxApiService, trainingRunRepository, userRefRepository, true);
    trainingInstance = new TrainingInstance();
    trainingInstance.setId(1L);
    trainingInstance.setTitle("Linux basics");
    trainingInstance.setPoolId(4L);
    trainingInstance.setAccessToken("linux-1234");
    trainingRun = new TrainingRun();
    trainingRun.setId(2L);
    trainingRun.setTrainingInstance(trainingInstance);
    trainingRun.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0));
  }

  @Test
  void isEnabledForInstanceThatIsNeitherManagedNorLocal() {
    assertTrue(traineeSandboxService.isEnabledFor(trainingInstance));

    trainingInstance.setManaged(true);
    assertFalse(traineeSandboxService.isEnabledFor(trainingInstance));

    trainingInstance.setManaged(false);
    trainingInstance.setLocalEnvironment(true);
    assertFalse(traineeSandboxService.isEnabledFor(trainingInstance));
  }

  @Test
  void isNotEnabledWhenDeploymentDisablesTraineeAllocation() {
    TraineeSandboxService disabled =
        new TraineeSandboxService(
            sandboxApiService, trainingRunRepository, userRefRepository, false);

    assertFalse(disabled.isEnabled());
    assertFalse(disabled.isEnabledFor(trainingInstance));
  }

  @Test
  void refreshSandboxOfRunWithoutAllocation() {
    assertEquals(SandboxReadiness.NONE, traineeSandboxService.refreshSandbox(trainingRun));

    then(sandboxApiService).shouldHaveNoInteractions();
  }

  @Test
  void refreshSandboxWhileItIsBuilt() {
    trainingRun.setSandboxInstanceAllocationId(7);
    given(sandboxApiService.findAllocationUnit(7))
        .willReturn(Optional.of(unit(7, null, BUILDING, null)));

    assertEquals(SandboxReadiness.NOT_READY, traineeSandboxService.refreshSandbox(trainingRun));

    then(trainingRunRepository).should(never()).save(any());
  }

  @Test
  void refreshSandboxAssignsFirstReadySandboxAndStartsRun() {
    trainingRun.setSandboxInstanceAllocationId(7);
    given(sandboxApiService.findAllocationUnit(7))
        .willReturn(Optional.of(unit(7, SANDBOX_ID, FINISHED, null)));

    assertEquals(SandboxReadiness.FIRST_READY, traineeSandboxService.refreshSandbox(trainingRun));

    assertEquals(SANDBOX_ID, trainingRun.getSandboxInstanceRefId());
    assertTrue(trainingRun.getStartTime().isAfter(LocalDateTime.of(2026, 1, 1, 10, 0)));
    then(trainingRunRepository).should().save(trainingRun);
  }

  @Test
  void refreshSandboxAlreadyAssigned() {
    trainingRun.setSandboxInstanceAllocationId(7);
    trainingRun.setSandboxInstanceRefId(SANDBOX_ID);
    given(sandboxApiService.findAllocationUnit(7))
        .willReturn(Optional.of(unit(7, SANDBOX_ID, FINISHED, null)));

    assertEquals(SandboxReadiness.READY, traineeSandboxService.refreshSandbox(trainingRun));

    then(trainingRunRepository).should(never()).save(any());
  }

  @Test
  void refreshSandboxAssignsReplacementSandboxWithoutRestartingRun() {
    trainingRun.setSandboxInstanceAllocationId(8);
    trainingRun.setPreviousSandboxInstanceRefId("removed-sandbox");
    given(sandboxApiService.findAllocationUnit(8))
        .willReturn(Optional.of(unit(8, SANDBOX_ID, FINISHED, null)));

    assertEquals(SandboxReadiness.READY, traineeSandboxService.refreshSandbox(trainingRun));

    assertEquals(SANDBOX_ID, trainingRun.getSandboxInstanceRefId());
    assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), trainingRun.getStartTime());
  }

  @Test
  void refreshSandboxBeingRemovedReleasesItAndKeepsAllocation() {
    trainingRun.setSandboxInstanceAllocationId(7);
    trainingRun.setSandboxInstanceRefId(SANDBOX_ID);
    given(sandboxApiService.findAllocationUnit(7))
        .willReturn(Optional.of(unit(7, null, FINISHED, BUILDING)));

    assertEquals(SandboxReadiness.NOT_READY, traineeSandboxService.refreshSandbox(trainingRun));

    assertNull(trainingRun.getSandboxInstanceRefId());
    assertEquals(SANDBOX_ID, trainingRun.getPreviousSandboxInstanceRefId());
    assertEquals(7, trainingRun.getSandboxInstanceAllocationId());
    then(trainingRunRepository).should().save(trainingRun);
  }

  @Test
  void refreshSandboxThatIsGoneClearsAllocation() {
    trainingRun.setSandboxInstanceAllocationId(7);
    trainingRun.setSandboxInstanceRefId(SANDBOX_ID);
    given(sandboxApiService.findAllocationUnit(7)).willReturn(Optional.empty());

    assertEquals(SandboxReadiness.NONE, traineeSandboxService.refreshSandbox(trainingRun));

    assertNull(trainingRun.getSandboxInstanceAllocationId());
    assertNull(trainingRun.getSandboxInstanceRefId());
    assertEquals(SANDBOX_ID, trainingRun.getPreviousSandboxInstanceRefId());
    then(trainingRunRepository).should().save(trainingRun);
  }

  @Test
  void refreshSandboxTheOrganizerAllocatedLeavesItAlone() {
    trainingRun.setSandboxInstanceAllocationId(7);
    trainingRun.setSandboxInstanceRefId(SANDBOX_ID);
    given(sandboxApiService.findAllocationUnit(7))
        .willThrow(
            new MicroserviceApiException(HttpStatus.FORBIDDEN, PythonApiError.of("Forbidden.")));

    assertEquals(SandboxReadiness.READY, traineeSandboxService.refreshSandbox(trainingRun));

    assertEquals(SANDBOX_ID, trainingRun.getSandboxInstanceRefId());
    then(trainingRunRepository).should(never()).save(any());
  }

  @Test
  void refreshSandboxRethrowsForbiddenForRunWithoutSandbox() {
    trainingRun.setSandboxInstanceAllocationId(7);
    given(sandboxApiService.findAllocationUnit(7))
        .willThrow(
            new MicroserviceApiException(HttpStatus.FORBIDDEN, PythonApiError.of("Forbidden.")));

    assertThrows(
        MicroserviceApiException.class, () -> traineeSandboxService.refreshSandbox(trainingRun));
  }

  @Test
  void prepareSandboxAllocatesForRunWithoutSandbox() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB)).willReturn(List.of());
    given(sandboxApiService.allocateSandboxForTrainee(4L, "linux-1234"))
        .willReturn(Optional.of(unit(9, null, BUILDING, null)));

    assertEquals(
        SandboxReadiness.NOT_READY, traineeSandboxService.prepareSandbox(trainingRun, USER_SUB));

    assertEquals(9, trainingRun.getSandboxInstanceAllocationId());
    then(trainingRunRepository).should().save(trainingRun);
  }

  @Test
  void prepareSandboxDoesNotAllocateWhileTraineeHasActiveSandboxElsewhere() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, 99L, SANDBOX_ID, FINISHED, null)));

    assertEquals(
        SandboxReadiness.NOT_READY, traineeSandboxService.prepareSandbox(trainingRun, USER_SUB));

    then(sandboxApiService).should(never()).allocateSandboxForTrainee(anyLong(), any());
    assertNull(trainingRun.getSandboxInstanceAllocationId());
  }

  @Test
  void prepareSandboxAdoptsUnusedSandboxInThePool() {
    AllocationUnitInfo unused = unit(5, SANDBOX_ID, FINISHED, null);
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB)).willReturn(List.of(unused));
    given(trainingRunRepository.findAllBySandboxInstanceAllocationIds(List.of(5)))
        .willReturn(List.of());
    given(sandboxApiService.findAllocationUnit(5)).willReturn(Optional.of(unused));

    assertEquals(
        SandboxReadiness.FIRST_READY, traineeSandboxService.prepareSandbox(trainingRun, USER_SUB));

    assertEquals(5, trainingRun.getSandboxInstanceAllocationId());
    assertEquals(SANDBOX_ID, trainingRun.getSandboxInstanceRefId());
    then(sandboxApiService).should(never()).allocateSandboxForTrainee(anyLong(), any());
  }

  @Test
  void prepareSandboxDoesNotAdoptSandboxAnotherRunUses() {
    TrainingRun otherRun = new TrainingRun();
    otherRun.setSandboxInstanceAllocationId(5);
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, SANDBOX_ID, FINISHED, null)));
    given(trainingRunRepository.findAllBySandboxInstanceAllocationIds(List.of(5)))
        .willReturn(List.of(otherRun));

    assertEquals(
        SandboxReadiness.NOT_READY, traineeSandboxService.prepareSandbox(trainingRun, USER_SUB));

    assertNull(trainingRun.getSandboxInstanceAllocationId());
    then(sandboxApiService).should(never()).allocateSandboxForTrainee(anyLong(), any());
  }

  @Test
  void prepareSandboxDoesNotAdoptSandboxAnArchivedRunWorkedIn() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, SANDBOX_ID, FINISHED, null)));
    given(trainingRunRepository.findAllBySandboxInstanceAllocationIds(List.of(5)))
        .willReturn(List.of());
    given(
            trainingRunRepository.existsBySandboxInstanceRefIdOrPreviousSandboxInstanceRefId(
                SANDBOX_ID, SANDBOX_ID))
        .willReturn(true);

    assertEquals(
        SandboxReadiness.NOT_READY, traineeSandboxService.prepareSandbox(trainingRun, USER_SUB));

    assertNull(trainingRun.getSandboxInstanceAllocationId());
  }

  @Test
  void prepareSandboxWhenSandboxServiceReportsOneInThePool() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB)).willReturn(List.of());
    given(sandboxApiService.allocateSandboxForTrainee(4L, "linux-1234"))
        .willReturn(Optional.empty());

    assertEquals(
        SandboxReadiness.NOT_READY, traineeSandboxService.prepareSandbox(trainingRun, USER_SUB));

    assertNull(trainingRun.getSandboxInstanceAllocationId());
    then(trainingRunRepository).should(never()).save(any());
  }

  @Test
  void canProvideSandboxDespiteFailedAndRemovedSandboxes() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(
            List.of(unit(5, 99L, null, FAILED, null), unit(6, 99L, null, FINISHED, FAILED)));

    assertTrue(traineeSandboxService.canProvideSandbox(USER_SUB, 4L));
  }

  @Test
  void canNotProvideSandboxWhileOneIsRemovedElsewhere() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, 99L, null, FINISHED, BUILDING)));

    assertFalse(traineeSandboxService.canProvideSandbox(USER_SUB, 4L));
  }

  @Test
  void canProvideSandboxThatNoRunUsesInThePool() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, null, BUILDING, null)));
    given(trainingRunRepository.findAllBySandboxInstanceAllocationIds(List.of(5)))
        .willReturn(List.of());

    assertTrue(traineeSandboxService.canProvideSandbox(USER_SUB, 4L));
  }

  @Test
  void lockUserTurnsAwayRequestWhileAnotherHoldsTheLock() {
    given(userRefRepository.lockByUserRefId(3L))
        .willThrow(new PessimisticLockingFailureException("could not obtain lock"));

    assertThrows(TooManyRequestsException.class, () -> traineeSandboxService.lockUser(3L));

    then(userRefRepository).should().createOrGet(3L);
  }

  @Test
  void removeSandboxOfFinishedRun() {
    trainingRun.setSandboxInstanceAllocationId(7);

    traineeSandboxService.removeSandboxOfFinishedRun(trainingRun);

    then(sandboxApiService).should().requestCleanup(7);
  }

  @Test
  void removeSandboxOfFinishedRunIgnoresFailure() {
    trainingRun.setSandboxInstanceAllocationId(7);
    given(sandboxApiService.requestCleanup(7))
        .willThrow(
            new MicroserviceApiException(HttpStatus.FORBIDDEN, PythonApiError.of("Forbidden.")));

    traineeSandboxService.removeSandboxOfFinishedRun(trainingRun);

    then(sandboxApiService).should().requestCleanup(7);
  }

  @Test
  void findOwnSandboxesTellsWhichMayBeRemoved() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(
            List.of(
                unit(5, SANDBOX_ID, FINISHED, null),
                unit(6, null, BUILDING, null),
                unit(7, null, FAILED, null),
                unit(8, null, FINISHED, BUILDING),
                unit(9, null, FINISHED, FAILED)));
    trainingRun.setSandboxInstanceAllocationId(5);
    given(trainingRunRepository.findAllBySandboxInstanceAllocationIds(List.of(5, 6, 7, 8, 9)))
        .willReturn(List.of(trainingRun));

    List<TraineeSandboxDTO> sandboxes = traineeSandboxService.findOwnSandboxes(USER_SUB);

    assertEquals(
        List.of(true, false, true, false, true),
        sandboxes.stream().map(TraineeSandboxDTO::isAllowRemove).toList());
    assertEquals("Linux basics", sandboxes.get(0).getTrainingInstanceTitle());
    assertEquals(1L, sandboxes.get(0).getTrainingInstanceId());
    assertNull(sandboxes.get(1).getTrainingInstanceTitle());
  }

  @Test
  void removeOwnSandbox() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, SANDBOX_ID, FINISHED, null)));

    traineeSandboxService.removeOwnSandboxBySandboxId(USER_SUB, SANDBOX_ID);

    then(sandboxApiService).should().requestCleanup(5);
  }

  @Test
  void removeSandboxTheTraineeDidNotAllocate() {
    given(sandboxApiService.findAllocationUnitsCreatedBy(USER_SUB))
        .willReturn(List.of(unit(5, SANDBOX_ID, FINISHED, null)));

    assertThrows(
        ForbiddenException.class, () -> traineeSandboxService.removeOwnSandbox(USER_SUB, 6));

    then(sandboxApiService).should(never()).requestCleanup(any());
  }

  private static AllocationUnitInfo unit(
      Integer id, String sandboxId, List<String> allocationStages, List<String> cleanupStages) {
    return unit(id, 4L, sandboxId, allocationStages, cleanupStages);
  }

  private static AllocationUnitInfo unit(
      Integer id,
      Long poolId,
      String sandboxId,
      List<String> allocationStages,
      List<String> cleanupStages) {
    AllocationUnitInfo unit = new AllocationUnitInfo();
    unit.setId(id);
    unit.setPoolId(poolId);
    unit.setSandboxId(sandboxId);
    unit.setAllocationRequest(request(id, allocationStages));
    unit.setCleanupRequest(cleanupStages == null ? null : request(id, cleanupStages));
    return unit;
  }

  private static SandboxRequestInfo request(Integer unitId, List<String> stages) {
    SandboxRequestInfo request = new SandboxRequestInfo();
    request.setId(unitId);
    request.setAllocationUnitId(unitId);
    request.setStages(stages);
    return request;
  }
}
