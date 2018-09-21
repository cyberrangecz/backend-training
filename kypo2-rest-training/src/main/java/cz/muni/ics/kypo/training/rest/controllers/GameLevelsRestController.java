package cz.muni.ics.kypo.training.rest.controllers;

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
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.GameLevelFacade;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Pavel Šeda
 *
 */
//@formatter:off
@Api(value = "/game-levels", 
  	 consumes = "application/json" 
)
//@formatter:on
@RestController
@RequestMapping(value = "/game-levels")
public class GameLevelsRestController {

	private static final Logger LOG = LoggerFactory.getLogger(GameLevelsRestController.class);

	private GameLevelFacade gameLevelFacade;
	private ObjectMapper objectMapper;

	@Autowired
	public GameLevelsRestController(GameLevelFacade gameLevelFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
		this.gameLevelFacade = gameLevelFacade;
		this.objectMapper = objectMapper;
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
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findGameLevelById(
      @ApiParam(value = "GameLevel ID")
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
      produces = "application/json"
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

}
