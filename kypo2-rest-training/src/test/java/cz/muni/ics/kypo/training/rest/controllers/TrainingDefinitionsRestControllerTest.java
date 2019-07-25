package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.snapshothook.SnapshotHookDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.ApiParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * @author Boris Jadus(445343)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class,
        BetaTestingGroupMapperImpl.class})
public class TrainingDefinitionsRestControllerTest {

    private TrainingDefinitionsRestController trainingDefinitionsRestController;

    @Autowired
    TrainingDefinitionMapper trainingDefinitionMapper;

    @Mock
    private TrainingDefinitionFacade trainingDefinitionFacade;

    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;

    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    private TrainingDefinitionByIdDTO trainingDefinition1DTO, trainingDefinition2DTO;
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO;

    private BetaTestingGroupCreateDTO betaTestingGroupCreateDTO;
    private BetaTestingGroupUpdateDTO betaTestingGroupUpdateDTO;

    private GameLevel gameLevel;
    private GameLevelUpdateDTO gameLevelUpdateDTO;

    private InfoLevel infoLevel;
    private InfoLevelUpdateDTO infoLevelUpdateDTO;

    private AssessmentLevel assessmentLevel;
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO;

    private AbstractLevelDTO abstractLevelDTO;

    private BasicLevelInfoDTO basicLevelInfoDTO;

    private Page p;

    private PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource;

    @ApiParam(value = "Pagination support.", required = false)
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionsRestController = new TrainingDefinitionsRestController(trainingDefinitionFacade, objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        gameLevel = new GameLevel();
        gameLevel.setId(1L);
        gameLevel.setTitle("GameTest");
        gameLevel.setContent("content");
        gameLevel.setSolution("solution");
        gameLevel.setFlag("FlagTest");
        gameLevel.setIncorrectFlagLimit(5);
        gameLevel.setMaxScore(50);
        gameLevel.setEstimatedDuration(30);

        gameLevelUpdateDTO = new GameLevelUpdateDTO();
        gameLevelUpdateDTO.setId(2L);
        gameLevelUpdateDTO.setTitle("title");
        gameLevelUpdateDTO.setAttachments(new String[3]);
        gameLevelUpdateDTO.setContent("Content");
        gameLevelUpdateDTO.setEstimatedDuration(1000);
        gameLevelUpdateDTO.setFlag("flag1");
        gameLevelUpdateDTO.setIncorrectFlagLimit(4);
        gameLevelUpdateDTO.setSolutionPenalized(true);
        gameLevelUpdateDTO.setMaxScore(20);

        infoLevel = new InfoLevel();
        infoLevel.setId(2L);
        infoLevel.setTitle("InfoTest");
        infoLevel.setContent("content");

        infoLevelUpdateDTO = new InfoLevelUpdateDTO();
        infoLevelUpdateDTO.setId(3L);
        infoLevelUpdateDTO.setTitle("some title");
        infoLevelUpdateDTO.setContent("some content");

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(3L);
        assessmentLevel.setTitle("AssTest");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);
        assessmentLevel.setQuestions("questions");

        assessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();
        assessmentLevelUpdateDTO.setId(9L);
        assessmentLevelUpdateDTO.setInstructions("instructions");
        assessmentLevelUpdateDTO.setMaxScore(50);
        assessmentLevelUpdateDTO.setQuestions("test");
        assessmentLevelUpdateDTO.setTitle("Some title");
        assessmentLevelUpdateDTO.setType(cz.muni.ics.kypo.training.api.enums.AssessmentType.QUESTIONNAIRE);

        UserRef authorRef = new UserRef();
        authorRef.setUserRefLogin("Author");
        authorRef.setUserRefId(1L);
        authorRef.setIss("https://oidc.muni.cz");
        authorRef.setUserRefGivenName("Pavel");
        authorRef.setUserRefFamilyName("Seda");
        authorRef.setUserRefFullName("Mgr. Ing. Pavel Seda");


        Set<UserRef> authorRefSet = new HashSet<>();
        authorRefSet.add(authorRef);

        UserRefDTO authorRefDTO = new UserRefDTO();
        authorRefDTO.setUserRefId(2L);
        authorRefDTO.setIss("https://oidc.muni.cz");
        authorRefDTO.setUserRefGivenName("Pavel22");
        authorRefDTO.setUserRefFamilyName("Seda33");
        authorRefDTO.setUserRefFullName("Mgr. Ing. Pavel Seda");

        Set<UserRefDTO> authorRefSetDTO = new HashSet<>();
        authorRefSetDTO.add(authorRefDTO);

        betaTestingGroupCreateDTO = new BetaTestingGroupCreateDTO();
        betaTestingGroupCreateDTO.setOrganizersRefIds(Set.of());

