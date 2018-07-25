package cz.muni.ics.kypo.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.model.TrainingInstance;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingInstancesRestController.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo"})
public class TrainingInstancesRestControllerTest {

    @Autowired
    private TrainingInstancesRestController trainingInstancesRestController;

    @MockBean
    private TrainingInstanceFacade trainingInstanceFacade;

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("objMapperRESTApi")
    private ObjectMapper objectMapper;

    private TrainingInstance trainingInstance1, trainingInstance2;

    private TrainingInstanceDTO trainingInstance1DTO, trainingInstance2DTO;

    private Page p;

    private PageResultResource<TrainingInstanceDTO> trainingInstanceDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setKeyword("test1");

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setKeyword("test2");

        trainingInstance1DTO = new TrainingInstanceDTO();
        trainingInstance1.setId(1L);
        trainingInstance1.setKeyword("test1");

        trainingInstance2DTO = new TrainingInstanceDTO();
        trainingInstance2.setId(2L);
        trainingInstance2.setKeyword("test2");

        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        p = new PageImpl<TrainingInstance>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        BeanMapping bM = new BeanMappingImpl(new ModelMapper());
        trainingInstanceDTOPageResultResource = bM.mapToPageResultDTO(p, TrainingInstanceDTO.class);
    }

    @Test
    public void findTrainingInstanceById() throws Exception {
        given(trainingInstanceFacade.findById(any(Long.class))).willReturn(trainingInstance1DTO);
        String valueTi = convertObjectToJsonBytes(trainingInstance1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
        MockHttpServletResponse result = mockMvc.perform(get("/training-instances" + "/{id}", 1l))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        System.out.println(result.getContentAsString());
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstance1DTO)), result.getContentAsString());
    }

    @Test
    public void findTrainingInstanceByIdWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(trainingInstanceFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/training-instances" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void findAllTrainingInstances() throws Exception {
        String valueTi = convertObjectToJsonBytes(trainingInstanceDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
        given(trainingInstanceFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(trainingInstanceDTOPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(get("/training-instances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstanceDTOPageResultResource)), result.getContentAsString());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
