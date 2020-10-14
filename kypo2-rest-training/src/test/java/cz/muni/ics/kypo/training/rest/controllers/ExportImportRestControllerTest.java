package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class ExportImportRestControllerTest {

	@Autowired
	private TestDataFactory testDataFactory;

	private ExportImportRestController exportImportRestController;

	private MockMvc mockMvc;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ExportImportFacade exportImportFacade;

	private TrainingInstanceArchiveDTO trainingInstanceArchiveDTO;
	private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;

	@Before
	public void init(){
		MockitoAnnotations.initMocks(this);
		exportImportRestController = new ExportImportRestController(exportImportFacade, objectMapper);
		this.mockMvc = MockMvcBuilders.standaloneSetup(exportImportRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(
								new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter(), new ByteArrayHttpMessageConverter())
				.setControllerAdvice(new CustomRestExceptionHandlerTraining())
				.build();

		trainingInstanceArchiveDTO = testDataFactory.getTrainingInstanceArchiveDTO();

		InfoLevelImportDTO infoLevelImportDTO = testDataFactory.getInfoLevelImportDTO();
		infoLevelImportDTO.setOrder(0);

		AssessmentLevelImportDTO assessmentLevelDTO = testDataFactory.getAssessmentLevelImportDTO();
		assessmentLevelDTO.setOrder(1);

		GameLevelImportDTO gameLevelImportDTO = testDataFactory.getGameLevelImportDTO();
		gameLevelImportDTO.setOrder(2);

		importTrainingDefinitionDTO = testDataFactory.getImportTrainingDefinitionDTO();
		importTrainingDefinitionDTO.setLevels(Arrays.asList(infoLevelImportDTO,assessmentLevelDTO,gameLevelImportDTO));

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
		willThrow(new EntityNotFoundException()).given(exportImportFacade).archiveTrainingInstance(any(Long.class));
		MockHttpServletResponse response = mockMvc.perform(get("/exports/training-instances" + "/{id}", 600l))
				.andExpect(status().isNotFound())
				.andReturn().getResponse();
		ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
		assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
		assertEquals("The requested entity could not be found", error.getMessage());
	}

	@Test
	public void importTrainingDefinition() throws Exception{
		System.out.println(convertObjectToJsonBytes(importTrainingDefinitionDTO));
		mockMvc.perform(post("/imports/training-definitions")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(convertObjectToJsonBytes(importTrainingDefinitionDTO)))
				.andExpect(status().isOk());
	}


	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.registerModule(new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
		return mapper.writeValueAsString(object);
	}
}
