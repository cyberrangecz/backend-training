package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.LevelFacade;
import cz.muni.ics.kypo.training.model.AbstractLevel;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.*;
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

import java.util.List;

/**
 *
 * @author Boris Jaduš
 *
 */

@Api(value = "/levels",
		 consumes = "application/json"
)
@RestController
@RequestMapping(value = "/levels")
public class LevelRestController {
	private static final Logger LOG = LoggerFactory.getLogger(LevelRestController.class);

	private LevelFacade levelFacade;
	private ObjectMapper objectMapper;

	@Autowired
	public LevelRestController(LevelFacade levelFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper){
			this.levelFacade = levelFacade;
			this.objectMapper = objectMapper;
	}

	@ApiOperation(httpMethod = "GET",
			value = "Get level by Id.",
			response = AbstractLevelDTO.class,
			nickname = "findLevelById",
			produces = "application/json"
	)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "The requested resource was not found.")
	})
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> findLevelById(@ApiParam(value = "level id") @PathVariable long id,
		@ApiParam(value = "Fields which should be returned in REST API response", required = false)
		@RequestParam(value = "fields", required = false) String fields) {
		LOG.debug("findLevelById({},{}", id, fields);
		try {
	 		AbstractLevelDTO levelResource = levelFacade.findById(id);
			Squiggly.init(objectMapper, fields);
			return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, levelResource), HttpStatus.OK);
		} catch (FacadeLayerException ex) {
			throw new ResourceNotFoundException(ex.getLocalizedMessage());
		}
	}

	@ApiObject(name = "Result info (Page)",
		description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
	private static class LevelRestResource extends PageResultResource<AbstractLevelDTO> {
		@JsonProperty(required = true)
		@ApiModelProperty(value = "Retrieved Levels from databases.")
		private List<AbstractLevelDTO> content;
		@JsonProperty(required = true)
		@ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
		private Pagination pagination;
	}

	@ApiOperation(httpMethod = "GET",
		value = "Get all levels.",
		response = LevelRestResource.class,
		nickname = "findAllLevels",
		produces = "application/json"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "The requested resource was not found.")
	})
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> findAllLevels(
		@PageableDefault(size = 10) Pageable pageable,
		@QuerydslPredicate(root = AbstractLevel.class) Predicate predicate,
		@RequestParam MultiValueMap<String, String> parameters,
		@ApiParam(value = "Fields which should be returned in REST API response", required = false)
		@RequestParam(value = "fields", required = false) String fields) {
		LOG.debug("findAllLevels({},{}", parameters, fields);
		try {
			PageResultResource<AbstractLevelDTO> levelResource = levelFacade.findAll(predicate, pageable);
			Squiggly.init(objectMapper, fields);
			return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, levelResource), HttpStatus.OK);
		} catch (FacadeLayerException ex) {
			throw new ResourceNotFoundException(ex.getLocalizedMessage());
		}
	}

}
