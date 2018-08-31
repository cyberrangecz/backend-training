package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.training.config.FacadeTestConfiguration;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.service.InfoLevelService;
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
import org.springframework.context.annotation.Import;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.training.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.facade", "cz.muni.ics.kypo.training.service", "cz.muni.ics.kypo.training.mapping"})
@Import(FacadeTestConfiguration.class)
public class InfoLevelFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private InfoLevelFacade infoLevelFacade;

    @MockBean
    private InfoLevelService infoLevelService;

    private InfoLevel infoLevel1, infoLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        infoLevel1 = new InfoLevel();
        infoLevel1.setId(1L);
        infoLevel1.setTitle("Test1");

        infoLevel2 = new InfoLevel();
        infoLevel2.setId(2L);
        infoLevel2.setTitle("Test2");
    }

    @Test
    public void findInfoLevelById() {
        given(infoLevelService.findById(infoLevel1.getId())).willReturn(Optional.of(infoLevel1));

        InfoLevelDTO infoLevelDTO = infoLevelFacade.findById(infoLevel1.getId());
        deepEquals(infoLevel1, infoLevelDTO);

        then(infoLevelService).should().findById(infoLevel1.getId());
    }

    @Test
    public void findNonexistentInfoLevelById() {
        Long id = 6L;
        given(infoLevelService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        infoLevelFacade.findById(id);
    }

    @Test
    public void findAllInfoLevels() {
        List<InfoLevel> expected = new ArrayList<>();
        expected.add(infoLevel1);
        expected.add(infoLevel2);

        Page p = new PageImpl<InfoLevel>(expected);
        PathBuilder<InfoLevel> iL = new PathBuilder<InfoLevel>(InfoLevel.class, "infoLevel");
        Predicate predicate = iL.isNotNull();

        given(infoLevelService.findAll(any(Predicate.class), any (Pageable.class))).willReturn(p);

        PageResultResource<InfoLevelDTO> infoLevelDTO = infoLevelFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(infoLevel1, infoLevelDTO.getContent().get(0));
        deepEquals(infoLevel2, infoLevelDTO.getContent().get(1));

        then(infoLevelService).should().findAll(predicate, PageRequest.of(0,2));

    }

    private void deepEquals(InfoLevel expected, InfoLevelDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

}
