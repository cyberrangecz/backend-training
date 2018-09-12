package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

import cz.muni.ics.kypo.training.config.ServiceTrainingConfigTest;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.hibernate.HibernateException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ServiceTrainingConfigTest.class)
public class TrainingInstanceServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TrainingInstanceService trainingInstanceService;
	@MockBean
	private TrainingInstanceRepository trainingInstanceRepository;
	@MockBean
	private RestTemplate restTemplate;

	private TrainingInstance trainingInstance1, trainingInstance2;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {

		trainingInstance1 = new TrainingInstance();
		trainingInstance1.setId(1L);
		// trainingInstance1.setKeyword("test1");
		trainingInstance1.setTitle("test1");

		trainingInstance2 = new TrainingInstance();
		trainingInstance2.setId(1L);
		// trainingInstance2.setKeyword("test2");
		trainingInstance2.setTitle("test2");
	}

	@Test
	public void getTrainingInstanceById() {
		given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

		TrainingInstance tI = trainingInstanceService.findById(trainingInstance1.getId()).get();
		deepEquals(trainingInstance1, tI);

		then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
	}

	@Test
	public void getTrainingInstanceByIdWithHibernateException() {
		long id = 6L;
		willThrow(HibernateException.class).given(trainingInstanceRepository).findById(id);
		thrown.expect(ServiceLayerException.class);
		trainingInstanceService.findById(id);
	}

	@Test
	public void getNonexistentTrainingInstanceById() {
		Long id = 6L;
		assertEquals(Optional.empty(), trainingInstanceService.findById(id));
	}

	@Test
	public void findAll() {
		List<TrainingInstance> expected = new ArrayList<>();
		expected.add(trainingInstance1);
		expected.add(trainingInstance2);

		Page<TrainingInstance> p = new PageImpl<TrainingInstance>(expected);
		PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
		Predicate predicate = tI.isNotNull();

		given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

		Page<TrainingInstance> pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2));
		assertEquals(2, pr.getTotalElements());
	}

	@Test
	public void createTrainingInstance() {
		given(trainingInstanceRepository.save(trainingInstance1)).willReturn(trainingInstance1);
		TrainingInstance tI = trainingInstanceService.create(trainingInstance1).get();
		deepEquals(trainingInstance1, tI);
		then(trainingInstanceRepository).should().save(trainingInstance1);
	}

	@Test
	public void createTrainingInstanceWithNull() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Input training instance must not be null");
		trainingInstanceService.create(null);
	}

	/**
	 * @Test public void updateTrainingInstance(){
	 *       given(trainingInstanceRepository.saveAndFlush(trainingInstance1)).willReturn(trainingInstance1);
	 *       TrainingInstance tI = trainingInstanceService.update(trainingInstance1).get();
	 *       deepEquals(trainingInstance1, tI); }
	 * 
	 * @Test public void updateTrainingInstanceWithNull(){
	 *       thrown.expect(IllegalArgumentException.class); thrown.expectMessage("Input training
	 *       instance must not be null"); trainingInstanceService.update(null); }
	 **/
	@Test
	public void deleteTrainingInstaceWithNull() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Input training instance id" + " must not be null");
		trainingInstanceService.delete(null);
	}

	@After
	public void after() {
		reset(trainingInstanceRepository);
	}

	private void deepEquals(TrainingInstance expected, TrainingInstance actual) {
		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getPassword(), actual.getPassword());
		assertEquals(expected.getTitle(), actual.getTitle());
	}

}
