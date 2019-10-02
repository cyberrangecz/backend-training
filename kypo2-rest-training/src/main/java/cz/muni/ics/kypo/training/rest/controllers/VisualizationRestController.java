package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.VisualizationFacade;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * The type Visualizations rest controller.
 *
 * @author Dominik Pilar (445537)
 */
@Api(value = "/visualizations", tags = "Visualizations", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
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
     * @param objectMapper      the object mapper
     */
    @Autowired
    public VisualizationRestController(VisualizationFacade visualizationFacade, ObjectMapper objectMapper) {
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
            value = "Get necessary info about levels for specific training run and additional info about training definition.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoDTO> gatherVisualizationInfoForTrainingRun(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId) {
        try {
            VisualizationInfoDTO visualizationInfoAboutTrainingRunDTO = visualizationFacade.getVisualizationInfoAboutTrainingRun(runId);
            return ResponseEntity.ok(visualizationInfoAboutTrainingRunDTO);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Gather all necessary information about levels of given training instance to visualize results of the training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about levels for specific training instance and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary info about levels for specific training instance and additional info about training definition.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/training-instances/{trainingInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoDTO> gatherVisualizationInfoForTrainingInstance(@ApiParam(value = "Training instance ID", required = true) @PathVariable Long trainingInstanceId) {
        try {
            VisualizationInfoDTO visualizationInfoDTO = visualizationFacade.getVisualizationInfoAboutTrainingInstance(trainingInstanceId);
            return ResponseEntity.ok(visualizationInfoDTO);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }



    /**
     * Gather all necessary information about participants of the given training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about participants specific training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary info about participants for specific training instance.",
            response = VisualizationInfoDTO.class,
            nickname = "getParticipantsForGivenTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = UserRefDTO.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/training-instances/{trainingInstanceId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRefDTO>> getParticipantsForGivenTrainingInstance(@ApiParam(value = "Training instance ID", required = true) @PathVariable Long trainingInstanceId) {
        try {
            List<UserRefDTO> visualizationInfoDTO = visualizationFacade.getParticipantsForGivenTrainingInstance(trainingInstanceId);
            return ResponseEntity.ok(visualizationInfoDTO);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Gather all necessary information about users with given ids.
     *
     * @param usersIds ids of users to retrieve.
     * @return necessary info about participants specific training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary info about participants for specific training instance.",
            response = VisualizationInfoDTO.class,
            nickname = "getParticipantsForGivenTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = UserRefDTO.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersByIds(Pageable pageable,
                                                @ApiParam(value = "usersIds", required = true) @RequestParam Set<Long> usersIds) {
        try {
            PageResultResource<UserRefDTO> visualizationInfoDTO = visualizationFacade.getUsersByIds(usersIds, pageable);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, visualizationInfoDTO));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }
}
