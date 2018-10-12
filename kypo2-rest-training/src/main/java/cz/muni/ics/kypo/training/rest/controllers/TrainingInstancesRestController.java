package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.NewTrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import cz.muni.ics.kypo.training.rest.exceptions.*;
import java.util.List;
import org.jsondoc.core.annotation.ApiObject;
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
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.validation.Valid;

/**
 * @author Pavel Šeda
 *
 */
//@formatter:off
@Api(value = "/training-instances",
  consumes = "application/json"
)
@ApiResponses(value = {
		@ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
		@ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(value = "/training-instances")
public class TrainingInstancesRestController {

	private static final Logger LOG = LoggerFactory.getLogger(cz.muni.ics.kypo.training.rest.controllers.TrainingInstancesRestController.class);

	private TrainingInstanceFacade trainingInstanceFacade;
	private ObjectMapper objectMapper;

	@Autowired
	public TrainingInstancesRestController(TrainingInstanceFacade trainingInstanceFacade,
			@Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
		this.trainingInstanceFacade = trainingInstanceFacade;
		this.objectMapper = objectMapper;
	}

	/**
	 * Get requested Training Instance by id.
	 * 
	 * @param id of Training Instance to return.
	 * @return Requested Training Instance by id.
	 */
  @ApiOperation(httpMethod = "GET", 
      value = "Get Training Instance by Id.", 
      response = TrainingDefinitionDTO.class,
      nickname = "findTrainingInstanceById",
      produces = "application/json"
  )
  @ApiResponses(value = {
  		@ApiResponse(code = 200, message = "Training instance found", response = TrainingInstanceDTO.class),
      @ApiResponse(code = 404, message = "Training instance with given id not found."),
			@ApiResponse(code = 500, message = "Unexpected condition was encountered.")

  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingInstanceById(@ApiParam(value = "Training Instance ID") @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
      @RequestParam(value = "fields", required = false) String fields) {
    LOG.debug("findTrainingInstanceById({},{})", id, fields);
    try {
      TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingInstanceResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
        throw ExceptionSorter.throwException(ex);
    }
  }

  @ApiObject(name = "Result info (Page)",
  		description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
	private static class TrainingInstanceRestResource extends PageResultResource<TrainingInstanceDTO>{
	 	 @JsonProperty(required = true)
	 	 @ApiModelProperty(value = "Retrieved Training Instances from databases.")
	 	 private List<TrainingInstanceDTO> content;
	 	 @JsonProperty(required = true)
		 @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
		 private Pagination pagination;
	}

	/**
	 * Get all Training Instances.
	 *
	 * @return all Training Instances.
	 */
  @ApiOperation(httpMethod = "GET",
      value = "Get all Training Instances.",
      response = TrainingInstanceRestResource.class,
      responseContainer = "Page",
      nickname = "findAllTrainingInstances",
      produces = "application/json"
  )
  @ApiResponses(value = {
  		@ApiResponse(code = 200, message = "All training instances found.", response = TrainingInstanceDTO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Unexpected condition was encountered.")
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingInstances(@QuerydslPredicate(root = TrainingInstance.class) Predicate predicate, 
  		@ApiParam(value = "Pagination support.", required = false)
  		Pageable pageable,
  		@ApiParam(value = "Parameters for filtering the objects.", required = false)
      @RequestParam MultiValueMap<String, String> parameters,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
      @RequestParam(value = "fields", required = false) String fields) {
      LOG.debug("findAllTrainingInstances({},{})", parameters, fields);
      PageResultResource<TrainingInstanceDTO> trainingInstanceResource = trainingInstanceFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingInstanceResource), HttpStatus.OK);
  }

	@ApiOperation(httpMethod = "POST",
			value = "Create Training Instance",
			response = TrainingInstanceDTO.class,
			nickname = "createTrainingInstance",
			produces = "application/json",
			consumes = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Training instance created.", response = NewTrainingInstanceDTO.class),
			@ApiResponse(code = 400, message = "Given training instance is not valid."),
			@ApiResponse(code = 500, message = "Unexpected condition was encountered.")
	})
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createTrainingInstance(@ApiParam(name = "Training instance to be created") @Valid @RequestBody TrainingInstanceCreateDTO trainingInstanceCreateDTO,
			@ApiParam(value = "Fields which should be returned in REST API response", required = false)
			@RequestParam(value = "fields", required = false) String fields) {
			try {
					NewTrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.create(trainingInstanceCreateDTO);
					Squiggly.init(objectMapper, fields);
					return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, trainingInstanceResource), HttpStatus.OK);
			} catch (FacadeLayerException ex) {
					throw ExceptionSorter.throwException(ex);
			}
	}
	
	@ApiOperation(httpMethod = "PUT",
			value = "Update Training Instance",
			response = String.class,
			nickname = "updateTrainingInstance",
			consumes = "application/json")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Training instance updated.", response = String.class),
			@ApiResponse(code = 400, message = "Given training instance is not valid."),
			@ApiResponse(code = 404, message = "Training instance with given id not found."),
			@ApiResponse(code = 409, message = "Starting time of instance must be in future."),
			@ApiResponse(code = 500, message = "Unexpected condition was encountered.")
	})
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateTrainingInstance(@ApiParam(name = "Training instance to be updated") @RequestBody TrainingInstanceUpdateDTO trainingInstanceUpdateDTO){
			try {
					String newPass = trainingInstanceFacade.update(trainingInstanceUpdateDTO);
					return new ResponseEntity<>(newPass, HttpStatus.OK);
			} catch (FacadeLayerException ex) {
					throw ExceptionSorter.throwException(ex);
			}
	}
	@ApiOperation(httpMethod = "DELETE",
			value = "Delete TrainingInstance",
			nickname = "deleteTrainingInstance"
			)
	@ApiResponses( value = {
			@ApiResponse(code = 200, message = "Training instance deleted."),
			@ApiResponse(code = 404, message = "Training instance with given id not found."),
			@ApiResponse(code = 409, message = "Only finished instances can be deleted."),
			@ApiResponse(code = 500, message = "Unexpected condition was encountered.")
	})
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> deleteTrainingInstance(@ApiParam(value = "Id of training instance to be deleted") @PathVariable(value = "id") Long id) {
			try {
					trainingInstanceFacade.delete(id);
					return new ResponseEntity<>(HttpStatus.OK);
			} catch (FacadeLayerException ex) {
					throw ExceptionSorter.throwException(ex);
			}
	
	}

	@ApiOperation(httpMethod = "POST",
			value = "Allocate sandboxes",
			response = Void.class,
			nickname = "allocateSandboxes")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Sandboxes have been allocated."),
			@ApiResponse(code = 404, message = "Training instance with given id not found."),
			@ApiResponse(code = 500, message = "Unexpected condition was encountered.")
	})
	@PostMapping(value = "/{instanceId}/sandbox-instances")
	public ResponseEntity<Void> allocateSandboxes(
			@ApiParam(value = "Id of trainingInstance")
			@PathVariable(value = "instanceId") Long instanceId) {
			try{
					return trainingInstanceFacade.allocateSandboxes(instanceId);
			} catch (FacadeLayerException ex){
					throw new ResourceNotFoundException(ex.getLocalizedMessage());
			}
	}


}
