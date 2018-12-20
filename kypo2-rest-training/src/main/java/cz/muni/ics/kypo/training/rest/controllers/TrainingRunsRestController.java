package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.*;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * @author Dominik Pilar (445537)
 */
@Api(value = "/training-runs", tags = "Training runs", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(value = "/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingRunsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunsRestController.class);

    private TrainingRunFacade trainingRunFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public TrainingRunsRestController(TrainingRunFacade trainingRunFacade, ObjectMapper objectMapper) {
        this.trainingRunFacade = trainingRunFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Get requested Training Run by id.
     *
     * @param runId of Training Run to return.
     * @return Requested Training Run by id.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get training run by Id.",
            response = TrainingRunDTO.class,
            nickname = "findTrainingRunById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run with given id found.", response = TrainingRunDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findTrainingRunById(@ApiParam(value = "Training run Id", required = true) @PathVariable Long runId,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("findTrainingRunById({},{})", runId, fields);
        try {
            TrainingRunDTO trainingRunResource = trainingRunFacade.findById(runId);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiModel(value = "TrainingRunRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class TrainingRunRestResource extends PageResultResource<TrainingRunDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Training Runs from databases.")
        private List<TrainingRunDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    /**
     * Get all Training Runs.
     *
     * @return all Training Runs.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all training Runs.",
            response = TrainingRunRestResource.class,
            nickname = "findAllTrainingRuns",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training runs found.", response = TrainingRunDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingRuns(@QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
                                                      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                      @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                      @RequestParam MultiValueMap<String, String> parameters,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("findAllTrainingRuns({},{})", parameters, fields);
        PageResultResource<TrainingRunDTO> trainingRunResource = trainingRunFacade.findAll(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
    }

    /**
     * Access training run.
     *
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
            @ApiResponse(code = 404, message = "There is no training instance with given password or first level not found in database."),
            @ApiResponse(code = 500, message = "Some error occurred during getting info about sandboxes."),
            @ApiResponse(code = 503, message = "There is no available sandbox, wait a minute and try again.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(@ApiParam(value = "password", required = true) @RequestParam(value = "password", required = false) String password) {
        LOG.debug("accessTrainingRun({})", password);
        try {
            AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(password);
            return new ResponseEntity<>(accessTrainingRunDTO, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
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

    /**
     * Get all accessed Training Runs.
     *
     * @return all accessed Training Runs.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all accessed training runs.",
            notes = "Returns training run which was accessed by logged in user",
            response = AccessedTrainingRunRestResource.class,
            responseContainer = "Page",
            nickname = "findAllTrainingRuns",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All accessed training runs found.", response = AccessedTrainingRunDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(path = "/accessible", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllAccessedTrainingRuns(@ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                             @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                             @RequestParam MultiValueMap<String, String> parameters,
                                                             @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                             @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS = trainingRunFacade.findAllAccessedTrainingRuns(pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, accessedTrainingRunDTOS), HttpStatus.OK);
    }

    /**
     * Get next level of given Training Run.
     *
     * @param runId of Training Run for which to get next level.
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
            @ApiResponse(code = 404, message = "There is no next level or could not be found in database."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{runId}/next-levels", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNextLevel(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId,
                                               @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                               @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getNextLevel({},{})", runId, fields);
        try {
            AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(runId);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, levelDTO));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
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
            @ApiResponse(code = 404, message = "Training run with given id cannot be found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have solution."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{runId}/solutions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSolution(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId) {
        LOG.debug("getSolution({})", runId);
        try {
            return ResponseEntity.ok(trainingRunFacade.getSolution(runId));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Get hint of current game level.
     *
     * @param runId of Training Run for which to get hint.
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
            @ApiResponse(code = 404, message = "Hint with given id not found."),
            @ApiResponse(code = 409, message = "Hint with given id is not in current level of training run."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have hints."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{runId}/hints/{hintId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHint(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId,
                                          @ApiParam(value = "Hint ID", required = true) @PathVariable Long hintId,
                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                          @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getHint({}, {})", runId, hintId);
        try {
            HintDTO hintDTO = trainingRunFacade.getHint(runId, hintId);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, hintDTO));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Check if submited flag is correct.
     *
     * @param flag submited string.
     * @return True if flag is correct, false if flag is wrong.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get boolean about flag correctness",
            notes = "Current level of given training run must be game level",
            response = Boolean.class,
            nickname = "isCorrectFlag",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Flag checked.", response = IsCorrectFlagDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have flag."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{runId}/is-correct-flag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IsCorrectFlagDTO> isCorrectFlag(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId,
                                                          @ApiParam(value = "Submitted flag", required = true) @RequestParam(value = "flag") String flag) {
        LOG.debug("isCorrectFlag({}, {})", runId, flag);
        try {
            return ResponseEntity.ok(trainingRunFacade.isCorrectFlag(runId, flag));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
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
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 409, message = "Cannot resume archived training run."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{runId}/resumption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> resumeTrainingRun(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId) {
        LOG.debug("resumeTrainingRun({})", runId);
        try {
            AccessTrainingRunDTO resumedTrainingRunDTO = trainingRunFacade.resumeTrainingRun(runId);
            return ResponseEntity.ok(resumedTrainingRunDTO);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Finish training run.
     *
     * @param runId id of training run.
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Finish given training run",
            response = Void.class,
            nickname = "finishTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run finished."),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 409, message = "Cannot finish archived training run."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> finishTrainingRun(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId) {
        LOG.debug("finishTrainingRun({})", runId);
        try {
            trainingRunFacade.archiveTrainingRun(runId);
            return ResponseEntity.ok().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Evaluate responses to assessment.
     *
     * @param runId     id of training run.
     * @param responses to assessment
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Evaluate responses to assessment",
            response = Void.class,
            nickname = "evaluateResponsesToAssessment",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Responses to assessment evaluated and stored ."),
            @ApiResponse(code = 409, message = "Current level of training is not assessment level or level has been already answered."),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(value = "/{runId}/assessment-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> evaluateResponsesToAssessment(@ApiParam(value = "Training run ID", required = true) @PathVariable Long runId,
                                                              @ApiParam(value = "Responses to assessment", required = true) @RequestBody String responses) {
        LOG.debug("evaluateResponsesToAssessment({})", runId);
        try {
            trainingRunFacade.evaluateResponsesToAssessment(runId, responses);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }
}
