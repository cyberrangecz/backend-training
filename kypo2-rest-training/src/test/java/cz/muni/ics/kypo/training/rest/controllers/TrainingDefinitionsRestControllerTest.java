package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.*;
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

import java.util.*;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@SpringBootTest(classes = {TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class TrainingDefinitionsRestControllerTest {

    private TrainingDefinitionsRestController trainingDefinitionsRestController;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingDefinitionMapper trainingDefinitionMapper;

    @Mock
    private TrainingDefinitionFacade trainingDefinitionFacade;

    private MockMvc mockMvc;

    private TrainingDefinition trainingDefinition1, trainingDefinition2;
    private TrainingDefinitionByIdDTO trainingDefinitionDTO1, trainingDefinitionDTO2;
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO;

    private GameLevel gameLevel;
    private GameLevelUpdateDTO gameLevelUpdateDTO;

    private InfoLevel infoLevel;
    private InfoLevelUpdateDTO infoLevelUpdateDTO;

    private AssessmentLevel assessmentLevel;
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO;

    private AbstractLevelDTO abstractLevelDTO;
    private BasicLevelInfoDTO basicGameLevelInfoDTO, basicInfoLevelInfoDTO;

    private UserRefDTO designerDTO1, designerDTO2, organizerDTO;

    private List<BasicLevelInfoDTO> basicLevelInfoDTOS;
    private PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionInfoDTOPageResultResource;
    private PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource;

    private Page page;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionsRestController = new TrainingDefinitionsRestController(trainingDefinitionFacade, new ObjectMapper());
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

        designerDTO1 = testDataFactory.getUserRefDTO1();
        designerDTO2 = testDataFactory.getUserRefDTO2();
        organizerDTO = testDataFactory.getUserRefDTO3();

        trainingDefinitionDTO1 = testDataFactory.getTrainingDefinitionByIdDTO();
        trainingDefinitionDTO1.setId(1L);

        trainingDefinitionDTO2 = testDataFactory.getTrainingDefinitionByIdDTO();
        trainingDefinitionDTO2.setId(2L);
        trainingDefinitionDTO2.setTitle("TDbyId2");

        trainingDefinitionUpdateDTO = testDataFactory.getTrainingDefinitionUpdateDTO();
        trainingDefinitionUpdateDTO.setId(4L);

        trainingDefinitionCreateDTO = testDataFactory.getTrainingDefinitionCreateDTO();

        abstractLevelDTO = testDataFactory.getAbstractLevelDTO();

        abstractLevelDTO.setId(1L);

        basicGameLevelInfoDTO = testDataFactory.getBasicGameLevelInfoDTO();
        basicGameLevelInfoDTO.setId(1L);
        basicGameLevelInfoDTO.setOrder(1);

        basicInfoLevelInfoDTO = testDataFactory.getBasicGameLevelInfoDTO();
        basicInfoLevelInfoDTO.setId(2L);
        basicInfoLevelInfoDTO.setOrder(2);

        trainingDefinition1 = testDataFactory.getArchivedDefinition();
        trainingDefinition2 = testDataFactory.getReleasedDefinition();

        basicLevelInfoDTOS = List.of(basicGameLevelInfoDTO, basicInfoLevelInfoDTO);
        page = new PageImpl<>(List.of(trainingDefinition1, trainingDefinition2));

        trainingDefinitionInfoDTOPageResultResource = trainingDefinitionMapper.mapToPageResultResourceInfoDTO(page);
        trainingDefinitionDTOPageResultResource = trainingDefinitionMapper.mapToPageResultResource(page);
    }

    @Test
    public void findTrainingDefinitionById() throws Exception {
        given(trainingDefinitionFacade.findById(any(Long.class))).willReturn(trainingDefinitionDTO1);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions" + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(trainingDefinitionDTO1, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void findTrainingDefinitionById_FacadeException() throws Exception {
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
        given(trainingDefinitionFacade.findAll(any(Predicate.class), any(Pageable.class))).willReturn(trainingDefinitionDTOPageResultResource);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(trainingDefinitionDTOPageResultResource.getContent(), convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()),
                new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {}).getContent());
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers() throws Exception {
        given(trainingDefinitionFacade.findAllForOrganizers(eq(TDState.RELEASED), any(Pageable.class))).willReturn(trainingDefinitionInfoDTOPageResultResource);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/for-organizers")
                .queryParam("state", TDState.RELEASED.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(trainingDefinitionInfoDTOPageResultResource.getContent(), convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()),
                new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {}).getContent());
    }

    @Test
    public void createTrainingDefinition() throws Exception {
        given(trainingDefinitionFacade.create(any(TrainingDefinitionCreateDTO.class))).willReturn(trainingDefinitionDTO1);
        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(trainingDefinitionDTO1, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
        then(trainingDefinitionFacade).should().create(any(TrainingDefinitionCreateDTO.class));
    }

    @Test
    public void createTrainingDefinition_FacadeException() throws Exception {
        willThrow(new UnprocessableEntityException()).given(trainingDefinitionFacade).create(any(TrainingDefinitionCreateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(post("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatus());
    }

    @Test
    public void updateTrainingDefinition() throws Exception {
        mockMvc.perform(put("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        then(trainingDefinitionFacade).should().update(any(TrainingDefinitionUpdateDTO.class));
    }

    @Test
    public void updateTrainingDefinition_FacadeException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).update(any(TrainingDefinitionUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void cloneTrainingDefinition() throws Exception {
        given(trainingDefinitionFacade.clone(any(Long.class), anyString())).willReturn(trainingDefinitionDTO1);
        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions/{id}", trainingDefinitionDTO1.getId())
                .param("title", "title")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertEquals(trainingDefinitionDTO1, convertJsonBytesToObject(result.getContentAsString(), TrainingDefinitionByIdDTO.class));
    }


    @Test
    public void cloneTrainingDefinition_FacadeException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).clone(any(Long.class), anyString());
        MockHttpServletResponse response = mockMvc.perform(post("/training-definitions/1")
                .param("title", "title")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void swapLevels() throws Exception {
        given(trainingDefinitionFacade.swapLevels(trainingDefinitionDTO1.getId(), gameLevel.getId(), infoLevel.getId())).willReturn(basicLevelInfoDTOS);
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}", trainingDefinitionDTO1.getId(), gameLevel.getId(), infoLevel.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(basicLevelInfoDTOS, convertJsonBytesToObject(response.getContentAsString(), new TypeReference<List<BasicLevelInfoDTO>>(){}));
        then(trainingDefinitionFacade).should().swapLevels(trainingDefinitionDTO1.getId(), gameLevel.getId(), infoLevel.getId());
    }

    @Test
    public void swapLevels_FacadeException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).swapLevels(trainingDefinitionDTO1.getId(), gameLevel.getId(), infoLevel.getId());
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}", trainingDefinitionDTO1.getId(), gameLevel.getId(), infoLevel.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void moveLevel() throws Exception {
        given(trainingDefinitionFacade.moveLevel(trainingDefinitionDTO1.getId(), gameLevel.getId(), gameLevel.getOrder() + 2)).willReturn(basicLevelInfoDTOS);
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdToBeMoved}/move-to/{newPosition}", trainingDefinitionDTO1.getId(), gameLevel.getId(), gameLevel.getOrder() + 2)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(basicLevelInfoDTOS, convertJsonBytesToObject(response.getContentAsString(), new TypeReference<List<BasicLevelInfoDTO>>(){}));
        then(trainingDefinitionFacade).should().moveLevel(trainingDefinitionDTO1.getId(), gameLevel.getId(), gameLevel.getOrder() + 2);
    }

    @Test
    public void moveLevel_FacadeException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).moveLevel(trainingDefinitionDTO1.getId(), gameLevel.getId(), gameLevel.getOrder() + 2);
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdToBeMoved}/move-to/{newPosition}", trainingDefinitionDTO1.getId(), gameLevel.getId(), gameLevel.getOrder() + 2)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        mockMvc.perform(delete("/training-definitions/{id}", trainingDefinitionDTO1.getId()))
                .andExpect(status().isOk());
        then(trainingDefinitionFacade).should().delete(trainingDefinitionDTO1.getId());
    }

    @Test
    public void deleteTrainingDefinition_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).delete(any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinitionDTO2.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(trainingDefinitionFacade).should().delete(trainingDefinitionDTO2.getId());
    }

    @Test
    public void deleteLevel() throws Exception {
        mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinitionDTO1.getId(), gameLevel.getId()))
                .andExpect(status().isOk());
        then(trainingDefinitionFacade).should().deleteOneLevel(trainingDefinitionDTO1.getId(), gameLevel.getId());
    }

    @Test
    public void deleteLevel_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinitionDTO2.getId(), gameLevel.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void updateGameLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinitionDTO1.getId())
                .content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        then(trainingDefinitionFacade).should().updateGameLevel(eq(trainingDefinitionDTO1.getId()), any(GameLevelUpdateDTO.class));
    }

    @Test
    public void updateGameLevel_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinitionDTO2.getId())
                .content(convertObjectToJsonBytes(gameLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void updateInfoLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinitionDTO1.getId())
                .content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        then(trainingDefinitionFacade).should().updateInfoLevel(eq(trainingDefinitionDTO1.getId()), any(InfoLevelUpdateDTO.class));
    }

    @Test
    public void updateInfoLevel_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinitionDTO2.getId())
                .content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void updateAssessmentLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/assessment-levels", trainingDefinitionDTO1.getId())
                .content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        then(trainingDefinitionFacade).should().updateAssessmentLevel(eq(trainingDefinitionDTO1.getId()), any(AssessmentLevelUpdateDTO.class));
    }

    @Test
    public void updateAssessmentLevel_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinitionDTO2.getId())
                .content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void findLevelById() throws Exception {
        given(trainingDefinitionFacade.findLevelById(any(Long.class))).willReturn(abstractLevelDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/levels" + "/{levelId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(abstractLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), AbstractLevelDTO.class));
    }

    @Test
    public void findLevelById_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).findLevelById(gameLevel.getId());
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/levels" + "/{levelId}", gameLevel.getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void createLevel() throws Exception {
        given(trainingDefinitionFacade.createGameLevel(any(Long.class))).willReturn(basicGameLevelInfoDTO);
        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinitionDTO1.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.GAME)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        then(trainingDefinitionFacade).should(never()).createAssessmentLevel(trainingDefinitionDTO1.getId());
        then(trainingDefinitionFacade).should(never()).createInfoLevel(trainingDefinitionDTO1.getId());
        assertEquals(basicGameLevelInfoDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), BasicLevelInfoDTO.class));
    }

    @Test
    public void createLevel_FacadeException() throws Exception {
        willThrow(new UnprocessableEntityException()).given(trainingDefinitionFacade).createGameLevel(trainingDefinitionDTO1.getId());
        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinitionDTO1.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.GAME)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatus());
        assertEquals("The requested data cannot be processed.", error.getMessage());
    }


    @Test
    public void getDesigners() throws Exception {
        List<UserRefDTO> designers = List.of(designerDTO1, designerDTO2);
        given(trainingDefinitionFacade.getUsersWithGivenRole(eq(RoleType.ROLE_TRAINING_DESIGNER), any(Pageable.class), eq(null), eq(null))).willReturn(new PageResultResource<>(designers));
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/designers")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(designers, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), new TypeReference<PageResultResource<UserRefDTO>>() {}).getContent());
    }

    @Test
    public void getDesigners_MicroserviceError() throws Exception {
        willThrow(new MicroserviceApiException("Error while getting users from user and group microservice."))
                .given(trainingDefinitionFacade).getUsersWithGivenRole(eq(RoleType.ROLE_TRAINING_DESIGNER), any(Pageable.class), eq(null), eq(null));
        MockHttpServletResponse response = mockMvc.perform(get("/training-definitions/designers")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isForbidden())
                        .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals("Error while getting users from user and group microservice.", error.getMessage());
    }

    @Test
    public void getOrganizers() throws Exception {
        List<UserRefDTO> organizers = List.of(organizerDTO, designerDTO2);
        given(trainingDefinitionFacade.getUsersWithGivenRole(eq(RoleType.ROLE_TRAINING_ORGANIZER), any(Pageable.class), eq(null), eq(null))).willReturn(new PageResultResource<>(organizers));
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/organizers")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(organizers, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), new TypeReference<PageResultResource<UserRefDTO>>() {}).getContent());
    }

    @Test
    public void getOrganizers_MicroserviceError() throws Exception {
        willThrow(new MicroserviceApiException("Error while getting users from user and group microservice."))
                .given(trainingDefinitionFacade).getUsersWithGivenRole(eq(RoleType.ROLE_TRAINING_ORGANIZER), any(Pageable.class), eq(null), eq(null));
        MockHttpServletResponse response = mockMvc.perform(get("/training-definitions/organizers")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals("Error while getting users from user and group microservice.", error.getMessage());
    }

    @Test
    public void getAuthors() throws Exception {
        List<UserRefDTO> authors = List.of(organizerDTO, designerDTO1);
        given(trainingDefinitionFacade.getAuthors(eq(trainingDefinitionDTO1.getId()), any(Pageable.class), eq(null), eq(null))).willReturn(new PageResultResource<>(authors));
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(authors, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), new TypeReference<PageResultResource<UserRefDTO>>() {}).getContent());
    }

    @Test
    public void getAuthors_FacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).getAuthors(eq(trainingDefinitionDTO1.getId()), any(Pageable.class), eq(null), eq(null));
        MockHttpServletResponse response = mockMvc.perform(get("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    public void editAuthors() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                .queryParam("authorsAddition", List.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId()).toString()
                        .replace("[", "")
                        .replace("]", ""))
                .queryParam("authorsRemoval", List.of(organizerDTO.getUserRefId()).toString()
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
        then(trainingDefinitionFacade).should().editAuthors(trainingDefinitionDTO1.getId(), Set.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId()), Set.of(organizerDTO.getUserRefId()));
    }

    @Test
    public void editAuthors_FacadeException() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).editAuthors(trainingDefinitionDTO1.getId(), Set.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId()), Set.of(organizerDTO.getUserRefId()));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                .queryParam("authorsAddition", List.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId()).toString()
                        .replace("[", "")
                        .replace("]", ""))
                .queryParam("authorsRemoval", List.of(organizerDTO.getUserRefId()).toString()
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    public void switchState() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/states/{state}", trainingDefinitionDTO1.getId(),
						cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
            .andExpect(status().isNoContent());
        then(trainingDefinitionFacade).should().switchState(trainingDefinitionDTO1.getId(), TDState.ARCHIVED);
    }

    @Test
    public void switchStateWithConflict() throws Exception {
        willThrow(new EntityConflictException()).given(trainingDefinitionFacade).switchState(any(Long.class), any(cz.muni.ics.kypo.training.api.enums.TDState.class));
        MockHttpServletResponse response = mockMvc.perform(put("/training-definitions/{definitionId}/states/{state}", trainingDefinitionDTO2.getId(),
								cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }
}
