package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.enums.SandboxStates;
import cz.muni.ics.kypo.training.exceptions.RestTemplateException;
import cz.muni.ics.kypo.training.mapping.mapstruct.GameLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.HintMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.InfoLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.*;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingRunsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
@TestPropertySource(properties = {"openstack-server.uri=http://localhost:8080"})
public class TrainingRunsIT {

    private MockMvc mvc;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TrainingRunsRestController trainingRunsRestController;

    @Autowired
    private TrainingRunRepository trainingRunRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    private InfoLevelRepository infoLevelRepository;
    @Autowired
    private GameLevelRepository gameLevelRepository;
    @Autowired
    private AssessmentLevelRepository assessmentLevelRepository;
    @Autowired
    private HintRepository hintRepository;
    @Autowired
    private TrainingRunMapperImpl trainingRunMapper;
    @Autowired
    private HintMapperImpl hintMapper;
    @Autowired
    private GameLevelMapperImpl gameLevelMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("objMapperRESTApi")
    private ObjectMapper mapper;

    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;

    @Autowired
    private InfoLevelMapperImpl infoLevelMapper;

    private TrainingRun trainingRun1, trainingRun2;
    private AssessmentLevel assessmentLevel1;
    private GameLevel gameLevel1;
    private InfoLevel infoLevel1;
    private IsCorrectFlagDTO isCorrectFlagDTO;
    private HintDTO hintDTO;
    private Hint hint;
    private SandboxInfo sandboxInfo;
    private Long nonExistentTrainingRunId;
    private TrainingInstance trainingInstance;
    private TrainingDefinition trainingDefinition;
    private SandboxInfo sandboxInfo1, sandboxInfo2, sandboxInfo3;
    private UserRef participant1, participant2, organizer;
    private UserRefDTO userRefDTO1, userRefDTO2;
    private UserRef participant;
    private PageResultResourcePython sandboxInfoPageResult;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(1L);
        sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId(2L);
        sandboxInfo3 = new SandboxInfo();
        sandboxInfo3.setId(3L);

        userRefDTO1 = new UserRefDTO();
        userRefDTO1.setUserRefFullName("Ing. Mgr. MuDr. Boris Jadus");
        userRefDTO1.setUserRefLogin("445469@muni.cz");
        userRefDTO1.setUserRefGivenName("Boris");
        userRefDTO1.setUserRefFamilyName("Jadus");
        userRefDTO1.setIss("https://oidc.muni.cz");
        userRefDTO1.setUserRefId(3L);

        userRefDTO2 = new UserRefDTO();
        userRefDTO2.setUserRefFullName("Ing. Jan Chudý");
        userRefDTO2.setUserRefLogin("445497@muni.cz");
        userRefDTO2.setUserRefGivenName("Jan");
        userRefDTO2.setUserRefFamilyName("Chudý");
        userRefDTO2.setIss("https://oidc.muni.cz");
        userRefDTO2.setUserRefId(4L);

        InfoLevel iL = new InfoLevel();
        iL.setContent("content");
        iL.setTitle("title");
        iL.setMaxScore(50);
        InfoLevel infoLevel = infoLevelRepository.save(iL);

        GameLevel gL = new GameLevel();
        gL.setContent("gameContent");
        gL.setTitle("gameTitle");
        gL.setSolution("gameSolution");
        gL.setFlag("gameFlag");
        gL.setSolutionPenalized(true);
        GameLevel gameLevel = gameLevelRepository.save(gL);

        AssessmentLevel aL = new AssessmentLevel();
        aL.setAssessmentType(AssessmentType.TEST);
        aL.setInstructions("instruction");
        aL.setQuestions("[]");
        aL.setTitle("assessmentTitle");
        assessmentLevelRepository.save(aL);

        organizer = new UserRef();
        organizer.setUserRefId(1L);
        participant1 = new UserRef();
        participant1.setUserRefId(3L);
        participant2 = new UserRef();
        participant2.setUserRefId(4L);
        userRefRepository.saveAll(Set.of(organizer, participant1, participant2));

        BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
        betaTestingGroup.setOrganizers(new HashSet<>(Arrays.asList(organizer)));

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setTitle("definition");
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setShowStepperBar(true);
        trainingDefinition.setBetaTestingGroup(betaTestingGroup);
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
        trainingInstanceRepository.save(trainingInstance);
        trainingInstance = trainingInstanceRepository.save(trainingInstance);

        nonExistentTrainingRunId = 100L;

