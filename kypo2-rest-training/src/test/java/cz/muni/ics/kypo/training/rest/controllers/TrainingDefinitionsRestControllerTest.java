package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class,
        BetaTestingGroupMapperImpl.class})
public class TrainingDefinitionsRestControllerTest {

    private TrainingDefinitionsRestController trainingDefinitionsRestController;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingDefinitionMapper trainingDefinitionMapper;

    @Mock
    private TrainingDefinitionFacade trainingDefinitionFacade;

    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;

    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    private TrainingDefinitionByIdDTO trainingDefinition1DTO, trainingDefinition2DTO;
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO;

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
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();

        gameLevel = testDataFactory.getPenalizedLevel();
        gameLevel.setId(1L);

        gameLevelUpdateDTO = testDataFactory.getGameLevelUpdateDTO();
        gameLevelUpdateDTO.setId(2L);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(2L);

        infoLevelUpdateDTO = testDataFactory.getInfoLevelUpdateDTO();
        infoLevelUpdateDTO.setId(3L);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(3L);

        assessmentLevelUpdateDTO = testDataFactory.getAssessmentLevelUpdateDTO();
        assessmentLevelUpdateDTO.setId(9L);

        UserRef authorRef = new UserRef();
        authorRef.setUserRefId(1L);

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

        trainingDefinition1 = testDataFactory.getUnreleasedDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setAuthors(authorRefSet);

        trainingDefinition2 = testDataFactory.getArchivedDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setAuthors(authorRefSet);

        trainingDefinition1DTO = testDataFactory.getTrainingDefinitionByIdDTO();
        trainingDefinition1DTO.setId(1L);

        trainingDefinition2DTO = testDataFactory.getTrainingDefinitionByIdDTO();
        trainingDefinition2DTO.setId(2L);
        trainingDefinition2DTO.setTitle("TDbyId2");

        trainingDefinitionUpdateDTO = testDataFactory.getTrainingDefinitionUpdateDTO();
        trainingDefinitionUpdateDTO.setId(4L);

        trainingDefinitionCreateDTO = testDataFactory.getTrainingDefinitionCreateDTO();

        abstractLevelDTO = testDataFactory.getAbstractLevelDTO();

        abstractLevelDTO.setId(1L);

        basicLevelInfoDTO = testDataFactory.getBasicLevelInfoDTO();
        basicLevelInfoDTO.setId(1L);
        basicLevelInfoDTO.setOrder(1);

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
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).findById(any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(get("/training-definitions" + "/{id}", 6L))
                        .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(trainingDefinitionFacade).should().findById(6L);
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

    @Test
    public void updateTrainingDefinition() throws Exception {
        mockMvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
    }

    @Test
    public void updateTrainingDefinitionWithFacadeException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).update(any(TrainingDefinitionUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
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
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).clone(any(Long.class), anyString());
        MockHttpServletResponse response = mockMvc.perform(post("/training-definitions/1").param("title", "title").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition1.getId())).andExpect(status().isOk());
    }

    @Test
    public void deleteTrainingDefinitionWithCannotBeDeletedException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).delete(any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void deleteTrainingDefinitionWithFacadeLayerException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).delete(any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(trainingDefinitionFacade).should().delete(trainingDefinition2.getId());
    }

    @Test
    public void deleteLevel() throws Exception {
        mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition2.getId(), gameLevel.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void deleteLevelWithFacadeLayerException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition2.getId(), gameLevel.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void updateGameLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateGameLevelWithFacadeLayerException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void updateGameLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void updateInfoLevel() throws Exception {

        mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(infoLevel)).contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
    }

    @Test
    public void updateInfoLevelWithFacadeLayerException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void updateInfoLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
                        .content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
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
        UserRefDTO user1 = new UserRefDTO();
        user1.setUserRefLogin("peter@mail.muni.cz");
        user1.setUserRefFullName("Peter Parker");
        UserRefDTO user2 = new UserRefDTO();
        user2.setUserRefLogin("dave@mail.muni.cz");
        user2.setUserRefFullName("David Holman");
        List<UserRefDTO> designers = Arrays.asList(user1, user2);
        String value = convertObjectToJsonBytes(designers);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(value);
        given(trainingDefinitionFacade.getUsersWithGivenRole(any(RoleType.class), any(Pageable.class), eq(null), eq(null))).willReturn(new PageResultResource<>(designers));
        MockHttpServletResponse result = mockMvc
                .perform(get("/training-definitions/designers")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(value), result.getContentAsString());
    }

    @Test
    public void getDesignersWithUnexpectedError() throws Exception {
        willThrow(new InternalServerErrorException("Error while getting users from user and group microservice."))
                .given(trainingDefinitionFacade).getUsersWithGivenRole(any(RoleType.class), any(Pageable.class), eq(null), eq(null));
        MockHttpServletResponse response = mockMvc.perform(get("/training-definitions/designers")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isInternalServerError())
                        .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
        assertEquals("Error while getting users from user and group microservice.", error.getMessage());
    }

    @Test
    public void switchState() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/states/{state}", trainingDefinition1.getId(),
						cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
            .andExpect(status().isNoContent());
    }

    @Test
    public void switchStateWithConflict() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).switchState(any(Long.class), any(cz.muni.ics.kypo.training.api.enums.TDState.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/states/{state}", trainingDefinition2.getId(),
								cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }
    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
