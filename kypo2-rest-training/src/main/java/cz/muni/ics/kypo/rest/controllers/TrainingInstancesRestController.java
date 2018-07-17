package cz.muni.ics.kypo.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
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

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.api.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.model.TrainingInstance;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * @author Pavel Šeda
 *
 */
//@formatter:off
@Api(value = "/training-instances", 
  consumes = "application/json", 
  authorizations = {
    @Authorization(value = "sampleoauth", 
      scopes = {
        @AuthorizationScope(
          scope = "HTTP operations on Training Instances Resource", 
          description = "allows operations on Training Instances Resource."
        )
      }
    )
  }
)
//@formatter:on
@RestController
@RequestMapping(value = "/training-instances")
public class TrainingInstancesRestController {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingInstancesRestController.class);

  private TrainingInstanceFacade trainingInstanceFacade;
  private ObjectMapper objectMapper;

  @Autowired
  public TrainingInstancesRestController(TrainingInstanceFacade trainingInstanceFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
    this.trainingInstanceFacade = trainingInstanceFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Get requested Training Instance by id.
   * 
   * @param id of Training Instance to return.
   * @return Requested Training Instance by id.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET", 
      value = "Get Training Instance by Id.", 
      response = TrainingDefinitionDTO.class,
      nickname = "findTrainingInstanceById",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find Training Instance by ID", 
                      description = "allows returning Training Instance by ID."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingInstanceById(@ApiParam(name = "Training Instance ID") @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findTrainingInstanceById({},{})", id, fields);
    try {
      TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingInstanceResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

  /**
   * Get all Training Instances.
   * 
   * @return all Training Instances.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET",
      value = "Get all Training Instances.",
      response = InfoLevelDTO.class,
      responseContainer = "Page",
      nickname = "findAllTrainingInstances",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find all Training Instances", 
                      description = "allows returning Training Instances."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingInstances(@QuerydslPredicate(root = TrainingInstance.class) Predicate predicate, Pageable pageable,
      @RequestParam MultiValueMap<String, String> parameters, 
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findAllTrainingInstances({},{})", parameters, fields);
    try {
      PageResultResource<TrainingInstanceDTO> trainingInstanceResource = trainingInstanceFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingInstanceResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

}
