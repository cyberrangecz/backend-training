package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.GameLevelDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.service.GameLevelService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(FacadeConfigTest.class)
public class GameLevelFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private GameLevelFacade gameLevelFacade;

    @MockBean
    private GameLevelService gameLevelService;

    private GameLevel gameLevel1, gameLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        gameLevel1 = new GameLevel();
        gameLevel1.setId(1L);
        gameLevel1.setSolution("test1");

        gameLevel2 = new GameLevel();
        gameLevel2.setId(2L);
        gameLevel2.setSolution("test2");
    }

    @Test
    public void findGameLevelById() {
        given(gameLevelService.findById(gameLevel1.getId())).willReturn(Optional.of(gameLevel1));

        GameLevelDTO gameLevelDTO = gameLevelFacade.findById(gameLevel1.getId());
        deepEquals(gameLevel1, gameLevelDTO);

        then(gameLevelService).should().findById(gameLevel1.getId());
    }

    @Test
    public void findNonexistentGameLevelById() {
        Long id = 6L;
        given(gameLevelService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        gameLevelFacade.findById(id);
    }

    @Test
    public void findAllGameLevels() {
        List<GameLevel> expected = new ArrayList<>();
        expected.add(gameLevel1);
        expected.add(gameLevel2);

        Page<GameLevel> p = new PageImpl<GameLevel>(expected);
        PathBuilder<GameLevel> gL = new PathBuilder<GameLevel>(GameLevel.class, "gameLevel");
        Predicate predicate = gL.isNotNull();

        given(gameLevelService.findAll(any(Predicate.class), any (Pageable.class))).willReturn(p);

        PageResultResource<GameLevelDTO> gameLevelDTO = gameLevelFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(gameLevel1, gameLevelDTO.getContent().get(0));
        deepEquals(gameLevel2, gameLevelDTO.getContent().get(1));

        then(gameLevelService).should().findAll(predicate, PageRequest.of(0,2));

    }

    private void deepEquals(GameLevel expected, GameLevelDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getSolution(), actual.getSolution());
    }
}
