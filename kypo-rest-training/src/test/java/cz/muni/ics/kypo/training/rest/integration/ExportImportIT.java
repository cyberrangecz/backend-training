package cz.muni.ics.kypo.training.rest.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.TrainingLevelImportDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.ExportImportRestController;
import cz.muni.ics.kypo.training.rest.controllers.TrainingDefinitionsRestController;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
		ExportImportRestController.class,
        IntegrationTestApplication.class,
		TestDataFactory.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class ExportImportIT {

	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private ExportImportRestController exportImportRestController;
	@Autowired
	private TrainingDefinitionRepository trainingDefinitionRepository;
	@Autowired
	private InfoLevelRepository infoLevelRepository;
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	@Autowired
	private TrainingLevelRepository trainingLevelRepository;
	@Autowired
	private AssessmentLevelRepository assessmentLevelRepository;
	@Autowired
	private UserRefRepository userRefRepository;
	@Autowired
	@Qualifier("userManagementExchangeFunction")
	private ExchangeFunction exchangeFunction;


	private MockMvc mvc;

	private TrainingDefinition trainingDefinition;
	private InfoLevel infoLevel;
	private AssessmentLevel assessmentLevel;
	private TrainingLevel trainingLevel, trainingLevelVariantAnswer;
	private AccessLevel accessLevel;
	private UserRef userRef;
	private UserRefDTO userRefDTO;

	@BeforeEach
	public void init(){
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
		objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

		this.mvc = MockMvcBuilders.standaloneSetup(exportImportRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(
								new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter(mapper), new ByteArrayHttpMessageConverter())
				.setControllerAdvice(new CustomRestExceptionHandlerTraining())
				.build();

		trainingDefinition = testDataFactory.getReleasedDefinition();

		infoLevel = testDataFactory.getInfoLevel2();
		infoLevel.setTrainingDefinition(trainingDefinition);
		infoLevel.setOrder(0);

		accessLevel = testDataFactory.getAccessLevel();
		accessLevel.setTrainingDefinition(trainingDefinition);
		accessLevel.setOrder(1);

		assessmentLevel = testDataFactory.getTest();
		assessmentLevel.setTrainingDefinition(trainingDefinition);
		assessmentLevel.setOrder(2);
		Question mcq = testDataFactory.getMultipleChoiceQuestion();
		mcq.setChoices(testDataFactory.getQuestionChoices(3, "Choice ", List.of(true, false, true), mcq));
		mcq.setAssessmentLevel(assessmentLevel);
		mcq.setOrder(0);
		Question emi = testDataFactory.getExtendedMatchingStatements();
		emi.setExtendedMatchingOptions(testDataFactory.getExtendedMatchingOptions(4, "Option ", emi));
		emi.setExtendedMatchingStatements(testDataFactory.getExtendedMatchingStatements(3, "State ", emi));
		emi.getExtendedMatchingStatements().get(0).setExtendedMatchingOption(emi.getExtendedMatchingOptions().get(1));
		emi.getExtendedMatchingStatements().get(1).setExtendedMatchingOption(emi.getExtendedMatchingOptions().get(3));
		emi.getExtendedMatchingStatements().get(2).setExtendedMatchingOption(emi.getExtendedMatchingOptions().get(0));
		emi.setAssessmentLevel(assessmentLevel);
		emi.setOrder(1);
		Question ffq = testDataFactory.getFreeFormQuestion();
		ffq.setChoices(testDataFactory.getQuestionChoices(2, "FFQ Choice ", List.of(true, true), ffq));
		ffq.setAssessmentLevel(assessmentLevel);
		ffq.setOrder(2);
		assessmentLevel.setQuestions(new ArrayList<>(List.of(mcq, emi, ffq)));

		trainingLevel = testDataFactory.getPenalizedLevel();
		trainingLevel.setTrainingDefinition(trainingDefinition);
		trainingLevel.setOrder(3);

		trainingLevelVariantAnswer = testDataFactory.getNonPenalizedLevel();
		trainingLevelVariantAnswer.setVariantAnswers(true);
		trainingLevelVariantAnswer.setAnswerVariableName("variable-name");
		trainingLevelVariantAnswer.setAnswer(null);
		trainingLevelVariantAnswer.setTrainingDefinition(trainingDefinition);
		trainingLevelVariantAnswer.setOrder(4);

		trainingDefinitionRepository.save(trainingDefinition);
		infoLevelRepository.save(infoLevel);
		trainingLevelRepository.save(trainingLevel);
		trainingLevelRepository.save(trainingLevelVariantAnswer);
		assessmentLevelRepository.save(assessmentLevel);
		accessLevelRepository.save(accessLevel);

		userRef = testDataFactory.getUserRef1();
		userRefRepository.save(userRef);

		userRefDTO = testDataFactory.getUserRefDTO1();
	}

	@Test
	public void exportAndImportTrainingDefinition() throws Exception{
		doReturn(buildMockResponse(userRefDTO)).when(exchangeFunction).exchange(any(ClientRequest.class));
		MockHttpServletResponse response = mvc.perform(get("/exports/training-definitions/{id}", trainingDefinition.getId()))
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		mvc.perform(post("/imports/training-definitions")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(response.getContentAsString()))
				.andExpect(status().isOk());

		assertEquals(2, assessmentLevelRepository.findAll().size());
		assertEquals(2, accessLevelRepository.findAll().size());
		assertEquals(2, infoLevelRepository.findAll().size());
		assertEquals(4, trainingLevelRepository.findAll().size());
		assertEquals(2, trainingDefinitionRepository.findAll().size());
	}

	private Mono<ClientResponse> buildMockResponse(Object body) throws IOException{
		ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
				.body(convertObjectToJsonBytes(body))
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
		return Mono.just(clientResponse);
	}
}
