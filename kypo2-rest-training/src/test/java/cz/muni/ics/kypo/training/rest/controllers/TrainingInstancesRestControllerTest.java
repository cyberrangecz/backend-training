package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.*;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingInstanceMapperImpl.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingInstancesRestControllerTest {

    private TrainingInstancesRestController trainingInstancesRestController;

    @Autowired
    private TestDataFactory testDataFactory;
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
    private TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO;
    private TrainingInstanceBasicInfoDTO trainingInstanceBasicInfoDTO;
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
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();

        pageable = PageRequest.of(0, 5);

        organizerDTO1 = createUserRefDTO(10L, "Bc. Dominik Meškal", "Meškal", "Dominik", "445533@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO2 = createUserRefDTO(20L, "Bc. Boris Makal", "Makal", "Boris", "772211@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO3 = createUserRefDTO(30L, "Ing. Pavel Flákal", "Flákal", "Pavel", "221133@muni.cz", "https://oidc.muni.cz/oidc", null);

        trainingInstance1 = testDataFactory.getConcludedInstance();
        trainingInstance1.setId(1L);

        trainingInstance2 = testDataFactory.getOngoingInstance();
        trainingInstance2.setId(2L);

        trainingInstance1DTO = testDataFactory.getTrainingInstanceDTO();
        trainingInstance1DTO.setId(1L);

        trainingInstance2DTO = testDataFactory.getTrainingInstanceDTO();
        trainingInstance2DTO.setId(2L);
        trainingInstance2DTO.setTitle("DTO2");

        trainingInstanceCreateDTO = testDataFactory.getTrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setTrainingDefinitionId(1L);

        trainingInstanceUpdateDTO = testDataFactory.getTrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setId(5L);
        trainingInstanceUpdateDTO.setTrainingDefinitionId(1L);

        trainingInstanceAssignPoolIdDTO = new TrainingInstanceAssignPoolIdDTO();
        trainingInstanceAssignPoolIdDTO.setPoolId(1L);

        trainingInstanceBasicInfoDTO = new TrainingInstanceBasicInfoDTO();

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
    public void findTrainingInstanceByIdNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingInstanceFacade).findById(any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(get("/training-instances" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
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
    public void assignPool() throws Exception {
        given(trainingInstanceFacade.assignPoolToTrainingInstance(1L, trainingInstanceAssignPoolIdDTO)).willReturn(trainingInstanceBasicInfoDTO);
        mockMvc.perform(patch("/training-instances" + "/{instanceId}/" + "assign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void assignPoolWithFacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingInstanceFacade).assignPoolToTrainingInstance(anyLong(), any(TrainingInstanceAssignPoolIdDTO.class));
        MockHttpServletResponse response = mockMvc.perform(patch("/training-instances" + "/{instanceId}/" + "assign-pool", 698L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void unassignPool() throws Exception {
        mockMvc.perform(patch("/training-instances" + "/{instanceId}/unassign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstance() throws Exception {
        PageResultResource<UserRefDTO> expectedUsersRefDTOs = new PageResultResource<>(List.of(organizerDTO1, organizerDTO2), pagination);
        pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
        given(trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null))).willReturn(expectedUsersRefDTOs);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(convertObjectToJsonBytes(expectedUsersRefDTOs));

        MockHttpServletResponse result = mockMvc.perform(get("/training-instances/{id}/organizers-not-in-training-instance", trainingInstance1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(expectedUsersRefDTOs), convertJsonBytesToString(result.getContentAsString()));
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstanceNotFoundError() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingInstanceFacade).getOrganizersNotInGivenTrainingInstance(eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null));
        MockHttpServletResponse response = mockMvc.perform(get("/training-instances/{id}/organizers-not-in-training-instance", trainingInstance1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstanceUserServiceError() throws Exception {
        willThrow(new InternalServerErrorException("Unexpected error when calling user and group service.")).given(trainingInstanceFacade).getOrganizersNotInGivenTrainingInstance(eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null));
        MockHttpServletResponse response = mockMvc.perform(get("/training-instances/{id}/organizers-not-in-training-instance", trainingInstance1.getId()))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
        assertEquals("Unexpected error when calling user and group service.", error.getMessage());
    }


    @Test
    public void editOrganizers() throws Exception {
        mockMvc.perform(put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2,3")
                .param("organizersRemoval", "3,4"))
                .andExpect(status().isNoContent());
        then(trainingInstanceFacade).should().editOrganizers(trainingInstance1.getId(), Set.of(1L, 2L, 3L), Set.of(3L, 4L));
    }

    @Test
    public void editOrganizersTrainingInstanceNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingInstanceFacade).editOrganizers(trainingInstance1.getId(), Set.of(1L, 2L), Set.of(4L));
        MockHttpServletResponse response = mockMvc.perform(put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2")
                .param("organizersRemoval", "4"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void editOrganizersUserAndGroupServiceError() throws Exception {
        willThrow(new InternalServerErrorException("Unexpected error when calling user and group service.")).given(trainingInstanceFacade).editOrganizers(trainingInstance1.getId(), Set.of(1L, 2L), Set.of(4L));
        MockHttpServletResponse response = mockMvc.perform(put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2")
                .param("organizersRemoval", "4"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
        assertEquals("Unexpected error when calling user and group service.", error.getMessage());
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