        betaTestingGroupUpdateDTO = new BetaTestingGroupUpdateDTO();
        betaTestingGroupUpdateDTO.setOrganizersRefIds(Set.of());

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.UNRELEASED);
        trainingDefinition1.setTitle("test");
        trainingDefinition1.setAuthors(authorRefSet);
        trainingDefinition1.setSandboxDefinitionRefId(1L);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.PRIVATED);
        trainingDefinition2.setTitle("test");
        trainingDefinition2.setAuthors(authorRefSet);
        trainingDefinition2.setSandboxDefinitionRefId(1L);

        trainingDefinition1DTO = new TrainingDefinitionByIdDTO();
        trainingDefinition1DTO.setId(1L);
        trainingDefinition1DTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        trainingDefinition1DTO.setTitle("test");
        trainingDefinition1DTO.setAuthors(authorRefSetDTO);
        trainingDefinition1DTO.setSandboxDefinitionRefId(1L);

        trainingDefinition2DTO = new TrainingDefinitionByIdDTO();
        trainingDefinition2DTO.setId(2L);
        trainingDefinition2DTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.PRIVATED);
        trainingDefinition2DTO.setTitle("test");
        trainingDefinition2DTO.setAuthors(authorRefSetDTO);
        trainingDefinition2DTO.setSandboxDefinitionRefId(1L);

        trainingDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();
        trainingDefinitionUpdateDTO.setId(4L);
        trainingDefinitionUpdateDTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        trainingDefinitionUpdateDTO.setTitle("training definition title");
        trainingDefinitionUpdateDTO.setAuthorsRefIds(Set.of());
        trainingDefinitionUpdateDTO.setSandboxDefinitionRefId(1L);
        trainingDefinitionUpdateDTO.setShowStepperBar(false);
        trainingDefinitionUpdateDTO.setBetaTestingGroup(betaTestingGroupUpdateDTO);

        trainingDefinitionCreateDTO = new TrainingDefinitionCreateDTO();
        trainingDefinitionCreateDTO.setDescription("TD desc");
        trainingDefinitionCreateDTO.setOutcomes(new String[0]);
        trainingDefinitionCreateDTO.setPrerequisities(new String[0]);
        trainingDefinitionCreateDTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED);
        trainingDefinitionCreateDTO.setTitle("TD some title");
        trainingDefinitionCreateDTO.setAuthorsRefIds(Set.of());
        trainingDefinitionCreateDTO.setShowStepperBar(true);
        trainingDefinitionCreateDTO.setSandboxDefinitionRefId(1L);
        trainingDefinitionCreateDTO.setBetaTestingGroup(betaTestingGroupCreateDTO);

        abstractLevelDTO = new AbstractLevelDTO();
        abstractLevelDTO.setId(1L);
        abstractLevelDTO.setTitle("title");
        abstractLevelDTO.setMaxScore(1000);
        abstractLevelDTO.setSnapshotHook(new SnapshotHookDTO());

        basicLevelInfoDTO = new BasicLevelInfoDTO();
        basicLevelInfoDTO.setId(1L);
        basicLevelInfoDTO.setTitle("level info title");
        basicLevelInfoDTO.setOrder(1);
        basicLevelInfoDTO.setLevelType(LevelType.GAME_LEVEL);

        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        p = new PageImpl<>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        trainingDefinitionDTOPageResultResource = trainingDefinitionMapper.mapToPageResultResource(p);

    }

    @Test
    public void findTrainingDefinitionById() throws Exception {
        given(trainingDefinitionFacade.findById(any(Long.class))).willReturn(trainingDefinition1DTO);
        String valueTd = convertObjectToJsonBytes(trainingDefinition1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions" + "/{id}", 1l)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinition1DTO)), result.getContentAsString());
    }

    @Test
    public void findTrainingDefinitionByIdWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).findById(any(Long.class));
        Exception exception =
                mockMvc.perform(get("/training-definitions" + "/{id}", 6l)).andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void findAllTrainingDefinitions() throws Exception {
        String valueTi = convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
        given(trainingDefinitionFacade.findAll(any(Predicate.class), any(Pageable.class))).willReturn(trainingDefinitionDTOPageResultResource);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource)), result.getContentAsString());
    }

