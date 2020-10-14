package cz.muni.ics.kypo.training.rest.controllers;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.ValidateFlagDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.service.AuditEventsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@RunWith(ConcurrentTestRunner.class)
@ContextConfiguration(classes = {TrainingRunsRestController.class, TrainingInstancesRestController.class, TestDataFactory.class})
@DataJpaTest
@Import(RestConfigTest.class)
@TestPropertySource(properties = {"openstack-server.uri=http://localhost:8080"})
public class TrainingRunsConcurrentIT {

    private MockMvc mvc;
    private TestContextManager testContextManager;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingRunsRestController trainingRunsRestController;
    @Autowired
    private TrainingInstancesRestController trainingInstancesRestController;
    @Autowired
    private TrainingRunRepository trainingRunRepository;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private GameLevelRepository gameLevelRepository;
    @Autowired
    private AssessmentLevelRepository assessmentLevelRepository;
    @Autowired
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    @Autowired
    @Qualifier("sandboxManagementExchangeFunction")
    private ExchangeFunction sandboxManagementExchangeFunction;
    @Autowired
    @Qualifier("userManagementExchangeFunction")
    private ExchangeFunction userManagementExchangeFunction;

    @MockBean
    private AuditEventsService auditEventsService;


    private GameLevel gameLevel;
    private AssessmentLevel assessmentLevel;
    private UserRefDTO userRefDTO1;
    private TrainingInstance trainingInstance;
    private TrainingDefinition trainingDefinition;
    private UserRef participant1, participant2;
    private SandboxInfo sandboxInfo1;
    private TrainingRun trainingRunGameLevel, trainingRunAssessmentLevel;

    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
        this.mvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController, trainingInstancesRestController)
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();
        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(1L);
        sandboxInfo1.setLockId(1);

        participant1 = new UserRef();
        participant1.setUserRefId(3L);
        participant2 = new UserRef();
        participant2.setUserRefId(4L);
        userRefRepository.saveAll(Set.of(participant1, participant2));

        userRefDTO1 = new UserRefDTO();
        userRefDTO1.setUserRefFullName("Ing. John Doe");
        userRefDTO1.setUserRefSub("mail@muni.cz");
        userRefDTO1.setUserRefGivenName("John");
        userRefDTO1.setUserRefFamilyName("Doe");
        userRefDTO1.setIss("https://oidc.muni.cz");
        userRefDTO1.setUserRefId(3L);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinitionRepository.save(trainingDefinition);

        trainingInstance = testDataFactory.getOngoingInstance();
        trainingInstance.setTrainingDefinition(trainingDefinition);
        trainingInstance = trainingInstanceRepository.save(trainingInstance);

        gameLevel = testDataFactory.getPenalizedLevel();
        gameLevel.setTrainingDefinition(trainingDefinition);
        gameLevel.setOrder(1);
        gameLevel.setIncorrectFlagLimit(3);
        gameLevelRepository.save(gameLevel);

        assessmentLevel = testDataFactory.getQuestionnaire();
        assessmentLevel.setTrainingDefinition(trainingDefinition);
        assessmentLevel.setOrder(2);
        assessmentLevelRepository.save(assessmentLevel);

        trainingRunAssessmentLevel = this.testDataFactory.getRunningRun();
        trainingRunAssessmentLevel.setCurrentLevel(assessmentLevel);
        trainingRunAssessmentLevel.setTrainingInstance(trainingInstance);
        trainingRunAssessmentLevel.setParticipantRef(participant2);

        trainingRunGameLevel = this.testDataFactory.getRunningRun();
        trainingRunGameLevel.setCurrentLevel(gameLevel);
        trainingRunGameLevel.setIncorrectFlagCount(0);
        trainingRunGameLevel.setTrainingInstance(trainingInstance);
        trainingRunGameLevel.setParticipantRef(participant2);
        trainingRunRepository.saveAll(Set.of(trainingRunAssessmentLevel, trainingRunGameLevel));

    }

    @Test
    @ThreadCount(4)
    public void concurrentAccessTrainingRun() throws Exception {
        given(userManagementExchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));
        given(sandboxManagementExchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(sandboxInfo1));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        mvc.perform(post("/training-runs")
                .param("accessToken", trainingInstance.getAccessToken()));
    }

    @Test
    @ThreadCount(4)
    public void concurrentAssessmentEvaluation() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        mvc.perform(put("/training-runs/" + trainingRunAssessmentLevel.getId() + "/assessment-evaluations")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(new ArrayList<>())));
        then(auditEventsService).should(times(1)).auditAssessmentAnswersAction(any(), any());
    }

    @Test
    @ThreadCount(4)
    public void concurrentIsCorrectFlag() throws Exception {
        ValidateFlagDTO validateFlagDTO = new ValidateFlagDTO();
        validateFlagDTO.setFlag(gameLevel.getFlag());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        mvc.perform(post("/training-runs/" + trainingRunGameLevel.getId() + "/is-correct-flag")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(validateFlagDTO)));
        then(auditEventsService).should(times(1)).auditCorrectFlagSubmittedAction(any(), any());
    }

    @After
    public void testAccessTrainingRun() throws Exception {
        List<TrainingRun> trainingRuns = trainingRunRepository.findAll();
        List<TRAcquisitionLock> locks = trAcquisitionLockRepository.findAll();
        assertEquals(3, trainingRuns.size());
        assertEquals(1, locks.size());
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }

    private void mockSpringSecurityContextForGet(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "mail2@muni.cz");
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), "Ing. Michael Johnson");
        sub.addProperty(AuthenticatedUserOIDCItems.GIVEN_NAME.getName(), "Michael");
        sub.addProperty(AuthenticatedUserOIDCItems.FAMILY_NAME.getName(), "Johnson");
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(auth);
        given(auth.getUserAuthentication()).willReturn(auth);
        given(auth.getCredentials()).willReturn(sub);
        given(auth.getAuthorities()).willReturn(authorities);
        given(authentication.getDetails()).willReturn(auth);
    }
}
