package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingRunsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingRunsIT {

	private MockMvc mvc;
	private BeanMapping beanMapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TrainingRunsRestController trainingRunsRestController;

	@Autowired
	private TrainingRunRepository trainingRunRepository;

	@Autowired
	private UserRefRepository userRefRepository;

	@Autowired
	private TrainingDefinitionRepository trainingDefinitionRepository;

	@Autowired
	private TrainingInstanceRepository trainingInstanceRepository;

	@Autowired
	private InfoLevelRepository infoLevelRepository;

	@Mock
	private RestTemplate restTemplate;

	private TrainingRun trainingRun1, trainingRun2;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.mvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		beanMapping = new BeanMappingImpl(new ModelMapper());

		SandboxInstanceRef sIR1 = new SandboxInstanceRef();
		sIR1.setSandboxInstanceRef(1L);
		SandboxInstanceRef sIR2 = new SandboxInstanceRef();
		sIR2.setSandboxInstanceRef(2L);

		InfoLevel iL = new InfoLevel();
		iL.setContent("content");
		iL.setTitle("title");
		iL.setMaxScore(50);
		InfoLevel infoLevel = infoLevelRepository.save(iL);

		UserRef userRef = new UserRef();
		userRef.setUserRefLogin("testDesigner");
		UserRef uR = userRefRepository.save(userRef);

		TDViewGroup tdViewGroup = new TDViewGroup();
		tdViewGroup.setTitle("testGroup");
		tdViewGroup.setDescription("test");
		tdViewGroup.setOrganizers(new HashSet<>(Arrays.asList(uR)));

		TrainingDefinition definition = new TrainingDefinition();
		definition.setTitle("definition");
		definition.setState(TDState.RELEASED);
		definition.setShowStepperBar(true);
		definition.setTdViewGroup(tdViewGroup);
		definition.setSandboxDefinitionRefId(1L);
		TrainingDefinition tD = trainingDefinitionRepository.save(definition);

		TrainingInstance trainingInstance = new TrainingInstance();
		trainingInstance.setStartTime(LocalDateTime.now().plusHours(24));
		trainingInstance.setEndTime(LocalDateTime.now().plusHours(80));
		trainingInstance.setTitle("futureInstance");
		trainingInstance.setPoolSize(20);
		trainingInstance.setAccessToken("pass-1234");
		trainingInstance.setTrainingDefinition(tD);
		trainingInstance.setOrganizers(new HashSet<>(Arrays.asList(uR)));
		TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
		tI.addSandboxInstanceRef(sIR1);
		tI.addSandboxInstanceRef(sIR2);
		tI = trainingInstanceRepository.save(tI);

		List<SandboxInstanceRef> sandboxInstanceRefs = new ArrayList<>();
		sandboxInstanceRefs.addAll(tI.getSandboxInstanceRefs());

		trainingRun1 = new TrainingRun();
		trainingRun1.setStartTime(LocalDateTime.now().plusHours(24));
		trainingRun1.setEndTime(LocalDateTime.now().plusHours(48));
		trainingRun1.setState(TRState.READY);
		trainingRun1.setIncorrectFlagCount(5);
		trainingRun1.setSolutionTaken(false);
		trainingRun1.setCurrentLevel(infoLevel);
		trainingRun1.setTrainingInstance(tI);
		trainingRun1.setSandboxInstanceRef(sandboxInstanceRefs.get(0));
		trainingRun1.setParticipantRef(uR);

		trainingRun2 = new TrainingRun();
		trainingRun2.setStartTime(LocalDateTime.now().plusHours(2));
		trainingRun2.setEndTime(LocalDateTime.now().plusHours(4));
		trainingRun2.setState(TRState.READY);
		trainingRun2.setIncorrectFlagCount(10);
		trainingRun2.setSolutionTaken(false);
		trainingRun2.setCurrentLevel(infoLevel);
		trainingRun2.setTrainingInstance(tI);
		trainingRun2.setSandboxInstanceRef(sandboxInstanceRefs.get(1));
		trainingRun2.setParticipantRef(uR);
	}

	@After
	public void reset() throws SQLException {
		DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_run");
	}

	@Test
	public void findAllTrainingRuns() throws Exception {
		TrainingRun tR1 = trainingRunRepository.save(trainingRun1);
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);

		List<TrainingRun> expected = new ArrayList<>();
		expected.add(tR1);
		expected.add(tR2);
		Page p = new PageImpl<TrainingRun>(expected);

		PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = beanMapping.mapToPageResultDTO(p, TrainingRunDTO.class);
		PageResultResource.Pagination pagination = trainingRunDTOPageResultResource.getPagination();
		pagination.setSize(20);
		trainingRunDTOPageResultResource.setPagination(pagination);

		MockHttpServletResponse result = mvc.perform(get("/training-runs"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
	}

	@Test
	public void findTrainingRunById() throws Exception {
		TrainingRun tR = trainingRunRepository.save(trainingRun1);

		MockHttpServletResponse result = mvc.perform(get("/training-runs/{id}", tR.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		TrainingRunDTO runDTO = beanMapping.mapTo(tR, TrainingRunDTO.class);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(runDTO)), result.getContentAsString());
	}

	@Test
	public void findTrainingRunByIdWithInstanceNotFound() throws Exception {
		Exception ex = mvc.perform(get("/training-runs/{id}", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training Run with runId: 100 not found."));
	}

	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}

}