        hint = new Hint();
        hint.setTitle("hintTitle");
        hint.setContent("hintContent");
        hint.setHintPenalty(10);

        hintDTO = new HintDTO();
        hintDTO.setContent("hint content");
        hintDTO.setTitle("hint title");

        isCorrectFlagDTO = new IsCorrectFlagDTO();
        isCorrectFlagDTO.setCorrect(true);
        isCorrectFlagDTO.setRemainingAttempts(5);

        infoLevel1 = new InfoLevel();
        infoLevel1.setTitle("info1");
        infoLevel1.setContent("testContent");
        infoLevel1.setMaxScore(0);
        infoLevel1.setTrainingDefinition(trainingDefinition);
        infoLevel1.setOrder(2);
        infoLevelRepository.save(infoLevel1);

        assessmentLevel1 = new AssessmentLevel();
        assessmentLevel1.setTitle("assessment1");
        assessmentLevel1.setAssessmentType(cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType.QUESTIONNAIRE);
        assessmentLevel1.setInstructions("testInstructions");
        assessmentLevel1.setQuestions("[]");
        assessmentLevel1.setMaxScore(20);

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

        trainingRun1 = new TrainingRun();
        trainingRun1.setStartTime(LocalDateTime.now().minusHours(24));
        trainingRun1.setEndTime(LocalDateTime.now().plusHours(48));
        trainingRun1.setState(TRState.RUNNING);
        trainingRun1.setIncorrectFlagCount(0);
        trainingRun1.setSolutionTaken(false);
        trainingRun1.setCurrentLevel(gameLevel1);
        trainingRun1.setLevelAnswered(false);
        trainingRun1.setTrainingInstance(trainingInstance);
        trainingRun1.setParticipantRef(participant1);
        trainingRun1.setSandboxInstanceRefId(1L);

        trainingRun2 = new TrainingRun();
        trainingRun2.setStartTime(LocalDateTime.now().plusHours(2));
        trainingRun2.setEndTime(LocalDateTime.now().plusHours(4));
        trainingRun2.setState(TRState.FINISHED);
        trainingRun2.setIncorrectFlagCount(10);
        trainingRun2.setSolutionTaken(true);
        trainingRun2.setCurrentLevel(infoLevel1);
        trainingRun2.setLevelAnswered(false);
        trainingRun2.setTrainingInstance(trainingInstance);
        trainingRun2.setParticipantRef(participant2);
        trainingRun2.setSandboxInstanceRefId(2L);

        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(1L);

