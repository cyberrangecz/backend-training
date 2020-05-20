package cz.muni.ics.kypo.training.rest.controllers;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiEntityError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TrainingDefinitionsRestController.class, TestDataFactory.class})
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingDefinitionsIT {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private MockMvc mvc;
    private static final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrainingDefinitionsRestController.class);

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TrainingDefinitionsRestController trainingDefinitionsRestController;
    @Autowired
    private LevelMapperImpl levelMapper;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private GameLevelRepository gameLevelRepository;
    @Autowired
    private InfoLevelRepository infoLevelRepository;
    @Autowired
    private AssessmentLevelRepository assessmentLevelRepository;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    @Qualifier("objMapperRESTApi")
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("userManagementExchangeFunction")
    private ExchangeFunction exchangeFunction;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO, invalidDefinitionUpdateDTO;
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
    private TrainingDefinitionByIdDTO invalidDefinitionDTO;
    private TrainingDefinition releasedTrainingDefinition, unreleasedTrainingDefinition, archivedTrainingDefinition;
    private TrainingDefinitionDTO releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionDTO, archivedTrainingDefinitionDTO;
    private TrainingDefinitionInfoDTO releasedTrainingDefinitionInfoDTO, unreleasedTrainingDefinitionInfoDTO, archivedTrainingDefinitionInfoDTO;
    private TrainingInstance trainingInstance;
    private UserRef organizer1, organizer2, author1, author2;
    private GameLevel gameLevel1, gameLevel2;
    private GameLevelUpdateDTO gameLevelUpdateDTO, invalidGameLevelUpdateDTO;
    private InfoLevel infoLevel1;
    private InfoLevelUpdateDTO infoLevelUpdateDTO, invalidInfoLevelUpdateDTO;
    private AssessmentLevel assessmentLevel1;
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO, invalidAssessmentLevelUpdateDTO;
    private UserRefDTO organizerDTO1, organizerDTO2, authorDTO1, authorDTO2;


    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();

        organizer1 = testDataFactory.getUserRef1();
        organizer2 = testDataFactory.getUserRef2();
        author1 = testDataFactory.getUserRef3();
        author2 = testDataFactory.getUserRef4();
        userRefRepository.saveAll(List.of(organizer1, organizer2, author1, author2));


        organizerDTO1 = testDataFactory.getUserRefDTO1();
        organizerDTO2 = testDataFactory.getUserRefDTO2();
        authorDTO1 = testDataFactory.getUserRefDTO3();
        authorDTO2 = testDataFactory.getUserRefDTO4();

        gameLevel1 = testDataFactory.getPenalizedLevel();
        gameLevelUpdateDTO = testDataFactory.getGameLevelUpdateDTO();
        invalidGameLevelUpdateDTO = new GameLevelUpdateDTO();
        gameLevel2 = testDataFactory.getNonPenalizedLevel();

        infoLevel1 = testDataFactory.getInfoLevel1();
        infoLevelUpdateDTO = testDataFactory.getInfoLevelUpdateDTO();
        invalidInfoLevelUpdateDTO = new InfoLevelUpdateDTO();

        assessmentLevel1 = testDataFactory.getQuestionnaire();
        assessmentLevel1.setQuestions("[]");
        assessmentLevelUpdateDTO = testDataFactory.getAssessmentLevelUpdateDTO();
        assessmentLevelUpdateDTO.setQuestions("[]");
        invalidAssessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();

        trainingDefinitionCreateDTO = testDataFactory.getTrainingDefinitionCreateDTO();

        invalidDefinitionDTO = new TrainingDefinitionByIdDTO();

        releasedTrainingDefinition = testDataFactory.getReleasedDefinition();
        releasedTrainingDefinition.setAuthors(new HashSet<>(List.of(author1)));
        unreleasedTrainingDefinition = testDataFactory.getUnreleasedDefinition();
        unreleasedTrainingDefinition.setAuthors(new HashSet<>(Collections.singletonList(author1)));
        archivedTrainingDefinition = testDataFactory.getArchivedDefinition();
        archivedTrainingDefinition.setAuthors(Set.of(author1));

        releasedTrainingDefinitionInfoDTO = testDataFactory.getReleasedDefinitionInfoDTO();
        unreleasedTrainingDefinitionInfoDTO = testDataFactory.getUnreleasedDefinitionInfoDTO();
        archivedTrainingDefinitionInfoDTO = testDataFactory.getArchivedDefinitionInfoDTO();

        unreleasedTrainingDefinitionDTO = testDataFactory.getUnreleasedDefinitionDTO();
        unreleasedTrainingDefinitionDTO.setCanBeArchived(true);
        releasedTrainingDefinitionDTO = testDataFactory.getReleasedDefinitionDTO();
        releasedTrainingDefinitionDTO.setCanBeArchived(true);
        archivedTrainingDefinitionDTO = testDataFactory.getArchivedDefinitionDTO();
        archivedTrainingDefinitionDTO.setCanBeArchived(true);
        trainingDefinitionUpdateDTO = testDataFactory.getTrainingDefinitionUpdateDTO();
        invalidDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();

        trainingInstance = testDataFactory.getOngoingInstance();
        trainingInstance.setOrganizers(Set.of(author1));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @After
    public void reset() throws Exception {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_definition", "abstract_level");
    }

    @Test
    public void findTrainingDefinitionById() throws Exception {
        TrainingDefinition expected = trainingDefinitionRepository.save(releasedTrainingDefinition);
        GameLevel gameLevel = gameLevelRepository.save(gameLevel1);
        gameLevel.setTrainingDefinition(expected);
        trainingDefinitionRepository.save(expected);

        MockHttpServletResponse result = mvc.perform(get("/training-definitions" + "/{id}", expected.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        TrainingDefinitionByIdDTO definitionDTO = trainingDefinitionMapper.mapToDTOById(expected);
        GameLevelDTO gameLevelDTO = levelMapper.mapToGameLevelDTO(gameLevel1);
        gameLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        definitionDTO.setLevels(new ArrayList<>(Collections.singleton(gameLevelDTO)));
        assertEquals(definitionDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void findTrainingDefinitionById_NotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-definitions" + "/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void findAllTrainingDefinitions_AdminRole() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name()));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {});
        releasedTrainingDefinitionDTO.setId(releasedTrainingDefinition.getId());
        unreleasedTrainingDefinitionDTO.setId(unreleasedTrainingDefinition.getId());
        archivedTrainingDefinitionDTO.setId(archivedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionDTO, archivedTrainingDefinitionDTO)));
    }

    @Test
    public void findAllTrainingDefinitions() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer1, organizer2));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {});
        releasedTrainingDefinitionDTO.setId(releasedTrainingDefinition.getId());
        unreleasedTrainingDefinitionDTO.setId(unreleasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionDTO)));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers_Released() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer1, organizer2));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions")
                .queryParam("state", "RELEASED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {});
        releasedTrainingDefinitionInfoDTO.setId(releasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().contains(releasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers_Unreleased_Admin() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer2));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions")
                .queryParam("state", "UNRELEASED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {});
        unreleasedTrainingDefinitionInfoDTO.setId(unreleasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().contains(unreleasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers_Unreleased_OrganizerAndDesigner() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer1));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name(), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions")
                .queryParam("state", "UNRELEASED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {});
        unreleasedTrainingDefinitionInfoDTO.setId(unreleasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().contains(unreleasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void createTrainingDefinition() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(organizerDTO1));

        MockHttpServletResponse result = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingDefinition> newDefinition = trainingDefinitionRepository.findById(1L);
        assertTrue(newDefinition.isPresent());
        TrainingDefinitionByIdDTO newDefinitionDTO = trainingDefinitionMapper.mapToDTOById(newDefinition.get());
        assertEquals(newDefinitionDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void createTrainingDefinition_InvalidDefinition() throws Exception {
        Exception ex = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingDefinition() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        userRefRepository.save(author1);
        unreleasedTrainingDefinition.addAuthor(author1);
        trainingDefinitionUpdateDTO.setId(unreleasedTrainingDefinition.getId());
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        Optional<TrainingDefinition> optionalDefinition = trainingDefinitionRepository.findById(unreleasedTrainingDefinition.getId());
        assertTrue(optionalDefinition.isPresent());
        TrainingDefinition updatedDefinition = optionalDefinition.get();
        assertEquals(trainingDefinitionUpdateDTO.getTitle(), updatedDefinition.getTitle());
        assertEquals(trainingDefinitionUpdateDTO.getDescription(), updatedDefinition.getDescription());
        assertEquals(trainingDefinitionUpdateDTO.getState().toString(), updatedDefinition.getState().toString());
        assertEquals(trainingDefinitionUpdateDTO.isShowStepperBar(), updatedDefinition.isShowStepperBar());
        assertEquals(unreleasedTrainingDefinition.getAuthors(), updatedDefinition.getAuthors());
    }

    @Test
    public void updateTrainingDefinition_InvalidDefinition() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingDefinition_DefinitionNotFound() throws Exception {
        trainingDefinitionUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateTrainingDefinition_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionUpdateDTO.setId(releasedTrainingDefinition.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateTrainingDefinition_CreatedInstance() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);
        trainingDefinitionUpdateDTO.setId(unreleasedTrainingDefinition.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedTrainingDefinition.getId().toString(),
                "Cannot update training definition with already created training instance. Remove training instance/s before updating training definition.");
    }

    @Test
    public void cloneTrainingDefinition() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(unreleasedTrainingDefinition);
        trainingDefinitionByIdDTO.setLevels(List.of(levelMapper.mapToDTO(gameLevel1)));

        MockHttpServletResponse result = mvc.perform(post("/training-definitions" + "/{id}", unreleasedTrainingDefinition.getId())
                .param("title", "title"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        Optional<TrainingDefinition> clonedTrainingDefinition = trainingDefinitionRepository.findById(2L);
        assertTrue(clonedTrainingDefinition.isPresent());
        assertEquals("title", clonedTrainingDefinition.get().getTitle());
        assertEquals(clonedTrainingDefinition.get().getState().toString(), TDState.UNRELEASED.toString());
        assertEquals(clonedTrainingDefinition.get().isShowStepperBar(), unreleasedTrainingDefinition.isShowStepperBar());
        assertNull(clonedTrainingDefinition.get().getBetaTestingGroup());
    }

    @Test
    public void cloneNonexistentTrainingDefinition() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/training-definitions" + "/{id}", 100L).param("title", "title"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void swapLevels() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setOrder(1);
        gameLevel2.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevel2.setOrder(2);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevel1.setOrder(3);
        gameLevelRepository.saveAll(Set.of(gameLevel1, gameLevel2));
        infoLevelRepository.save(infoLevel1);

        mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), infoLevel1.getId(), gameLevel1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Optional<GameLevel> optGameLevel = gameLevelRepository.findById(gameLevel1.getId());
        Optional<InfoLevel> optInfoLevel = infoLevelRepository.findById(infoLevel1.getId());
        assertTrue(optGameLevel.isPresent());
        assertTrue(optInfoLevel.isPresent());
        assertEquals(1, gameLevel1.getOrder());
        assertEquals(3, infoLevel1.getOrder());
    }

    @Test
    public void swapLevels_FromLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), 50, infoLevel1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "50",
                "Level not found.");
    }

    @Test
    public void swapLevels_ToLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), infoLevel1.getId(), 50))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "50",
                "Level not found.");
    }

    @Test
    public void swapLevels_CreatedInstance() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), gameLevel1.getId(), infoLevel1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedTrainingDefinition.getId().toString(),
                "Cannot update training definition with already created training instance. Remove training instance/s before updating training definition.");
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevel1);
        gameLevelRepository.save(gameLevel1);
        infoLevelRepository.save(infoLevel1);

        mvc.perform(delete("/training-definitions" + "/{id}", unreleasedTrainingDefinition.getId()))
                .andExpect(status().isOk());
        assertFalse(trainingDefinitionRepository.findById(unreleasedTrainingDefinition.getId()).isPresent());
        assertFalse(assessmentLevelRepository.findById(assessmentLevel1.getId()).isPresent());
        assertFalse(gameLevelRepository.findById(gameLevel1.getId()).isPresent());
        assertFalse(infoLevelRepository.findById(infoLevel1.getId()).isPresent());
    }

    @Test
    public void deleteDefinition_NotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void deleteTrainingDefinition_Released() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{Id}", releasedTrainingDefinition.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot delete released training definition.");
    }

    @Test
    public void deleteDefinition_WithTrainingInstances() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);

        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{id}", unreleasedTrainingDefinition.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedTrainingDefinition.getId().toString(),
                "Cannot delete training definition with already created training instance. Remove training instance/s before deleting training definition.");
    }

    @Test
    public void deleteOneLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        infoLevelRepository.save(infoLevel1);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);

        mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", unreleasedTrainingDefinition.getId(), gameLevel1.getId()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        assertTrue(trainingDefinitionRepository.findById(unreleasedTrainingDefinition.getId()).isPresent());
        assertFalse(gameLevelRepository.findById(gameLevel1.getId()).isPresent());
        assertTrue(infoLevelRepository.findById(infoLevel1.getId()).isPresent());
    }

    @Test
    public void deleteOneLevel_NotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", unreleasedTrainingDefinition.getId(), 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "100",
                "Level not found.");
    }

    @Test
    public void deleteOneLevel_DefinitionNotFound() throws Exception {
        gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", 100L, gameLevel1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void deleteOneLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}",
                releasedTrainingDefinition.getId(), gameLevel1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void deleteOneLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}",
                releasedTrainingDefinition.getId(), gameLevel1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateGameLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevelUpdateDTO.setId(gameLevel1.getId());

        mvc.perform(put("/training-definitions/{definitionId}/game-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<GameLevel> updatedGameLevel = gameLevelRepository.findById(gameLevel1.getId());
        assertTrue(updatedGameLevel.isPresent());
        assertEquals(updatedGameLevel.get().getTitle(), gameLevelUpdateDTO.getTitle());
        assertEquals(updatedGameLevel.get().getContent(), gameLevelUpdateDTO.getContent());
        assertEquals(updatedGameLevel.get().getFlag(), gameLevelUpdateDTO.getFlag());
        assertEquals(updatedGameLevel.get().getSolution(), gameLevelUpdateDTO.getSolution());
        assertEquals(updatedGameLevel.get().isSolutionPenalized(), gameLevelUpdateDTO.isSolutionPenalized());
        assertEquals(updatedGameLevel.get().getMaxScore(), gameLevelUpdateDTO.getMaxScore());
    }

    @Test
    public void updateGameLevel_InvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(invalidGameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateGameLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        gameLevelUpdateDTO.setId(gameLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels",
                releasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateGameLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        gameLevelUpdateDTO.setId(gameLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels", archivedTrainingDefinition.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateGameLevel_DefinitionNotFound() throws Exception {
        gameLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateGameLevel_NotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        gameLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", gameLevelUpdateDTO.getId().toString(),
                "Level was not found in definition (id: " + unreleasedTrainingDefinition.getId() + ").");
    }

    @Test
    public void updateInfoLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevelUpdateDTO.setId(infoLevel1.getId());

        mvc.perform(put("/training-definitions/{definitionId}/info-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<InfoLevel> updatedInfoLevel = infoLevelRepository.findById(infoLevel1.getId());
        assertTrue(updatedInfoLevel.isPresent());
        assertEquals(updatedInfoLevel.get().getTitle(), infoLevelUpdateDTO.getTitle());
        assertEquals(updatedInfoLevel.get().getContent(), infoLevelUpdateDTO.getContent());
    }

    @Test
    public void updateInfoLevel_InvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(invalidInfoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateInfoLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(infoLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", releasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateInfoLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(infoLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels",
                archivedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateInfoLevel_DefinitionNotFound() throws Exception {
        infoLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateInfoLevel_LevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", infoLevelUpdateDTO.getId().toString(),
                "Level was not found in definition (id: " + unreleasedTrainingDefinition.getId() + ").");

    }

    @Test
    public void updateAssessmentLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevel1);
        assessmentLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelUpdateDTO.setId(assessmentLevel1.getId());

        mvc.perform(put("/training-definitions/{definitionId}/assessment-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<AssessmentLevel> updatedAssessmentLevel = assessmentLevelRepository.findById(assessmentLevel1.getId());
        assertTrue(updatedAssessmentLevel.isPresent());
        assertEquals(updatedAssessmentLevel.get().getTitle(), assessmentLevelUpdateDTO.getTitle());
        assertEquals(updatedAssessmentLevel.get().getAssessmentType().toString(), assessmentLevelUpdateDTO.getType().toString());
        assertEquals(updatedAssessmentLevel.get().getQuestions(), assessmentLevelUpdateDTO.getQuestions());
        assertEquals(updatedAssessmentLevel.get().getInstructions(), assessmentLevelUpdateDTO.getInstructions());
        assertEquals(updatedAssessmentLevel.get().getMaxScore(), assessmentLevelUpdateDTO.getMaxScore());
    }

    @Test
    public void updateAssessmentLevel_InvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(invalidAssessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateAssessmentLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevel1);
        assessmentLevelUpdateDTO.setId(assessmentLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", releasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateAssessmentLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevel1);
        assessmentLevelUpdateDTO.setId(assessmentLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels",
                archivedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateAssessmentLevel_DefinitionNotFound() throws Exception {
        assessmentLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateAssessmentLevel_LevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", assessmentLevelUpdateDTO.getId().toString(),
                "Level was not found in definition (id: " + unreleasedTrainingDefinition.getId() + ").");
    }

    @Test
    public void findGameLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        gameLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", gameLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        GameLevelDTO gameLevelDTO = levelMapper.mapToGameLevelDTO(gameLevel1);
        gameLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        assertEquals(gameLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), GameLevelDTO.class));
    }

    @Test
    public void findInfoLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", infoLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        InfoLevelDTO infoLevelDTO = levelMapper.mapToInfoLevelDTO(infoLevel1);
        infoLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        assertEquals(infoLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), InfoLevelDTO.class));
    }

    @Test
    public void findAssessmentLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", assessmentLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        AssessmentLevelDTO assessmentLevelDTO = levelMapper.mapToAssessmentLevelDTO(assessmentLevel1);
        assessmentLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assertEquals(assessmentLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), AssessmentLevelDTO.class));
    }

    @Test
    public void findGameLevelById_NotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-definitions/levels/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "100",
                "Level not found.");
    }

    @Test
    public void createGameLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.GAME))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        Optional<GameLevel> optionalGameLevel = gameLevelRepository.findById(1L);
        assertTrue(optionalGameLevel.isPresent());
        GameLevel gameLevel = optionalGameLevel.get();
        assertEquals(100, gameLevel.getMaxScore() );
        assertEquals("Title of game level", gameLevel.getTitle());
        assertEquals(100, gameLevel.getIncorrectFlagLimit());
        assertEquals("Secret flag", gameLevel.getFlag());
        assertTrue(gameLevel.isSolutionPenalized());
        assertEquals("Solution of the game should be here", gameLevel.getSolution());
        assertEquals("The test entry should be here", gameLevel.getContent());
    }

    @Test
    public void createInfoLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.INFO))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        Optional<InfoLevel> optionalInfoLevel = infoLevelRepository.findById(1L);
        assertTrue(optionalInfoLevel.isPresent());
        InfoLevel infoLevel = optionalInfoLevel.get();
        assertEquals(0, infoLevel.getMaxScore());
        assertEquals("Title of info level", infoLevel.getTitle());
        assertEquals("Content of info level should be here.", infoLevel.getContent());
    }

    @Test
    public void createAssessmentLevel() throws Exception {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        Optional<AssessmentLevel> optionalAssessmentLevel = assessmentLevelRepository.findById(1L);
        assertTrue(optionalAssessmentLevel.isPresent());
        AssessmentLevel assessmentLevel = optionalAssessmentLevel.get();
        assertEquals(0, assessmentLevel.getMaxScore());
        assertEquals("Title of assessment level", assessmentLevel.getTitle());
        assertEquals(AssessmentType.QUESTIONNAIRE.toString(), assessmentLevel.getAssessmentType().toString());
        assertEquals("Instructions should be here", assessmentLevel.getInstructions());
    }

    @Test
    public void createLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void createLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", archivedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void createLevel_DefinitionNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", 100L, cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void editAuthors_Add() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertTrue(releasedTrainingDefinition.getAuthors().size() == 1 && releasedTrainingDefinition.getAuthors().contains(author1));
        ArgumentMatcher<ClientRequest> userInfoMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/info");
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(argThat(userInfoMatcher));
        ArgumentMatcher<ClientRequest> usersIdsMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/ids");
        doReturn(buildMockResponse(new PageResultResource<UserRefDTO>(List.of(organizerDTO1, authorDTO1, authorDTO2), new PageResultResource.Pagination(0,3,3,3,1))))
                .when(exchangeFunction).exchange(argThat(usersIdsMatcher));

        mvc.perform(put("/training-definitions/{definitionId}/authors", releasedTrainingDefinition.getId())
                .queryParam("authorsAddition",  StringUtils.collectionToDelimitedString(List.of(author1.getUserRefId(), author2.getUserRefId(), organizer1.getUserRefId()), ","))
                .queryParam("authorsRemoval", ""))
                .andExpect(status().isNoContent());
        assertEquals(3, releasedTrainingDefinition.getAuthors().size());
        assertEquals(Set.of(author1, author2, organizer1), releasedTrainingDefinition.getAuthors());
    }

    @Test
    public void editAuthors_Remove() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(new HashSet<>(Set.of(author1, author2, organizer1, organizer2)));
        ArgumentMatcher<ClientRequest> userInfoMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/info");
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(argThat(userInfoMatcher));

        mvc.perform(put("/training-definitions/{definitionId}/authors", releasedTrainingDefinition.getId())
                .queryParam("authorsAddition",  "")
                .queryParam("authorsRemoval", StringUtils.collectionToDelimitedString(List.of(organizer2.getUserRefId(), organizer1.getUserRefId()), ",")))
                .andExpect(status().isNoContent());
        assertEquals(2, releasedTrainingDefinition.getAuthors().size());
        assertEquals(Set.of(author1, author2), releasedTrainingDefinition.getAuthors());
    }

    @Test
    public void editAuthors_RemoveLoggedInAuthor() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(new HashSet<>(Set.of(author1, author2)));
        ArgumentMatcher<ClientRequest> userInfoMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/info");
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(argThat(userInfoMatcher));

        mvc.perform(put("/training-definitions/{definitionId}/authors", releasedTrainingDefinition.getId())
                .queryParam("authorsAddition",  "")
                .queryParam("authorsRemoval", StringUtils.collectionToDelimitedString(List.of(author1.getUserRefId()), ",")))
                .andExpect(status().isNoContent());
        assertEquals(2, releasedTrainingDefinition.getAuthors().size());
        assertEquals(Set.of(author1, author2), releasedTrainingDefinition.getAuthors());
    }

    @Test
    public void switchStateOfDefinition_DefinitionNotFound() throws Exception {
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", 1000, cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "1000",
                "Entity TrainingDefinition (id: 1000) not found.");
    }

    @Test
    public void switchStateOfDefinition_UnreleasedToReleased() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assertEquals(TDState.UNRELEASED.name(), unreleasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", unreleasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.RELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.RELEASED.name(), unreleasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_UnreleasedToUnreleased() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);

        assertEquals(TDState.UNRELEASED.name(), unreleasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", unreleasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.UNRELEASED.name(), unreleasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_ReleasedToArchived() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.ARCHIVED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.ARCHIVED.name(), releasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_ReleasedToUnreleased_Allowed() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.UNRELEASED.name(), releasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_ReleasedToUnreleased_NotAllowed() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(releasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);

        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot update training definition with already created training instance(s). Remove training " +
                        "instance(s) before changing the state from released to unreleased training definition.");
    }

    @Test
    public void switchStateOfDefinition_ArchivedToUnreleased() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);

        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}",
                archivedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot switch from " + TDState.ARCHIVED.name() + " to " + TDState.UNRELEASED.name());
    }


    private static void assertTwoJsons(String object1, String object2) {
        JSONAssert.assertEquals(object1, object2, false);
    }

    private void mockSpringSecurityContextForGet(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "778932@muni.cz");
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), "Peter Černý");
        sub.addProperty(AuthenticatedUserOIDCItems.FAMILY_NAME.getName(), "Černý");
        sub.addProperty(AuthenticatedUserOIDCItems.GIVEN_NAME.getName(), "Peter");
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

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        if (entityErrorDetail.getIdentifierValue() == null) {
            assertEquals(value, entityErrorDetail.getIdentifierValue());
        } else {
            assertEquals(value, entityErrorDetail.getIdentifierValue().toString());
        }
        assertEquals(reason, entityErrorDetail.getReason());
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException{
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }
}

