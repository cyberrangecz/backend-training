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
import cz.muni.ics.kypo.training.rest.interfaces.ApiPageableSwagger;
import io.swagger.annotations.*;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import java.util.List;
import org.jsondoc.core.annotation.ApiObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * @author Dominik Pilar (445537)
 */
@Api(value = "/training-runs",
        consumes = "application/json"
)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(value = "/training-runs")
public class TrainingRunsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunsRestController.class);

    private TrainingRunFacade trainingRunFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public TrainingRunsRestController(TrainingRunFacade trainingRunFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
        this.trainingRunFacade = trainingRunFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Get requested Training Run by id.
     *
     * @param runId of Training Run to return.
     * @return Requested Training Run by id.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get Training Run by Id.",
            response = TrainingRunDTO.class,
            nickname = "findTrainingRunById",

            produces = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run with given id found.", response = TrainingRunDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")

    })
    @GetMapping(value = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findTrainingRunById(@ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("findTrainingRunById", runId, fields);
        try {
            TrainingRunDTO trainingRunResource = trainingRunFacade.findById(runId);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiObject(name = "Result info (Page)",
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
            value = "Get all Training Runs.",
            response = TrainingRunRestResource.class,
            nickname = "findAllTrainingRuns",
            produces = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training runs found.", response = TrainingRunDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingRuns(@QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
                                                      @ApiParam(value = "Pagination support.", required = false)
                                                              Pageable pageable,
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
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training run accessed.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "There is no training instance with given password or first level not found in database."),
            @ApiResponse(code = 500, message = "Some error occurred during getting info about sandboxes."),
            @ApiResponse(code = 503, message = "There is no available sandbox, wait a minute and try again.")
    })
    @PostMapping(value = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(@ApiParam(value = "password", required = true) @RequestParam(value = "password", required = false) String password) {
        LOG.debug("accessTrainingRun({})", password);
        try {
            AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(password);
            return new ResponseEntity<>(accessTrainingRunDTO, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiObject(name = "Result info (Page)",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
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
            value = "Get all accessed Training Runs.",
            response = AccessedTrainingRunRestResource.class,
            responseContainer = "Page",
            nickname = "findAllTrainingRuns",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All accessed training runs found.", response = AccessedTrainingRunDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(value = "/accessed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllAccessedTrainingRuns(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        LOG.debug("findAllAccessedTrainingRuns()");
        PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS = trainingRunFacade.findAllAccessedTrainingRuns(pageable);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, accessedTrainingRunDTOS), HttpStatus.OK);
    }

    /**
     * Get next level of given Training Run.
     *
     * @param runId of Training Run for which to get next level.
     * @return Requested next level.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get Level of given Training Run.",
            response = AbstractLevelDTO.class,
            nickname = "getNextLevel",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Next level found.", response = AbstractLevelDTO.class),
            @ApiResponse(code = 404, message = "There is no next level or could not be found in database."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(value = "/{runId}/next-levels", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNextLevel(@ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId,
                                               @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                               @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getNextLevel({},{})", runId, fields);
        try {
            AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(runId);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, levelDTO), HttpStatus.OK);
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
            response = String.class,
            nickname = "getSolution",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Solution found.", response = String.class),
            @ApiResponse(code = 404, message = "Training run with given id cannot be found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have solution."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(value = "/{runId}/solutions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSolution(@ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId) {
        LOG.debug("getSolution({})", runId);
        try {
            String solution = trainingRunFacade.getSolution(runId);
            return new ResponseEntity<>(solution, HttpStatus.OK);
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
            response = String.class,
            nickname = "getHint",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Hint found.", response = HintDTO.class),
            @ApiResponse(code = 404, message = "Hint with given id not found."),
            @ApiResponse(code = 409, message = "Hint with given id is not in current level of training run."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have hints."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(value = "/{runId}/hints/{hintId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHint(@ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId,
                                          @ApiParam(value = "Hint ID", required = true) @PathVariable Long hintId,
                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                          @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getHint({}, {})", runId, hintId);
        try {
            HintDTO hintDTO = trainingRunFacade.getHint(runId, hintId);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, hintDTO), HttpStatus.OK);
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
            value = "Get boolean about flag correctness .",
            response = Boolean.class,
            nickname = "isCorrectFlag",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Flag checked.", response = IsCorrectFlagDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not have flag."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(value = "/{runId}/is-correct-flag", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<IsCorrectFlagDTO> isCorrectFlag(@ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId,
                                                          @ApiParam(value = "Submitted flag", required = true) @RequestParam(value = "flag") String flag) {
        LOG.debug("isCorrectFlag({}, {})", runId, flag);
        try {
            IsCorrectFlagDTO isCorrectFlagDTO = trainingRunFacade.isCorrectFlag(runId, flag);
            return new ResponseEntity<>(isCorrectFlagDTO, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }
}
