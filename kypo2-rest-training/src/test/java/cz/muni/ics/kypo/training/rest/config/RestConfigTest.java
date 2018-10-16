package cz.muni.ics.kypo.training.rest.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

/**
 * @author Simon Hasak (456221)
 */
@Configuration()
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model", basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.rest.controllers")
public class RestConfigTest {

//	public MockMvc createMock(Class c) {
//		return MockMvcBuilders.standaloneSetup(c)
//			.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
//			 new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
//			.setMessageConverters(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()).build();
//	}

}
