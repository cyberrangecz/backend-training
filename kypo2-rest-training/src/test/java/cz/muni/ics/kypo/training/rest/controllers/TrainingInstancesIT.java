package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.JsonObject;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.muni.ics.kypo.training.api.responses.*;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.exceptions.RestTemplateException;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingInstancesRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingInstancesIT {

    private MockMvc mvc;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TrainingInstancesRestController trainingInstancesRestController;
    @Autowired
    private TrainingInstanceMapperImpl trainingInstanceMapper;
    @Autowired
    private TrainingRunMapperImpl trainingRunMapper;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private TrainingRunRepository trainingRunRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private InfoLevelRepository infoLevelRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("objMapperRESTApi")
    private ObjectMapper mapper;
    @Autowired
    private TrainingEventsService trainingEventsServiceMock;

    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;

    private TrainingInstance futureTrainingInstance, notConcludedTrainingInstance, finishedTrainingInstance;
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;
    private TrainingRun trainingRun1, trainingRun2;
    private TrainingDefinition trainingDefinition;
    private UserRef organizer1, organizer2, participant1, participant2;
    private UserRefDTO userRefDTO1, userRefDTO2;
    private SandboxInfo sandboxInfo1, sandboxInfo2;
    private SandboxPoolInfo sandboxPoolInfo;
    private LockedPoolInfo lockedPoolInfo;
    private TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO;


    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();

        organizer1 = new UserRef();
        organizer1.setUserRefId(1L);
        organizer2 = new UserRef();
        organizer2.setUserRefId(2L);
        participant1 = new UserRef();
        participant1.setUserRefId(3L);
        participant2 = new UserRef();
        participant2.setUserRefId(4L);
        userRefRepository.saveAll(Set.of(organizer1, organizer2, participant1, participant2));

        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(1L);
        sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId(2L);

        sandboxPoolInfo = new SandboxPoolInfo();
        sandboxPoolInfo.setId(15L);
        sandboxPoolInfo.setDefinition(10L);
        sandboxPoolInfo.setSize(0L);

        BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
        betaTestingGroup.setOrganizers(new HashSet<>(List.of(organizer1)));

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setTitle("definition");
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setShowStepperBar(true);
        trainingDefinition.setBetaTestingGroup(betaTestingGroup);
        trainingDefinition.setLastEdited(LocalDateTime.now());
        trainingDefinition.setAuthors(new HashSet<>(List.of(organizer1)));
        TrainingDefinition tD = trainingDefinitionRepository.save(trainingDefinition);

        futureTrainingInstance = new TrainingInstance();
        futureTrainingInstance.setStartTime(LocalDateTime.now().plusHours(24));
        futureTrainingInstance.setEndTime(LocalDateTime.now().plusHours(80));
        futureTrainingInstance.setTitle("futureInstance");
        futureTrainingInstance.setPoolSize(20);
        futureTrainingInstance.setAccessToken("pass-1234");
        futureTrainingInstance.setTrainingDefinition(tD);
        futureTrainingInstance.setOrganizers(new HashSet<>(List.of(organizer1)));

        finishedTrainingInstance = new TrainingInstance();
        finishedTrainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
        finishedTrainingInstance.setEndTime(LocalDateTime.now().minusHours(2));
        finishedTrainingInstance.setTrainingDefinition(trainingDefinition);
        finishedTrainingInstance.setTitle("Finished training instance");
        finishedTrainingInstance.setAccessToken("token-1254");
        finishedTrainingInstance.setPoolSize(5);
        finishedTrainingInstance.setPoolId(8L);
        finishedTrainingInstance.setOrganizers(new HashSet<>(List.of(organizer1)));

        notConcludedTrainingInstance = new TrainingInstance();
        notConcludedTrainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
        notConcludedTrainingInstance.setEndTime(LocalDateTime.now().plusHours(24));
        notConcludedTrainingInstance.setTitle("NotConcluded");
        notConcludedTrainingInstance.setPoolSize(25);
        notConcludedTrainingInstance.setAccessToken("key-9999");
        notConcludedTrainingInstance.setTrainingDefinition(tD);
        notConcludedTrainingInstance.setOrganizers(new HashSet<>(List.of(organizer1)));

        trainingInstanceCreateDTO = new TrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setStartTime(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
        trainingInstanceCreateDTO.setEndTime(LocalDateTime.now().plusHours(80));
        trainingInstanceCreateDTO.setTrainingDefinitionId(tD.getId());
        trainingInstanceCreateDTO.setTitle("newInstance");
        trainingInstanceCreateDTO.setPoolSize(50);
        trainingInstanceCreateDTO.setAccessToken("pass-1235");

        trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setPoolSize(6);
        trainingInstanceUpdateDTO.setStartTime(LocalDateTime.now().plusHours(4));
        trainingInstanceUpdateDTO.setEndTime(LocalDateTime.now().plusHours(45));
        trainingInstanceUpdateDTO.setTitle("Training instance to be updated");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(trainingDefinition.getId());

        InfoLevel iL = new InfoLevel();
        iL.setContent("content");
        iL.setTitle("title");
        iL.setMaxScore(50);
        InfoLevel infoLevel = infoLevelRepository.save(iL);

        trainingRun1 = new TrainingRun();
        trainingRun1.setStartTime(LocalDateTime.now().plusHours(24));
        trainingRun1.setEndTime(LocalDateTime.now().plusHours(48));
        trainingRun1.setState(TRState.RUNNING);
        trainingRun1.setIncorrectFlagCount(5);
        trainingRun1.setSolutionTaken(false);
        trainingRun1.setCurrentLevel(infoLevel);
        trainingRun1.setTrainingInstance(futureTrainingInstance);
        trainingRun1.setParticipantRef(participant2);
        trainingRun1.setSandboxInstanceRefId(sandboxInfo1.getId());
        trainingRun1.setParticipantRef(organizer1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setStartTime(LocalDateTime.now().plusHours(2));
        trainingRun2.setEndTime(LocalDateTime.now().plusHours(4));
        trainingRun2.setState(TRState.RUNNING);
        trainingRun2.setIncorrectFlagCount(10);
        trainingRun2.setSolutionTaken(false);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setTrainingInstance(futureTrainingInstance);
        trainingRun2.setParticipantRef(participant1);

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

        trainingRun2.setSandboxInstanceRefId(sandboxInfo2.getId());
        trainingRun2.setParticipantRef(organizer1);

        trainingInstanceAssignPoolIdDTO = new TrainingInstanceAssignPoolIdDTO();
        trainingInstanceAssignPoolIdDTO.setId(1L);
        trainingInstanceAssignPoolIdDTO.setPoolId(1L);

        lockedPoolInfo = new LockedPoolInfo();
        lockedPoolInfo.setId(1L);
        lockedPoolInfo.setPool(1L);
    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_instance");
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_run");
    }

@Test
    public void findAllTrainingInstancesAsAdmin() throws Exception {
        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceRepository.save(futureTrainingInstance);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        PageResultResourcePython<SandboxInfo> pageResult = new PageResultResourcePython<>();
        pageResult.setResults(new ArrayList<>());
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pageResult, HttpStatus.OK));
        MockHttpServletResponse result = mvc.perform(get("/training-instances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingInstanceDTO> trainingInstancesPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingInstanceDTO>>() {
        });
        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(notConcludedTrainingInstance)));
        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(futureTrainingInstance)));
    }

    @Test
    public void findTrainingInstanceById() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        sandboxInfo1.setLocked(true);

        PageResultResourcePython<SandboxInfo> pageResult = new PageResultResourcePython<>();
        pageResult.setResults(new ArrayList<>(List.of(sandboxInfo1)));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pageResult, HttpStatus.OK));
        MockHttpServletResponse result = mvc.perform(get("/training-instances/{id}", futureTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        TrainingInstanceDTO expectedInstanceDTO = trainingInstanceMapper.mapToDTO(futureTrainingInstance);
        expectedInstanceDTO.setSandboxesWithTrainingRun(List.of(sandboxInfo1.getId()));
        TrainingInstanceDTO responseInstanceDTO = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingInstanceDTO.class);
        assertEquals(expectedInstanceDTO, responseInstanceDTO);
    }

    @Test
    public void findTrainingInstanceByIdNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-instances/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", "100",
                "Training instance not found.");
    }

    @Test
    public void createTrainingInstance() throws Exception {
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(new ArrayList<>(List.of(userRefDTO1)));
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(1L);
        assertTrue(newInstance.isPresent());
        TrainingInstanceDTO newInstanceDTO = trainingInstanceMapper.mapToDTO(newInstance.get());

        assertEquals(newInstanceDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingInstanceDTO.class));
    }

    @Test
    public void createInvalidTrainingInstance() throws Exception {
        Exception ex = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(new TrainingInstanceCreateDTO()))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingInstance() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingInstanceUpdateDTO.setAccessToken(futureTrainingInstance.getAccessToken());
        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO1));

        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(futureTrainingInstance.getId());
        assertTrue(newInstance.isPresent());

        assertEquals(newInstance.get().getAccessToken(), convertJsonBytesToString(result.getContentAsString()));
    }

    @Test
    public void updateTrainingInstanceTrainingDefinitionNotFoundd() throws Exception {
        trainingInstanceUpdateDTO.setId(100L);
        trainingInstanceUpdateDTO.setAccessToken("preff");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(100L);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void updateTrainingInstanceNotFound() throws Exception {
        trainingInstanceUpdateDTO.setAccessToken("someToken");
        trainingInstanceUpdateDTO.setId(500L);
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO1));
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", "500",
                "Training instance not found.");
    }

    @Test
    public void updateTrainingInstanceWrongStartTime() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingInstanceUpdateDTO.setAccessToken("someToken");
        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
        trainingInstanceUpdateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO1));
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", futureTrainingInstance.getId().toString(),
                "End time must be later than start time.");
    }

    @Test
    public void deleteTrainingInstance() throws Exception {
        TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);
        doNothing().when(trainingEventsServiceMock).deleteEventsByTrainingInstanceId(anyLong());

        mvc.perform(delete("/training-instances/{id}", tI.getId()))
                .andExpect(status().isOk());
        Optional<TrainingInstance> optTI = trainingInstanceRepository.findById(tI.getId());
        assertFalse(optTI.isPresent());
    }

    @Test
    public void deleteFinishedTrainingInstanceWithTrainingRuns() throws Exception {
        trainingInstanceRepository.save(finishedTrainingInstance);
        trainingRun1.setTrainingInstance(finishedTrainingInstance);
        trainingRunRepository.save(trainingRun1);

        MockHttpServletResponse response = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", finishedTrainingInstance.getId().toString(),
                "Training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance and try again or contact administrator.");
    }

    @Test
    public void deleteTrainingInstanceWithSandboxes() throws Exception {
        trainingInstanceRepository.save(finishedTrainingInstance);

        PageResultResourcePython<SandboxInfo> pageResult = new PageResultResourcePython<>();
        pageResult.setResults(new ArrayList<>(List.of(sandboxInfo1)));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pageResult, HttpStatus.OK));
        Exception ex = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andReturn().getResolvedException();
    }

    @Test
    public void assignPool() throws Exception {
        futureTrainingInstance.setPoolId(1L);
        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.postForObject(anyString(), any(HttpEntity.class), any())).
                willReturn(lockedPoolInfo);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        mvc.perform(patch("/training-instances" + "/{instanceId}/" + "assign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void assignPoolWithMicroserviceException() throws Exception {
        futureTrainingInstance.setPoolId(1L);
        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.postForObject(anyString(), any(HttpEntity.class), any())).
                willThrow(RestTemplateException.class);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception exception = mvc.perform(patch("/training-instances" + "/{instanceId}/" + "assign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(MicroserviceApiException.class, exception.getClass());
        assertEquals("Currently, it is not possible to lock and assign pool with (ID: " + trainingInstanceAssignPoolIdDTO.getPoolId() + ").", exception.getMessage());
    }

    @Test
    public void reassignPool() throws Exception {
        PoolInfoDto poolInfoDto = new PoolInfoDto();
        poolInfoDto.setLock(1L);

        futureTrainingInstance.setPoolId(1L);
        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.postForObject(anyString(), any(HttpEntity.class), any())).
                willReturn(lockedPoolInfo);
        given(restTemplate.getForEntity(anyString(), any())).willReturn(new ResponseEntity<>(poolInfoDto, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        mvc.perform(patch("/training-instances" + "/{instanceId}/" + "reassign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }
    @Test
    public void reassignPoolWithMicroserviceException() throws Exception {
        futureTrainingInstance.setPoolId(1L);
        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.postForObject(anyString(), any(HttpEntity.class), any())).
                willThrow(RestTemplateException.class);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception exception = mvc.perform(patch("/training-instances" + "/{instanceId}/" + "reassign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(MicroserviceApiException.class, exception.getClass());
        assertEquals("Currently, it is not possible to lock and assign pool with (ID: " + trainingInstanceAssignPoolIdDTO.getPoolId() + ").", exception.getMessage());
    }

    @Test
    public void findAllTrainingRunsByTrainingInstanceId() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingRunRepository.save(trainingRun1);
        trainingRunRepository.save(trainingRun2);

        given(restTemplate.exchange(eq(userAndGroupURI + "/users/"+trainingRun1.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO1, HttpStatus.OK));
        given(restTemplate.exchange(eq(userAndGroupURI + "/users/"+trainingRun2.getParticipantRef().getUserRefId()), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRefDTO.class))).
                willReturn(new ResponseEntity<UserRefDTO>(userRefDTO2, HttpStatus.OK));
        MockHttpServletResponse result = mvc.perform(get("/training-instances/{instanceId}/training-runs", futureTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingRunDTO> trainingRunsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingRunDTO>>() {
        });
        TrainingRunDTO trainingRunDTO1 = trainingRunMapper.mapToDTO(trainingRun1);
        trainingRunDTO1.setParticipantRef(userRefDTO2);
        TrainingRunDTO trainingRunDTO2 = trainingRunMapper.mapToDTO(trainingRun2);
        trainingRunDTO2.setParticipantRef(userRefDTO2);
        assertTrue(trainingRunsPage.getContent().contains(trainingRunDTO1));
        assertTrue(trainingRunsPage.getContent().contains(trainingRunDTO2));
    }

    @Test
    public void findAllTrainingRunsByTrainingInstanceIdWithNonexistentInstance() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-instances/{instanceId}/training-runs", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", "100",
                "Training instance not found.");
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("SimpleModule").addSerializer(new LocalDateTimeUTCSerializer());
        mapper.registerModule(simpleModule);
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

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        if(entityErrorDetail.getIdentifierValue() == null) {
            assertEquals(value, entityErrorDetail.getIdentifierValue());
        } else {
            assertEquals(value, entityErrorDetail.getIdentifierValue().toString());
        }
        assertEquals(reason, entityErrorDetail.getReason());
    }


}

