package cz.muni.ics.kypo.training.rest.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.rest.controllers.TrainingDefinitionsRestController;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotModifiedException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingDefinitionsRestController.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo")
public class TrainingDefinitionsRestControllerTest {

    @Autowired
    private TrainingDefinitionsRestController trainingDefinitionsRestController;

    @MockBean
    private TrainingDefinitionFacade trainingDefinitionFacade;

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("objMapperRESTApi")
    private ObjectMapper objectMapper;

    @MockBean
    private BeanMapping beanMapping;

    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    private TrainingDefinitionDTO trainingDefinition1DTO, trainingDefinition2DTO;

    private GameLevel gameLevel;

    private InfoLevel infoLevel;

    private AssessmentLevel assessmentLevel;

    private Page p;

    private PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
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

        infoLevel = new InfoLevel();
        infoLevel.setId(2L);
        infoLevel.setTitle("InfoTest");
        infoLevel.setContent("content");

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(3L);
        assessmentLevel.setTitle("AssTest");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);
        assessmentLevel.setQuestions("questions");

        AuthorRef authorRef = new AuthorRef();
        Set<AuthorRef> authorRefSet = new HashSet<>();
        authorRefSet.add(authorRef);

        AuthorRefDTO authorRefDTO = new AuthorRefDTO();
        Set<AuthorRefDTO> authorRefSetDTO = new HashSet<>();
        authorRefSetDTO.add(authorRefDTO);

        SandboxDefinitionRef sandboxDefinitionRef = new SandboxDefinitionRef();
        SandboxDefinitionRefDTO sandboxDefinitionRefDTO = new SandboxDefinitionRefDTO();

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.UNRELEASED);
        trainingDefinition1.setStartingLevel(1L);
        trainingDefinition1.setTitle("test");
        trainingDefinition1.setAuthorRef(authorRefSet);
        trainingDefinition1.setSandBoxDefinitionRef(sandboxDefinitionRef);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.PRIVATED);
        trainingDefinition2.setTitle("test");
        trainingDefinition2.setAuthorRef(authorRefSet);
        trainingDefinition2.setSandBoxDefinitionRef(sandboxDefinitionRef);

        trainingDefinition1DTO = new TrainingDefinitionDTO();
        trainingDefinition1DTO.setId(1L);
        trainingDefinition1DTO.setState(TDState.UNRELEASED);
        trainingDefinition1DTO.setTitle("test");
        trainingDefinition1DTO.setAuthorRefDTO(authorRefSetDTO);
        trainingDefinition1DTO.setSandBoxDefinitionRefDTO(sandboxDefinitionRefDTO);

        trainingDefinition2DTO = new TrainingDefinitionDTO();
        trainingDefinition2DTO.setId(2L);
        trainingDefinition2DTO.setState(TDState.PRIVATED);
        trainingDefinition2DTO.setTitle("test");
        trainingDefinition2DTO.setAuthorRefDTO(authorRefSetDTO);
        trainingDefinition2DTO.setSandBoxDefinitionRefDTO(sandboxDefinitionRefDTO);


        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        p = new PageImpl<TrainingDefinition>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        BeanMapping bM = new BeanMappingImpl(new ModelMapper());
        trainingDefinitionDTOPageResultResource = bM.mapToPageResultDTO(p, TrainingDefinitionDTO.class);

    }

    @Test
    public void findTrainingDefinitionById() throws Exception {
        given(trainingDefinitionFacade.findById(any(Long.class))).willReturn(trainingDefinition1DTO);
        String valueTd = convertObjectToJsonBytes(trainingDefinition1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions" + "/{id}", 1l))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinition1DTO)), result.getContentAsString());
    }

    @Test
    public void findTrainingDefinitionByIdWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/training-definitions" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }
/*
    @Test
    public void findAllTrainingDefinitions() throws Exception {
        String valueTd = convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        given(trainingDefinitionFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(trainingDefinitionDTOPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource)), result.getContentAsString());
    }
*/
    @Test
    public void updateTrainingDefinition() throws Exception {
        given(beanMapping.mapTo(any(TrainingDefinitionDTO.class), eq(TrainingDefinition.class))).willReturn(trainingDefinition1);
        mockMvc.perform(put("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinition1DTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateTrainingDefinitionWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).update(any(TrainingDefinition.class));
        given(beanMapping.mapTo(any(TrainingDefinitionDTO.class), eq(TrainingDefinition.class))).willReturn(trainingDefinition1);
        Exception exception = mockMvc.perform(put("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinition1DTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotModifiedException.class, exception.getClass());
    }
/*
    @Test
    public void cloneTrainingDefinition() throws Exception {
        TrainingDefinitionDTO trainingDefinitionClone = new TrainingDefinitionDTO();
        trainingDefinitionClone.setId(8L);
        trainingDefinitionClone.setTitle("Clone of " + trainingDefinition2.getTitle());
        trainingDefinitionClone.setState(TDState.UNRELEASED);
        trainingDefinitionClone.setStartingLevel(null);
        given(trainingDefinitionFacade.clone(any(Long.class))).willReturn(trainingDefinitionClone);

        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions/{id}", 2L))
                .andExpect(status().isOk())
                //.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(trainingDefinitionClone.toString(),"TrainingDefinitionDTO" + result.getContentAsString());
    }
*/
    @Test
    public void cloneTrainingDefinitionWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).clone(any(Long.class));
        Exception exception = mockMvc.perform(post("/training-definitions/1")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotCreatedException.class, exception.getClass());
    }

    @Test
    public void swapLeft() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-left", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void swapLeftWithCannotBeUpdatedException() throws Exception {
        willThrow(CannotBeUpdatedException.class).given(trainingDefinitionFacade).swapLeft(any(Long.class), any(Long.class));
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-left", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void swapLeftWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).swapLeft(any(Long.class), any(Long.class));
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-left", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotModifiedException.class, exception.getClass());
    }

    @Test
    public void swapRight() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-right", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void swapRightWithCannotBeUpdatedException() throws Exception {
        willThrow(CannotBeUpdatedException.class).given(trainingDefinitionFacade).swapRight(any(Long.class), any(Long.class));
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-right", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void swapRightWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).swapRight(any(Long.class), any(Long.class));
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-right", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotModifiedException.class, exception.getClass());
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteTrainingDefinitionWithCannotBeDeletedException() throws Exception {
        willThrow(CannotBeDeletedException.class).given(trainingDefinitionFacade).delete(any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();

        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void deleteTrainingDefinitionWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).delete(any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void deleteLevel() throws Exception{
        mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition1.getId(), gameLevel.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(CannotBeUpdatedException.class).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}",trainingDefinition2.getId(), gameLevel.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void deleteLevelWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}",trainingDefinition2.getId(), gameLevel.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateGameLevel() throws Exception {
        mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(gameLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateGameLevelWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevel .class));
        given(beanMapping.mapTo(any(GameLevelDTO.class), eq(GameLevel.class))).willReturn(gameLevel);
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
                .content(convertObjectToJsonBytes(gameLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateGameLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(CannotBeUpdatedException.class).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevel .class));
        given(beanMapping.mapTo(any(GameLevelDTO.class), eq(GameLevel.class))).willReturn(gameLevel);
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
                .content(convertObjectToJsonBytes(gameLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    @Test
    public void updateInfoLevel() throws Exception {

        mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(infoLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateInfoLevelWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevel .class));
        given(beanMapping.mapTo(any(InfoLevelDTO.class), eq(InfoLevel.class))).willReturn(infoLevel);
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
                .content(convertObjectToJsonBytes(infoLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateInfoLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(CannotBeUpdatedException.class).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevel .class));
        given(beanMapping.mapTo(any(InfoLevelDTO.class), eq(InfoLevel.class))).willReturn(infoLevel);
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
                .content(convertObjectToJsonBytes(gameLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }
/*
    @Test
    public void updateAssessmentLevel() throws Exception {

        mockMvc.perform(put("/training-definitions/{definitionId}/assessment-levels", trainingDefinition1.getId())
                .content(convertObjectToJsonBytes(assessmentLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateAssessmentLevelWithFacadeLayerException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).updateAssessmentLevel(any(Long.class), any(AssessmentLevel .class));
        given(beanMapping.mapTo(any(AssessmentLevelDTO.class), eq(AssessmentLevel.class))).willReturn(assessmentLevel);
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/assessment-levels", trainingDefinition2.getId())
                .content(convertObjectToJsonBytes(infoLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void updateAssessmentLevelWithCannotBeUpdatedException() throws Exception {
        willThrow(CannotBeUpdatedException.class).given(trainingDefinitionFacade).updateAssessmentLevel(any(Long.class), any(AssessmentLevel .class));
        given(beanMapping.mapTo(any(AssessmentLevelDTO.class), eq(AssessmentLevel.class))).willReturn(assessmentLevel);
        Exception exception = mockMvc.perform(put("/training-definitions/{definitionId}/assessment-levels", trainingDefinition2.getId())
                .content(convertObjectToJsonBytes(gameLevel))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }
*/
    @Test
    public void createTrainingDefinition() throws Exception {
        String valueTd = convertObjectToJsonBytes(trainingDefinition1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
        given(trainingDefinitionFacade.create(any(TrainingDefinition.class))).willReturn(trainingDefinition1DTO);
        given(beanMapping.mapTo(any(TrainingDefinitionDTO.class), eq(TrainingDefinition.class))).willReturn(trainingDefinition1);
        MockHttpServletResponse result = mockMvc.perform(post("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinition1DTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(trainingDefinition1DTO), result.getContentAsString());
    }

    @Test
    public void createTrainingDefinitionWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).create(any(TrainingDefinition.class));
        given(beanMapping.mapTo(any(TrainingDefinitionDTO.class), eq(TrainingDefinition.class))).willReturn(trainingDefinition1);
        Exception exception = mockMvc.perform(post("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinition1DTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotCreatedException.class, exception.getClass());
    }
    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }


}
