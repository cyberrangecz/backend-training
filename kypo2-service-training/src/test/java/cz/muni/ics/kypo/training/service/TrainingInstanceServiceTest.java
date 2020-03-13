package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingInstanceServiceTest {

    @Autowired
    TestDataFactory testDataFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingInstanceService trainingInstanceService;

    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private RestTemplate pythonRestTemplate;
    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private TrainingRunRepository trainingRunRepository;
    @Mock
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    @Mock
    private UserRefRepository organizerRefRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private TrainingEventsService trainingEventsService;
    @Mock
    private TrainingDefinition trainingDefinition;
    @Mock
    private SandboxPoolInfo sandboxPoolInfo;
    private SandboxInfo sandboxInfo1 ,sandboxInfo2;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private TrainingRun trainingRun1, trainingRun2;
    private UserRef user;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceService = new TrainingInstanceService(trainingInstanceRepository, accessTokenRepository,
                trainingRunRepository, organizerRefRepository, pythonRestTemplate, securityService, trainingEventsService,
                trAcquisitionLockRepository);

        trainingInstance1 = testDataFactory.getConcludedInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTrainingDefinition(trainingDefinition);

        user = new UserRef();

        trainingInstance2 = testDataFactory.getFutureInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTrainingDefinition(trainingDefinition);
        trainingInstance2.setPoolId(null);

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setTrainingInstance(trainingInstance1);

        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(2L);

        sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId(3L);
    }

    @Test
    public void getTrainingInstanceById() {
        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

        TrainingInstance tI = trainingInstanceService.findById(trainingInstance1.getId());
        deepEquals(trainingInstance1, tI);

        then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNonexistentTrainingInstanceById() {
        trainingInstanceService.findById(10L);
    }

    @Test
    public void findAll() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);

        Page p = new PageImpl<TrainingInstance>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void createTrainingInstance() {
        mockSpringSecurityContextForGet();
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);
        given(pythonRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
        TrainingInstance tI = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, tI);
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }

    @Test(expected = EntityConflictException.class)
    public void createTrainingInstanceWithInvalidTimes() {
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));
        trainingInstanceService.create(trainingInstance1);
    }

    @Test
    public void updateTrainingInstance() {
        mockSpringSecurityContextForGet();
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);

        String token = trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingInstanceWithInvalidTimes() {
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));
        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        trainingInstanceService.update(trainingInstance1);
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstanceService.delete(trainingInstance2);
        then(trainingInstanceRepository).should().delete(trainingInstance2);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteTrainingInstanceWithAssignedTrainingRuns() {
        List<TrainingRun> runs = new ArrayList<>();
        runs.add(trainingRun1);
        runs.add(trainingRun2);
        Page p = new PageImpl<>(runs);

        given(trainingRunRepository.existsAnyForTrainingInstance(trainingInstance1.getId())).willReturn(true);
        trainingInstanceService.delete(trainingInstance1);
    }

    @Test
    public void findTrainingRunsByTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), null, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findTrainingRunsByTrainingInstance_notContainedId() {
        trainingInstanceService.findTrainingRunsByTrainingInstance(10L, null, PageRequest.of(0, 2));
    }

    @After
    public void after() {
        reset(trainingInstanceRepository);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstance actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    private void mockSpringSecurityContextForGet() {
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "participant");
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), "Pavel");
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(auth);
        given(auth.getUserAuthentication()).willReturn(auth);
        given(auth.getCredentials()).willReturn(sub);
        given(auth.getAuthorities()).willReturn(Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
        given(authentication.getDetails()).willReturn(auth);
    }

}
