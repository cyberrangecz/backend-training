package cz.muni.ics.kypo.rest.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.api.dto.TrainingRunDTO;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.TrainingRun;
import cz.muni.ics.kypo.model.enums.TRState;
import cz.muni.ics.kypo.service.TrainingRunService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.Optional;


@RunWith(SpringRunner.class)
public class TrainingRunRestControllerTest {

    @Mock
    private TrainingRunService trainingRunService;

    @Mock
    BeanMapping beanMapping;

    @InjectMocks
    private TrainingRunsRestController trainingRunsRestController;

    private MockMvc mockMvc;

    private Pageable pageable;

    private TrainingRun trainingRun1, trainingRun2;

    private TrainingRunDTO trainingRun1DTO, trainingRun2DTO;

    @Before
    public void init() {
        pageable = PageRequest.of(0, 10);
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
        

        given(beanMapping.mapTo(trainingRun1, TrainingRunDTO.class)).willReturn(trainingRun1DTO);
        given(beanMapping.mapTo(trainingRun2, TrainingRunDTO.class)).willReturn(trainingRun2DTO);
    }

    @Test
    public void findTrainingRunById() throws Exception {
        given(trainingRunService.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));

        mockMvc.perform(get("/training-runs/{id}", trainingRun1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(convertObjectToJsonBytes(trainingRun1DTO)));
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
