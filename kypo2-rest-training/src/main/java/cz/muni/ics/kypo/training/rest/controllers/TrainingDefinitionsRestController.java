package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.enums.LevelType;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.rest.exceptions.*;
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

import javax.validation.Valid;

/**
 * @author Pavel Šeda
 *
 */
//@formatter:off
@Api(value = "/training-definitions",
  consumes = "application/json"
)
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
  @ApiOperation(httpMethod = "GET",
      value = "Get Training Definition by Id.",
      response = TrainingDefinitionDTO.class,
      nickname = "findTrainingDefinitionById",
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.")
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingDefinitionById(
  		@ApiParam(value = "TrainingDefinition ID")
  		@PathVariable long id,
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

  @ApiObject(name = "Result info (Page)",
  		description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
	private static class TrainingDefinitionRestResource extends PageResultResource<TrainingDefinitionDTO>{
	 	 @JsonProperty(required = true)
	 	 @ApiModelProperty(value = "Retrieved Training Definitions from databases.")
	 	 private List<TrainingDefinitionDTO> content;
	 	 @JsonProperty(required = true)
		 @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
		 private Pagination pagination;
	}

	/**
	 * Get all Training Definitions.
	 *
	 * @return all Training Definitions.
	 */
  @ApiOperation(httpMethod = "GET",
      value = "Get all Training Definitions.",
      response = TrainingDefinitionRestResource.class,
      nickname = "findAllTrainingDefinitions",
      produces = "application/json"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.")
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitions(
  		@QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate, Pageable pageable,
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

  @ApiOperation( httpMethod = "GET",
          value = "Get all training definition by sandbox definition id",
          response = TrainingDefinitionRestResource.class,
          nickname = "findAllTrainingDefinitionsBySandboxDefinitionId",
          produces = "application/json"
  )
  @ApiResponses( value = {
          @ApiResponse(code = 404, message = "The requested resource was not found")
  })
  @GetMapping(value = "/sandbox-definitions/{sandboxDefinitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitionsBySandboxDefinitionId(
  		@ApiParam(value = "Id of sandbox definition")
  		@PathVariable(value = "sandboxDefinitionId") Long sandboxDefinitionId,
      @PageableDefault(size = 10, page = 0) Pageable pageable){
    LOG.debug("findAllTrainingDefinitionsBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
    try {
      PageResultResource<TrainingDefinitionDTO> trainingDefinitionResource = trainingDefinitionFacade.findAllBySandboxDefinitionId(sandboxDefinitionId, pageable);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource), HttpStatus.OK);
    } catch (FacadeLayerException ex){
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }

  }

  @ApiOperation(httpMethod = "POST",
          value = "Create Training Definition",
          response = TrainingDefinitionDTO.class,
          nickname = "createTrainingDefinition",
          produces = "application/json",
          consumes = "application/json")
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "The requested resource was not created")
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createTrainingDefinition(
  		@ApiParam(name = "Training Definition to be created")
  		@RequestBody @Valid TrainingDefinitionCreateDTO trainingDefinitionCreateDTO,
			@ApiParam(value = "Fields which should be returned in REST API response", required = false)
      @RequestParam(value = "fields", required = false) String fields) {
    try {
      TrainingDefinitionDTO trainingDefinitionResource = trainingDefinitionFacade.create(trainingDefinitionCreateDTO);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotCreatedException(ex.getLocalizedMessage());
    }
  }

  @ApiOperation(httpMethod = "PUT",
      value = "Update Training Definition",
      response = TrainingDefinitionDTO.class,
      nickname = "updateTrainingDefinition",
      produces = "application/json",
      consumes = "application/json")
  @ApiResponses(value = {
          @ApiResponse(code = 404, message = "The requested resource was not found."),
          @ApiResponse(code = 409, message = "The requested resource was not modified because of its status")
  })
  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateTrainingDefinition(
  		@ApiParam(value = "Training definition to be updated")
  		@RequestBody @Valid TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO){
    try {
      trainingDefinitionFacade.update(trainingDefinitionUpdateDTO);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (FacadeLayerException ex) {
      	throw throwException(ex);
    }
  }

	@ApiOperation(httpMethod = "POST",
			value = "Clone Training Definition",
			response = TrainingDefinitionDTO.class,
			nickname = "cloneTrainingDefinition",
			produces = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "The requested resource was not found."),
			@ApiResponse(code = 409, message = "The requested resource was not created because of the status of origin resource")
			}
	)
	@PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TrainingDefinitionDTO> cloneTrainingDefinition(
			@ApiParam(value = "Id of training definition to be cloned")
			@PathVariable("id") Long id) {
		try {
			TrainingDefinitionDTO trainingDefinitionDTO = trainingDefinitionFacade.clone(id);
			return new ResponseEntity<>(trainingDefinitionDTO, HttpStatus.OK);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "PUT",
			value = "Swap level to the left",
			nickname = "swapLeft",
			response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not modified because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@PutMapping(value = "/{definitionId}/levels/{levelId}/swap-left")
	public ResponseEntity<Void> swapLeft(
			@ApiParam(value = "Id of definition")
			@PathVariable("definitionId") Long definitionId,
			@ApiParam(value = "Id of level to be swapped")
			@PathVariable("levelId") Long levelId) {
		try {
			trainingDefinitionFacade.swapLeft(definitionId, levelId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "PUT",
			value = "Swap level to the right",
			nickname = "swapRight",
			response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not modified because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@PutMapping(value = "/{definitionId}/levels/{levelId}/swap-right")
	public ResponseEntity<Void> swapRight(
			@ApiParam(value = "Id of definition")
			@PathVariable("definitionId") Long definitionId,
			@ApiParam(value = "Id of level to be swapped")
			@PathVariable("levelId") Long levelId) {
		try {
			trainingDefinitionFacade.swapRight(definitionId, levelId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "DELETE",
			value = "Delete training definition",
			nickname = "deleteTrainingDefinition",
			response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not deleted because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> deleteTrainingDefinition(
			@ApiParam(value = "Id of definition")
			@PathVariable("id") Long id) {
		try {
			trainingDefinitionFacade.delete(id);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "DELETE",
			value = "Delete specific level from definition",
			nickname = "deleteOneLevel",
			response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not modified because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@DeleteMapping(value = "/{definitionId}/levels/{levelId}")
	public ResponseEntity<Void> deleteOneLevel(
			@ApiParam(value = "Id of definition")
			@PathVariable("definitionId") Long definitionId,
			@ApiParam(value = "Id of level to be deleted")
			@PathVariable("levelId") Long levelId) {
		try {
			trainingDefinitionFacade.deleteOneLevel(definitionId, levelId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "PUT",
			value = "Update specific game level from definition",
			nickname = "updateGameLevel",
			response = Void.class,
			consumes = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not modified because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@PutMapping(value = "/{definitionId}/game-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> updateGameLevel(
			@ApiParam(value = "Id of definition")
			@PathVariable(value = "definitionId") Long definitionId,
			@ApiParam(value = "Game level to be updated")
			@RequestBody @Valid GameLevelUpdateDTO gameLevelUpdateDTO) {
		try {
			trainingDefinitionFacade.updateGameLevel(definitionId, gameLevelUpdateDTO);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "PUT",
			value = "Update specific info level from definition",
			nickname = "updateInfoLevel",
			response = Void.class,
			consumes = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not modified because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@PutMapping(value = "/{definitionId}/info-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> updateInfoLevel(
			@ApiParam(value = "Id of definition")
			@PathVariable(value = "definitionId") Long definitionId,
			@ApiParam(value = "Info level to be updated")
			@RequestBody @Valid InfoLevelUpdateDTO infoLevelUpdateDTO) {
		try {
			trainingDefinitionFacade.updateInfoLevel(definitionId, infoLevelUpdateDTO);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "PUT",
			value = "Update specific assessment level from definition",
			nickname = "updateAssessmentLevel",
			response = Void.class,
			consumes = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 409, message = "The requested resource was not modified because of its status"),
			@ApiResponse(code = 404, message = "The requested resource was not found")
			}
	)
	@PutMapping(value = "/{definitionId}/assessment-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> updateAssessmentLevel(
			@ApiParam(value = "Id of definition")
			@PathVariable(value = "definitionId") Long definitionId,
			@ApiParam(value = "Assessment level to be updated")
			@RequestBody @Valid AssessmentLevelUpdateDTO assessmentLevelUpdateDTO) {
		try {
			trainingDefinitionFacade.updateAssessmentLevel(definitionId, assessmentLevelUpdateDTO);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (FacadeLayerException ex) {
				throw throwException(ex);
		}
	}

	@ApiOperation(httpMethod = "GET",
			value = "Find level by id",
			response = AbstractLevelDTO.class,
			nickname = "findLevelById",
			produces = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "The requested resource was not found")
	})
	@GetMapping(value = "/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> findLevelById(@ApiParam(value = "Id of wanted level") @PathVariable(value = "levelId") Long levelId,
			@ApiParam(value = "Fields which should be returned in REST API response", required = false)
      @RequestParam(value = "fields", required = false) String fields){
  	try {
			AbstractLevelDTO level = trainingDefinitionFacade.findLevelById(levelId);
			Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, level), HttpStatus.OK);
		} catch (FacadeLayerException ex){
  		throw new ResourceNotFoundException(ex.getLocalizedMessage());
		}
	}

	@ApiOperation(httpMethod = "POST",
			value = "Create Level",
			//response = GameLevelCreateDTO.class,
			nickname = "createLevel",
			produces = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "The requested resource was not found"),
			}
	)
	@PostMapping(value = "/{definitionId}/levels/{levelType}")
	public ResponseEntity<Object> createLevel(
			@ApiParam(value = "Id of definition")
			@PathVariable(value = "definitionId") Long definitionId,
			@ApiParam(value = "Level type")
			@PathVariable (value = "levelType") LevelType levelType) {
		try {
			if (levelType.equals(LevelType.GAME)) {
				return new ResponseEntity<>(trainingDefinitionFacade.createGameLevel(definitionId), HttpStatus.CREATED);
			} else if (levelType.equals(LevelType.ASSESSMENT)) {
				return new ResponseEntity<>(trainingDefinitionFacade.createAssessmentLevel(definitionId), HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(trainingDefinitionFacade.createInfoLevel(definitionId), HttpStatus.CREATED);
			}
		} catch (FacadeLayerException ex) {
			throw new ResourceNotCreatedException(ex.getLocalizedMessage());
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
