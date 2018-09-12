package cz.muni.ics.kypo.training.rest.controllers;

import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.model.TrainingRun;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

  /**
   * Get all Training Runs.
   * 
   * @return all Training Runs.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET",
      value = "Get all Training Runs.",
      response = TrainingRunDTO.class,
      responseContainer = "Page",
      nickname = "findAllTrainingRuns",
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingRuns(@QuerydslPredicate(root = TrainingRun.class) Predicate predicate, Pageable pageable,
      @RequestParam MultiValueMap<String, String> parameters, 
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findAllTrainingRuns({},{})", parameters, fields);
    try {
      PageResultResource<TrainingRunDTO> trainingRunResource = trainingRunFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on


    /**
     * Access training run.
     *
     * @return first level of training run.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "POST",
            value = "Access training run.",
            response = AccessTrainingRunDTO.class,
            nickname = "accessTrainingRun",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(@ApiParam(value = "password", required = true) @RequestParam(value = "password", required = false) String password) {
        LOG.debug("accessTrainingRun({})", password);
        try {
            AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(password);
            return new ResponseEntity<AccessTrainingRunDTO>(accessTrainingRunDTO, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }




    /**
     * Get all accessed Training Runs.
     *
     * @return all accessed Training Runs.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get all accessed Training Runs.",
            response = AccessedTrainingRunDTO.class,
            responseContainer = "Page",
            nickname = "findAllTrainingRuns",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/accessed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllAccessedTrainingRuns(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        LOG.debug("findAllAccessedTrainingRuns()");
        try {
            PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS = trainingRunFacade.findAllAccessedTrainingRuns(pageable);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, accessedTrainingRunDTOS), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    /**
     * Get next level of given Training Run.
     *
     * @param id of Training Run for which to get next level.
     * @return Requested next level.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get Level of given Training Run.",
            response = AbstractLevelDTO.class,
            nickname = "getNextLevel",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/{id}/get-next-level", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNextLevel(@ApiParam(name = "Training Run ID") @PathVariable Long id,
                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                      @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getNextLevel({},{})", id, fields);
        try {
            AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(id);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, levelDTO), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    /**
     * Get solution of current game level.
     *
     * @param id of Training Run for which to get solution.
     * @return Requested solution of game level.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get solution of game level.",
            response = String.class,
            nickname = "getSolution",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/{id}/get-solution", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSolution(@ApiParam(name = "Training Run ID") @PathVariable Long id){
                LOG.debug("getSolution({})", id);
        try {
            String solution = trainingRunFacade.getSolution(id);
            return new ResponseEntity<>(solution, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    /**
     * Get hint of current game level.
     *
     * @param id of Training Run for which to get hint.
     * @return Requested hint of game level.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get hint of game level.",
            response = String.class,
            nickname = "getHint",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
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
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    /**
     * Check if submited flag is correct.
     *
     * @param flag submited string.
     * @return True if flag is correct, false if flag is wrong.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get boolean about flag correctness .",
            response = Boolean.class,
            nickname = "isCorrectFlag",
            produces = "application/json",
            authorizations = {
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/{id}/is-correct", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> isCorrectFlag(@ApiParam(name = "Training Run ID") @PathVariable Long id,
                                                      @ApiParam(value = "Submitted flag") @RequestParam(value = "flag") String flag,
    @ApiParam(value = "Solution taken") @RequestParam(value = "solutionTaken") boolean solutionTaken) {
        LOG.debug("isCorrectFlag({}, {})", id, flag);
        try {
            IsCorrectFlagDTO isCorrectFlagDTO = trainingRunFacade.isCorrectFlag(id, flag, solutionTaken);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper,isCorrectFlagDTO), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }
}
