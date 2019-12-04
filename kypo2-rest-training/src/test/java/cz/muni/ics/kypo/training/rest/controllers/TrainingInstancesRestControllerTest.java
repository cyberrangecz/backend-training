package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.rest.exceptions.InternalServerErrorException;
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
import org.springframework.data.domain.PageRequest;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Boris Jadus(445343)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingInstanceMapperImpl.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class TrainingInstancesRestControllerTest {

    private TrainingInstancesRestController trainingInstancesRestController;

    @Autowired
    TrainingInstanceMapper trainingInstanceMapper;

    @Mock
    private TrainingInstanceFacade trainingInstanceFacade;

    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;
    private ObjectMapper testObjectMapper;

    private TrainingInstance trainingInstance1, trainingInstance2;

    private TrainingInstanceDTO trainingInstance1DTO, trainingInstance2DTO;
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;
    private UserRefDTO organizerDTO1, organizerDTO2, organizerDTO3;
    private UserRef participant1;
    private Pageable pageable;
    private PageResultResource.Pagination pagination;

    private Page p;

    private PageResultResource<TrainingInstanceFindAllResponseDTO> trainingInstanceDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstancesRestController = new TrainingInstancesRestController(trainingInstanceFacade, objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        pageable = PageRequest.of(0,5);

        organizerDTO1 = createUserRefDTO(10L, "Bc. Dominik Meškal", "Meškal", "Dominik", "445533@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO2 = createUserRefDTO(20L, "Bc. Boris Makal", "Makal", "Boris", "772211@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO3 = createUserRefDTO(30L, "Ing. Pavel Flákal", "Flákal", "Pavel", "221133@muni.cz", "https://oidc.muni.cz/oidc", null);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test1");

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
        LocalDateTime startTime = LocalDateTime.now(Clock.systemUTC());
        trainingInstanceCreateDTO.setStartTime(LocalDateTime.now(Clock.systemUTC()));
        LocalDateTime endTime = LocalDateTime.now(Clock.systemUTC()).plusHours(10);
        trainingInstanceCreateDTO.setEndTime(endTime);
        trainingInstanceCreateDTO.setAccessToken("pass");
        trainingInstanceCreateDTO.setPoolSize(20);
        trainingInstanceCreateDTO.setTrainingDefinitionId(1L);

        trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setId(5L);
        trainingInstanceUpdateDTO.setTitle("update instance title");
        trainingInstanceUpdateDTO.setStartTime(startTime.plusHours(1));
        trainingInstanceUpdateDTO.setEndTime(endTime);
        trainingInstanceUpdateDTO.setPoolSize(5);
        //trainingInstanceUpdateDTO.setKeyword("pass-2586");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(1L);

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
    public void allocateSandboxes() throws Exception {
        mockMvc.perform(post("/training-instances" + "/{instanceId}/" + "sandbox-instances", 1L))
                .andExpect(status().isAccepted());
    }

    @Test
    public void allocateSandboxesWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).allocateSandboxes(any(Long.class), isNull());
        Exception exception =
                mockMvc.perform(post("/training-instances" + "/{instanceId}/" + "sandbox-instances", 698L))
                        .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void deleteSandboxes() throws Exception {
        mockMvc.perform(delete("/training-instances" + "/{instanceId}/sandbox-instances", 1L)
                .param("sandboxIds", "1")
                .param("sandboxIds", "2"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteSandboxesWithFacadeException() throws Exception {
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).deleteSandboxes(any(Long.class), any(Set.class));
        Exception exception =
                mockMvc.perform(delete("/training-instances" + "/{instanceId}/" + "sandbox-instances", 698L)
                        .param("sandboxIds", "1")
                        .param("sandboxIds", "2"))
                        .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstance() throws Exception {
        PageResultResource<UserRefDTO> expectedUsersRefDTOs = new PageResultResource<>(List.of(organizerDTO1, organizerDTO2),pagination);
        pagination = new PageResultResource.Pagination(0,2,5,2,1);
        given(trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null))).willReturn(expectedUsersRefDTOs);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(convertObjectToJsonBytes(expectedUsersRefDTOs));

        MockHttpServletResponse result = mockMvc.perform(get("/training-instances/{id}/organizers-not-in-training-instance", trainingInstance1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(expectedUsersRefDTOs), convertJsonBytesToString(result.getContentAsString()));
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstanceNotFoundError() throws Exception {
        willThrow(new FacadeLayerException(new ServiceLayerException("Training instance not found.",
                ErrorCode.RESOURCE_NOT_FOUND))).given(trainingInstanceFacade).getOrganizersNotInGivenTrainingInstance(eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null));
        Exception ex = mockMvc.perform(get("/training-instances/{id}/organizers-not-in-training-instance", trainingInstance1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, ex.getClass());
        assertEquals("Training instance not found.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstanceUserServiceError() throws Exception {
        willThrow(new FacadeLayerException(new ServiceLayerException("Unexpected error when calling user and group service.",
                ErrorCode.UNEXPECTED_ERROR))).given(trainingInstanceFacade).getOrganizersNotInGivenTrainingInstance(eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null));
        Exception ex = mockMvc.perform(get("/training-instances/{id}/organizers-not-in-training-instance", trainingInstance1.getId()))
                .andExpect(status().isInternalServerError())
                .andReturn().getResolvedException();
        assertEquals(InternalServerErrorException.class, ex.getClass());
        assertEquals("Unexpected error when calling user and group service.", ex.getCause().getCause().getLocalizedMessage());
    }


    @Test
    public void editOrganizers() throws Exception {
        mockMvc.perform(put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2,3")
                .param("organizersRemoval", "3,4"))
                .andExpect(status().isNoContent());
        then(trainingInstanceFacade).should().editOrganizers(trainingInstance1.getId(), Set.of(1L,2L,3L), Set.of(3L,4L));
    }

    @Test
    public void editOrganizersTrainingInstanceNotFound() throws Exception {
        willThrow(new FacadeLayerException(new ServiceLayerException("Training instance not found.",
                ErrorCode.RESOURCE_NOT_FOUND))).given(trainingInstanceFacade).editOrganizers(trainingInstance1.getId(), Set.of(1L,2L), Set.of(4L));
        Exception ex = mockMvc.perform(put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2")
                .param("organizersRemoval", "4"))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, ex.getClass());
        assertEquals("Training instance not found.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void editOrganizersUserAndGroupServiceError() throws Exception {
        willThrow(new FacadeLayerException(new ServiceLayerException("Unexpected error when calling user and group service.",
                ErrorCode.UNEXPECTED_ERROR))).given(trainingInstanceFacade).editOrganizers(trainingInstance1.getId(), Set.of(1L,2L), Set.of(4L));
        Exception ex = mockMvc.perform(put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2")
                .param("organizersRemoval", "4"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResolvedException();
        assertEquals(InternalServerErrorException.class, ex.getClass());
        assertEquals("Unexpected error when calling user and group service.", ex.getCause().getCause().getLocalizedMessage());
    }


    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
        return mapper.writeValueAsString(object);
    }

    private static String convertJsonBytesToString(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }

    private <T> T convertResultStringToDTO(String resultAsString, Class<T> claas) throws Exception {
        System.out.println(resultAsString);
        return testObjectMapper.readValue(convertJsonBytesToString(resultAsString), claas);
    }

    private UserRefDTO createUserRefDTO(Long userRefId, String fullName, String familyName, String givenName, String login, String iss, byte[] picture) {
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefId(userRefId);
        userRefDTO.setUserRefFullName(fullName);
        userRefDTO.setUserRefFamilyName(familyName);
        userRefDTO.setUserRefGivenName(givenName);
        userRefDTO.setUserRefLogin(login);
        userRefDTO.setIss(iss);
        userRefDTO.setPicture(picture);
        return userRefDTO;
    }
}
