package cz.muni.ics.kypo.rest.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingRunDTO;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.model.TrainingRun;
import cz.muni.ics.kypo.model.enums.TRState;
import cz.muni.ics.kypo.service.TrainingRunService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RunWith(SpringRunner.class)
public class TrainingRunRestControllerTest {

    @MockBean
    private TrainingRunService trainingRunService;

    @MockBean
    private BeanMapping beanMapping;

    @MockBean
    @Qualifier("objMapperRESTApi")
    private ObjectMapper objectMapper;


    @Autowired
    private TrainingRunsRestController trainingRunsRestController;

    private MockMvc mockMvc;

    private Pageable pageable;

    private TrainingRun trainingRun1, trainingRun2;

    private TrainingRunDTO trainingRun1DTO, trainingRun2DTO;

    private PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource;

    private Page p;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.NEW);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.ARCHIVED);

        trainingRun1DTO = new TrainingRunDTO();
        trainingRun1DTO.setId(1L);
        trainingRun1DTO.setState(TRState.NEW);

        trainingRun2DTO = new TrainingRunDTO();
        trainingRun2DTO.setId(2L);
        trainingRun2DTO.setState(TRState.ARCHIVED);

        List<TrainingRun> expected = new ArrayList();
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
        given(trainingRunService.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String valueTr = convertObjectToJsonBytes(trainingRun1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);

        mockMvc.perform(get("/training-runs/{id}", trainingRun1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(convertObjectToJsonBytes(trainingRun1DTO)));
    }
    /*
    @Test
    public void findAllTrainingRuns() throws Exception {
        String valueTr = convertObjectToJsonBytes(trainingRunDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade)
    }
*/
    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
