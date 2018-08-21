package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.repository.GameLevelRepository;
import cz.muni.ics.kypo.training.service.GameLevelService;
import org.hibernate.HibernateException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.BDDMockito.any;
import static org.mockito.Mockito.reset;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.training.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class GameLevelServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private GameLevelService gameLevelService;

    @MockBean
    private GameLevelRepository gameLevelRepository;

    private GameLevel gameLevel1, gameLevel2;

    @SpringBootApplication
    static class TestConfiguration{
    }

    @Before
    public void init() {
        gameLevel1 = new GameLevel();
        gameLevel1.setId(1L);
        gameLevel1.setMaxScore(20);

        gameLevel2 = new GameLevel();
        gameLevel2.setId(2L);
        gameLevel2.setMaxScore(42);
    }

    @Test
    public void getGameLevelById() {
        given(gameLevelRepository.findById(gameLevel1.getId())).willReturn(Optional.of(gameLevel1));

        GameLevel gL = gameLevelService.findById(gameLevel1.getId()).get();
        deepEquals(gL, gameLevel1);

        then(gameLevelRepository).should().findById(gameLevel1.getId());
    }

    @Test
    public void getGameLevelByIdWithHibernateException() {
        Long id = 1L;
        willThrow(HibernateException.class).given(gameLevelRepository).findById(id);
        thrown.expect(ServiceLayerException.class);
        gameLevelService.findById(id);
    }

    @Test
    public void getNonexistentGameLevelById() {
        Long id = 6L;
        assertEquals(Optional.empty(), gameLevelService.findById(id));
    }

    @Test
    public void findAll() {
        List<GameLevel> expected = new ArrayList<>();
        expected.add(gameLevel1);
        expected.add(gameLevel2);

        Page p = new PageImpl<GameLevel>(expected);
        PathBuilder<GameLevel> gL = new PathBuilder<GameLevel>(GameLevel.class, "gameLevel");
        Predicate predicate = gL.isNotNull();

        given(gameLevelRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = gameLevelService.findAll(predicate, PageRequest.of(0,2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void updateGameLevel() {
        given(gameLevelRepository.findById(gameLevel1.getId())).willReturn(Optional.of(gameLevel1));
        gameLevelService.update(gameLevel1);
        then(gameLevelRepository).should().save(gameLevel1);
    }

    @Test
    public void updateGameLevelWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Game level must not be null");
        gameLevelService.update(null);
    }

    @After
    public void after(){
        reset(gameLevelRepository);
    }

    private void deepEquals(GameLevel expected, GameLevel actual){
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMaxScore(), actual.getMaxScore());

    }

}
