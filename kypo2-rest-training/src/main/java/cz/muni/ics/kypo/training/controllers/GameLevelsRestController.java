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
import org.springframework.data.web.PageableDefault;
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
import cz.muni.ics.kypo.training.api.dto.GameLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.GameLevelFacade;
import cz.muni.ics.kypo.training.model.GameLevel;
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
@Api(value = "/game-levels", 
  consumes = "application/json", 
  authorizations = {
    @Authorization(value = "sampleoauth", 
      scopes = {
        @AuthorizationScope(
          scope = "HTTP operations on Game Level Resource", 
          description = "allows operations on Game Level Resource."
        )
      }
    )
  }
)
//@formatter:on
@RestController
@RequestMapping(value = "/game-levels")
public class GameLevelsRestController {

  private static final Logger LOG = LoggerFactory.getLogger(GameLevelsRestController.class);

  private GameLevelFacade gameLevelFacade;
  private ObjectMapper objectMapper;
  private BeanMapping dtoMapper;

  @Autowired
  public GameLevelsRestController(GameLevelFacade gameLevelFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper, BeanMapping dtoMapper) {
    this.gameLevelFacade = gameLevelFacade;
    this.objectMapper = objectMapper;
    this.dtoMapper = dtoMapper;
  }

  /**
   * Get requested Game Level by id.
   * 
   * @param id of Game Level to return.
   * @return Requested Assessment Level by id.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET", 
      value = "Get Game Level by Id.", 
      response = GameLevelDTO.class,
      nickname = "findGameLevelById",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find Game Level by ID", 
                      description = "allows returning Game Level by ID."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findGameLevelById(
      @ApiParam(name = "GameLevel ID")
      @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findGameLevelById({},{})", id, fields);
    try {
      GameLevelDTO gameLevelResource = gameLevelFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, gameLevelResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

  /**
   * Get all Game Level.
   * 
   * @return all Game Level.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET",
      value = "Get all Game Levels.",
      response = GameLevelDTO.class,
      responseContainer = "Page",
      nickname = "findAllGameLevels",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find all Game Levels", 
                      description = "allows returning Game Levels."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllGameLevels(
      @QuerydslPredicate(root = GameLevel.class) Predicate predicate, 
      @PageableDefault(size = 20) final Pageable pageable,
      @RequestParam MultiValueMap<String, String> parameters, 
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findAllGameLevels({},{})", parameters, fields);
    try {
      PageResultResource<GameLevelDTO> gameLevelResource = gameLevelFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, gameLevelResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on


  @ApiOperation(httpMethod = "PUT",
          value = "Update Game Level",
          response = GameLevelDTO.class,
          nickname = "updateGameLevel",
          produces = "application/json",
          consumes = "application/json")
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "The requested resource was not modified")
  })
  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateGameLevel(@ApiParam(value = "Game level to be updated") @RequestBody GameLevelDTO gameLevelDTO) {
    try {
      GameLevel infoLevel = dtoMapper.mapTo(gameLevelDTO, GameLevel.class);
      gameLevelFacade.update(infoLevel);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotModifiedException(ex.getLocalizedMessage());
    }
  }


  @ApiOperation(httpMethod = "POST",
          value = "Create Game Level",
          response = GameLevelDTO.class,
          nickname = "createGameLevel",
          produces = "application/json",
          consumes = "application/json")
  @ApiResponses(value = {
          @ApiResponse( code = 400, message = "The requested resource was not created")
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GameLevelDTO> createGameLevel(@ApiParam(value = "Game level to be created") @RequestBody GameLevelDTO gameLevelDTO){
    try {
      GameLevel gameLevel = dtoMapper.mapTo(gameLevelDTO, GameLevel.class);
      GameLevelDTO newGameLevel = gameLevelFacade.create(gameLevel);
      return new ResponseEntity<>(newGameLevel, HttpStatus.CREATED);
    } catch (FacadeLayerException ex){
      throw new ResourceNotCreatedException(ex.getLocalizedMessage());
    }

  }

}
