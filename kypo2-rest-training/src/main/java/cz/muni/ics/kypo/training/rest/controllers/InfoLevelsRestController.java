package cz.muni.ics.kypo.training.rest.controllers;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.InfoLevelFacade;
import cz.muni.ics.kypo.training.model.InfoLevel;
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
@Api(value = "/info-levels", 
  	 consumes = "application/json"
)
//@formatter:on
@RestController
@RequestMapping(value = "/info-levels")
public class InfoLevelsRestController {

	private static final Logger LOG = LoggerFactory.getLogger(InfoLevelsRestController.class);

	private InfoLevelFacade infoLevelFacade;
	private ObjectMapper objectMapper;

	@Autowired
	public InfoLevelsRestController(InfoLevelFacade infoLevelFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
		this.infoLevelFacade = infoLevelFacade;
		this.objectMapper = objectMapper;
	}

	/**
	 * Get requested Info Level by id.
	 * 
	 * @param id of Info Level to return.
	 * @return Requested Info by id.
	 */
	//@formatter:off
  @ApiOperation(httpMethod = "GET", 
      value = "Get Info level by Id.", 
      response = InfoLevelDTO.class,
      nickname = "findInfoLevelById",
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findInfoLevelById(@ApiParam(name = "InfoLevel ID") @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findInfoLevelById({},{}", id, fields);
    try {
      InfoLevelDTO infoLevelResource = infoLevelFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoLevelResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

	/**
	 * Get all Info Levels.
	 * 
	 * @return all Info levels.
	 */
	//@formatter:off
  @ApiOperation(httpMethod = "GET",
      value = "Get all info levels.",
      response = InfoLevelDTO.class,
      responseContainer = "Page",
      nickname = "findAllInfoLevels",
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllInfoLevels(
      @PageableDefault(size = 10) Pageable pageable,
      @QuerydslPredicate(root = InfoLevel.class) Predicate predicate,
      @RequestParam MultiValueMap<String, String> parameters, 
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findAllInfoLevels({},{}", parameters, fields);
    try {
      PageResultResource<InfoLevelDTO> infoLevelResource = infoLevelFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoLevelResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
}
