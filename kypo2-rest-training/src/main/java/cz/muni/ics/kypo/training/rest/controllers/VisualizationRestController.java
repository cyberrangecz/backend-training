package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.ClusteringVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.leveltabs.LevelTabsLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.timeline.TimelineDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.VisualizationProgressDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.facade.VisualizationFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rest controller for Visualizations.
 */
@Api(value = "/visualizations",
        tags = "Visualizations",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/visualizations", produces = MediaType.APPLICATION_JSON_VALUE)
public class VisualizationRestController {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationRestController.class);

    private VisualizationFacade visualizationFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Visualization rest controller.
     *
     * @param visualizationFacade the visualization facade
     * @param objectMapper        the object mapper
     */
    @Autowired
    public VisualizationRestController(VisualizationFacade visualizationFacade,
                                       ObjectMapper objectMapper) {
        this.visualizationFacade = visualizationFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Gather all necessary information about levels of given training run to visualize results of the training run.
     *
     * @param runId id of training run.
     * @return necessary info about levels for specific training run and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training run.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoDTO> gatherVisualizationInfoForTrainingRun(@ApiParam(value = "Training run ID", required = true)
                                                                                      @PathVariable("runId") Long runId) {
        VisualizationInfoDTO visualizationInfoAboutTrainingRunDTO = visualizationFacade.getVisualizationInfoAboutTrainingRun(runId);
        return ResponseEntity.ok(visualizationInfoAboutTrainingRunDTO);
    }

    /**
     * Gather all commands in a training run.
     *
     * @param runId id of training run.
     * @return the list of commands including its timestamp.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all commands in a training run.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherAllCommandsInTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Commands in training run.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/training-runs/{runId}/commands", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> gatherAllCommandsInTrainingRun(@ApiParam(value = "Training instance ID", required = true)
                                                                                    @PathVariable("instanceId") Long instanceId,
                                                                                    @ApiParam(value = "Training run ID", required = true)
                                                                                    @PathVariable("runId") Long runId) {
        return ResponseEntity.ok(visualizationFacade.getAllCommandsInTrainingRun(instanceId, runId));
    }

    /**
     * Gather all necessary information about levels of given training instance to visualize results of the training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about levels for specific training instance and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training instance.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoDTO> gatherVisualizationInfoForTrainingInstance(@ApiParam(value = "Training instance ID", required = true)
                                                                                           @PathVariable("instanceId") Long trainingInstanceId) {
        VisualizationInfoDTO visualizationInfoDTO = visualizationFacade.getVisualizationInfoAboutTrainingInstance(trainingInstanceId);
        return ResponseEntity.ok(visualizationInfoDTO);
    }


    /**
     * Gather all necessary information about participants of the given training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about participants specific training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary info about participants for specific training instance.",
            response = UserRefDTO[].class,
            nickname = "getParticipantsForGivenTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = UserRefDTO[].class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRefDTO>> getParticipantsForGivenTrainingInstance(@ApiParam(value = "Training instance ID", required = true)
                                                                                    @PathVariable("instanceId") Long trainingInstanceId) {
        List<UserRefDTO> participants = visualizationFacade.getParticipantsForGivenTrainingInstance(trainingInstanceId);
        return ResponseEntity.ok(participants);
    }

    /**
     * Gather all necessary information about users with given ids.
     *
     * @param pageable the pageable
     * @param usersIds ids of users to retrieve.
     * @return necessary info about participants specific training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get users by IDs.",
            response = TrainingDefinitionsRestController.UserInfoRestResource.class,
            nickname = "getUsersByIds",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = TrainingDefinitionsRestController.UserInfoRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersByIds(Pageable pageable,
                                                @ApiParam(value = "usersIds", required = true)
                                                @RequestParam Set<Long> usersIds) {
        PageResultResource<UserRefDTO> visualizationInfoDTO = visualizationFacade.getUsersByIds(usersIds, pageable);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, visualizationInfoDTO));
    }

    /**
     * Get data for clustering visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for clustering visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training instance.",
            response = ClusteringVisualizationDTO.class,
            nickname = "getClusteringVisualizations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = ClusteringVisualizationDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/clustering", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusteringVisualizationDTO> getClusteringVisualizations(@ApiParam(value = "Training instance ID", required = true)
                                                                                  @PathVariable("instanceId") Long trainingInstanceId) {
        ClusteringVisualizationDTO clusteringVisualizationDTO = visualizationFacade.getClusteringVisualizations(trainingInstanceId);
        return ResponseEntity.ok(clusteringVisualizationDTO);
    }

    /**
     * Get data for table dat visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for timeline visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary table visualization info for training instance.",
            response = PlayerDataDTO[].class,
            nickname = "getTableVisualizations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = PlayerDataDTO[].class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlayerDataDTO>> getTableVisualizations(@ApiParam(value = "Training instance ID", required = true)
                                                                      @PathVariable("instanceId") Long trainingInstanceId) {
        List<PlayerDataDTO> tableVisualizationsDTO = visualizationFacade.getTableVisualizations(trainingInstanceId);
        return ResponseEntity.ok(tableVisualizationsDTO);
    }

    /**
     * Get data for level tabs visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for level tabs visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary level tabs visualization info for training instance.",
            response = LevelTabsLevelDTO[].class,
            nickname = "getLevelTabsVisualizations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = LevelTabsLevelDTO[].class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/level-tabs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LevelTabsLevelDTO>> getLevelTabsVisualizations(@ApiParam(value = "Training instance ID", required = true)
                                                                              @PathVariable("instanceId") Long trainingInstanceId) {
        List<LevelTabsLevelDTO> levelTabsVisualizationsDTO = visualizationFacade.getLevelTabsVisualizations(trainingInstanceId);
        return ResponseEntity.ok(levelTabsVisualizationsDTO);
    }

    /**
     * Get data for timeline visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for timeline visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary timeline visualization info for training instance.",
            response = TimelineDTO.class,
            nickname = "getTimelineVisualizations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = TimelineDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/timeline", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimelineDTO> getTimelineVisualizations(@ApiParam(value = "Training instance ID", required = true)
                                                                 @PathVariable("instanceId") Long trainingInstanceId) {
        TimelineDTO timelineVisualizationDTO = visualizationFacade.getTimelineVisualizations(trainingInstanceId);
        return ResponseEntity.ok(timelineVisualizationDTO);
    }

    /**
     * Gather all necessary information about levels of given training instance to visualize results of the training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about levels for specific training instance and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training instance.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingInstanceProgress",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/progress", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> gatherVisualizationInfoForTrainingInstanceProgress(@ApiParam(value = "Training instance ID", required = true)
                                                                                     @PathVariable("instanceId") Long trainingInstanceId) {
        VisualizationProgressDTO visualizationProgressDTO = visualizationFacade.getProgressVisualizationAboutTrainingInstance(trainingInstanceId);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, visualizationProgressDTO));
    }
}
