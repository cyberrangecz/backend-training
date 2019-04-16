package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class ExportImportRestControllerTest {

	private ExportImportRestController exportImportRestController;

	private MockMvc mockMvc;

	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private ExportImportFacade exportImportFacade;

	private TrainingInstanceArchiveDTO trainingInstanceArchiveDTO;

	@Before
	public void init(){
		MockitoAnnotations.initMocks(this);
		exportImportRestController = new ExportImportRestController(exportImportFacade, objectMapper);
		this.mockMvc = MockMvcBuilders.standaloneSetup(exportImportRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter(), new ByteArrayHttpMessageConverter()).build();

		trainingInstanceArchiveDTO = new TrainingInstanceArchiveDTO();
		trainingInstanceArchiveDTO.setExportTrainingDefinitionAndLevelsDTO(new ExportTrainingDefinitionAndLevelsDTO());
		trainingInstanceArchiveDTO.setAccessToken("pass-123");
		LocalDateTime startTime = LocalDateTime.now();
		trainingInstanceArchiveDTO.setStartTime(startTime.minusHours(12));
		trainingInstanceArchiveDTO.setEndTime(startTime.minusHours(5));
		trainingInstanceArchiveDTO.setPoolSize(10);
		trainingInstanceArchiveDTO.setTitle("title");

		ObjectMapper obj = new ObjectMapper();
		obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());
	}

	@Test
	public void archiveTrainingInstance() throws Exception{
		FileToReturnDTO file = new FileToReturnDTO();
		file.setContent(convertObjectToJsonBytes(trainingInstanceArchiveDTO).getBytes());
		file.setTitle(trainingInstanceArchiveDTO.getTitle());
		given(exportImportFacade.archiveTrainingInstance(any(Long.class))).willReturn(file);
		String valueTi = convertObjectToJsonBytes(trainingInstanceArchiveDTO);
		given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
		
		mockMvc.perform(get("/exports/training-instances" + "/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
	}

	@Test
	public void archiveTrainingInstanceWithFacadeException() throws Exception{
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(exportImportFacade).archiveTrainingInstance(any(Long.class));
		Exception exception = mockMvc.perform(get("/exports/training-instances" + "/{id}", 600l))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
		return mapper.writeValueAsString(object);
	}

}