        sandboxInfoPageResult = new PageResultResourcePython();
        sandboxInfoPageResult.setResults(Arrays.asList(sandboxInfo));
    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_run", "abstract_level");
    }

    @Test
    public void findTrainingRunById() throws Exception {
        TrainingRun trainingRun = trainingRunRepository.save(trainingRun1);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        MockHttpServletResponse result = mvc.perform(get("/training-runs/{id}", trainingRun.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        TrainingRunByIdDTO runDTO = trainingRunMapper.mapToFindByIdDTO(trainingRun);
        runDTO.setDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        runDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        runDTO.setParticipantRef(userRefDTO1);
        TrainingRunByIdDTO resultDTO = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingRunByIdDTO.class);
        assertEquals(runDTO.toString(), resultDTO.toString());
    }

    @Test
    public void findTrainingRunByIdNotFound() throws Exception {
        Exception ex = mvc.perform(get("/training-runs/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("Training Run with runId: 100 not found."));
    }

    @Test
    public void findAllTrainingRuns() throws Exception {
        trainingRunRepository.save(trainingRun1);
        trainingRunRepository.save(trainingRun2);
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/" + trainingRun1.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/" + trainingRun2.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO2, HttpStatus.OK));
        System.out.println(userAndGroupURI + "/users/" + trainingRun1.getParticipantRef().getUserRefId());
        System.out.println(userAndGroupURI + "/users/" + trainingRun2.getParticipantRef().getUserRefId());
        MockHttpServletResponse result = mvc.perform(get("/training-runs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingRunDTO> trainingRunsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingRunDTO>>() {
        });
        TrainingRunDTO trainingRunDTO1 = trainingRunMapper.mapToDTO(trainingRun1);
        trainingRunDTO1.setParticipantRef(userRefDTO1);
        TrainingRunDTO trainingRunDTO2 = trainingRunMapper.mapToDTO(trainingRun2);
        trainingRunDTO2.setParticipantRef(userRefDTO2);
        System.out.println(trainingRunsPage.getContent());
        assertTrue(trainingRunsPage.getContent().contains(trainingRunDTO1));
        assertTrue(trainingRunsPage.getContent().contains(trainingRunDTO2));
    }

    @Test
    public void accessTrainingRun() throws Exception {
        String url = "http://localhost:8080" + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/unlocked/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(eq(builder.toUriString()), eq(HttpMethod.GET), any(HttpEntity.class), eq(SandboxInfo.class))).
                willReturn(new ResponseEntity<SandboxInfo>(sandboxInfo1, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-runs")
                .param("accessToken", "pass-1234"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = new JSONObject(result.getContentAsString());
        Long trainingRunId = jsonObject.getLong("trainingRunID");
        Optional<TrainingRun> trainingRun = trainingRunRepository.findById(trainingRunId);
        assertTrue(trainingRun.isPresent());
        assertEquals(gameLevel1, trainingRun.get().getCurrentLevel());
        assertEquals(trainingInstance, trainingRun.get().getTrainingInstance());
    }

    @Test
    public void accessTrainingRunWithAlreadyStartedTrainingRun() throws Exception {
        trainingRun1.setParticipantRef(participant1);
        trainingRun1.setSandboxInstanceRefId(sandboxInfo.getId());
        trainingRunRepository.save(trainingRun1);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-runs")
                .param("accessToken", "pass-1234"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = new JSONObject(result.getContentAsString());
        Long trainingRunId = jsonObject.getLong("trainingRunID");
        Optional<TrainingRun> trainingRun = trainingRunRepository.findById(trainingRunId);
        assertTrue(trainingRun.isPresent());
        assertEquals(gameLevel1, trainingRun.get().getCurrentLevel());
        assertEquals(trainingInstance, trainingRun.get().getTrainingInstance());
    }

    @Test
    public void accessTrainingRunWithTrainingInstanceWithoutSandboxes() throws Exception {
        trainingInstance.setPoolId(null);
        trainingInstanceRepository.save(trainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        Exception exception = mvc.perform(post("/training-runs")
                .param("accessToken", "pass-1234"))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(exception).getClass());
        assertEquals("At first organizer must allocate sandboxes for training instance.", exception.getCause().getCause().getMessage());
    }

    @Test
    public void accessTrainingRunDefinitionWithoutLevels() throws Exception {
        String url = "http://localhost:8080" + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        builder.queryParam("page", 1);
        builder.queryParam("page_size", 1000);

        gameLevel1.setTrainingDefinition(null);
        infoLevel1.setTrainingDefinition(null);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(eq(builder.toUriString()), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(sandboxInfoPageResult, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        Exception exception = mvc.perform(post("/training-runs")
                .param("accessToken", "pass-1234"))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(exception).getClass());
        assertEquals("No starting level available for this training definition.", exception.getCause().getCause().getMessage());
    }

    @Test
    public void accessTrainingRunNoAvailableSandbox() throws Exception {
        trainingInstanceRepository.save(trainingInstance);
        PageResultResourcePython<SandboxInfo> pageResult = new PageResultResourcePython<>();
        pageResult.setResults(new ArrayList<>());
        String url = "http://localhost:8080" + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/unlocked/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        builder.queryParam("page", 1);
        builder.queryParam("page_size", 1000);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        willThrow(new RestTemplateException("No unlocked sandbox.", HttpStatus.CONFLICT.toString())).given(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SandboxInfo.class));

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        Exception exception = mvc.perform(post("/training-runs")
                .param("accessToken", "pass-1234"))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals(ServiceUnavailableException.class, Objects.requireNonNull(exception).getClass());
        assertEquals("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.", exception.getCause().getCause().getMessage());
    }

    @Test
    public void accessTrainingRunAccessTokenNotFound() throws Exception {
        trainingRunRepository.save(trainingRun2);

        Exception ex = mvc.perform(post("/training-runs")
                .param("accessToken", "notFoundToken"))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
    }


    @Test
    public void getAllAccessedTrainingRuns() throws Exception {
        trainingRun1.setParticipantRef(participant1);
        trainingRunRepository.save(trainingRun1);
        trainingRunRepository.save(trainingRun2);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));

        AccessedTrainingRunDTO expectedAccessTrainingRunDTO = new AccessedTrainingRunDTO();
        expectedAccessTrainingRunDTO.setId(trainingRun1.getId());
        expectedAccessTrainingRunDTO.setCurrentLevelOrder(2);
        expectedAccessTrainingRunDTO.setNumberOfLevels(3);
        expectedAccessTrainingRunDTO.setTitle(trainingRun1.getTrainingInstance().getTitle());
        expectedAccessTrainingRunDTO.setInstanceId(trainingRun1.getTrainingInstance().getId());
        expectedAccessTrainingRunDTO.setTrainingInstanceStartDate(trainingInstance.getStartTime());
        expectedAccessTrainingRunDTO.setTrainingInstanceEndDate(trainingInstance.getEndTime());

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));
        MockHttpServletResponse response = mvc.perform(get("/training-runs/accessible"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<AccessedTrainingRunDTO> responseTrainingRunDTOPage = mapper.
                readValue(convertJsonBytesToString(response.getContentAsString()), new TypeReference<PageResultResource<AccessedTrainingRunDTO>>() {
                });
        assertTrue(responseTrainingRunDTOPage.getContent().contains(expectedAccessTrainingRunDTO));
    }

    @Test
    public void getNextLevel() throws Exception {
        trainingRun1.setLevelAnswered(true);
        trainingRunRepository.save(trainingRun1);

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/next-levels", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        InfoLevelDTO infoLevelDTO = mapper.readValue(convertJsonBytesToString(response.getContentAsString()), InfoLevelDTO.class);
        assertEquals(infoLevelDTO, infoLevelMapper.mapToDTO(infoLevel1));
    }

    @Test
    public void getNextLevelNoLevelAnswered() throws Exception {
        trainingRunRepository.save(trainingRun2);

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        Exception ex = mvc.perform(get("/training-runs/{runId}/next-levels", trainingRun2.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("At first you need to answer the level.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void getNextLevelNoNextLevel() throws Exception {
        trainingRun2.setLevelAnswered(true);
        trainingRunRepository.save(trainingRun2);

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));
        Exception ex = mvc.perform(get("/training-runs/{runId}/next-levels", trainingRun2.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("There is no next level.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void getSolution() throws Exception {
        trainingRun1.setMaxLevelScore(gameLevel1.getMaxScore());
        trainingRun1.setTotalScore(10 + gameLevel1.getMaxScore());
        trainingRunRepository.save(trainingRun1);
        assertFalse(trainingRun1.isSolutionTaken());
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/" + trainingRun1.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/solutions", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(((GameLevel) trainingRun1.getCurrentLevel()).getSolution(), convertJsonBytesToString(response.getContentAsString()));
        assertTrue(trainingRun1.isSolutionTaken());
    }

    @Test
    public void getSolutionSecondTime() throws Exception {
        trainingRun1.setMaxLevelScore(1);
        trainingRun1.setTotalScore(11);
        trainingRun1.setSolutionTaken(true);
        trainingRunRepository.save(trainingRun1);
        assertTrue(trainingRun1.isSolutionTaken());
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/" + trainingRun1.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/solutions", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(((GameLevel) trainingRun1.getCurrentLevel()).getSolution(), convertJsonBytesToString(response.getContentAsString()));
        assertTrue(trainingRun1.isSolutionTaken());
        assertEquals(11, trainingRun1.getTotalScore());
    }

    @Test
    public void getSolutionNotGameLevel() throws Exception {
        trainingRun1.setCurrentLevel(infoLevel1);
        trainingRunRepository.save(trainingRun1);

        Exception ex = mvc.perform(get("/training-runs/{runId}/solutions", trainingRun1.getId()))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), BadRequestException.class);
        assertTrue(ex.getMessage().contains("Current level is not game level and does not have solution."));
    }

    @Test
    public void getSolutionLevelNotFound() throws Exception {
        Exception ex = mvc.perform(get("/training-runs/{runId}/solutions", nonExistentTrainingRunId))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Training Run with id: " + nonExistentTrainingRunId + " not found.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void getHint() throws Exception {
        hintRepository.save(hint);
        trainingRun1.setTotalScore(10 + gameLevel1.getMaxScore());
        trainingRun1.setMaxLevelScore(gameLevel1.getMaxScore());
        trainingRunRepository.save(trainingRun1);
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hint.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(hintMapper.mapToDTO(hint), mapper.readValue(convertJsonBytesToString(response.getContentAsString()), HintDTO.class));
        assertTrue(trainingRun1.getHintInfoList().contains(new HintInfo(trainingRun1.getCurrentLevel().getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder())));
    }

    @Test
    public void getHintNotFound() throws Exception {
        long nonExistentHintId = 100L;
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), nonExistentHintId))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertEquals("Hint with id " + nonExistentHintId + " not found.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void getHintTrainingRunNotFound() throws Exception {
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(get("/training-runs/{runId}/hints/{hintId}", nonExistentTrainingRunId, 1L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertEquals("Training Run with id: " + nonExistentTrainingRunId + " not found.", ex.getCause().getCause().getMessage());

    }

    @Test
    public void getHintWrongLevelType() throws Exception {
        trainingRunRepository.save(trainingRun2);
        hintRepository.save(hint);

        Exception exception = mvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun2.getId(), hint.getId()))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertEquals(BadRequestException.class, Objects.requireNonNull(exception).getClass());
    }

    @Test
    public void isCorrectFlag() throws Exception {
        trainingRunRepository.save(trainingRun1);
        isCorrectFlagDTO.setRemainingAttempts(gameLevel1.getIncorrectFlagLimit());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        assertFalse(trainingRun1.isLevelAnswered());
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/is-correct-flag", trainingRun1.getId())
                .param("flag", "testFlag"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(isCorrectFlagDTO, mapper.readValue(response.getContentAsString(), IsCorrectFlagDTO.class));
        assertTrue(trainingRun1.isLevelAnswered());
    }

    @Test
    public void isCorrectFlagWrongFlag() throws Exception {
        trainingRunRepository.save(trainingRun1);

        isCorrectFlagDTO.setRemainingAttempts(gameLevel1.getIncorrectFlagLimit() - 1);
        isCorrectFlagDTO.setCorrect(false);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/" + trainingRun1.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));

        assertFalse(trainingRun1.isLevelAnswered());
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/is-correct-flag", trainingRun1.getId())
                .param("flag", "wrongFlag"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(isCorrectFlagDTO, mapper.readValue(response.getContentAsString(), IsCorrectFlagDTO.class));
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test
    public void isCorrectFlagWrongFlagFlagCountSameAsFlagLimit() throws Exception {
        trainingRun1.setIncorrectFlagCount(gameLevel1.getIncorrectFlagLimit());
        trainingRunRepository.save(trainingRun1);

        isCorrectFlagDTO.setRemainingAttempts(0);
        isCorrectFlagDTO.setCorrect(false);
        isCorrectFlagDTO.setSolution(gameLevel1.getSolution());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        assertFalse(trainingRun1.isLevelAnswered());
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/is-correct-flag", trainingRun1.getId())
                .param("flag", "wrongFlag"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(isCorrectFlagDTO, mapper.readValue(response.getContentAsString(), IsCorrectFlagDTO.class));
        assertFalse(trainingRun1.isLevelAnswered());
        assertTrue(trainingRun1.isSolutionTaken());
    }

    @Test
    public void isCorrectFlagWrongFlagFlagCountLessOneThanFlagCount() throws Exception {
        trainingRun1.setIncorrectFlagCount(gameLevel1.getIncorrectFlagLimit() - 1);
        trainingRunRepository.save(trainingRun1);

        isCorrectFlagDTO.setRemainingAttempts(0);
        isCorrectFlagDTO.setCorrect(false);
        isCorrectFlagDTO.setSolution(gameLevel1.getSolution());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        assertFalse(trainingRun1.isLevelAnswered());
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/is-correct-flag", trainingRun1.getId())
                .param("flag", "wrongFlag"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(isCorrectFlagDTO, mapper.readValue(response.getContentAsString(), IsCorrectFlagDTO.class));
        assertFalse(trainingRun1.isLevelAnswered());
        assertTrue(trainingRun1.isSolutionTaken());
    }

    @Test
    public void isCorrectFlagNoGameLevel() throws Exception {
        trainingRunRepository.save(trainingRun2);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        Exception ex = mvc.perform(get("/training-runs/{runId}/is-correct-flag", trainingRun2.getId())
                .param("flag", "gameFlag"))
                .andExpect(status().isBadRequest()).andReturn().getResolvedException();

        assertEquals(BadRequestException.class, Objects.requireNonNull(ex).getClass());
    }

    @Test
    public void resumeTrainingRunWithoutTakenSolutionAndHints() throws Exception {
        trainingRunRepository.save(trainingRun1);
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = new JSONObject(response.getContentAsString());
        Long trainingRunId = jsonObject.getLong("trainingRunID");
        Object takenSolution = jsonObject.get("takenSolution");
        JSONArray arrayOfHints = jsonObject.getJSONArray("takenHints");
        assertTrue(takenSolution.equals(null));
        assertTrue(arrayOfHints.toList().isEmpty());
        Optional<TrainingRun> trainingRun = trainingRunRepository.findById(trainingRunId);
        assertTrue(trainingRun.isPresent());
        assertEquals(trainingRun1, trainingRun.get());
    }

    @Test
    public void resumeTrainingRunWithTakenSolution() throws Exception {
        trainingRun1.setSolutionTaken(true);
        trainingRunRepository.save(trainingRun1);
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = new JSONObject(response.getContentAsString());
        Long trainingRunId = jsonObject.getLong("trainingRunID");
        String takenSolution = jsonObject.getString("takenSolution");
        assertEquals(gameLevel1.getSolution(), takenSolution);
        Optional<TrainingRun> trainingRun = trainingRunRepository.findById(trainingRunId);
        assertTrue(trainingRun.isPresent());
        assertEquals(trainingRun1, trainingRun.get());
    }

    @Test
    public void resumeTrainingRunWithTakenHints() throws Exception {
        trainingRun1.addHintInfo(new HintInfo(gameLevel1.getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder()));
        trainingRunRepository.save(trainingRun1);
        TakenHintDTO expectedTakenHint = new TakenHintDTO();
        expectedTakenHint.setId(hint.getId());
        expectedTakenHint.setTitle(hint.getTitle());
        expectedTakenHint.setContent(hint.getContent());
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<SandboxInfo>(sandboxInfo1, HttpStatus.OK));
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/" + trainingRun1.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        MockHttpServletResponse response = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = new JSONObject(response.getContentAsString());
        Long trainingRunId = jsonObject.getLong("trainingRunID");
        JSONArray arrayOfHints = jsonObject.getJSONArray("takenHints");
        assertFalse(arrayOfHints.toList().isEmpty());
        List<TakenHintDTO> takenHints = new ArrayList<>();
        for (int i = 0; i < arrayOfHints.length(); i++) {
            takenHints.add(mapper.readValue(arrayOfHints.getJSONObject(i).toString(), TakenHintDTO.class));
        }
        assertFalse(takenHints.isEmpty());
        assertTrue(takenHints.contains(expectedTakenHint));
        Optional<TrainingRun> trainingRun = trainingRunRepository.findById(trainingRunId);
        assertTrue(trainingRun.isPresent());
        assertEquals(trainingRun1, trainingRun.get());
    }

    @Test
    public void resumeFinishedTrainingRun() throws Exception {
        trainingRun2.setState(TRState.FINISHED);
        trainingRunRepository.save(trainingRun2);
        Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun2.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Cannot resume finished training run.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void resumeTrainingRunOfFinishedTrainingInstance() throws Exception {
        trainingInstance.setEndTime(LocalDateTime.now().minusHours(2));
        trainingInstanceRepository.save(trainingInstance);
        trainingRun1.setTrainingInstance(trainingInstance);
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Cannot resume training run after end of training instance.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void resumeTrainingRunWithDeletedSandbox() throws Exception {
        trainingRun1.setSandboxInstanceRefId(null);
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Sandbox of this training run was already deleted, you have to start new game.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void resumeArchivedTrainingRun() throws Exception {
        trainingRun2.setState(TRState.ARCHIVED);
        trainingRunRepository.save(trainingRun2);
        Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", trainingRun2.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Cannot resume finished training run.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void resumeTrainingRunNotFound() throws Exception {
        Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", nonExistentTrainingRunId))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Training Run with id: " + nonExistentTrainingRunId + " not found.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void finishTrainingRun() throws Exception {
        trainingRun1.setCurrentLevel(infoLevel1);
        trainingRunRepository.save(trainingRun1);
        mvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isOk());
        assertEquals(TRState.FINISHED, trainingRun1.getState());
    }

    @Test
    public void finishTrainingRunWithNotLastLevel() throws Exception {
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Cannot finish training run because current level is not last.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void finishTrainingRunWithNonAnsweredLevel() throws Exception {
        trainingRun1.setCurrentLevel(infoLevel1);
        trainingRun1.setLevelAnswered(false);
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Cannot finish training run because current level is not answered.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void finishTrainingRunNotFound() throws Exception {
        Exception ex = mvc.perform(put("/training-runs/{runId}", nonExistentTrainingRunId))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, ex.getClass());
        assertEquals("Training Run with runId: " + nonExistentTrainingRunId + " not found.", ex.getCause().getCause().getLocalizedMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
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

    private static String convertJsonBytesToString(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }
}
