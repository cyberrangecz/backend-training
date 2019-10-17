package cz.muni.ics.kypo.training.rest.controllers;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.enums.SandboxStates;
import cz.muni.ics.kypo.training.exceptions.RestTemplateException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(ConcurrentTestRunner.class)
@ContextConfiguration(classes = {TrainingRunsRestController.class, TrainingInstancesRestController.class})
@DataJpaTest
@Import(RestConfigTest.class)
@TestPropertySource(properties = {"openstack-server.uri=http://localhost:8080"})
public class TrainingRunsConcurrentIT {

    private MockMvc mvc;
    private TestContextManager testContextManager;

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
    private RestTemplate restTemplate;
    @Autowired
    private TRAcquisitionLockRepository trAcquisitionLockRepository;

    private GameLevel gameLevel1;
    private UserRefDTO userRefDTO1;
    private TrainingInstance trainingInstance;
    private TrainingDefinition trainingDefinition;
    private Hint hint;
    private PageResultResourcePython<SandboxInfo> sandboxInfoPageResult;
    private UserRef participant;

    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;

    @SpringBootApplication
    static class TestConfiguration{
    }

    @Before
    public void init() throws Exception{
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
        this.mvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController, trainingInstancesRestController).build();
        SandboxInfo sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(1L);
        sandboxInfo1.setStatus(SandboxStates.FULL_BUILD_COMPLETE.getName());
        sandboxInfo1.setPool(1L);
        sandboxInfo1.setLocked(false);
        SandboxInfo sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId(2L);
        sandboxInfo2.setStatus(SandboxStates.FULL_BUILD_COMPLETE.getName());
        sandboxInfo2.setPool(1L);
        sandboxInfo2.setLocked(false);
        SandboxInfo sandboxInfo3 = new SandboxInfo();
        sandboxInfo3.setId(3L);
        sandboxInfo3.setStatus(SandboxStates.FULL_BUILD_COMPLETE.getName());
        sandboxInfo3.setPool(1L);
        sandboxInfo3.setLocked(false);
        SandboxInfo sandboxInfo4 = new SandboxInfo();
        sandboxInfo4.setId(4L);
        sandboxInfo4.setStatus(SandboxStates.FULL_BUILD_COMPLETE.getName());
        sandboxInfo4.setPool(1L);
        sandboxInfo4.setLocked(false);

        participant = new UserRef();
        participant.setUserRefId(3L);
        userRefRepository.save(participant);

        sandboxInfoPageResult = new PageResultResourcePython();
        sandboxInfoPageResult.setResults(Arrays.asList(sandboxInfo1, sandboxInfo2, sandboxInfo3, sandboxInfo4));

        userRefDTO1 = new UserRefDTO();
        userRefDTO1.setUserRefFullName("Ing. Mgr. MuDr. Boris Jadus");
        userRefDTO1.setUserRefLogin("445469@muni.cz");
        userRefDTO1.setUserRefGivenName("Boris");
        userRefDTO1.setUserRefFamilyName("Jadus");
        userRefDTO1.setIss("https://oidc.muni.cz");
        userRefDTO1.setUserRefId(3L);

        UserRef organizer = new UserRef();
        organizer.setUserRefId(1L);

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setTitle("definition");
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setShowStepperBar(true);
        trainingDefinition.setSandboxDefinitionRefId(1L);
        trainingDefinition.setLastEdited(LocalDateTime.now());
        trainingDefinitionRepository.save(trainingDefinition);

        trainingInstance = new TrainingInstance();
        trainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
        trainingInstance.setEndTime(LocalDateTime.now().plusHours(80));
        trainingInstance.setTitle("futureInstance");
        trainingInstance.setPoolSize(20);
        trainingInstance.setPoolId(1L);
        trainingInstance.setAccessToken("pass-1234");
        trainingInstance.setTrainingDefinition(trainingDefinition);
        trainingInstance.setOrganizers(new HashSet<>(Arrays.asList(organizer)));
        trainingInstance = trainingInstanceRepository.save(trainingInstance);

        hint = new Hint();
        hint.setTitle("hintTitle");
        hint.setContent("hintContent");
        hint.setHintPenalty(10);

        gameLevel1 = new GameLevel();
        gameLevel1.setTitle("testTitle");
        gameLevel1.setContent("testContent");
        gameLevel1.setFlag("testFlag");
        gameLevel1.setSolution("testSolution");
        gameLevel1.setSolutionPenalized(true);
        gameLevel1.setMaxScore(30);
        gameLevel1.setHints(new HashSet<>(Arrays.asList(hint)));
        gameLevel1.setTrainingDefinition(trainingDefinition);
        gameLevel1.setOrder(1);
        gameLevel1.setIncorrectFlagLimit(4);
        gameLevelRepository.save(gameLevel1);
    }

    @Test
    @ThreadCount(4)
    public void concurrentAccessTrainingRun() throws Exception{
        String url = "http://localhost:8080" + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        builder.queryParam("page", 1);
        builder.queryParam("page_size", 1000);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/info"), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(eq(builder.toUriString()), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(sandboxInfoPageResult, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));

        mvc.perform(post("/training-runs")
                .param("accessToken", "pass-1234"));
    }

    @After
    public void testAccessTrainingRun() throws Exception {
        List<TrainingRun> trainingRuns = trainingRunRepository.findAll();
        List<TRAcquisitionLock> locks = trAcquisitionLockRepository.findAll();
        assertEquals(1, trainingRuns.size());
        assertEquals(1, locks.size());
        RestTemplateException exception = new RestTemplateException("Sandbox not found.", "NOT_FOUND");
        willThrow(exception).given(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        mvc.perform(delete("/training-instances/{instanceId}/sandbox-instances", trainingInstance.getId())
                .param("sandboxIds", sandboxInfoPageResult.getResults().get(0).getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        mvc.perform(delete("/training-runs/"  + trainingRuns.get(0).getId()));

        assertEquals(0, trainingRunRepository.findAll().size());
        assertEquals(0, trAcquisitionLockRepository.findAll().size());

    }

    private void mockSpringSecurityContextForGet(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "556978@muni.cz");
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
