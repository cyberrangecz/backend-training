package cz.muni.ics.kypo.training.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.model.TrainingRun;
import cz.muni.ics.kypo.training.model.enums.TRState;
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

import static org.mockito.BDDMockito.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingRunsRestController.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo"})
public class TrainingRunsRestControllerTest {

    @Autowired
    private TrainingRunsRestController trainingRunsRestController;

    @MockBean
    private TrainingRunFacade trainingRunFacade;

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("objMapperRESTApi")
    private ObjectMapper objectMapper;

    private TrainingRun trainingRun1, trainingRun2;

    private TrainingRunDTO trainingRun1DTO, trainingRun2DTO;

    private Page p;

    private PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.ARCHIVED);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.READY);

        trainingRun1DTO = new TrainingRunDTO();
        trainingRun1DTO.setId(1L);
        trainingRun1DTO.setState(TRState.ARCHIVED);

        trainingRun2DTO = new TrainingRunDTO();
        trainingRun2DTO.setId(2L);
        trainingRun2DTO.setState(TRState.READY);

        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        p = new PageImpl<TrainingRun>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        BeanMapping bM = new BeanMappingImpl(new ModelMapper());
        trainingRunDTOPageResultResource = bM.mapToPageResultDTO(p, TrainingRunDTO.class);

    }

    @Test
    public void findTrainingRunById() throws Exception {
        given(trainingRunFacade.findById(any(Long.class))).willReturn(trainingRun1DTO);
        String valueTr = convertObjectToJsonBytes(trainingRun1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs" + "/{id}", 1l))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRun1DTO)), result.getContentAsString());
    }

    @Test
    public void findTrainingRunByIdWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingRunFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/training-runs" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void findAllTrainingRuns() throws Exception {
        String valueTr = convertObjectToJsonBytes(trainingRunDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(trainingRunDTOPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(get("/training-runs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}
