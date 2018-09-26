package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingInstancesRestController.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo")
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
	private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
	private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;

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
		LocalDateTime startTime = LocalDateTime.now();
		trainingInstanceCreateDTO.setStartTime(startTime);
		LocalDateTime endTime = LocalDateTime.now().plusHours(10);
		trainingInstanceCreateDTO.setEndTime(endTime);

		trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
		trainingInstanceUpdateDTO.setId(5L);
		trainingInstanceUpdateDTO.setTitle("update instance title");
		trainingInstanceUpdateDTO.setStartTime(startTime);
		trainingInstanceUpdateDTO.setEndTime(endTime);

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
				given(trainingInstanceFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(trainingInstanceDTOPageResultResource);

				MockHttpServletResponse result = mockMvc.perform(get("/training-instances"))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstanceDTOPageResultResource)), result.getContentAsString());
		}


		@Test
		public void createTrainingInstance() throws Exception {
				String valueTi = convertObjectToJsonBytes(trainingInstance1DTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
				given(trainingInstanceFacade.create(any(TrainingInstanceCreateDTO.class))).willReturn(trainingInstanceCreateDTO);
				MockHttpServletResponse result = mockMvc.perform(post("/training-instances")
						.content(convertObjectToJsonBytes(trainingInstance1))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstance1DTO)), result.getContentAsString());
		}



		@Test
		public void updateTrainingInstance() throws Exception {
				MockHttpServletResponse result = mockMvc.perform(put("/training-instances")
						.content(convertObjectToJsonBytes(trainingInstance1))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
						.andExpect(status().isNoContent())
						.andReturn().getResponse();
		}

		@Test
		public void updateTrainingInstanceWithFacadeException() throws Exception {
				Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
				willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).update(any(TrainingInstanceUpdateDTO.class));
				Exception exception = mockMvc.perform(put("/training-instances")
						.content(convertObjectToJsonBytes(trainingInstance1))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
						.andExpect(status().isConflict())
						.andReturn().getResolvedException();
				assertEquals(ConflictException.class, exception.getClass());
		}

		@Test
		public void deleteTrainingInstance() throws Exception {
				mockMvc.perform(delete("/training-instances")
						.param("trainingInstanceId", "1")
						.contentType(MediaType.APPLICATION_JSON_VALUE))
						.andExpect(status().isOk());
		}

		@Test
		public void deleteTrainingInstanceWithFacadeException() throws Exception {
				Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
				willThrow(new FacadeLayerException(exceptionThrow)).given(trainingInstanceFacade).delete(any(Long.class));
				Exception exception = mockMvc.perform(delete("/training-instances")
						.param("trainingInstanceId", "1")
						.contentType(MediaType.APPLICATION_JSON_VALUE))
						.andExpect(status().isNotFound())
						.andReturn().getResolvedException();
				assertEquals(ResourceNotFoundException.class, exception.getClass());
		}






	// TODO json parser cannot parse LocalDateTime
	// @Test
	// public void createTrainingInstance() throws Exception {
	// String valueTi = convertObjectToJsonBytes(trainingInstanceCreateDTO);
	// given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
	// given(trainingInstanceFacade.create(any(TrainingInstanceCreateDTO.class))).willReturn(trainingInstanceCreateDTO);
	// given(beanMapping.mapTo(any(TrainingInstanceCreateDTO.class),
	// eq(TrainingInstance.class))).willReturn(trainingInstance1);
	// MockHttpServletResponse result = mockMvc
	// .perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
	// .contentType(MediaType.APPLICATION_JSON_VALUE))
	// .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
	// assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstanceCreateDTO)),
	// result.getContentAsString());
	// }
	//
	// @Test
	// public void createTrainingInstanceWithFacadeException() throws Exception {
	// willThrow(FacadeLayerException.class).given(trainingInstanceFacade).create(trainingInstanceCreateDTO);
	// Exception exception =
	// mockMvc.perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
	// .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNotAcceptable()).andReturn().getResolvedException();
	// assertEquals(ResourceNotCreatedException.class, exception.getClass());
	// }

	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}
}
