package cz.muni.ics.kypo.training.rest.controllers;

import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingDefinitionsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingDefinitionsIntegrationTest {

	private MockMvc mvc;
	private BeanMapping beanMapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TrainingDefinitionsRestController trainingDefinitionsRestController;


	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		this.mvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		beanMapping = new BeanMappingImpl(new ModelMapper());

	}

	@Test
	public void testTest() {
		System.out.println("Works!!!!!!!!!!!!!!!!!!!!!");
	}
}
