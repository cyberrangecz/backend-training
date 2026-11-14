package cz.cyberrange.platform.training.service.unit.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class SecurityServiceTest {

  private static final long CALLER_USER_REF_ID = 42L;

  private TrainingRunRepository trainingRunRepository;
  private TrainingInstanceRepository trainingInstanceRepository;
  private UserService userService;
  private SecurityService securityService;

  @BeforeEach
  void setUp() {
    trainingRunRepository = mock(TrainingRunRepository.class);
    trainingInstanceRepository = mock(TrainingInstanceRepository.class);
    userService = mock(UserService.class);
    securityService =
        spy(
            new SecurityService(
                trainingInstanceRepository,
                mock(TrainingDefinitionRepository.class),
                trainingRunRepository,
                mock(WebClient.class),
                userService,
                mock(AbstractLevelRepository.class),
                mock(TrainingLevelRepository.class),
                mock(HintRepository.class),
                mock(AbstractDetectionEventRepository.class),
                mock(CheatingDetectionRepository.class)));
    doReturn(CALLER_USER_REF_ID).when(securityService).getUserRefIdFromUserAndGroup();
    when(userService.findUserByUserRefId(CALLER_USER_REF_ID)).thenReturn(Optional.empty());
  }

  @Test
  void participatesInTrainingDefinitionsIsFalseForCallerWithoutUserRefRow() {
    assertFalse(securityService.participatesInTrainingDefinitions(List.of(5L)));
    verifyNoInteractions(trainingRunRepository, trainingInstanceRepository);
  }

  @Test
  void participatesInTrainingDefinitionsIsTrueForEmptyListAndCallerWithoutUserRefRow() {
    assertTrue(securityService.participatesInTrainingDefinitions(List.of()));
  }

  @Test
  void participatesInLevelsWithHintsIsFalseForCallerWithoutUserRefRow() {
    assertFalse(securityService.participatesInLevelsWithHints(List.of(1L)));
  }

  @Test
  void participatesInTrainingDefinitionsIsFalseForCallerWithRowButNoRunOrOrganizedInstance() {
    UserRef userRef = mock(UserRef.class);
    when(userService.findUserByUserRefId(CALLER_USER_REF_ID)).thenReturn(Optional.of(userRef));
    when(trainingRunRepository.findAllByParticipantRefId(CALLER_USER_REF_ID))
        .thenReturn(List.of());
    when(trainingInstanceRepository.findAllByOrganizersContainsOrderById(userRef))
        .thenReturn(List.of());

    assertFalse(securityService.participatesInTrainingDefinitions(List.of(5L)));
  }
}