//    @Test
//    public void findAllTrainingDefinitionsBySandboxDefinitionId() throws Exception {
//        String valueTi = convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource);
//        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
//        given(trainingDefinitionFacade.findAllBySandboxDefinitionId(any(Long.class), any(Pageable.class))).willReturn(trainingDefinitionDTOPageResultResource);
//        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/sandbox-definitions" + "/{id}", 1L))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse();
//        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource)), result.getContentAsString());
//    }

    @Test
    public void updateTrainingDefinition() throws Exception {
        mockMvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
    }

    @Test
    public void updateTrainingDefinitionWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).update(any(TrainingDefinitionUpdateDTO.class));
        Exception exception = mockMvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void cloneTrainingDefinition() throws Exception {
        String valueTd = convertObjectToJsonBytes(trainingDefinition1DTO);
        given(objectMapper.writeValueAsString(any(Long.class))).willReturn(valueTd);
        given(trainingDefinitionFacade.clone(any(Long.class), anyString())).willReturn(trainingDefinition1DTO);
        MockHttpServletResponse result = mockMvc
                .perform(post("/training-definitions/{id}", trainingDefinition1.getId()).param("title", "title")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(trainingDefinition1DTO), result.getContentAsString());
    }


    @Test
    public void cloneTrainingDefinitionWithFacadeException() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).clone(any(Long.class), anyString());
        Exception exception = mockMvc.perform(post("/training-definitions/1").param("title", "title").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict()).andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition1.getId())).andExpect(status().isOk());
    }

    @Test
    public void deleteTrainingDefinitionWithCannotBeDeletedException() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).delete(any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
                .andExpect(status().isConflict()).andReturn().getResolvedException();

        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void deleteTrainingDefinitionWithFacadeLayerException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).delete(any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
                .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void deleteLevel() throws Exception {
        mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteLevelWithCannotBeUpdatedException() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        Exception exception =
                mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition2.getId(), gameLevel.getId()))
                        .andExpect(status().isConflict()).andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void deleteLevelWithFacadeLayerException() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        Exception exception =
                mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition2.getId(), gameLevel.getId()))
                        .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateGameLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateGameLevelWithFacadeLayerException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
        Exception exception = mockMvc
                .perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateGameLevelWithCannotBeUpdatedException() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
        Exception exception = mockMvc
                .perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict()).andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void updateInfoLevel() throws Exception {

        mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(infoLevel)).contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
    }

    @Test
    public void updateInfoLevelWithFacadeLayerException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
        Exception exception = mockMvc
                .perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateInfoLevelWithCannotBeUpdatedException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
        Exception exception = mockMvc
                .perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict()).andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void updateAssessmentLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/assessment-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(assessmentLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
    }

    @Test
    public void findLevelById() throws Exception {
        given(trainingDefinitionFacade.findLevelById(any(Long.class))).willReturn(abstractLevelDTO);
        String valueTd = convertObjectToJsonBytes(abstractLevelDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/levels" + "/{levelId}", 1L)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTd), result.getContentAsString());
    }

    @Test
    public void createLevel() throws Exception {
        given(trainingDefinitionFacade.createGameLevel(any(Long.class))).willReturn(basicLevelInfoDTO);
        String valueTd = convertObjectToJsonBytes(basicLevelInfoDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition1.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.GAME)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTd), result.getContentAsString());
    }


    @Test
    public void createTrainingDefinition() throws Exception {
        String valueTd = convertObjectToJsonBytes(trainingDefinitionCreateDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        given(trainingDefinitionFacade.create(any(TrainingDefinitionCreateDTO.class))).willReturn(trainingDefinition1DTO);
        MockHttpServletResponse result = mockMvc
                .perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    @Test
    public void getDesigners() throws Exception {
        UserInfoDTO user1 = new UserInfoDTO();
        user1.setLogin("peter@mail.muni.cz");
        user1.setFullName("Peter Parker");
        UserInfoDTO user2 = new UserInfoDTO();
        user2.setLogin("dave@mail.muni.cz");
        user2.setFullName("David Holman");
        List<UserInfoDTO> designers = Arrays.asList(user1, user2);
        String value = convertObjectToJsonBytes(designers);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(value);
        given(trainingDefinitionFacade.getUsersWithGivenRole(any(RoleType.class), any(Pageable.class))).willReturn(designers);
        MockHttpServletResponse result = mockMvc
                .perform(get("/training-definitions/designers")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(value), result.getContentAsString());
    }

    @Test
    public void getDesignersWithUnexpectedError() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("Error while getting users from user and group microservice.", ErrorCode.UNEXPECTED_ERROR);
        willThrow(new FacadeLayerException(exceptionThrow))
                .given(trainingDefinitionFacade).getUsersWithGivenRole(any(RoleType.class), any(Pageable.class));
        Exception ex = mockMvc.perform(get("/training-definitions/designers")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isInternalServerError())
                        .andReturn().getResolvedException();
        assertEquals(InternalServerErrorException.class, ex.getClass());
        assertEquals("Error while getting users from user and group microservice.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void switchState() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/states/{state}", trainingDefinition1.getId(),
						cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
            .andExpect(status().isNoContent());
    }

    @Test
    public void switchStateWithConflict() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).switchState(any(Long.class), any(cz.muni.ics.kypo.training.api.enums.TDState.class));
        Exception exception =
            mockMvc.perform(put("/training-definitions/{definitionId}/states/{state}", trainingDefinition2.getId(),
								cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }
    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
