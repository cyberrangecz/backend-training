package cz.muni.ics.kypo.training.rest.controllers;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.mapping.mapstruct.AssessmentLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.GameLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.InfoLevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Boris Jadus(445343)
 * @author Dominik Pilar
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingDefinitionsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingDefinitionsIT {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private MockMvc mvc;
    private static final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrainingDefinitionsRestController.class);

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
    private RestTemplate restTemplate;
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
    private UserInfoDTO userInfoDTO;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

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

        userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFullName("Ing. Mgr. MuDr. Boris Jadus");
        userInfoDTO.setLogin("445469@muni.cz");
        userInfoDTO.setGivenName("Boris");
        userInfoDTO.setFamilyName("Jadus");
        userInfoDTO.setIss("https://oidc.muni.cz");
        userInfoDTO.setUserRefId(1L);

        gameLevel1 = new GameLevel();
        gameLevel1.setTitle("testTitle");
        gameLevel1.setContent("testContent");
        gameLevel1.setFlag("testFlag");
        gameLevel1.setSolution("testSolution");
        gameLevel1.setSolutionPenalized(true);
        gameLevel1.setMaxScore(25);

        gameLevelUpdateDTO = new GameLevelUpdateDTO();
        gameLevelUpdateDTO.setTitle("newTitle");
        gameLevelUpdateDTO.setContent("newContent");
        gameLevelUpdateDTO.setFlag("newFlag");
        gameLevelUpdateDTO.setSolution("newSolution");
        gameLevelUpdateDTO.setSolutionPenalized(false);
        gameLevelUpdateDTO.setMaxScore(50);

        invalidGameLevelUpdateDTO = new GameLevelUpdateDTO();

        gameLevel2 = new GameLevel();
        gameLevel2.setTitle("testTitle2");
        gameLevel2.setContent("testContent2");
        gameLevel2.setFlag("testFlag2");
        gameLevel2.setSolution("testSolution2");
        gameLevel2.setSolutionPenalized(false);
        gameLevel2.setMaxScore(50);

        infoLevel1 = new InfoLevel();
        infoLevel1.setTitle("info1");
        infoLevel1.setContent("testContent");
        infoLevel1.setMaxScore(0);

        infoLevelUpdateDTO = new InfoLevelUpdateDTO();
        infoLevelUpdateDTO.setTitle("newTitle");
        infoLevelUpdateDTO.setContent("newContent");

        invalidInfoLevelUpdateDTO = new InfoLevelUpdateDTO();

        assessmentLevel1 = new AssessmentLevel();
        assessmentLevel1.setTitle("assessment1");
        assessmentLevel1.setAssessmentType(cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType.QUESTIONNAIRE);
        assessmentLevel1.setInstructions("testInstructions");
        assessmentLevel1.setQuestions("[]");
        assessmentLevel1.setMaxScore(20);

        assessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();
        assessmentLevelUpdateDTO.setTitle("newTitle");
        assessmentLevelUpdateDTO.setType(AssessmentType.TEST);
        assessmentLevelUpdateDTO.setInstructions("newInstructions");
        assessmentLevelUpdateDTO.setQuestions("[]");
        assessmentLevelUpdateDTO.setMaxScore(50);

        invalidAssessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();

        trainingDefinitionCreateDTO = new TrainingDefinitionCreateDTO();
        trainingDefinitionCreateDTO.setTitle("testTitle");
        trainingDefinitionCreateDTO.setDescription("testDescription");
        trainingDefinitionCreateDTO.setShowStepperBar(true);
        trainingDefinitionCreateDTO.setState(TDState.UNRELEASED);
        trainingDefinitionCreateDTO.setSandboxDefinitionRefId(1L);
        trainingDefinitionCreateDTO.setBetaTestingGroup(betaTestingGroupCreateDTO);
        trainingDefinitionCreateDTO.setAuthorsRefIds(new HashSet<>(Arrays.asList(author1.getUserRefId())));

        releasedTrainingDefinition = new TrainingDefinition();
        releasedTrainingDefinition.setTitle("released");
        releasedTrainingDefinition.setDescription("released");
        releasedTrainingDefinition.setShowStepperBar(true);
        releasedTrainingDefinition.setState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.RELEASED);
        releasedTrainingDefinition.setSandboxDefinitionRefId(2L);
        releasedTrainingDefinition.setBetaTestingGroup(betaTestingGroup);
        releasedTrainingDefinition.setAuthors(new HashSet<>(Arrays.asList(author1)));
        releasedTrainingDefinition.setLastEdited(LocalDateTime.now());

        invalidDefinitionDTO = new TrainingDefinitionByIdDTO();

        unreleasedDefinition = new TrainingDefinition();
        unreleasedDefinition.setTitle("testTitle");
        unreleasedDefinition.setDescription("testDescription");
        unreleasedDefinition.setShowStepperBar(false);
        unreleasedDefinition.setState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED);
        unreleasedDefinition.setSandboxDefinitionRefId(1L);
        unreleasedDefinition.setBetaTestingGroup(betaTestingGroup2);
        unreleasedDefinition.setAuthors(new HashSet<>(Arrays.asList(author1)));
        unreleasedDefinition.setLastEdited(LocalDateTime.now());

        archivedTrainingDefinition = new TrainingDefinition();
        archivedTrainingDefinition.setTitle("testArchivedDefinition");
        archivedTrainingDefinition.setDescription("archivedDefinitionDescription");
        archivedTrainingDefinition.setShowStepperBar(true);
        archivedTrainingDefinition.setState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.ARCHIVED);
        archivedTrainingDefinition.setSandboxDefinitionRefId(2L);
        archivedTrainingDefinition.setAuthors(Set.of(author1));
        archivedTrainingDefinition.setLastEdited(LocalDateTime.now());

        trainingDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();
        trainingDefinitionUpdateDTO.setTitle("newTitle");
        trainingDefinitionUpdateDTO.setDescription("newDescription");
        trainingDefinitionUpdateDTO.setShowStepperBar(true);
        trainingDefinitionUpdateDTO.setState(TDState.UNRELEASED);
        trainingDefinitionUpdateDTO.setSandboxDefinitionRefId(1L);
        trainingDefinitionUpdateDTO.setAuthorsRefIds(new HashSet<>(Arrays.asList(author1.getUserRefId())));
        trainingDefinitionUpdateDTO.setBetaTestingGroup(betaTestingGroupUpdateDTO);

        invalidDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();

        updateForNonexistingDefinition = new TrainingDefinitionUpdateDTO();
        updateForNonexistingDefinition.setId(100L);
        updateForNonexistingDefinition.setTitle("test");
        updateForNonexistingDefinition.setState(TDState.UNRELEASED);
        updateForNonexistingDefinition.setSandboxDefinitionRefId(1L);
        updateForNonexistingDefinition.setBetaTestingGroup(betaTestingGroupUpdateDTO);
        updateForNonexistingDefinition.setAuthorsRefIds(new HashSet<>(Arrays.asList(author1.getUserRefId())));
        updateForNonexistingDefinition.setShowStepperBar(true);

        trainingInstance = new TrainingInstance();
        trainingInstance.setPoolSize(5);
        trainingInstance.setOrganizers(Set.of(author1));
        trainingInstance.setAccessToken("pref-7895");
        trainingInstance.setTitle("Port scanning");
        trainingInstance.setStartTime(LocalDateTime.now().minusHours(2));
        trainingInstance.setEndTime(LocalDateTime.now().plusHours(2));

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
        assertTwoJsons(convertObjectToJsonBytes(definitionDTO), convertJsonBytesToString(result.getContentAsString()));
    }

    @Test
    public void findTrainingDefinitionByIdWithDefinitionNotFound() throws Exception {
        Exception ex = mvc.perform(get("/training-definitions" + "/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
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
    public void findAllTrainingDefinitionsForOrganizersOrganizerRole() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedDefinition);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions/for-organizers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {
        });

        TrainingDefinitionInfoDTO releasedTrainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        releasedTrainingDefinitionInfoDTO.setId(releasedTrainingDefinition.getId());
        releasedTrainingDefinitionInfoDTO.setState(TDState.RELEASED);
        releasedTrainingDefinitionInfoDTO.setTitle(releasedTrainingDefinition.getTitle());

        assertTrue(trainingDefinitionsPage.getContent().contains(releasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizersOrganizerAndDesignerRole() throws Exception {
        unreleasedDefinition.addAuthor(organizer1);
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedDefinition);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name(), RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions/for-organizers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {
        });

        TrainingDefinitionInfoDTO releasedTrainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        releasedTrainingDefinitionInfoDTO.setId(releasedTrainingDefinition.getId());
        releasedTrainingDefinitionInfoDTO.setState(TDState.RELEASED);
        releasedTrainingDefinitionInfoDTO.setTitle(releasedTrainingDefinition.getTitle());

        TrainingDefinitionInfoDTO unreleasedTrainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        unreleasedTrainingDefinitionInfoDTO.setId(unreleasedDefinition.getId());
        unreleasedTrainingDefinitionInfoDTO.setState(TDState.UNRELEASED);
        unreleasedTrainingDefinitionInfoDTO.setTitle(unreleasedDefinition.getTitle());
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(unreleasedTrainingDefinitionInfoDTO, releasedTrainingDefinitionInfoDTO)));
    }

    @Test
    public void findAllTrainingDefinitionsBySandboxDefinitionId() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name()));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions/sandbox-definitions/" + releasedTrainingDefinition.getSandboxDefinitionRefId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {
        });

        TrainingDefinitionInfoDTO releasedTrainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        releasedTrainingDefinitionInfoDTO.setId(releasedTrainingDefinition.getId());
        releasedTrainingDefinitionInfoDTO.setState(TDState.RELEASED);
        releasedTrainingDefinitionInfoDTO.setTitle(releasedTrainingDefinition.getTitle());

        TrainingDefinitionInfoDTO archivedTrainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        archivedTrainingDefinitionInfoDTO.setId(archivedTrainingDefinition.getId());
        archivedTrainingDefinitionInfoDTO.setState(TDState.ARCHIVED);
        archivedTrainingDefinitionInfoDTO.setTitle(archivedTrainingDefinition.getTitle());
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(archivedTrainingDefinitionInfoDTO, releasedTrainingDefinitionInfoDTO)));
    }

    @Test
    public void createTrainingDefinition() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));

        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserInfoDTO>(userInfoDTO, HttpStatus.OK));

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
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserInfoDTO>(userInfoDTO, HttpStatus.OK));
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
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<UserInfoDTO>(userInfoDTO, HttpStatus.OK));
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
        assertEquals(updatedDefinition.getSandboxDefinitionRefId(), unreleasedDefinition.getSandboxDefinitionRefId());
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
        Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(updateForNonexistingDefinition))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
    }

    @Test
    public void updateTrainingDefinitionWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionUpdateDTO.setId(tD.getId());
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));

        Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training definition"));
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
        Exception ex = mvc.perform(post("/training-definitions" + "/{id}", 100L).param("title", "title"))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
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

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedDefinition.getId(), 50, infoLevel1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("Level not found."));
    }

    @Test
    public void swapLevelsSecondLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        infoLevel1.setTrainingDefinition(unreleasedDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedDefinition.getId(), infoLevel1.getId(), 50))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("Level not found."));
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
        Exception ex = mvc.perform(delete("/training-definitions/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
    }

    @Test
    public void deleteReleasedTrainingDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        Exception ex = mvc.perform(delete("/training-definitions/{Id}", tD.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot delete released training definition"));
    }

    @Test
    public void deleteDefinitionWithTrainingInstances() throws Exception {
        trainingDefinitionRepository.save(unreleasedDefinition);
        trainingInstance.setTrainingDefinition(unreleasedDefinition);
        trainingInstanceRepository.save(trainingInstance);

        Exception ex = mvc.perform(delete("/training-definitions/{id}", unreleasedDefinition.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot delete training definition with already created training instance. Remove training instance/s before deleting training definition."));
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
        Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Level not found"));
    }

    @Test
    public void deleteOneLevelWithNonExistentDefinition() throws Exception {
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", 100L, gL.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
    }

    @Test
    public void deleteOneLevelWithReleasedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training definition"));
    }

    @Test
    public void deleteOneLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training definition"));
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

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
    }

    @Test
    public void updateGameLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        GameLevel gL = gameLevelRepository.save(gameLevel1);
        gameLevelUpdateDTO.setId(gL.getId());

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
    }

    @Test
    public void updateGameLevelWithNonexistentDefinition() throws Exception {
        gameLevelUpdateDTO.setId(1L);
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
    }

    @Test
    public void updateGameLevelWithNonExistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        gameLevelUpdateDTO.setId(100L);
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Level was not found"));
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

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
    }

    @Test
    public void updateInfoLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        InfoLevel iL = infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(iL.getId());

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
    }

    @Test
    public void updateInfoLevelWithNonExistentDefinition() throws Exception {
        infoLevelUpdateDTO.setId(1L);
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
    }

    @Test
    public void updateInfoLevelWithNonExistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        infoLevelUpdateDTO.setId(100L);
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Level was not found"));
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

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
    }

    @Test
    public void updateAssessmentLevelWithArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
        assessmentLevelUpdateDTO.setId(aL.getId());

        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
    }

    @Test
    public void updateAssessmentLevelWithNonExistentDefinition() throws Exception {
        assessmentLevelUpdateDTO.setId(1L);
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
    }

    @Test
    public void updateAssessmentLevelWithNonExistentLevel() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
        assessmentLevelUpdateDTO.setId(100L);
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Level was not found"));
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
        Exception ex = mvc.perform(get("/training-definitions/levels/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Level with id: 100, not found"));
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
        Exception ex = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot create level in released or archived training definition"));
    }

    @Test
    public void createLevelOnArchivedDefinition() throws Exception {
        TrainingDefinition tD = trainingDefinitionRepository.save(archivedTrainingDefinition);
        Exception ex = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ConflictException.class);
        assertTrue(ex.getMessage().contains("Cannot create level in released or archived training definition"));
    }

    @Test
    public void createLevelOnNonExistingDefinition() throws Exception {
        Exception ex = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", 100L, cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
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
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertTrue(ex.getMessage().contains("Cannot update training definition with already created training instance(s). Remove training " +
                "instance(s) before changing the state from released to unreleased training definition."));
    }

    @Test
    public void switchStateOfDefinition_ARCHIVED_to_UNRELEASED() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);

        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", archivedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
        assertEquals("Cannot switch from " + TDState.ARCHIVED.name() + " to " + TDState.UNRELEASED.name(), ex.getCause().getCause().getMessage());
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
        userRef.setUserRefLogin(login);
        userRef.setUserRefFullName(fullName);
        userRef.setUserRefGivenName(givenName);
        userRef.setUserRefFamilyName(familyName);
        userRef.setIss(iss);
        userRef.setUserRefId(userRefId);
        return userRef;
    }

}

