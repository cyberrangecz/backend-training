package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.impl.TrainingRunFacadeImpl;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
public class TrainingRunFacadeTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private TrainingRunFacade trainingRunFacade;

	@Mock
	private TrainingRunService trainingRunService;

    private TrainingRun trainingRun1, trainingRun2;
    private Hint hint;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private AssessmentLevel assessmentLevel;

    @Before
    public void init() {
    	MockitoAnnotations.initMocks(this);
    	trainingRunFacade = new TrainingRunFacadeImpl(trainingRunService, new BeanMappingImpl(new ModelMapper()));

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.READY);
        trainingRun1.setSolutionTaken(false);

        hint = new Hint();
        hint.setId(1L);
        hint.setContent("Hint");
        hint.setTitle("Hint Title");

        gameLevel = new GameLevel();
        gameLevel.setId(1L);
        gameLevel.setFlag("game flag");
        gameLevel.setContent("game content");

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(2L);
        assessmentLevel.setInstructions("Instructions");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);
        infoLevel.setContent("content");

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.ARCHIVED);
    }

	@Test
	public void findTrainingRunById() {
		given(trainingRunService.findById(any(Long.class))).willReturn(trainingRun1);
		trainingRunFacade.findById(trainingRun1.getId());
		then(trainingRunService).should().findById(trainingRun1.getId());
	}

	@Test
	public void findTrainingRunByIdWithFacadeLayerException() {
		thrown.expect(FacadeLayerException.class);
		willThrow(ServiceLayerException.class).given(trainingRunService).findById(1L);
		trainingRunFacade.findById(1L);
	}

	@Test
	public void findAllTrainingRuns() {
		List<TrainingRun> expected = new ArrayList<>();
		expected.add(trainingRun1);
		expected.add(trainingRun2);

		Page<TrainingRun> p = new PageImpl<TrainingRun>(expected);

		PathBuilder<TrainingRun> tR = new PathBuilder<TrainingRun>(TrainingRun.class, "trainingRun");
		Predicate predicate = tR.isNotNull();

		given(trainingRunService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);
	}

	@Test
	public void isCorrectFlagBeforeSolutionTaken() {
		given(trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(true);
		given(trainingRunService.getRemainingAttempts(trainingRun1.getId())).willReturn(1);
		IsCorrectFlagDTO correctFlagDTO = trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag");
		assertEquals(true, correctFlagDTO.isCorrect());
		assertEquals(1, correctFlagDTO.getRemainingAttempts());
	}

	@Test
	public void isCorrectFlagAfterSolutionTaken() {
		given(trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(false);
		IsCorrectFlagDTO correctFlagDTO = trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag");
		assertEquals(false, correctFlagDTO.isCorrect());
		assertEquals(0, correctFlagDTO.getRemainingAttempts());
	}

    @Test
    public void accessTrainingRun() {
        given(trainingRunService.accessTrainingRun("password")).willReturn(gameLevel);
        given(trainingRunService.getLevels(1L)).willReturn(Arrays.asList(gameLevel,infoLevel,assessmentLevel));
        Object result = trainingRunFacade.accessTrainingRun("password");
        assertEquals(AccessTrainingRunDTO.class, result.getClass());
        then(trainingRunService).should().accessTrainingRun("password");
    }

    @Test
		public void accessTrainingRunWithFacadeLayerException(){
			thrown.expect(FacadeLayerException.class);
			willThrow(ServiceLayerException.class).given(trainingRunService).accessTrainingRun("pass");
			trainingRunFacade.accessTrainingRun("pass");
		}

		@Test
		public void getNextLevel(){
			given(trainingRunService.getNextLevel(1L)).willReturn((AbstractLevel) assessmentLevel);
			trainingRunFacade.getNextLevel(1L);
			then(trainingRunService).should().getNextLevel(1L);
		}

		@Test
		public void getNextLevelWithFacadeLayerException(){
			thrown.expect(FacadeLayerException.class);
			willThrow(ServiceLayerException.class).given(trainingRunService).getNextLevel(1L);
			trainingRunFacade.getNextLevel(1L);
		}

    @Test
    public void getHint() {
			given(trainingRunService.getHint(anyLong(),anyLong())).willReturn(hint);
      trainingRunFacade.getHint(1L,1L);
			then(trainingRunService).should().getHint(1L,1L);
		}

		@Test
		public void getHintWithFacadeLayerException() {
			thrown.expect(FacadeLayerException.class);
			willThrow(ServiceLayerException.class).given(trainingRunService).getHint(1L,1L);
			trainingRunFacade.getHint(1L,1L);
		}

    @Test
    public void getSolution() {
        given(trainingRunService.getSolution(1L)).willReturn("game solution");
        trainingRunFacade.getSolution(trainingRun1.getId());
        then(trainingRunService).should().getSolution(1L);
    }

    @Test
		public void getSolutionWithFacadeLayerException() {
			thrown.expect(FacadeLayerException.class);
			willThrow(ServiceLayerException.class).given(trainingRunService).getSolution(1L);
			trainingRunFacade.getSolution(1L);
		}

    private void deepEquals(TrainingRun expected, TrainingRunDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());
		}

}

