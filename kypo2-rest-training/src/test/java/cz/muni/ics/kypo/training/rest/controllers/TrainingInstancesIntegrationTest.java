package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingInstancesRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingInstancesIntegrationTest {

	private MockMvc mvc;
	private BeanMapping beanMapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TrainingInstancesRestController trainingInstancesRestController;

	@Autowired
	private TrainingInstanceRepository trainingInstanceRepository;

	@Autowired
	private TrainingDefinitionRepository trainingDefinitionRepository;

	@Autowired
	private TrainingRunRepository trainingRunRepository;

	@Autowired
	private UserRefRepository userRefRepository;

	@Autowired
	private InfoLevelRepository infoLevelRepository;
/*
	@Autowired
	private SandboxInstanceRefRepository sandboxInstanceRefRepository;
*/
	private TrainingInstance futureTrainingInstance, notConcludedTrainingInstance;
	private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
	private TrainingRun trainingRun1, trainingRun2;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		this.mvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		beanMapping = new BeanMappingImpl(new ModelMapper());

		UserRef userRef = new UserRef();
		userRef.setUserRefLogin("testUser");
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

		futureTrainingInstance = new TrainingInstance();
		futureTrainingInstance.setStartTime(LocalDateTime.now().plusHours(24));
		futureTrainingInstance.setEndTime(LocalDateTime.now().plusHours(80));
		futureTrainingInstance.setTitle("futureInstance");
		futureTrainingInstance.setPoolSize(20);
		futureTrainingInstance.setAccessToken("pass-1234");
		futureTrainingInstance.setTrainingDefinition(tD);
		futureTrainingInstance.setOrganizers(new HashSet<>(Arrays.asList(uR)));

		notConcludedTrainingInstance = new TrainingInstance();
		notConcludedTrainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
		notConcludedTrainingInstance.setEndTime(LocalDateTime.now().plusHours(24));
		notConcludedTrainingInstance.setTitle("NotConcluded");
		notConcludedTrainingInstance.setPoolSize(25);
		notConcludedTrainingInstance.setAccessToken("key-9999");
		notConcludedTrainingInstance.setTrainingDefinition(tD);
		notConcludedTrainingInstance.setOrganizers(new HashSet<>(Arrays.asList(uR)));

		trainingInstanceCreateDTO = new TrainingInstanceCreateDTO();
		trainingInstanceCreateDTO.setStartTime(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
		trainingInstanceCreateDTO.setEndTime(LocalDateTime.now(Clock.systemUTC()).plusHours(80));
		//trainingInstanceCreateDTO.setEndTime((LocalDateTime) "2020-11-20T10:28:02.727Z");
		trainingInstanceCreateDTO.setTrainingDefinitionId(tD.getId());
		trainingInstanceCreateDTO.setTitle("newInstance");
		trainingInstanceCreateDTO.setPoolSize(50);
		trainingInstanceCreateDTO.setOrganizerLogins(new HashSet<>(Arrays.asList("TestUser")));
/*
		ParticipantRef pR = new ParticipantRef();
		pR.setParticipantRefLogin("login");
		ParticipantRef participantRef = participantRefRepository.save(pR);
*/
		SandboxInstanceRef sIR1 = new SandboxInstanceRef();
		sIR1.setSandboxInstanceRef(1L);
		SandboxInstanceRef sIR2 = new SandboxInstanceRef();
		sIR2.setSandboxInstanceRef(2L);
		/*
		SandboxInstanceRef sandboxInstanceRef1 = sandboxInstanceRefRepository.save(sIR1);
		SandboxInstanceRef sandboxInstanceRef2 = sandboxInstanceRefRepository.save(sIR2);
		*/
		InfoLevel iL = new InfoLevel();
		iL.setContent("content");
		iL.setTitle("title");
		iL.setMaxScore(50);
		InfoLevel infoLevel = infoLevelRepository.save(iL);

		trainingRun1 = new TrainingRun();
		trainingRun1.setStartTime(LocalDateTime.now().plusHours(24));
		trainingRun1.setEndTime(LocalDateTime.now().plusHours(48));
		trainingRun1.setState(TRState.READY);
		trainingRun1.setIncorrectFlagCount(5);
		trainingRun1.setSolutionTaken(false);
		trainingRun1.setCurrentLevel(infoLevel);
		trainingRun1.setTrainingInstance(futureTrainingInstance);
		trainingRun1.setSandboxInstanceRef(sIR1);
		trainingRun1.setParticipantRef(uR);

		trainingRun2 = new TrainingRun();
		trainingRun2.setStartTime(LocalDateTime.now().plusHours(2));
		trainingRun2.setEndTime(LocalDateTime.now().plusHours(4));
		trainingRun2.setState(TRState.READY);
		trainingRun2.setIncorrectFlagCount(10);
		trainingRun2.setSolutionTaken(false);
		trainingRun2.setCurrentLevel(infoLevel);
		trainingRun2.setTrainingInstance(futureTrainingInstance);
		trainingRun2.setSandboxInstanceRef(sIR2);
		trainingRun2.setParticipantRef(uR);

	}

	@After
	public void reset() throws SQLException {
		DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_instance");
	}

	@Test
	public void findAllTrainingInstances() throws Exception {
		TrainingInstance tI1 = trainingInstanceRepository.save(notConcludedTrainingInstance);
		TrainingInstance tI2 = trainingInstanceRepository.save(futureTrainingInstance);

		List<TrainingInstance> expected = new ArrayList<>();
		expected.add(tI1);
		expected.add(tI2);
		Page p = new PageImpl<TrainingInstance>(expected);

		PageResultResource<TrainingInstanceDTO>  trainingInstanceDTOPageResultResource = beanMapping.mapToPageResultDTO(p, TrainingInstanceDTO.class);
		PageResultResource.Pagination pagination = trainingInstanceDTOPageResultResource.getPagination();
		pagination.setSize(20);
		trainingInstanceDTOPageResultResource.setPagination(pagination);

		MockHttpServletResponse result = mvc.perform(get("/training-instances"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstanceDTOPageResultResource)), result.getContentAsString());
	}

	@Test
	public void findTrainingInstanceById() throws Exception {
		TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);

		MockHttpServletResponse result = mvc.perform(get("/training-instances/{id}", tI.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		TrainingInstanceDTO instanceDTO = beanMapping.mapTo(tI, TrainingInstanceDTO.class);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(instanceDTO)), result.getContentAsString());
	}

	@Test
	public void findTrainingInstanceByIdWithInstanceNotFound() throws Exception {
		Exception ex = mvc.perform(get("/training-instances/{id}", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training instance with id: 100 not found"));
	}

	@Test
	public void createTrainingInstance() throws Exception {

		System.out.println(trainingInstanceCreateDTO.getStartTime());
		MockHttpServletResponse result = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				//.andExpect(status().isOk())
				//.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		Optional<TrainingInstance> newInstance= trainingInstanceRepository.findById(1L);
		assertTrue(newInstance.isPresent());
		TrainingInstanceDTO newInstanceDTO = beanMapping.mapTo(newInstance.get(), TrainingInstanceDTO.class);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(newInstanceDTO)), result.getContentAsString());
	}


	@Test
	public void deleteTrainingInstance() throws Exception {
		TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);
		System.out.println(tI.getStartTime());
		mvc.perform(delete("/training-instances/{id}", tI.getId()))
				.andExpect(status().isOk());
		Optional<TrainingInstance> optTI = trainingInstanceRepository.findById(tI.getId());
		assertFalse(optTI.isPresent());
	}

	@Test
	public void deleteNonexistentInstance() throws Exception {
		Exception ex = mvc.perform(delete("/training-instances/{id}", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training instance with id: 100, not found"));
	}

	@Test
	public void deleteNotConcludedTrainingInstance() throws Exception {
		TrainingInstance tI = trainingInstanceRepository.save(notConcludedTrainingInstance);
		Exception ex = mvc.perform(delete("/training-instances/{id}", tI.getId()))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Only finished instances can be deleted"));
	}

	@Test
	public void findAllTrainingRunsByTrainingInstanceId() throws Exception {
		TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);
		TrainingRun tR1 = trainingRunRepository.save(trainingRun1);
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);

		List<TrainingRun> expected = new ArrayList<>();
		expected.add(tR1);
		expected.add(tR2);
		Page p = new PageImpl<TrainingRun>(expected);
		PageResultResource<TrainingRunDTO>  trainingRunDTOPageResultResource = beanMapping.mapToPageResultDTO(p, TrainingRunDTO.class);

		PageResultResource.Pagination pagination = trainingRunDTOPageResultResource.getPagination();
		pagination.setSize(20);
		trainingRunDTOPageResultResource.setPagination(pagination);

		MockHttpServletResponse result = mvc.perform(get("/training-instances/{instanceId}/training-runs",tI.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
	}

	@Test
	public void findAllTrainingRunsByTrainingInstanceIdWithNonexistentInstance() throws Exception{
		Exception ex = mvc.perform(get("/training-instances/{instanceId}/training-runs", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training instance with id: 100 not found."));
	}

	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}
}

