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
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.model.TrainingDefinition;
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

  @Autowired
  public TrainingDefinitionsRestController(TrainingDefinitionFacade trainingDefinitionFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
    this.trainingDefinitionFacade = trainingDefinitionFacade;
    this.objectMapper = objectMapper;
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
      response = InfoLevelDTO.class,
      responseContainer = "Page",
      nickname = "findAllTrainingDefinitions",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find all Training Definitions", 
                      description = "allows returning Training Definitions."
                  )
              }
          )
      }
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
  //@formatter:on

}
