package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.visualization.*;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.AssessmentLevelMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.GameLevelMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.HintMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.InfoLevelMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.VisualizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class VisualizationFacadeImpl implements VisualizationFacade {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationFacadeImpl.class);

    private TrainingRunService trainingRunService;
    private VisualizationService visualizationService;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private HintMapper hintMapper;

    @Autowired
    public VisualizationFacadeImpl(TrainingRunService trainingRunService, VisualizationService visualizationService,
                                 HintMapper hintMapper, GameLevelMapper gameLevelMapper, InfoLevelMapper infoLevelMapper,
                                   AssessmentLevelMapper assessmentLevelMapper) {
        this.trainingRunService = trainingRunService;
        this.visualizationService = visualizationService;
        this.gameLevelMapper = gameLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.hintMapper = hintMapper;
    }

    @Override
    @TransactionalWO
    public VisualizationInfoAboutTrainingRunDTO getVisualizationInfoAboutTrainingRun(Long trainingRunId) {
        try {
            TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
            TrainingDefinition trainingDefinitionOfTrainingRun = trainingRun.getTrainingInstance().getTrainingDefinition();
            return new VisualizationInfoAboutTrainingRunDTO(trainingDefinitionOfTrainingRun.getId(), trainingDefinitionOfTrainingRun.getTitle(),
                    trainingDefinitionOfTrainingRun.getEstimatedDuration(), gatherVisualizationLevelInfo(trainingRun));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private List<AbstractLevelVisualizationDTO> gatherVisualizationLevelInfo(TrainingRun trainingRun) {
        List<AbstractLevel> levels = visualizationService.getLevelsForVisualization(trainingRun);
        List<AbstractLevelVisualizationDTO> visualizationLevelInfoDTOs = new ArrayList<>();

        levels.forEach(level -> {
            if(level instanceof GameLevel) {
                GameLevelVisualizationDTO gameLevelVisualizationDTO = gameLevelMapper.mapToVisualizationGameLevelDTO((GameLevel) level);
                gameLevelVisualizationDTO.setHints(hintMapper.mapToListDTO(((GameLevel) level).getHints()));
                gameLevelVisualizationDTO.setLevelType(LevelType.GAME_LEVEL);
                visualizationLevelInfoDTOs.add(gameLevelVisualizationDTO);

            } else if (level instanceof AssessmentLevel) {
                AssessmentLevelVisualizationDTO assessmentLevelVisualizationDTO = assessmentLevelMapper.mapToVisualizationAssessmentLevelDTO((AssessmentLevel) level);
                assessmentLevelVisualizationDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
                visualizationLevelInfoDTOs.add(assessmentLevelVisualizationDTO);
            } else if (level instanceof InfoLevel) {
                InfoLevelVisualizationDTO infoLevelVisualizationDTO = infoLevelMapper.mapToVisualizationInfoLevelDTO((InfoLevel) level);
                infoLevelVisualizationDTO.setLevelType(LevelType.INFO_LEVEL);
                visualizationLevelInfoDTOs.add(infoLevelVisualizationDTO);
            } else {
                throw new ServiceLayerException("Level with id: " + level.getId() + " in given training definition with id: " + trainingRun.getTrainingInstance().getTrainingDefinition().getId() +
                        " is not instance of assessment, game or info level.", ErrorCode.UNEXPECTED_ERROR);
            }
        });
        return visualizationLevelInfoDTOs;
    }
}
