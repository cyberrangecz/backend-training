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
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.rest.exceptions.*;
import io.swagger.annotations.*;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;

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
 *
 */
//@formatter:off
@Api(value = "/training-runs", 
  consumes = "application/json" 
)
//@formatter:on
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
	 * @param id of Training Run to return.
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
      @ApiResponse(code = 200, message = "The requested resource has been found."),
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingRunById(@ApiParam(name = "Training Run ID") @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findTrainingRunById", id, fields);
    try {
      TrainingRunDTO trainingRunResource = trainingRunFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
       throw throwException(ex);
    }
  }

  @ApiObject(name = "Result info (Page)",
  		description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
	private static class TrainingRunRestResource extends PageResultResource<TrainingRunDTO>{
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
      @ApiResponse(code = 200, message = "The requested resource has been found."),
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
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
            @ApiResponse(code = 200, message = "The requested resource has been found."),
            @ApiResponse(code = 404, message = "The requested resource was not found."),
            @ApiResponse(code = 500, message = "Getting info about sandboxes ended with error."),
            @ApiResponse(code = 503, message = "There is no available sandbox, try again later.")
    })
    @GetMapping(value = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(@ApiParam(value = "password", required = true) @RequestParam(value = "password", required = false) String password) {
        LOG.debug("accessTrainingRun({})", password);
        try {
            AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(password);
            return new ResponseEntity<AccessTrainingRunDTO>(accessTrainingRunDTO, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw throwException(ex);
        }
    }

    @ApiObject(name = "Result info (Page)",
    		description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  	private static class AccessedTrainingRunRestResource extends PageResultResource<AccessedTrainingRunDTO>{
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
            @ApiResponse(code = 200, message = "The requested resource has been found."),
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/accessed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllAccessedTrainingRuns(
    		@ApiParam(value = "Pagination support.", required = false)
    		@PageableDefault(size = 10, page = 0) Pageable pageable) {
        LOG.debug("findAllAccessedTrainingRuns()");
        PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS = trainingRunFacade.findAllAccessedTrainingRuns(pageable);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, accessedTrainingRunDTOS), HttpStatus.OK);
    }

    /**
     * Get next level of given Training Run.
     *
     * @param id of Training Run for which to get next level.
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
            @ApiResponse(code = 200, message = "The requested resource has been found."),
            @ApiResponse(code = 404, message = "The requested resource was not found or there is no next level."),
    })
    @GetMapping(value = "/{id}/get-next-level", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNextLevel(@ApiParam(name = "Training Run ID") @PathVariable Long id,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getNextLevel({},{})", id, fields);
        System.out.println("FUNGUJE");
        try {
            AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(id);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, levelDTO), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw throwException(ex);
        }
    }

    /**
     * Get solution of current game level.
     *
     * @param id of Training Run for which to get solution.
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
            @ApiResponse(code = 200, message = "The requested resource has been found."),
            @ApiResponse(code = 404, message = "The requested resource was not found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not contain solution.")
    })
    @GetMapping(value = "/{id}/get-solution", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSolution(@ApiParam(name = "Training Run ID") @PathVariable Long id){
                LOG.debug("getSolution({})", id);
        try {
            String solution = trainingRunFacade.getSolution(id);
            return new ResponseEntity<>(solution, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw throwException(ex);
        }
    }

    /**
     * Get hint of current game level.
     *
     * @param id of Training Run for which to get hint.
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
            @ApiResponse(code = 200, message = "The requested resource has been found."),
            @ApiResponse(code = 404, message = "The requested resource was not found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not contain hint.")
    })
    @GetMapping(value = "/{id}/get-hint/{hintId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHint(@ApiParam(name = "Training Run ID") @PathVariable Long id,
                                          @ApiParam(name = "Hint ID") @PathVariable Long hintId,
                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                              @RequestParam(value = "fields", required = false) String fields){
        LOG.debug("getHint({}, {})", id, hintId);
        try {
            HintDTO hintDTO = trainingRunFacade.getHint(id, hintId);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, hintDTO), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw throwException(ex);
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
            @ApiResponse(code = 200, message = "The requested resource has been found."),
            @ApiResponse(code = 404, message = "The requested resource was not found."),
            @ApiResponse(code = 400, message = "Current level is not game level and does not contain flag.")
    })
    @GetMapping(value = "/{id}/is-correct-flag", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<IsCorrectFlagDTO> isCorrectFlag(@ApiParam(name = "Training Run ID") @PathVariable Long id,
                                                      	  @ApiParam(value = "Submitted flag") @RequestParam(value = "flag") String flag,
    																											@ApiParam(value = "Solution taken") @RequestParam(value = "solutionTaken") boolean solutionTaken) {
        LOG.debug("isCorrectFlag({}, {})", id, flag);
        try {
            IsCorrectFlagDTO isCorrectFlagDTO = trainingRunFacade.isCorrectFlag(id, flag, solutionTaken);
            return new ResponseEntity<>(isCorrectFlagDTO, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw throwException(ex);
        }
    }

    private RuntimeException throwException(RuntimeException ex) {
        switch (((ServiceLayerException) ex.getCause()).getCode()) {
            case WRONG_LEVEL_TYPE:
                return new BadRequestException(ex.getLocalizedMessage());
            case RESOURCE_NOT_FOUND:
                return new ResourceNotFoundException(ex.getLocalizedMessage());
            case NO_NEXT_LEVEL:
                return new ResourceNotFoundException(ex.getLocalizedMessage());
            case UNEXPECTED_ERROR:
                return new InternalServerErrorException(ex.getLocalizedMessage());
            case RESOURCE_CONFLICT:
                return new ConflictException(ex.getLocalizedMessage());
            case NO_AVAILABLE_SANDBOX:
            default:
                return new ServiceUnavailableException(ex.getLocalizedMessage());
        }
    }
}
