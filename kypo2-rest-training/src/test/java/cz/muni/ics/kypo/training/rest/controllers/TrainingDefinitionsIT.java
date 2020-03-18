package cz.muni.ics.kypo.training.rest.controllers;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.mapping.mapstruct.AssessmentLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.GameLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.InfoLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.ApiEntityError;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.*;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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
    private GameLevelMapperImpl gameLevelMapper;
    @Autowired
    private InfoLevelMapperImpl infoLevelMapper;
    @Autowired
    private AssessmentLevelMapperImpl assessmentLevelMapper;
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
    @Qualifier("javaRestTemplate")
    private RestTemplate javaRestTemplate;
    @Autowired
    @Qualifier("pythonRestTemplate")
    private RestTemplate pythonRestTemplate;
    @Autowired
    @Qualifier("objMapperRESTApi")
    private ObjectMapper mapper;

    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO, invalidDefinitionUpdateDTO, updateForNonexistingDefinition;
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
    private TrainingDefinitionByIdDTO invalidDefinitionDTO;
    private TrainingDefinition releasedTrainingDefinition, unreleasedDefinition, archivedTrainingDefinition;
    private TrainingInstance trainingInstance;
    private UserRef organizer1, organizer2, author1, author2;
    private GameLevel gameLevel1, gameLevel2;
    private GameLevelUpdateDTO gameLevelUpdateDTO, invalidGameLevelUpdateDTO;
    private InfoLevel infoLevel1;
    private InfoLevelUpdateDTO infoLevelUpdateDTO, invalidInfoLevelUpdateDTO;
    private AssessmentLevel assessmentLevel1;
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO, invalidAssessmentLevelUpdateDTO;
    private UserRefDTO userRefDTO;

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

        organizer1 = createUserRef("778932@muni.cz", "Peter Černý", "Peter", "Černý", "https://oidc.muni.cz", 1L);
        organizer2 = createUserRef("773254@muni.cz", "Jakub Plátal", "Jakub", "Plátal", "https://oidc.muni.cz", 2L);
        author1 = createUserRef("672932@muni.cz", "Jozef Hajek", "Jozef", "Hajek", "https://oidc.muni.cz", 3L);
        author2 = createUserRef("347932@muni.cz", "Miroslav Drobný", "Miroslav", "Drobný", "https://oidc.muni.cz", 4L);
        userRefRepository.saveAll(List.of(organizer1, organizer2, author1, author2));


        BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
        betaTestingGroup.setOrganizers(new HashSet<>(List.of(organizer1, organizer2)));

        BetaTestingGroup betaTestingGroup2 = new BetaTestingGroup();
        betaTestingGroup2.setOrganizers(new HashSet<>(List.of(organizer2)));

        BetaTestingGroupCreateDTO betaTestingGroupCreateDTO = new BetaTestingGroupCreateDTO();
        betaTestingGroupCreateDTO.setOrganizersRefIds(Set.of(organizer2.getUserRefId()));

        BetaTestingGroupUpdateDTO betaTestingGroupUpdateDTO = new BetaTestingGroupUpdateDTO();
        betaTestingGroupUpdateDTO.setOrganizersRefIds(Set.of(organizer2.getUserRefId()));

        userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefFullName("Ing. Mgr. MuDr. Boris Jadus");
        userRefDTO.setUserRefLogin("445469@muni.cz");
        userRefDTO.setUserRefGivenName("Boris");
        userRefDTO.setUserRefFamilyName("Jadus");
        userRefDTO.setIss("https://oidc.muni.cz");
        userRefDTO.setUserRefId(1L);

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

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        trainingDefinitionCreateDTO = testDataFactory.getTrainingDefinitionCreateDTO();

        releasedTrainingDefinition = testDataFactory.getReleasedDefinition();
        releasedTrainingDefinition.setAuthors(new HashSet<>(List.of(author1)));

        invalidDefinitionDTO = new TrainingDefinitionByIdDTO();

        unreleasedDefinition = testDataFactory.getUnreleasedDefinition();
        unreleasedDefinition.setAuthors(new HashSet<>(Arrays.asList(author1)));

        archivedTrainingDefinition = testDataFactory.getArchivedDefinition();
        archivedTrainingDefinition.setAuthors(Set.of(author1));

        trainingDefinitionUpdateDTO = testDataFactory.getTrainingDefinitionUpdateDTO();

        invalidDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();

        trainingInstance = testDataFactory.getOngoingInstance();
        trainingInstance.setOrganizers(Set.of(author1));
    }

    @After
    public void reset() throws SQLException {
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
        GameLevelDTO gameLevelDTO = gameLevelMapper.mapToDTO(gameLevel1);
        gameLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        definitionDTO.setLevels(new ArrayList<>(Collections.singleton(gameLevelDTO)));
        assertEquals(definitionDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void findTrainingDefinitionByIdWithDefinitionNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-definitions" + "/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void findAllTrainingDefinitions() throws Exception {
        TrainingDefinition tD1 = trainingDefinitionRepository.save(releasedTrainingDefinition);
        TrainingDefinition tD2 = trainingDefinitionRepository.save(unreleasedDefinition);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name()));

        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(tD1);
        expected.add(tD2);
        Page p = new PageImpl<TrainingDefinition>(expected);
        PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource = trainingDefinitionMapper.mapToPageResultResource(p);
        for (TrainingDefinitionDTO trainingDefinitionDTO : trainingDefinitionDTOPageResultResource.getContent()) {
            trainingDefinitionDTO.setCanBeArchived(true);
        }
        trainingDefinitionDTOPageResultResource.getPagination().setSize(20);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionDTO> trainingDefinitionsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {
        });

        TrainingDefinitionDTO releasedTrainingDefinitionDTO = trainingDefinitionMapper.mapToDTO(releasedTrainingDefinition);
        releasedTrainingDefinitionDTO.setCanBeArchived(true);
        TrainingDefinitionDTO unreleasedTrainingDefinitionInfoDTO = trainingDefinitionMapper.mapToDTO(unreleasedDefinition);
        unreleasedTrainingDefinitionInfoDTO.setCanBeArchived(true);
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionInfoDTO)));
    }

    @Test
    public void createTrainingDefinition() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO));

        given(javaRestTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        given(javaRestTemplate.getForObject(anyString(), eq(UserRefDTO.class))).
                willReturn(userRefDTO);

        MockHttpServletResponse result = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingDefinition> newDefinition = trainingDefinitionRepository.findById(1L);
        assertTrue(newDefinition.isPresent());
        TrainingDefinitionByIdDTO newDefinitionDTO = trainingDefinitionMapper.mapToDTOById(newDefinition.get());
        assertEquals(newDefinitionDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void createTrainingDefinitionWithoutBetaTestingGroup() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO));
        given(javaRestTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        given(javaRestTemplate.getForObject(anyString(), eq(UserRefDTO.class))).
                willReturn(userRefDTO);
        trainingDefinitionCreateDTO.setBetaTestingGroup(null);
        MockHttpServletResponse result = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingDefinition> newDefinition = trainingDefinitionRepository.findById(1L);
        assertTrue(newDefinition.isPresent());
        TrainingDefinitionByIdDTO newDefinitionDTO = trainingDefinitionMapper.mapToDTOById(newDefinition.get());
        assertEquals(newDefinitionDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void createTrainingDefinitionWithInvalidDefinition() throws Exception {
        Exception ex = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        trainingDefinitionUpdateDTO.setId(tD.getId());
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO));
        given(javaRestTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));
        given(javaRestTemplate.getForObject(anyString(), eq(UserRefDTO.class))).
                willReturn(userRefDTO);
        mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        Optional<TrainingDefinition> optionalDefinition = trainingDefinitionRepository.findById(tD.getId());
        assertTrue(optionalDefinition.isPresent());
        TrainingDefinition updatedDefinition = optionalDefinition.get();
        assertEquals(updatedDefinition.getTitle(), trainingDefinitionUpdateDTO.getTitle());
        assertEquals(updatedDefinition.getDescription(), trainingDefinitionUpdateDTO.getDescription());
        assertEquals(updatedDefinition.getState().toString(), trainingDefinitionUpdateDTO.getState().toString());
        assertEquals(updatedDefinition.isShowStepperBar(), trainingDefinitionUpdateDTO.isShowStepperBar());
        assertEquals(updatedDefinition.getAuthors(), unreleasedDefinition.getAuthors());
    }

    @Test
    public void updateTrainingDefinitionWithInvalidDefinition() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingDefinitionWithNonexistentDefinition() throws Exception {
        trainingDefinitionUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void updateTrainingDefinitionWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionUpdateDTO.setId(tD.getId());
        PageResultResource<UserRefDTO> userRefDTOPageResultResource = new PageResultResource<>();
        userRefDTOPageResultResource.setContent(List.of(userRefDTO));
        given(javaRestTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserRefDTO>>(userRefDTOPageResultResource, HttpStatus.OK));

        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void cloneTrainingDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        gameLevel1.setTrainingDefinition(tD);
        gameLevelRepository.save(gameLevel1);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(tD);
        trainingDefinitionByIdDTO.setLevels(List.of(gameLevelMapper.mapToDTO(gameLevel1)));


        MockHttpServletResponse result = mvc.perform(post("/training-definitions" + "/{id}", tD.getId()).param("title", "title"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        Optional<TrainingDefinition> opt = trainingDefinitionRepository.findById(2L);
        assertTrue(opt.isPresent());
        TrainingDefinition clonedTD = opt.get();
        assertEquals("title", clonedTD.getTitle());
        assertEquals(clonedTD.getState().toString(), TDState.UNRELEASED.toString());
        assertEquals(clonedTD.isShowStepperBar(), tD.isShowStepperBar());
        assertNull(clonedTD.getBetaTestingGroup());
    }

    @Test
    public void cloneNonexistentTrainingDefinition() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/training-definitions" + "/{id}", 100L).param("title", "title"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void swapLevels() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        gameLevel1.setTrainingDefinition(unreleasedDefinition);
        gameLevel1.setOrder(3);
        infoLevel1.setTrainingDefinition(unreleasedDefinition);
        infoLevel1.setOrder(1);
        gameLevel2.setTrainingDefinition(unreleasedDefinition);
        gameLevel2.setOrder(2);
        gameLevelRepository.saveAll(Set.of(gameLevel1, gameLevel2));
        infoLevelRepository.save(infoLevel1);

        mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedDefinition.getId(), infoLevel1.getId(), gameLevel1.getId()))
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
    public void swapLevelsFirstLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        infoLevel1.setTrainingDefinition(unreleasedDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedDefinition.getId(), 50, infoLevel1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "50",
                "Level not found.");
    }

    @Test
    public void swapLevelsSecondLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        infoLevel1.setTrainingDefinition(unreleasedDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedDefinition.getId(), infoLevel1.getId(), 50))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "50",
                "Level not found.");
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        InfoLevel iL = infoLevelRepository.save(infoLevel1);
        aL.setTrainingDefinition(tD);
        gL.setTrainingDefinition(tD);
        iL.setTrainingDefinition(tD);

        mvc.perform(delete("/training-definitions" + "/{id}", tD.getId()))
                .andExpect(status().isOk());
        Optional<TrainingDefinition> optTD = trainingDefinitionRepository.findById(tD.getId());
        Optional<AssessmentLevel> optAL = assessmentLevelRepository.findById(aL.getId());
        Optional<GameLevel> optGL = gameLevelRepository.findById(gL.getId());
        Optional<InfoLevel> optIL = infoLevelRepository.findById(iL.getId());
        assertFalse(optTD.isPresent());
        assertFalse(optAL.isPresent());
        assertFalse(optGL.isPresent());
        assertFalse(optIL.isPresent());
    }

    @Test
    public void deleteNonexistentDefinition() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void deleteReleasedTrainingDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{Id}", tD.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot delete released training definition.");
    }

    @Test
    public void deleteDefinitionWithTrainingInstances() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        trainingInstance.setTrainingDefinition(unreleasedDefinition);
        trainingInstanceRepository.save(trainingInstance);

        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{id}", unreleasedDefinition.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedDefinition.getId().toString(),
                "Cannot delete training definition with already created training instance. Remove training instance/s before deleting training definition.");
    }

    @Test
    public void deleteOneLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        InfoLevel iL = infoLevelRepository.save(infoLevel1);
        gL.setTrainingDefinition(tD);
        iL.setTrainingDefinition(tD);

        mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        Optional<TrainingDefinition> optTD = trainingDefinitionRepository.findById(tD.getId());
        Optional<GameLevel> optGL = gameLevelRepository.findById(gL.getId());
        Optional<InfoLevel> optIl = infoLevelRepository.findById(iL.getId());
        assertTrue(optTD.isPresent());
        assertFalse(optGL.isPresent());
        assertTrue(optIl.isPresent());
    }

    @Test
    public void deleteOneLevelWithNonexistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "100",
                "Level not found.");
    }

    @Test
    public void deleteOneLevelWithNonExistentDefinition() throws Exception {
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", 100L, gL.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void deleteOneLevelWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void deleteOneLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateGameLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        gL.setTrainingDefinition(tD);
        gameLevelUpdateDTO.setId(gL.getId());

        mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<GameLevel> optGL = gameLevelRepository.findById(gL.getId());
        assertTrue(optGL.isPresent());
        GameLevel updatedGL = optGL.get();
        assertEquals(updatedGL.getTitle(), gameLevelUpdateDTO.getTitle());
        assertEquals(updatedGL.getContent(), gameLevelUpdateDTO.getContent());
        assertEquals(updatedGL.getFlag(), gameLevelUpdateDTO.getFlag());
        assertEquals(updatedGL.getSolution(), gameLevelUpdateDTO.getSolution());
        assertEquals(updatedGL.isSolutionPenalized(), gameLevelUpdateDTO.isSolutionPenalized());
        assertEquals(updatedGL.getMaxScore(), gameLevelUpdateDTO.getMaxScore());
    }

    @Test
    public void updateGameLevelWithInvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(invalidGameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateGameLevelWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        gameLevelUpdateDTO.setId(gL.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateGameLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        gameLevelUpdateDTO.setId(gL.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateGameLevelWithNonexistentDefinition() throws Exception {
        gameLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void updateGameLevelWithNonExistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        gameLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", gameLevelUpdateDTO.getId().toString(),
                "Level was not found in definition (id: " + tD.getId() + ").");
    }

    @Test
    public void updateInfoLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        InfoLevel iL = infoLevelRepository.save(infoLevel1);
        iL.setTrainingDefinition(tD);
        infoLevelUpdateDTO.setId(iL.getId());

        mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<InfoLevel> optIL = infoLevelRepository.findById(iL.getId());
        assertTrue(optIL.isPresent());
        InfoLevel updatedIL = optIL.get();
        assertEquals(updatedIL.getTitle(), infoLevelUpdateDTO.getTitle());
        assertEquals(updatedIL.getContent(), infoLevelUpdateDTO.getContent());
    }

    @Test
    public void updateInfoLevelWithInvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(invalidInfoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateInfoLevelWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        InfoLevel iL = infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(iL.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateInfoLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        InfoLevel iL = infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(iL.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateInfoLevelWithNonExistentDefinition() throws Exception {
        infoLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void updateInfoLevelWithNonExistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        infoLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", infoLevelUpdateDTO.getId().toString(),
                "Level was not found in definition (id: " + tD.getId() + ").");

    }

    @Test
    public void updateAssessmentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
        aL.setTrainingDefinition(tD);
        assessmentLevelUpdateDTO.setId(aL.getId());

        mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<AssessmentLevel> optAL = assessmentLevelRepository.findById(aL.getId());
        assertTrue(optAL.isPresent());
        AssessmentLevel updatedAL = optAL.get();
        assertEquals(updatedAL.getTitle(), assessmentLevelUpdateDTO.getTitle());
        assertEquals(updatedAL.getAssessmentType().toString(), assessmentLevelUpdateDTO.getType().toString());
        assertEquals(updatedAL.getQuestions(), assessmentLevelUpdateDTO.getQuestions());
        assertEquals(updatedAL.getInstructions(), assessmentLevelUpdateDTO.getInstructions());
        assertEquals(updatedAL.getMaxScore(), assessmentLevelUpdateDTO.getMaxScore());
    }

    @Test
    public void updateAssessmentLevelWithInvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(invalidAssessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateAssessmentLevelWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
        assessmentLevelUpdateDTO.setId(aL.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateAssessmentLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
        assessmentLevelUpdateDTO.setId(aL.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateAssessmentLevelWithNonExistentDefinition() throws Exception {
        assessmentLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void updateAssessmentLevelWithNonExistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        assessmentLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", assessmentLevelUpdateDTO.getId().toString(),
                "Level was not found in definition (id: " + tD.getId() + ").");
    }

    @Test
    public void findGameLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        gameLevel1.setTrainingDefinition(unreleasedDefinition);
        gameLevelRepository.save(gameLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", gameLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        GameLevelDTO gameLevelDTO = gameLevelMapper.mapToDTO(gameLevel1);
        gameLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        assertEquals(gameLevelDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), GameLevelDTO.class));
    }

    @Test
    public void findInfoLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        infoLevel1.setTrainingDefinition(unreleasedDefinition);
        infoLevelRepository.save(infoLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", infoLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        InfoLevelDTO infoLevelDTO = infoLevelMapper.mapToDTO(infoLevel1);
        infoLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        assertEquals(infoLevelDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), InfoLevelDTO.class));
    }

    @Test
    public void findAssessmentLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        assessmentLevel1.setTrainingDefinition(unreleasedDefinition);
        assessmentLevelRepository.save(assessmentLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", assessmentLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        AssessmentLevelDTO assessmentLevelDTO = assessmentLevelMapper.mapToDTO(assessmentLevel1);
        assessmentLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assertEquals(assessmentLevelDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), AssessmentLevelDTO.class));
    }

    @Test
    public void findGameLevelByIdNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-definitions/levels/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "100",
                "Level not found");
    }

    @Test
    public void createGameLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedDefinition);
        mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.GAME))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        Optional<GameLevel> optionalGameLevel = gameLevelRepository.findById(1L);
        assertTrue(optionalGameLevel.isPresent());
        GameLevel gameLevel = optionalGameLevel.get();
        assertEquals(gameLevel.getMaxScore(), 100);
        assertEquals(gameLevel.getTitle(), "Title of game level");
        assertEquals(gameLevel.getIncorrectFlagLimit(), 100);
        assertEquals(gameLevel.getFlag(), "Secret flag");
        assertTrue(gameLevel.isSolutionPenalized());
        assertEquals(gameLevel.getSolution(), "Solution of the game should be here");
        assertEquals(gameLevel.getContent(), "The test entry should be here");
    }

    @Test
    public void createInfoLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedDefinition);
        mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.INFO))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        Optional<InfoLevel> optionalInfoLevel = infoLevelRepository.findById(1L);
        assertTrue(optionalInfoLevel.isPresent());
        InfoLevel infoLevel = optionalInfoLevel.get();
        assertEquals(infoLevel.getMaxScore(), 0);
        assertEquals(infoLevel.getTitle(), "Title of info level");
        assertEquals(infoLevel.getContent(), "Content of info level should be here.");
    }

    @Test
    public void createAssessmentLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedDefinition);
        mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        Optional<AssessmentLevel> optionalAssessmentLevel = assessmentLevelRepository.findById(1L);
        assertTrue(optionalAssessmentLevel.isPresent());
        AssessmentLevel assessmentLevel = optionalAssessmentLevel.get();
        assertEquals(assessmentLevel.getMaxScore(), 0);
        assertEquals(assessmentLevel.getTitle(), "Title of assessment level");
        assertEquals(assessmentLevel.getAssessmentType().toString(), AssessmentType.QUESTIONNAIRE.toString());
        assertEquals(assessmentLevel.getInstructions(), "Instructions should be here");
        assertEquals(assessmentLevel.getQuestions(), "[{\"answer_required\":false,\"order\":0,\"penalty\":0,\"points\":0,\"text\":\"Example Question\",\"question_type\":\"FFQ\",\"correct_choices\":[]}]");
    }

    @Test
    public void createLevelOnReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot create level in released or archived training definition");
    }

    @Test
    public void createLevelOnArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", tD.getId().toString(),
                "Cannot create level in released or archived training definition");
    }

    @Test
    public void createLevelOnNonExistingDefinition() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", 100L, cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Training definition not found.");
    }

    @Test
    public void switchStateOfDefinition_UNRELEASED_to_RELEASED() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        assertEquals(TDState.UNRELEASED.name(), unreleasedDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", unreleasedDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.RELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.RELEASED.name(), unreleasedDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_RELEASED_to_ARCHIVED() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.ARCHIVED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.ARCHIVED.name(), releasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_RELEASED_to_UNRELEASED_allowed() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.UNRELEASED.name(), releasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_RELEASED_to_UNRELEASED_notAllowed() throws Exception {
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
    public void switchStateOfDefinition_ARCHIVED_to_UNRELEASED() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);

        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", archivedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot switch from " + TDState.ARCHIVED.name() + " to " + TDState.UNRELEASED.name());
    }

    @Test
    public void switchStateOfDefinition_UNRELEASED_to_UNRELEASED() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);

        assertEquals(TDState.UNRELEASED.name(), unreleasedDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", unreleasedDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.UNRELEASED.name(), unreleasedDefinition.getState().name());
    }


    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private static String convertJsonBytesToString(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
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

    private UserRef createUserRef(String login, String fullName, String givenName, String familyName, String iss, Long userRefId) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userRefId);
        return userRef;
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

