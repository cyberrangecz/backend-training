package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoAboutTrainingRunDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.VisualizationFacade;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            response = VisualizationInfoAboutTrainingRunDTO.class,
            nickname = "gatherVisualizationInfoForTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run resumed.", response = VisualizationInfoAboutTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 409, message = "Cannot resume finished training run."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoAboutTrainingRunDTO> gatherVisualizationInfoForTrainingRun(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId) {
        try {
            VisualizationInfoAboutTrainingRunDTO visualizationInfoAboutTrainingRunDTO = visualizationFacade.getVisualizationInfoAboutTrainingRun(runId);
            return ResponseEntity.ok(visualizationInfoAboutTrainingRunDTO);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }
}
