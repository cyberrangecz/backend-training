package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.*;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * The type Training runs rest controller.
 */
@Api(value = "/training-runs", tags = "Training runs", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingRunsRestController {

    private TrainingRunFacade trainingRunFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Training runs rest controller.
     *
     * @param trainingRunFacade the training run facade
     * @param objectMapper      the object mapper
     */
    @Autowired
    public TrainingRunsRestController(TrainingRunFacade trainingRunFacade, ObjectMapper objectMapper) {
        this.trainingRunFacade = trainingRunFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Delete training runs.
     *
     * @param trainingRunIds the training run ids
     * @param forceDelete    the force delete
     * @return the response entity
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete training runs",
            nickname = "deleteTrainingRuns")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training runs were deleted"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered", response = ApiError.class)
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteTrainingRuns(
            @ApiParam(value = "Ids of training runs that will be deleted", required = true)
            @RequestParam(value = "trainingRunIds", required = true) List<Long> trainingRunIds,
            @ApiParam(value = "Indication if this training run must be deleted no matter of any check (force it)", required = false)
            @RequestParam(value = "forceDelete", required = false) boolean forceDelete) {
        trainingRunFacade.deleteTrainingRuns(trainingRunIds, forceDelete);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Delete a given training run.
     *
     * @param runId       the training run id
     * @param forceDelete the force delete
     * @return the response entity
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete training run",
            nickname = "deleteTrainingRun")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run was deleted"),
            @ApiResponse(code = 404, message = "Training run not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Sandbox assigned to the training run was not successfully deleted.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered", response = ApiError.class)
    })
    @DeleteMapping(path = "/{runId}")
    public ResponseEntity<Void> deleteTrainingRun(
            @ApiParam(value = "Id of training run that will be deleted", required = true)
            @PathVariable("runId") Long runId,
            @ApiParam(value = "Indication if this training run must be deleted no matter of any check (force it)", required = false)
            @RequestParam(value = "forceDelete", required = false) boolean forceDelete) {
        trainingRunFacade.deleteTrainingRun(runId, forceDelete);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Get requested Training Run by id.
     *
     * @param runId  of Training Run to return.
     * @param fields attributes of the object to be returned as the result.
     * @return Requested Training Run by id.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get training run by ID.",
            response = TrainingRunByIdDTO.class,
            nickname = "findTrainingRunById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run found.", response = TrainingRunDTO.class),
            @ApiResponse(code = 404, message = "Training run not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findTrainingRunById(@ApiParam(value = "Training run Id", required = true)
                                                      @PathVariable("runId") Long runId,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        TrainingRunByIdDTO trainingRunResource = trainingRunFacade.findById(runId);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
    }

    /**
     * Get all Training Runs.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @return all Training Runs.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all training runs.",
            response = TrainingRunRestResource.class,
            nickname = "findAllTrainingRuns",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training runs found.", response = TrainingRunRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingRuns(@QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
                                                      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<TrainingRunDTO> trainingRunResource = trainingRunFacade.findAll(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
    }

    /**
     * Access training run.
     *
     * @param accessToken the access token
     * @return first level of training run.
     */
    @ApiOperation(httpMethod = "POST",
            value = "Access training run.",
            response = AccessTrainingRunDTO.class,
            nickname = "accessTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run accessed.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "There is no training instance with given accessToken or first level not found in database.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Some error occurred during getting info about sandboxes.", response = ApiError.class),
            @ApiResponse(code = 503, message = "There is no available sandbox, wait a minute and try again.", response = ApiError.class)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(@ApiParam(value = "accessToken", required = true)
                                                                  @RequestParam(value = "accessToken", required = false) String accessToken) {
        AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(accessToken);
        return ResponseEntity.ok(accessTrainingRunDTO);
    }

    /**
     * Get all accessed Training Runs.
     *
     * @param pageable    pageable parameter with information about pagination.
     * @param fields      attributes of the object to be returned as the result.
     * @param sortByTitle "asc" for ascending alphabetical sort by title, "desc" for descending
     * @return all accessed Training Runs.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all accessed training runs.",
            notes = "Returns training run which was accessed by logged in user",
            response = AccessedTrainingRunRestResource.class,
            nickname = "getAllAccessedTrainingRuns",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All accessed training runs found.", response = AccessedTrainingRunDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/accessible", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllAccessedTrainingRuns(@ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                             @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                             @RequestParam(value = "fields", required = false) String fields,
                                                             @ApiParam(value = "Sort by title attribute. As values us asc|desc", required = false, example = "asc")
                                                             @RequestParam(value = "sortByTitle", required = false) String sortByTitle) {
        PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS = trainingRunFacade.findAllAccessedTrainingRuns(pageable, sortByTitle);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, accessedTrainingRunDTOS), HttpStatus.OK);
    }

    /**
     * Get next level of given Training Run.
     *
     * @param runId  of Training Run for which to get next level.
     * @param fields attributes of the object to be returned as the result.
     * @return Requested next level.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get level of given training run.",
            notes = "Returns (assessment, game, info) level if any next level exists and training run as well",
            response = AbstractLevelDTO.class,
            nickname = "getNextLevel",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Next level found.", response = AbstractLevelDTO.class),
            @ApiResponse(code = 404, message = "There is no next level or could not be found in database.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}/next-levels", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNextLevel(@ApiParam(value = "Training run ID", required = true)
                                               @PathVariable("runId") Long runId,
                                               @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                               @RequestParam(value = "fields", required = false) String fields) {
        AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(runId);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, levelDTO));
    }

    /**
     * Get solution of current game level.
     *
     * @param runId of Training Run for which to get solution.
     * @return Requested solution of game level.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get solution of game level.",
            notes = "Returns solution if given training runs exists and current level is game level",
            response = String.class,
            nickname = "getSolution",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Solution found.", response = String.class),
            @ApiResponse(code = 404, message = "Training run with given id cannot be found.", response = ApiError.class),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have solution.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}/solutions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSolution(@ApiParam(value = "Training run ID", required = true)
                                              @PathVariable("runId") Long runId) {
        return ResponseEntity.ok(trainingRunFacade.getSolution(runId));
    }

    /**
     * Get hint of current game level.
     *
     * @param runId  of Training Run for which to get hint.
     * @param hintId the hint id
     * @param fields attributes of the object to be returned as the result.
     * @return Requested hint of game level.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get hint of game level.",
            notes = "Returns hint if given training runs exists and current level is game level",
            response = String.class,
            nickname = "getHint",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Hint found.", response = HintDTO.class),
            @ApiResponse(code = 404, message = "Hint with given id not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Hint with given id is not in current level of training run.", response = ApiError.class),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have hints.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}/hints/{hintId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHint(@ApiParam(value = "Training run ID", required = true)
                                          @PathVariable("runId") Long runId,
                                          @ApiParam(value = "Hint ID", required = true)
                                          @PathVariable Long hintId,
                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                          @RequestParam(value = "fields", required = false) String fields) {
        HintDTO hintDTO = trainingRunFacade.getHint(runId, hintId);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, hintDTO));
    }

    /**
     * Check if submitted flag is correct.
     *
     * @param runId the run id
     * @param flag  submitted string.
     * @return True if flag is correct, false if flag is wrong.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Check flag of game level",
            notes = "Current level of given training run must be game level",
            response = Boolean.class,
            nickname = "isCorrectFlag",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Flag checked.", response = IsCorrectFlagDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have flag.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}/is-correct-flag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IsCorrectFlagDTO> isCorrectFlag(@ApiParam(value = "Training run ID", required = true)
                                                          @PathVariable("runId") Long runId,
                                                          @ApiParam(value = "Submitted flag", required = true)
                                                          @RequestParam(value = "flag") String flag) {
        return ResponseEntity.ok(trainingRunFacade.isCorrectFlag(runId, flag));
    }

    /**
     * Resume paused training run.
     *
     * @param runId id of training run.
     * @return current level of training run.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get current level of resumed training run",
            response = AccessTrainingRunDTO.class,
            nickname = "resumeTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run resumed.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Cannot resume finished training run.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}/resumption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> resumeTrainingRun(@ApiParam(value = "Training run ID", required = true)
                                                                  @PathVariable("runId") Long runId) {
        AccessTrainingRunDTO resumedTrainingRunDTO = trainingRunFacade.resumeTrainingRun(runId);
        return ResponseEntity.ok(resumedTrainingRunDTO);
    }

    /**
     * Finish training run.
     *
     * @param runId id of training run.
     * @return the response entity
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Finish training run",
            nickname = "finishTrainingRun",
            notes = "Training run will be finished if the current level is the last level and it is answered.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run finished."),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Cannot finish finished training run.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PutMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> finishTrainingRun(@ApiParam(value = "Training run ID", required = true)
                                                  @PathVariable("runId") Long runId) {
        trainingRunFacade.finishTrainingRun(runId);
        return ResponseEntity.ok().build();
    }

    /**
     * Evaluate responses to assessment.
     *
     * @param runId     id of training run.
     * @param responses to assessment
     * @return the response entity
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Evaluate responses to assessment",
            nickname = "evaluateResponsesToAssessment",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Responses to assessment evaluated and stored."),
            @ApiResponse(code = 409, message = "Current level of training is not assessment level or level has been already answered.", response = ApiError.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PutMapping(value = "/{runId}/assessment-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> evaluateResponsesToAssessment(@ApiParam(value = "Training run ID", required = true)
                                                              @PathVariable("runId") Long runId,
                                                              @ApiParam(value = "Responses to assessment", required = true)
                                                              @RequestBody String responses) {
        trainingRunFacade.evaluateResponsesToAssessment(runId, responses);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get requested participant of the given training run.
     *
     * @param trainingRunId id of training run for which to get participant
     * @return Participant of specific training run.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get participant.",
            response = UserRefDTO.class,
            nickname = "getParticipant",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Participant found.", response = UserRefDTO.class),
            @ApiResponse(code = 404, message = "Training run not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{runId}/participant", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getParticipant(
            @ApiParam(value = "Get participant for the given runId.")
            @PathVariable("runId") Long trainingRunId) {
        UserRefDTO participant = trainingRunFacade.getParticipant(trainingRunId);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, participant));
    }

    /**
     * The type Training run rest resource.
     */
    @ApiModel(value = "TrainingRunRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    public static class TrainingRunRestResource extends PageResultResource<TrainingRunDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Training Runs from databases.")
        private List<TrainingRunDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    @ApiModel(description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class AccessedTrainingRunRestResource extends PageResultResource<AccessedTrainingRunDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Accessed Training Runs from databases.")
        private List<AccessedTrainingRunDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }


}
