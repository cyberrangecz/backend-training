package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.training.enums.SandboxStates;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import cz.muni.ics.kypo.training.utils.SandboxPoolInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Boris Jadus(445343)
 */
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

    private TrainingInstance futureTrainingInstance, notConcludedTrainingInstance, finishedTrainingInstance;
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;
    private TrainingRun trainingRun1, trainingRun2;
    private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2;
    private TrainingDefinition trainingDefinition;
    private UserRef organizer1, organizer2;
    private UserInfoDTO userInfoDTO;
    private SandboxInfo sandboxInfo1, sandboxInfo2;
    private SandboxPoolInfo sandboxPoolInfo;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        organizer1 = createUserRef("778932@muni.cz", "Peter Černý", "Peter", "Černý");
        organizer2 = createUserRef("773254@muni.cz", "Jakub Plátal", "Jakub", "Plátal");
        userRefRepository.saveAll(Set.of(organizer1, organizer2));


        sandboxInstanceRef1 = new SandboxInstanceRef();
        sandboxInstanceRef1.setSandboxInstanceRef(1L);
        sandboxInstanceRef2 = new SandboxInstanceRef();
        sandboxInstanceRef2.setSandboxInstanceRef(2L);

        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(15L);
        sandboxInfo1.setStatus(SandboxStates.FULL_BUILD_IN_PROGRESS.getName());
        sandboxInfo1.setPool(3L);
        sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId(16L);
        sandboxInfo2.setStatus(SandboxStates.FULL_BUILD_IN_PROGRESS.getName());
        sandboxInfo2.setPool(3L);

        sandboxPoolInfo = new SandboxPoolInfo();
        sandboxPoolInfo.setId(15L);
        sandboxPoolInfo.setDefinition(10L);
        sandboxPoolInfo.setSize(0L);

        BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
        betaTestingGroup.setOrganizers(new HashSet<>(Arrays.asList(organizer1)));

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setTitle("definition");
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setShowStepperBar(true);
        trainingDefinition.setBetaTestingGroup(betaTestingGroup);
        trainingDefinition.setSandboxDefinitionRefId(1L);
        trainingDefinition.setLastEdited(LocalDateTime.now());
        trainingDefinition.setAuthors(new HashSet<>(Arrays.asList(organizer1)));
        TrainingDefinition tD = trainingDefinitionRepository.save(trainingDefinition);

        futureTrainingInstance = new TrainingInstance();
        futureTrainingInstance.setStartTime(LocalDateTime.now().plusHours(24));
        futureTrainingInstance.setEndTime(LocalDateTime.now().plusHours(80));
        futureTrainingInstance.setTitle("futureInstance");
        futureTrainingInstance.setPoolSize(20);
        futureTrainingInstance.setAccessToken("pass-1234");
        futureTrainingInstance.setTrainingDefinition(tD);
        futureTrainingInstance.setOrganizers(new HashSet<>(Arrays.asList(organizer1)));

        finishedTrainingInstance = new TrainingInstance();
        finishedTrainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
        finishedTrainingInstance.setEndTime(LocalDateTime.now().minusHours(2));
        finishedTrainingInstance.setTrainingDefinition(trainingDefinition);
        finishedTrainingInstance.setTitle("Finished training instance");
        finishedTrainingInstance.setAccessToken("token-1254");
        finishedTrainingInstance.setOrganizers(Set.of(organizer1));

        notConcludedTrainingInstance = new TrainingInstance();
        notConcludedTrainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
        notConcludedTrainingInstance.setEndTime(LocalDateTime.now().plusHours(24));
        notConcludedTrainingInstance.setTitle("NotConcluded");
        notConcludedTrainingInstance.setPoolSize(25);
        notConcludedTrainingInstance.setAccessToken("key-9999");
        notConcludedTrainingInstance.setTrainingDefinition(tD);
        notConcludedTrainingInstance.setOrganizers(new HashSet<>(Arrays.asList(organizer1)));

        trainingInstanceCreateDTO = new TrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setStartTime(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
        trainingInstanceCreateDTO.setEndTime(LocalDateTime.now().plusHours(80));
        trainingInstanceCreateDTO.setTrainingDefinitionId(tD.getId());
        trainingInstanceCreateDTO.setTitle("newInstance");
        trainingInstanceCreateDTO.setPoolSize(50);
        trainingInstanceCreateDTO.setAccessToken("pass-1235");
        trainingInstanceCreateDTO.setOrganizersLogin(Set.of());

        trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setOrganizersLogin(Set.of(organizer2.getUserRefLogin()));
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
        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);
        trainingRun1.setParticipantRef(organizer1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setStartTime(LocalDateTime.now().plusHours(2));
        trainingRun2.setEndTime(LocalDateTime.now().plusHours(4));
        trainingRun2.setState(TRState.RUNNING);
        trainingRun2.setIncorrectFlagCount(10);
        trainingRun2.setSolutionTaken(false);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setTrainingInstance(futureTrainingInstance);
        trainingRun2.setSandboxInstanceRef(sandboxInstanceRef2);
        trainingRun2.setParticipantRef(organizer1);

        userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFullName("Ing. Mgr. MuDr. Boris Jadus");
        userInfoDTO.setLogin("445469@muni.cz");
        userInfoDTO.setGivenName("Boris");
        userInfoDTO.setFamilyName("Jadus");

    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_instance");
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "sandbox_instance_ref");
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_run");
    }
    @Test
    public void findAllTrainingInstancesAsAdmin() throws Exception {
        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceRepository.save(futureTrainingInstance);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));

        MockHttpServletResponse result = mvc.perform(get("/training-instances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingInstanceDTO> trainingInstancesPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingInstanceDTO>>() {});
        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(notConcludedTrainingInstance)));
        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(futureTrainingInstance)));
    }

    @Test
    public void findTrainingInstanceById() throws Exception {
        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
        futureTrainingInstance.setSandboxInstanceRefs(Set.of(sandboxInstanceRef1, sandboxInstanceRef2));
        trainingInstanceRepository.save(futureTrainingInstance);

        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);
        trainingRunRepository.save(trainingRun1);
        MockHttpServletResponse result = mvc.perform(get("/training-instances/{id}", futureTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        TrainingInstanceDTO expectedInstanceDTO = trainingInstanceMapper.mapToDTO(futureTrainingInstance);
        expectedInstanceDTO.setSandboxesWithTrainingRun(List.of(sandboxInstanceRef1.getId()));
        TrainingInstanceDTO responseInstanceDTO = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingInstanceDTO.class);
        assertEquals(expectedInstanceDTO, responseInstanceDTO);
    }

    @Test
    public void findTrainingInstanceByIdNotFound() throws Exception {
        Exception ex = mvc.perform(get("/training-instances/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training instance with id: 100 not found"));
    }

    @Test
    public void createTrainingInstance() throws Exception {
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(1L);
        assertTrue(newInstance.isPresent());
        TrainingInstanceDTO newInstanceDTO = trainingInstanceMapper.mapToDTO(newInstance.get());

        assertTrue(newInstanceDTO.getOrganizers().stream().anyMatch(userRefDTO -> userRefDTO.getUserRefLogin().equals("556978@muni.cz")));
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
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
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
        Exception ex = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Training definition with id: 100 not found.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void updateTrainingInstanceNotFound() throws Exception {
        trainingInstanceUpdateDTO.setAccessToken("someToken");
        trainingInstanceUpdateDTO.setId(500L);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception ex = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Training instance with id: 500, not found.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void updateTrainingInstanceWrongStartTime() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingInstanceUpdateDTO.setAccessToken("someToken");
        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
        trainingInstanceUpdateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception ex = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();

        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("End time must be later than start time.", ex.getCause().getCause().getMessage());
    }

    @Test
    public void deleteTrainingInstance() throws Exception {
        TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);
        mvc.perform(delete("/training-instances/{id}", tI.getId()))
                .andExpect(status().isOk());
        Optional<TrainingInstance> optTI = trainingInstanceRepository.findById(tI.getId());
        assertFalse(optTI.isPresent());
    }

    @Test
    public void deleteNonExistentInstance() throws Exception {
        Exception ex = mvc.perform(delete("/training-instances/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training instance with id: 100, not found"));
    }

    @Test
    public void deleteNotConcludedTrainingInstance() throws Exception {
        TrainingInstance tI = trainingInstanceRepository.save(notConcludedTrainingInstance);
        Exception ex = mvc.perform(delete("/training-instances/{id}", tI.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("The training instance which is running cannot be deleted."));
    }

    @Test
    public void deleteFinishedTrainingInstanceWithTrainingRuns() throws Exception {
        trainingInstanceRepository.save(finishedTrainingInstance);
        trainingRun1.setTrainingInstance(finishedTrainingInstance);
        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);
        sandboxInstanceRef1.setTrainingInstance(finishedTrainingInstance);
        trainingRunRepository.save(trainingRun1);
        Exception ex = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("Finished training instance with already assigned training runs cannot be deleted."));
    }

    @Test
    public void deleteFinishedTrainingInstanceWithSandboxes() throws Exception {
        sandboxInstanceRef1.setTrainingInstance(finishedTrainingInstance);
        finishedTrainingInstance.setPoolId(5L);
        finishedTrainingInstance.setPoolSize(3);
        finishedTrainingInstance.setSandboxInstanceRefs(Set.of(sandboxInstanceRef1));
        trainingInstanceRepository.save(finishedTrainingInstance);
        Exception ex = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("Cannot delete training instance because it contains some sandboxes. Please delete sandboxes and try again."));
    }

    @Test
    public void allocateSandboxes() throws Exception {
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(2);
        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1, sandboxInfo2)), HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        mvc.perform(post("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
    }

    @Test
    public void allocateSandboxesWithoutCreatedPool() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, Objects.requireNonNull(exception).getClass());
        assertEquals("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.", exception.getCause().getCause().getMessage());
    }

    @Test
    public void allocateSandboxesWithFullPool() throws Exception {
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(1);
        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
        futureTrainingInstance.setSandboxInstanceRefs(Set.of(sandboxInstanceRef1));

        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1, sandboxInfo2)), HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertEquals("Pool of sandboxes of training instance with id: 1 is full.", exception.getCause().getCause().getMessage());
    }

    @Test
    public void createPoolForSandboxes() throws Exception {
        futureTrainingInstance.setPoolSize(5);
        trainingInstanceRepository.save(futureTrainingInstance);
        sandboxPoolInfo.setMaxSize(5L);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-instances/{instanceId}/pools", futureTrainingInstance.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        assertEquals(sandboxPoolInfo.getId().toString(), result.getContentAsString());
    }

    @Test
    public void createPoolInInstanceWithAlreadyCreatedPool() throws Exception {
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(1);

        trainingInstanceRepository.save(futureTrainingInstance);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-instances/{instanceId}/pools", futureTrainingInstance.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        assertEquals("3", result.getContentAsString());
    }
    @Test
    public void findAllTrainingRunsByTrainingInstanceId() throws Exception {
        futureTrainingInstance.addSandboxInstanceRef(trainingRun1.getSandboxInstanceRef());
        futureTrainingInstance.addSandboxInstanceRef(trainingRun2.getSandboxInstanceRef());
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingRunRepository.save(trainingRun1);
        trainingRunRepository.save(trainingRun2);

        MockHttpServletResponse result = mvc.perform(get("/training-instances/{instanceId}/training-runs", futureTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingRunDTO> trainingRunsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingRunDTO>>() {});
        assertTrue(trainingRunsPage.getContent().contains(trainingRunMapper.mapToDTO(trainingRun1)));
        assertTrue(trainingRunsPage.getContent().contains(trainingRunMapper.mapToDTO(trainingRun2)));
    }

    @Test
    public void findAllTrainingRunsByTrainingInstanceIdWithNonexistentInstance() throws Exception {
        Exception ex = mvc.perform(get("/training-instances/{instanceId}/training-runs", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training instance with id: 100 not found."));
    }

    @Test
    public void deleteSandboxes() throws Exception {
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(2);
        futureTrainingInstance.addSandboxInstanceRef(sandboxInstanceRef1);
        futureTrainingInstance.addSandboxInstanceRef(sandboxInstanceRef2);

        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        mvc.perform(delete("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
                .param("sandboxIds", sandboxInstanceRef2.getSandboxInstanceRef().toString(), sandboxInstanceRef1.getSandboxInstanceRef().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().isEmpty());
    }

    @Test
    public void deleteNonExistentSandbox() throws Exception {
        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(3);
        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));

        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        mvc.perform(delete("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
                .param("sandboxIds", sandboxInstanceRef1.getSandboxInstanceRef().toString(), "156")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        assertEquals(1, futureTrainingInstance.getSandboxInstanceRefs().size());
        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef2));
    }

    @Test
    public void reallocateSandbox() throws Exception {
        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(10);
        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));

        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1)), HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        mvc.perform(post("/training-instances/{instanceId}/sandbox-instances/{sandboxId}", futureTrainingInstance.getId(), sandboxInstanceRef1.getSandboxInstanceRef())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef2));
        assertFalse(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef1));
        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().stream().anyMatch(sandboxInstanceRef ->
                sandboxInstanceRef.getSandboxInstanceRef().equals(sandboxInfo1.getId())));
    }

    @Test
    public void reallocateSandboxWithNoSpaceForNewSandbox() throws Exception {
        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(2);
        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));

        trainingInstanceRepository.save(futureTrainingInstance);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1)), HttpStatus.OK));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances/{sandboxId}", futureTrainingInstance.getId(), sandboxInstanceRef1.getSandboxInstanceRef())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef2));
        assertFalse(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef1));
        assertEquals("Sandbox cannot be reallocated because pool of training instance with id: " + futureTrainingInstance.getId() + " is full. " +
                "Given sandbox with id: " + sandboxInstanceRef1.getSandboxInstanceRef() + " is probably in the process of removing right now. Please wait and try allocate new sandbox later or contact administrator.",
                Objects.requireNonNull(exception).getCause().getCause().getMessage());
    }

    @Test
    public void reallocateNonExistentSandbox() throws Exception {
        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
        futureTrainingInstance.setPoolId(3L);
        futureTrainingInstance.setPoolSize(2);
        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));

        trainingInstanceRepository.save(futureTrainingInstance);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances/{sandboxId}", futureTrainingInstance.getId(), 156)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(exception).getClass());
        assertEquals("Given sandbox with id: 156 is not in DB or is not assigned to given training instance.",
                exception.getCause().getCause().getMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("SimpleModule").addSerializer(new LocalDateTimeUTCSerializer());
        mapper.registerModule(simpleModule);
        return mapper.writeValueAsString(object);
    }

    private void mockSpringSecurityContextForGet(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role: roles) {
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

    private UserRef createUserRef(String login, String fullName, String givenName, String familyName) {
        UserRef userRef = new UserRef();
        userRef.setUserRefLogin(login);
        userRef.setUserRefFullName(fullName);
        userRef.setUserRefGivenName(givenName);
        userRef.setUserRefFamilyName(familyName);
        return userRef;
    }

}

