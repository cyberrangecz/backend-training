package cz.muni.ics.kypo.training.rest.controllers;

import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Pavel Seda
 */
@Api(value = "/training-events", tags = "Training events", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(path = "/training-events", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingEventsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingEventsRestController.class);

    private TrainingEventsService trainingEventsService;

    @Autowired
    public TrainingEventsRestController(TrainingEventsService trainingEventsService) {
        this.trainingEventsService = trainingEventsService;
    }

    /**
     * Get all events in particular training instance.
     *
     * @return all events in selected training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all events in particular training definition and training instance.",
            nickname = "getAllEventsByTrainingDefinitionAndTrainingInstanceId",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All events in particular training run by id was found.", responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/training-definitions/{trainingDefinitionId}/training-instances/{trainingInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllEventsByTrainingDefinitionAndTrainingInstanceId(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable(value = "trainingDefinitionId") Long trainingDefinitionId,
            @ApiParam(value = "Training instance ID", required = true) @PathVariable(value = "trainingInstanceId") Long trainingInstanceId) {
        try {
            return ResponseEntity.ok(trainingEventsService.findAllEventsByTrainingDefinitionAndTrainingInstanceId(trainingDefinitionId, trainingInstanceId));
        } catch (IOException ex) {
            throw new ElasticsearchTrainingDataLayerException("It is not possible to retrieve Elasticsearch documents from this event.", ex);
        }
    }

    /**
     * Get all events in particular training run.
     *
     * @return all events in selected training run.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all events in particular training run.",
            nickname = "getAllEventsFromTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All events in particular training run by id was found.", responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/training-definitions/{trainingDefinitionId}/training-instances/{trainingInstanceId}/training-runs/{trainingRunId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllEventsFromTrainingRun(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable(value = "trainingDefinitionId") Long trainingDefinitionId,
            @ApiParam(value = "Training instance ID", required = true) @PathVariable(value = "trainingInstanceId") Long trainingInstanceId,
            @ApiParam(value = "Training run ID", required = true) @PathVariable(value = "trainingRunId") Long trainingRunId) {
        try {
            return ResponseEntity.ok(trainingEventsService.findAllEventsFromTrainingRun(trainingDefinitionId, trainingInstanceId, trainingRunId));
        } catch (IOException ex) {
            throw new ElasticsearchTrainingDataLayerException("It is not possible to retrieve Elasticsearch documents from this game.", ex);
        }
    }

}