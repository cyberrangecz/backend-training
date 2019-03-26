package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, SnapshotHookMapperImpl.class,
        TrainingInstanceMapperImpl.class, TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class TrainingInstancesRestControllerTest {

    private TrainingInstancesRestController trainingInstancesRestController;

    @Autowired
    TrainingInstanceMapper trainingInstanceMapper;

    @Mock
    private TrainingInstanceFacade trainingInstanceFacade;

    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;

    private TrainingInstance trainingInstance1, trainingInstance2;

    private TrainingInstanceDTO trainingInstance1DTO, trainingInstance2DTO;
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;
    private UserInfoDTO userInfoDTO1;

    private Page p;

    private PageResultResource<TrainingInstanceDTO> trainingInstanceDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstancesRestController = new TrainingInstancesRestController(trainingInstanceFacade, objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        userInfoDTO1 = new UserInfoDTO();
        userInfoDTO1.setLogin("441048@mail.muni.cz");
        userInfoDTO1.setFullName("Mgr. Ing. Pavel Šeda");

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test1");
        trainingInstance1.setSandboxInstanceRefs(new HashSet<>());

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test2");

        trainingInstance1DTO = new TrainingInstanceDTO();
        trainingInstance1DTO.setId(1L);
        trainingInstance1DTO.setTitle("test1");

        trainingInstance2DTO = new TrainingInstanceDTO();
        trainingInstance2DTO.setId(2L);
        trainingInstance2DTO.setTitle("test2");

        trainingInstanceCreateDTO = new TrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setTitle("create instance title");
        LocalDateTime startTime = LocalDateTime.now();
        trainingInstanceCreateDTO.setStartTime(startTime);
        LocalDateTime endTime = LocalDateTime.now().plusHours(10);
        trainingInstanceCreateDTO.setEndTime(endTime);
        trainingInstanceCreateDTO.setAccessToken("pass");
        trainingInstanceCreateDTO.setPoolSize(20);
        trainingInstanceCreateDTO.setOrganizers(Set.of(userInfoDTO1));
        trainingInstanceCreateDTO.setTrainingDefinitionId(1L);

        trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setId(5L);
        trainingInstanceUpdateDTO.setTitle("update instance title");
        trainingInstanceUpdateDTO.setStartTime(startTime.plusHours(1));
        trainingInstanceUpdateDTO.setEndTime(endTime);
        trainingInstanceUpdateDTO.setPoolSize(5);
        //trainingInstanceUpdateDTO.setKeyword("pass-2586");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(1L);
        trainingInstanceUpdateDTO.setOrganizers(Set.of(userInfoDTO1));

        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        p = new PageImpl<>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        trainingInstanceDTOPageResultResource = trainingInstanceMapper.mapToPageResultResource(p);
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
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstance1DTO)), result.getContentAsString());
    }

    @Test
    public void findTrainingInstanceByIdWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/training-instances" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void findAllTrainingInstances() throws Exception {
        String valueTi = convertObjectToJsonBytes(trainingInstanceDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
        given(trainingInstanceFacade.findAll(any(Predicate.class), any(Pageable.class))).willReturn(trainingInstanceDTOPageResultResource);
        MockHttpServletResponse result = mockMvc.perform(get("/training-instances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstanceDTOPageResultResource)), result.getContentAsString());
    }

    @Test
    public void deleteTrainingInstance() throws Exception {
        mockMvc.perform(delete("/training-instances" + "/{id}", 1l)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteTrainingInstanceWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).delete(any(Long.class));
        Exception exception = mockMvc.perform(delete("/training-instances" + "/{id}", 1l)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void allocateSandboxesWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).allocateSandboxes(any(Long.class));
        Exception exception =
                mockMvc.perform(post("/training-instances" + "/{instanceId}/" + "sandbox-instances", 698L))
                        .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void createPoolForSandboxes() throws Exception {
        given(trainingInstanceFacade.createPoolForSandboxes(1L)).willReturn(5L);
        MockHttpServletResponse result = mockMvc.perform(post("/training-instances/{instanceId}/pools", 1L))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(result.getContentAsString(), "5");
    }

    @Test
    public void createPoolForSandboxesWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).createPoolForSandboxes(any(Long.class));
        Exception exception =
                mockMvc.perform(post("/training-instances/{instanceId}/pools", 698L))
                        .andExpect(status().isConflict()).andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
        return mapper.writeValueAsString(object);
    }
}
