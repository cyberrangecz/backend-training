package cz.muni.ics.kypo.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.repository.InfoLevelRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.reset;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo"})
public class InfoLevelServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private InfoLevelService infoLevelService;

    @MockBean
    private InfoLevelRepository infoLevelRepository;

    private InfoLevel infoLevel1, infoLevel2;

    @SpringBootApplication
    static class TestConfiguration{
    }

    @Before
    public void init() {
        infoLevel1 = new InfoLevel();
        infoLevel1.setId(1L);
        infoLevel1.setContent("test");

        infoLevel2 = new InfoLevel();
        infoLevel2.setId(2L);
        infoLevel2.setContent("test1");
    }

    @Test
    public void getInfoLevelById() {
        given(infoLevelRepository.findById(infoLevel1.getId())).willReturn(Optional.of(infoLevel1));

        InfoLevel iL = infoLevelService.findById(infoLevel1.getId()).get();
        deepEquals(iL, infoLevel1);

        then(infoLevelRepository).should().findById(infoLevel1.getId());
    }

    @Test
    public void getInfoLevelByIdWithHibernateException() {
        Long id = 1L;
        willThrow(HibernateException.class).given(infoLevelRepository).findById(id);
        thrown.expect(ServiceLayerException.class);
        infoLevelService.findById(id);
    }

    @Test
    public void getNonexistentInfoLevelById() {
        Long id = 6L;
        assertEquals(Optional.empty(), infoLevelService.findById(id));
    }

    @Test
    public void findAll() {
        List<InfoLevel> expected = new ArrayList<>();
        expected.add(infoLevel1);
        expected.add(infoLevel2);

        Page p = new PageImpl<InfoLevel>(expected);
        PathBuilder<InfoLevel> iL = new PathBuilder<InfoLevel>(InfoLevel.class, "infoLevel");
        Predicate predicate = iL.isNotNull();

        given(infoLevelRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = infoLevelService.findAll(predicate, PageRequest.of(0,2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void updateInfoLevel() {
        given(infoLevelRepository.findById(infoLevel1.getId())).willReturn(Optional.of(infoLevel1));
        infoLevelService.update(infoLevel1);
        then(infoLevelRepository).should().save(infoLevel1);
    }

    @Test
    public void updateInfoLevelWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Info level must not be null");
        infoLevelService.update(null);
    }

    @After
    public void after(){
        reset(infoLevelRepository);
    }

    private void deepEquals(InfoLevel expected, InfoLevel actual){
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getContent(), actual.getContent());

    }

}
