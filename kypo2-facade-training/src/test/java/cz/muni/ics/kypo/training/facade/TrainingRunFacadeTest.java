package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.model.enums.LevelType;
import cz.muni.ics.kypo.training.model.enums.TRState;
import cz.muni.ics.kypo.training.service.TrainingRunService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(FacadeConfigTest.class)
public class TrainingRunFacadeTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TrainingRunFacade trainingRunFacade;

	@MockBean
	private TrainingRunService trainingRunService;

    private TrainingRun trainingRun1, trainingRun2;
    private Hint hint;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private AssessmentLevel assessmentLevel;

	@SpringBootApplication
	static class TestConfiguration {
	}


    @Before
    public void init() {
        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.READY);

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
		given(trainingRunService.findById(trainingRun1.getId())).willReturn(trainingRun1);

		TrainingRunDTO trainingRunDTO = trainingRunFacade.findById(trainingRun1.getId());
		deepEquals(trainingRun1, trainingRunDTO);

		then(trainingRunService).should().findById(trainingRun1.getId());
	}

	@Test
	public void isCorrectFlagBeforeSolutionTaken() {
		given(trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(true);
		given(trainingRunService.getRemainingAttempts(trainingRun1.getId())).willReturn(2);
		IsCorrectFlagDTO correctFlagDTO = trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag", false);
		assertEquals(true, correctFlagDTO.isCorrect());
		assertEquals(1, correctFlagDTO.getRemainingAttempts());
	}

	@Test
	public void isCorrectFlagAfterSolutionTaken() {
		given(trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(false);
		IsCorrectFlagDTO correctFlagDTO = trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag", true);
		assertEquals(false, correctFlagDTO.isCorrect());
		assertEquals(0, correctFlagDTO.getRemainingAttempts());
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
    public void accessTrainingRun() {
        given(trainingRunService.accessTrainingRun("password")).willReturn(gameLevel);
        given(trainingRunService.getLevels(1L)).willReturn(Arrays.asList(gameLevel,infoLevel,assessmentLevel));
        AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun("password");
        assertEquals(gameLevel.getId(), ((GameLevelDTO) accessTrainingRunDTO.getAbstractLevelDTO()).getId());
        assertEquals(gameLevel.getFlag(), ((GameLevelDTO) accessTrainingRunDTO.getAbstractLevelDTO()).getFlag());
        assertEquals(3, accessTrainingRunDTO.getInfoAboutLevels().size());
        assertEquals(LevelType.GAME, accessTrainingRunDTO.getInfoAboutLevels().get(0).getLevelType());
        assertEquals(LevelType.INFO, accessTrainingRunDTO.getInfoAboutLevels().get(1).getLevelType());
        assertEquals(LevelType.ASSESSMENT, accessTrainingRunDTO.getInfoAboutLevels().get(2).getLevelType());

    }

    @Test
    public void getNextLevelAssessment() {
        given(trainingRunService.getNextLevel(trainingRun1.getId())).willReturn((AbstractLevel) assessmentLevel);
        AbstractLevelDTO assessmentLevelDTO = trainingRunFacade.getNextLevel(trainingRun1.getId());
        assertEquals(assessmentLevel.getId(), ((AssessmentLevelDTO) assessmentLevelDTO).getId()) ;
        assertEquals(assessmentLevel.getAssessmentType(), ((AssessmentLevelDTO) assessmentLevelDTO).getAssessmentType()) ;
        assertEquals(assessmentLevel.getInstructions(), ((AssessmentLevelDTO) assessmentLevelDTO).getInstructions()) ;

    }
    @Test
    public void getNextLevelInfo() {
        given(trainingRunService.getNextLevel(trainingRun1.getId())).willReturn((AbstractLevel) infoLevel);
        AbstractLevelDTO infoLevelDTO = trainingRunFacade.getNextLevel(trainingRun1.getId());
        assertEquals(infoLevel.getId(), ((InfoLevelDTO) infoLevelDTO).getId()) ;
        assertEquals(infoLevel.getContent(), ((InfoLevelDTO) infoLevelDTO).getContent()) ;
    }

    @Test
    public void getNextLevelGame() {
        given(trainingRunService.getNextLevel(trainingRun1.getId())).willReturn((AbstractLevel) gameLevel);
        AbstractLevelDTO gameLevelDTO = trainingRunFacade.getNextLevel(trainingRun1.getId());
        assertEquals(gameLevel.getId(), ((GameLevelDTO) gameLevelDTO).getId()) ;
        assertEquals(gameLevel.getFlag(), ((GameLevelDTO) gameLevelDTO).getFlag()) ;
        assertEquals(gameLevel.getContent(), ((GameLevelDTO) gameLevelDTO).getContent());

    }
    @Test
    public void getHint() {
        given(trainingRunService.getHint(anyLong(),anyLong())).willReturn(hint);
        HintDTO hintDTO = trainingRunFacade.getHint(1L,1L);
        assertEquals(hint.getId(), hintDTO.getId());
        assertEquals(hint.getContent(), hintDTO.getContent());
        assertEquals(hint.getTitle(), hintDTO.getTitle());
    }

    @Test
    public void getSolution() {
        given(trainingRunService.getSolution(trainingRun1.getId())).willReturn("game solution");
        String solution = trainingRunFacade.getSolution(trainingRun1.getId());
        assertEquals("game solution", solution);


    }
    private void deepEquals(TrainingRun expected, TrainingRunDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());

		}

}

