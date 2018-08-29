package cz.muni.ics.kypo.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.InfoLevelFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotFoundException;
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
@SpringBootTest(classes = InfoLevelsRestController.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo"})
public class InfoLevelsRestControllerTest {

    @Autowired
    private InfoLevelsRestController infoLevelsRestController;

    @MockBean
    private InfoLevelFacade infoLevelFacade;

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("objMapperRESTApi")
    private ObjectMapper objectMapper;

    @MockBean
    private BeanMapping beanMapping;

    private InfoLevel infoLevel1, infoLevel2;

    private InfoLevelDTO infoLevel1DTO, infoLevel2DTO;

    private Page p;

    private PageResultResource<InfoLevelDTO> infoLevelDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(infoLevelsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        infoLevel1 = new InfoLevel();
        infoLevel1.setId(1L);
        infoLevel1.setTitle("test1");

        infoLevel2 = new InfoLevel();
        infoLevel2.setId(2L);
        infoLevel2.setTitle("test2");

        infoLevel1DTO = new InfoLevelDTO();
        infoLevel1DTO.setId(1L);
        infoLevel1DTO.setTitle("test1");

        infoLevel2DTO = new InfoLevelDTO();
        infoLevel2DTO.setId(2L);
        infoLevel2DTO.setTitle("test2");

        List<InfoLevel> expected = new ArrayList<>();
        expected.add(infoLevel1);
        expected.add(infoLevel2);

        p = new PageImpl<InfoLevel>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        BeanMapping bM = new BeanMappingImpl(new ModelMapper());
        infoLevelDTOPageResultResource = bM.mapToPageResultDTO(p, InfoLevelDTO.class);
    }

    @Test
    public void findInfoLevelById() throws Exception {
        given(infoLevelFacade.findById(any(Long.class))).willReturn(infoLevel1DTO);
        String valueIl = convertObjectToJsonBytes(infoLevel1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueIl);
        MockHttpServletResponse result = mockMvc.perform(get("/info-levels" + "/{id}", 1l))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(infoLevel1DTO)), result.getContentAsString());
    }

    @Test
    public void findInfoLevelByIdWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(infoLevelFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/info-levels" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void findAllInfoLevels() throws Exception {
        String valueIl = convertObjectToJsonBytes(infoLevelDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueIl);
        given(infoLevelFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(infoLevelDTOPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(get("/info-levels"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(infoLevelDTOPageResultResource)), result.getContentAsString());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
