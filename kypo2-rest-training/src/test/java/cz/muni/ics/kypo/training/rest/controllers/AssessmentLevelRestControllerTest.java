package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.facade.AssessmentLevelFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
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
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssessmentLevelsRestController.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training")
public class AssessmentLevelRestControllerTest {

		@Autowired
		private AssessmentLevelsRestController assessmentLevelsRestController;

		@MockBean
		private AssessmentLevelFacade assessmentLevelFacade;


		private MockMvc mockMvc;

		@MockBean
		@Qualifier("objMapperRESTApi")
		private ObjectMapper objectMapper;

		@MockBean
		private BeanMapping beanMapping;


		@Autowired
		private WebApplicationContext webApplicationContext;


		private AssessmentLevelDTO al1DTO, al2DTO;

		private AssessmentLevel al1, al2;

		private Page p, pDTO;

		private PageResultResource<AssessmentLevelDTO> assessmentLevelDTOPageResultResource;


		@Before
		public void setup() throws  RuntimeException {
				MockitoAnnotations.initMocks(this);
				this.mockMvc = MockMvcBuilders.standaloneSetup(assessmentLevelsRestController)
						.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(), new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
								Optional.empty()))
						.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

				al1DTO = new AssessmentLevelDTO();
				al1DTO.setId(1L);
				al1DTO.setNextLevel(2L);
				al1DTO.setAssessmentType(AssessmentType.TEST);
				al1DTO.setTitle("Test1");

				al2DTO = new AssessmentLevelDTO();
				al2DTO.setId(2L);
				al2DTO.setNextLevel(3L);
				al2DTO.setAssessmentType(AssessmentType.TEST);
				al2DTO.setTitle("Test2");

				al1 = new AssessmentLevel();
				al1.setId(1L);
				al1.setNextLevel(2L);
				al1.setAssessmentType(AssessmentType.TEST);
				al1.setTitle("Test1");

				al2 = new AssessmentLevel();
				al2.setId(2L);
				al2.setNextLevel(3L);
				al2.setAssessmentType(AssessmentType.TEST);
				al2.setTitle("Test2");

				List<AssessmentLevel> expected = new ArrayList();
				expected.add(al1);
				expected.add(al2);

				p = new PageImpl<AssessmentLevel>(expected);

				List<AssessmentLevelDTO> expectedDTO = new ArrayList();
				expectedDTO.add(al1DTO);
				expectedDTO.add(al2DTO);
				pDTO = new PageImpl<AssessmentLevelDTO>(expectedDTO);

				ObjectMapper obj = new ObjectMapper();
				obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
				given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

				BeanMapping bM = new BeanMappingImpl(new ModelMapper());
				assessmentLevelDTOPageResultResource = bM.mapToPageResultDTO(p, AssessmentLevelDTO.class);



		}

		@Test
		public void findAssessmentLevelById() throws Exception {
				given(assessmentLevelFacade.findById(any(Long.class))).willReturn(al1DTO);
				String valueAs = convertObjectToJsonBytes(al1DTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
				MockHttpServletResponse result = mockMvc.perform(get("/assessment-levels" + "/{id}", 1l))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				System.out.println(result.getContentAsString());
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(al1DTO)), result.getContentAsString());
		}

		@Test
		public void findAssessmentLevelByIdWithFacadeException() throws Exception {
				willThrow(FacadeLayerException.class).given(assessmentLevelFacade).findById(any(Long.class));
				Exception exception = mockMvc.perform(get("/assessment-levels" + "/{id}", 1l))
						.andExpect(status().isNotFound())
						.andReturn().getResolvedException();
				assertEquals(ResourceNotFoundException.class, exception.getClass());
		}

		@Test
		public void findAllAssessmentLevels() throws Exception {
				String valueAs = convertObjectToJsonBytes(assessmentLevelDTOPageResultResource);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
				given(assessmentLevelFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(assessmentLevelDTOPageResultResource);

				MockHttpServletResponse result = mockMvc.perform(get("/assessment-levels"))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(assessmentLevelDTOPageResultResource)), result.getContentAsString());
		}

		@Test
		public void findAllAssessmentLevelsWithFacadeException() throws Exception {
				PageResultResource<AssessmentLevelDTO> assessmentLevelDTOPageResultResource = beanMapping.mapToPageResultDTO(p, AssessmentLevelDTO.class);
				willThrow(FacadeLayerException.class).given(assessmentLevelFacade).findAll(any(Predicate.class),any(Pageable.class));

				Exception exception = mockMvc.perform(get("/assessment-levels"))
						.andExpect(status().isNotFound())
						.andReturn().getResolvedException();
				assertEquals(ResourceNotFoundException.class, exception.getClass());
		}

		private static String convertObjectToJsonBytes(Object object) throws IOException {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(object);

		}

		private static AssessmentLevelDTO convertFromJsonBytes(String object) throws IOException {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(object, AssessmentLevelDTO.class);

		}
}

