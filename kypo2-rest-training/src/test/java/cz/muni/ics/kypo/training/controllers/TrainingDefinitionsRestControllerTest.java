package cz.muni.ics.kypo.training.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.ResourceNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingDefinitionsRestController.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo"})
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

    private Page p;

    private PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.UNRELEASED);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.PRIVATED);

        trainingDefinition1DTO = new TrainingDefinitionDTO();
        trainingDefinition1DTO.setId(1L);
        trainingDefinition1DTO.setState(TDState.UNRELEASED);

        trainingDefinition2DTO = new TrainingDefinitionDTO();
        trainingDefinition2DTO.setId(2L);
        trainingDefinition2DTO.setState(TDState.PRIVATED);

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

    @Test
    public void updateTrainingDefinition() throws Exception {
        given(beanMapping.mapTo(any(TrainingDefinitionDTO.class), eq(TrainingDefinition.class))).willReturn(trainingDefinition1);

        MockHttpServletResponse result = mockMvc.perform(put("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinition1))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
    }

    @Test
    public void updateTrainingDefinitionWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).update(any(TrainingDefinition.class));
        given(beanMapping.mapTo(any(TrainingDefinitionDTO.class), eq(TrainingDefinition.class))).willReturn(trainingDefinition1);
        Exception exception = mockMvc.perform(put("/training-definitions")
                .content(convertObjectToJsonBytes(trainingDefinition1))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotModifiedException.class, exception.getClass());
    }

    @Test
    public void cloneTrainingDefinitionWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingDefinitionFacade).clone(any(Long.class));
        Exception exception = mockMvc.perform(post("/training-definitions/1")
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
