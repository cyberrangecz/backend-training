package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDTO;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.apache.http.HttpHeaders;
import org.elasticsearch.client.Client;
import org.json.HTTP;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private TrainingRunRepository trainingRunRepository;
    @Mock
    private UserRefRepository organizerRefRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private TrainingDefinition trainingDefinition;
    @Mock
    private ExchangeFunction exchangeFunction;
    @Mock
    private WebClient sandboxServiceWebClient;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private TrainingRun trainingRun1, trainingRun2;
    private UserRef user;
    private LockedPoolInfo lockedPoolInfo;
    private PoolInfoDTO poolInfoDTO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        sandboxServiceWebClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
        trainingInstanceService = new TrainingInstanceService(trainingInstanceRepository, accessTokenRepository,
                trainingRunRepository, organizerRefRepository, sandboxServiceWebClient, securityService);

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

        lockedPoolInfo = testDataFactory.getLockedPoolInfo();
        poolInfoDTO = testDataFactory.getPoolInfoDTO();
    }

    @Test
    public void findById() {
        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

        TrainingInstance tI = trainingInstanceService.findById(trainingInstance1.getId());
        deepEquals(trainingInstance1, tI);
        then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findById_NotFound() {
        trainingInstanceService.findById(10L);
    }

    @Test
    public void findByIdIncludingDefinition() {
        given(trainingInstanceRepository.findByIdIncludingDefinition(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));
        TrainingInstance result = trainingInstanceService.findByIdIncludingDefinition(trainingInstance1.getId());
        deepEquals(trainingInstance1, result);
        assertEquals(trainingDefinition, result.getTrainingDefinition());
        then(trainingInstanceRepository).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findByIdIncludingDefinition_NotFound() {
        trainingInstanceService.findByIdIncludingDefinition(10L);
    }

    @Test
    public void findAll() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page<TrainingInstance> p = new PageImpl<TrainingInstance>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page<TrainingInstance> pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findAllByLoggedInUser() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page<TrainingInstance> p = new PageImpl<>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class), anyLong())).willReturn(p);

        Page<TrainingInstance> pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2), 1L);
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void createTrainingInstance_createAuthor() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);

        TrainingInstance tI = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, tI);
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }
    @Test
    public void createTrainingInstance_authorIsCreated() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());

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
    public void updateTrainingInstance_createUser() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);

        String token = trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test
    public void updateTrainingInstance_alreadyCreatedUser() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());

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

        then(trainingInstanceRepository).should(never()).save(any(TrainingInstance.class));
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstanceService.delete(trainingInstance2);
        then(trainingInstanceRepository).should().delete(trainingInstance2);
    }

    @Test
    public void deleteTrainingInstanceById() {
        trainingInstanceService.deleteById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().deleteById(trainingInstance2.getId());
    }

    @Test
    public void updateTrainingInstancePool() {
        trainingInstanceService.updateTrainingInstancePool(trainingInstance1);
        then(trainingInstanceRepository).should().saveAndFlush(trainingInstance1);
    }

    @Test
    public void lockPool() throws Exception {
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(lockedPoolInfo));
        LockedPoolInfo result = trainingInstanceService.lockPool(lockedPoolInfo.getPoolId());
        assertEquals(lockedPoolInfo, result);
    }

    @Test(expected = MicroserviceApiException.class)
    public void lockPool_MicroserviceError() throws Exception {
        willThrow(new CustomWebClientException("Error when trying to lock pool.", HttpStatus.CONFLICT)).given(exchangeFunction).exchange(any(ClientRequest.class));
        trainingInstanceService.lockPool(lockedPoolInfo.getPoolId());
    }

    @Test
    public void unlockPool() throws Exception {
        poolInfoDTO.setId(trainingInstance1.getPoolId());
        ArgumentMatcher<ClientRequest> poolsRequest = clientRequest -> clientRequest.url().equals(URI.create("/pools/" + poolInfoDTO.getId()));
        doReturn(buildMockResponse(poolInfoDTO)).when(exchangeFunction).exchange(argThat(poolsRequest));
        trainingInstanceService.unlockPool(trainingInstance1.getPoolId());
    }

    @Test(expected = MicroserviceApiException.class)
    public void unlockPool_GetLockIdMicroserviceError() throws Exception {
        willThrow(new CustomWebClientException("Cannot get lock id.", HttpStatus.CONFLICT)).given(exchangeFunction).exchange(any(ClientRequest.class));
        trainingInstanceService.unlockPool(trainingInstance1.getPoolId());
    }

    @Test
    public void findTrainingRunsByTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page<TrainingRun> pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), null, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_OnlyActive() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllActiveByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page<TrainingRun> pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), true, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_OnlyInActive() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllInactiveByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page<TrainingRun> pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), false, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findTrainingRunsByTrainingInstance_NotFound() {
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

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(object);
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }
}
