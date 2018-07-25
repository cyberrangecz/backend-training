package cz.muni.ics.kypo.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.GameLevelDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.GameLevelFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.model.GameLevel;
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
@SpringBootTest(classes = GameLevelsRestController.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo"})
public class GameLevelsRestControllerTest {

    @Autowired
    private GameLevelsRestController gameLevelsRestController;

    @MockBean
    private GameLevelFacade gameLevelFacade;

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("objMapperRESTApi")
    private ObjectMapper objectMapper;

    private GameLevel gameLevel1, gameLevel2;

    private GameLevelDTO gameLevel1DTO, gameLevel2DTO;

    private Page p;

    private PageResultResource<GameLevelDTO> gameLevelDTOPageResultResource;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(gameLevelsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        gameLevel1 = new GameLevel();
        gameLevel1.setId(1L);
        gameLevel1.setSolution("test1");

        gameLevel2 = new GameLevel();
        gameLevel2.setId(2L);
        gameLevel2.setSolution("test2");

        gameLevel1DTO = new GameLevelDTO();
        gameLevel1DTO.setId(1L);
        gameLevel1DTO.setSolution("test1");

        gameLevel2DTO = new GameLevelDTO();
        gameLevel2DTO.setId(2L);
        gameLevel2DTO.setSolution("test2");

        List<GameLevel> expected = new ArrayList<>();
        expected.add(gameLevel1);
        expected.add(gameLevel2);

        p = new PageImpl<GameLevel>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        BeanMapping bM = new BeanMappingImpl(new ModelMapper());
        gameLevelDTOPageResultResource = bM.mapToPageResultDTO(p, GameLevelDTO.class);
    }

    @Test
    public void findGameLevelById() throws Exception {
        given(gameLevelFacade.findById(any(Long.class))).willReturn(gameLevel1DTO);
        String valueGl = convertObjectToJsonBytes(gameLevel1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueGl);
        MockHttpServletResponse result = mockMvc.perform(get("/game-levels" + "/{id}", 1l))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(gameLevel1DTO)), result.getContentAsString());
    }

    @Test
    public void findGameLevelByIdWithFacadeException() throws Exception {
        willThrow(FacadeLayerException.class).given(gameLevelFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/game-levels" + "/{id}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void findAllGameLevels() throws Exception {
        String valueGl = convertObjectToJsonBytes(gameLevelDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueGl);
        given(gameLevelFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(gameLevelDTOPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(get("/game-levels"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(gameLevelDTOPageResultResource)), result.getContentAsString());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}
