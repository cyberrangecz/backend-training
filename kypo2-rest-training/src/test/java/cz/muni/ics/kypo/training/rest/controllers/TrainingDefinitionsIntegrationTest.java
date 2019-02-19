package cz.muni.ics.kypo.training.rest.controllers;

import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingDefinitionsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingDefinitionsIntegrationTest {

	@Test
	public void testTest() {
		System.out.println("Works!!!!!!!!!!!!!!!!!!!!!");
	}
}
