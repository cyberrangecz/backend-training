package cz.muni.ics.kypo.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.config.FacadeTestConfiguration;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.AssessmentLevel;
import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.model.enums.TDState;
import cz.muni.ics.kypo.service.TrainingDefinitionService;
import org.junit.Assert;
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

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.facade", "cz.muni.ics.kypo.service", "cz.muni.ics.kypo.mapping"})
@Import(FacadeTestConfiguration.class)
public class TrainingDefinitionFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TrainingDefinitionFacade trainingDefinitionFacade;

    @MockBean
    private TrainingDefinitionService trainingDefinitionService;

    private TrainingDefinition trainingDefinition1, trainingDefinition2, unreleasedDefinition, releasedDefinition;

    private AssessmentLevel level1;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        level1 = new AssessmentLevel();
        level1.setId(1L);

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.RELEASED);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.ARCHIVED);

        unreleasedDefinition = new TrainingDefinition();
        unreleasedDefinition.setId(4L);
        unreleasedDefinition.setState(TDState.UNRELEASED);
        unreleasedDefinition.setStartingLevel(level1.getId());

        releasedDefinition = new TrainingDefinition();
        releasedDefinition.setState(TDState.RELEASED);
        releasedDefinition.setId(5L);

    }

    @Test
    public void findTrainingDefinitionById() {
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));

        TrainingDefinitionDTO trainingDefinitionDTO = trainingDefinitionFacade.findById(trainingDefinition1.getId());
        deepEquals(trainingDefinition1, trainingDefinitionDTO);

        then(trainingDefinitionService).should().findById(trainingDefinition1.getId());
    }

    @Test
    public void findNonexistentTrainingDefinitionById() {
        Long id = 6L;
        given(trainingDefinitionService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.findById(id);
    }

    @Test
    public void findAllTrainingDefinitions() {
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        Page p = new PageImpl<TrainingDefinition>(expected);
        PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingDefinition");
        Predicate predicate = tD.isNotNull();

        given(trainingDefinitionService.findAll(any(Predicate.class), any (Pageable.class))).willReturn(p);

        PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTO = trainingDefinitionFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(trainingDefinition1, trainingDefinitionDTO.getContent().get(0));
        deepEquals(trainingDefinition2, trainingDefinitionDTO.getContent().get(1));

        then(trainingDefinitionService).should().findAll(predicate, PageRequest.of(0,2));
    }

    @Test
    public void updateTrainingDefinition() {
        trainingDefinitionFacade.update(unreleasedDefinition);
        then(trainingDefinitionService).should().update(unreleasedDefinition);
    }

    @Test
    public void udateTrainingDefinitionWithNull() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.update(null);
    }

    @Test
    public void cloneTrainingDefinition() {
        TrainingDefinition clonedDefinition = new TrainingDefinition();
        clonedDefinition.setId(3L);
        clonedDefinition.setState(TDState.UNRELEASED);
        clonedDefinition.setTitle("Clone of " + trainingDefinition1.getTitle());

        given(trainingDefinitionService.clone(trainingDefinition1.getId())).willReturn(Optional.of(clonedDefinition));

        TrainingDefinitionDTO newClone = trainingDefinitionFacade.clone(trainingDefinition1.getId());
        assertEquals("Clone of " + trainingDefinition1.getTitle(), newClone.getTitle());
        assertNotEquals(trainingDefinition1.getState(), newClone.getState());
        assertNotEquals(trainingDefinition1.getId(), newClone.getId());

        then(trainingDefinitionService).should().clone(trainingDefinition1.getId());
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.clone(null);
    }

    @Test
    public void swapLeft() {
        trainingDefinitionFacade.swapLeft(unreleasedDefinition.getId(),level1.getId());
        then(trainingDefinitionService).should().swapLeft(unreleasedDefinition.getId(), level1.getId());
    }

    @Test
    public void swapLeftWithNullDefinition() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapLeft(null, level1.getId());
    }

    @Test
    public void swapLeftWithNullLevel() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapLeft(releasedDefinition.getId(), null);
    }


    @Test
    public void swapRight() {
        trainingDefinitionFacade.swapRight(unreleasedDefinition.getId(),level1.getId());
        then(trainingDefinitionService).should().swapRight(unreleasedDefinition.getId(), level1.getId());
    }

    @Test
    public void swapRightWithNullDefinition() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapRight(null, level1.getId());
    }

    @Test
    public void swapRightWithNullLevel() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapRight(releasedDefinition.getId(), null);
    }

    @Test
    public void deleteTrainingDefinition() {
        trainingDefinitionFacade.delete(trainingDefinition1.getId());
        then(trainingDefinitionService).should().delete(trainingDefinition1.getId());
    }

    @Test
    public void deleteTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.delete(null);
    }

    @Test
    public void deleteOneLevel(){
        trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), level1.getId());
        then(trainingDefinitionService).should().deleteOneLevel(trainingDefinition1.getId(), level1.getId());
    }

    @Test
    public void deleteOneLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.deleteOneLevel(null, level1.getId());
    }

    @Test
    public void deleteOneLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), null);
    }

    @Test
    public void updateLevel() {
        trainingDefinitionFacade.updateLevel(unreleasedDefinition.getId(), level1);
        then(trainingDefinitionService).should().updateLevel(unreleasedDefinition.getId(), level1);
    }

    @Test
    public void updateLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateLevel(null, level1);
    }

    @Test
    public void updateLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateLevel(trainingDefinition1.getId(), null);
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());
    }

}
