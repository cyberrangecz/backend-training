package cz.muni.ics.kypo.training.rest.controllers.logging;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.rest.controllers.TrainingRunsRestController;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TrainingRunMapperImpl.class, SandboxInstanceRefMapperImpl.class})
public class LoggerTest {

    private TrainingRunsRestController trainingRunsRestController;

    private TrainingRunDTO trainingRun1DTO;

    @Mock
    private TrainingRunFacade trainingRunFacade;

    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    TrainingRunMapper trainingRunMapper;

    @Before
    public void init() {

        trainingRun1DTO = new TrainingRunDTO();
        trainingRun1DTO.setId(1L);
        trainingRun1DTO.setState(cz.muni.ics.kypo.training.api.enums.TRState.ARCHIVED);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        obj.registerModule(new JavaTimeModule());
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        MockitoAnnotations.initMocks(this);
        trainingRunsRestController = new TrainingRunsRestController(trainingRunFacade, objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()).build();
    }

    @Test
    public void testFindTrainingRunById() throws Exception{
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        final Appender mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);

        given(trainingRunFacade.findById(any(Long.class))).willReturn(trainingRun1DTO);
        String valueTr = convertObjectToJsonBytes(trainingRun1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        mockMvc.perform(get("/training-runs" + "/{runId}", 1l));

        verify(mockAppender).doAppend(argThat((final Object argument) ->
                ((LoggingEvent)argument).getFormattedMessage().contains("findTrainingRunById(1,null)")
        ));
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}