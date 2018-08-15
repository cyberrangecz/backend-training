package cz.muni.ics.kypo.training.controllers;

import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotModifiedException;
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
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.exceptions.ResourceNotFoundException;
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
@Api(value = "/training-definitions", 
  consumes = "application/json", 
  authorizations = {
    @Authorization(value = "sampleoauth", 
      scopes = {
        @AuthorizationScope(
          scope = "HTTP operations on Training Definition Resource", 
          description = "allows operations on Training Definition Resource."
        )
      }
    )
  }
)
//@formatter:on
@RestController
@RequestMapping(value = "/training-definitions")
public class TrainingDefinitionsRestController {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionsRestController.class);

  private TrainingDefinitionFacade trainingDefinitionFacade;
  private ObjectMapper objectMapper;
  private BeanMapping dtoMapper;

  @Autowired
  public TrainingDefinitionsRestController(TrainingDefinitionFacade trainingDefinitionFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper, BeanMapping dtoMapper) {
    this.trainingDefinitionFacade = trainingDefinitionFacade;
    this.objectMapper = objectMapper;
    this.dtoMapper = dtoMapper;
  }

  /**
   * Get requested Training Definition by id.
   * 
   * @param id of Training Definition to return.
   * @return Requested Training Definition by id.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET", 
      value = "Get Training Definition by Id.", 
      response = TrainingDefinitionDTO.class,
      nickname = "findTrainingDefinitionById",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find Training Definition by ID", 
                      description = "allows returning Training Definition by ID."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingDefinitionById(@ApiParam(name = "TrainingDefinition ID") @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findTrainingDefinitionById({},{})", id, fields);
    try {
      TrainingDefinitionDTO trainingDefinitionResource = trainingDefinitionFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

  /**
   * Get all Training Definitions.
   * 
   * @return all Training Definitions.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET",
      value = "Get all Training Definitions.",
      response = TrainingDefinitionDTO.class,
      responseContainer = "Page",
      nickname = "findAllTrainingDefinitions",
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitions(@QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate, Pageable pageable,
      @RequestParam MultiValueMap<String, String> parameters, 
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findAllTrainingDefinitions({},{})", parameters, fields);
    try {
      PageResultResource<TrainingDefinitionDTO> trainingDefinitionResource = trainingDefinitionFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }

  @ApiOperation(httpMethod = "PUT",
      value = "Update Training Definition",
      response = TrainingDefinitionDTO.class,
      nickname = "updateTrainingDefinition",
      produces = "application/json",
      consumes = "application/json")
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "The requested resource was not modified")
  })
  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateTrainingDefinition(@ApiParam(value = "Training definition to be updated") @RequestBody TrainingDefinitionDTO trainingDefinitionDTO){
    try {
      TrainingDefinition trainingDefinition = dtoMapper.mapTo(trainingDefinitionDTO,TrainingDefinition.class);
      trainingDefinitionFacade.update(trainingDefinition);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotModifiedException(ex.getLocalizedMessage());
    }
  }

  //@formatter:on

  @ApiOperation(httpMethod = "POST",
      value = "Clone Training Definition",
      response = TrainingDefinitionDTO.class,
      nickname = "cloneTrainingDefinition",
      produces = "application/json")
  @ApiResponses(value = {
          @ApiResponse(code = 404, message = "The requested resource was not found."),
          @ApiResponse(code = 400, message = "The requested resource was not created")
  })
  @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> cloneTrainingDefinition(@ApiParam(value = "Id of training definition to be cloned") @PathVariable("id") Long id){
    try{
      TrainingDefinitionDTO trainingDefinitionDTO = trainingDefinitionFacade.clone(id);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingDefinitionDTO), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotCreatedException(ex.getLocalizedMessage());
    }
  }

}
