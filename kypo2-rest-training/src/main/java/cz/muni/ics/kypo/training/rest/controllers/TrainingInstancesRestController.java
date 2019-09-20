package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.RestResponses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceIsFinishedInfoDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * The type Training instances rest controller.
 *
 * @author Pavel Šeda
 * @author Boris Jadus
 */
@Api(value = "/training-instances", tags = "Training instances", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(path = "/training-instances", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingInstancesRestController {

    private TrainingInstanceFacade trainingInstanceFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Training instances rest controller.
     *
     * @param trainingInstanceFacade the training instance facade
     * @param objectMapper           the object mapper
     */
    @Autowired
    public TrainingInstancesRestController(TrainingInstanceFacade trainingInstanceFacade, ObjectMapper objectMapper) {
        this.trainingInstanceFacade = trainingInstanceFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Get requested Training Instance by id.
     *
     * @param id     of Training Instance to return.
     * @param fields attributes of the object to be returned as the result.
     * @return Requested Training Instance by id.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get training instance by Id.",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "findTrainingInstanceById",
            notes = "Returns training instance by id and also contains particular training definition in it.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance found", response = TrainingInstanceDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findTrainingInstanceById(
            @ApiParam(value = "Training instance ID", required = true) @PathVariable Long id,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false) @RequestParam(value = "fields", required = false) String fields) {
        try {
            TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.findById(id);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiModel(value = "TrainingInstanceRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class TrainingInstanceRestResource extends PageResultResource<TrainingInstanceDTO> {
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
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @return all Training Instances.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all training instances.",
            response = TrainingInstanceRestResource.class,
            responseContainer = "Page",
            nickname = "findAllTrainingInstances",
            produces = MediaType.APPLICATION_JSON_VALUE
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
        PageResultResource<TrainingInstanceDTO> trainingInstanceResource = trainingInstanceFacade.findAll(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
    }

    /**
     * Create new training instance.
     *
     * @param trainingInstanceCreateDTO the training instance to be created
     * @param fields                    attributes of the object to be returned as the result.
     * @return the newly created instance
     */
    @ApiOperation(httpMethod = "POST",
            value = "Create training instance",
            notes = "This can only be done by the organizer or administrator",
            response = TrainingInstanceDTO.class,
            nickname = "createTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Training instance created.", response = TrainingInstanceCreateDTO.class),
            @ApiResponse(code = 400, message = "Given training instance is not valid."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTrainingInstance(@ApiParam(value = "Training instance to be created", required = true) @Valid @RequestBody TrainingInstanceCreateDTO trainingInstanceCreateDTO,
                                                         @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                         @RequestParam(value = "fields", required = false) String fields) {
        try {
            TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.create(trainingInstanceCreateDTO);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Update training instance.
     *
     * @param trainingInstanceUpdateDTO the training instance to be updated
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Update training instance",
            notes = "This can only be done by organizer of training instance or administrator",
            response = String.class,
            nickname = "updateTrainingInstance",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found"),
            @ApiResponse(code = 409, message = "The requested resource was not deleted because of its finish time")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateTrainingInstance(@ApiParam(value = "Training instance to be updated") @RequestBody @Valid TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        try {
            String newToken = trainingInstanceFacade.update(trainingInstanceUpdateDTO);
            return new ResponseEntity<>(newToken, HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Delete training instance.
     *
     * @param id the id of instance to be deleted
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete training instance",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "deleteTrainingInstance"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance updated.", response = String.class),
            @ApiResponse(code = 400, message = "Given training instance is not valid."),
            @ApiResponse(code = 404, message = "Training instance with given id not found."),
            @ApiResponse(code = 409, message = "Starting time of instance must be in future."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTrainingInstance(@ApiParam(value = "Id of training instance to be deleted", required = true)
                                                       @PathVariable(value = "id") Long id) {
        try {
            trainingInstanceFacade.delete(id);
            return ResponseEntity.ok().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Allocate sandboxes.
     *
     * @param instanceId the instance id
     * @param count      the number of sandboxes tobe allocated
     */
    @ApiOperation(httpMethod = "POST",
            value = "Allocate sandboxes",
            response = Void.class,
            nickname = "allocateSandboxes"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sandboxes have been allocated."),
            @ApiResponse(code = 404, message = "Training instance with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(path = "/{instanceId}/sandbox-instances")
    public ResponseEntity<Void> allocateSandboxes(
            @ApiParam(value = "Id of training instance for which sandboxes are allocated", required = true)
            @PathVariable(value = "instanceId") Long instanceId,
            @ApiParam(value = "Number of sandboxes that will be created", required = false)
            @RequestParam(value = "count", required = false) Integer count) {
        try {
            trainingInstanceFacade.allocateSandboxes(instanceId, count);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Create pool for sandboxes.
     *
     * @param instanceId the instance id
     * @return the id of new pool
     */
    @ApiOperation(httpMethod = "POST",
            value = "Create pool",
            response = Long.class,
            nickname = "createPool"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Pool has been created."),
            @ApiResponse(code = 404, message = "Training instance with given id not found."),
            @ApiResponse(code = 409, message = "Pool has been already created before."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(path = "/{instanceId}/pools")
    public ResponseEntity<Long> createPoolForSandboxes(
            @ApiParam(value = "Id of training instance for which pool is created", required = true)
            @PathVariable(value = "instanceId") Long instanceId) {
        try {
            Long poolId = trainingInstanceFacade.createPoolForSandboxes(instanceId);
            return new ResponseEntity<>(poolId, HttpStatus.CREATED);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiModel(description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class TrainingRunRestResource extends PageResultResource<TrainingRunDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Training Runs from databases.")
        private List<TrainingRunDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    /**
     * Get all Training Runs by Training Instance id.
     *
     * @param instanceId the instance id
     * @param isActive   if true, only active runs are returned
     * @param pageable   pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @return all Training Runs in given Training Instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all training runs of specific training instance",
            response = TrainingInstancesRestController.TrainingRunRestResource.class,
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "findAllTrainingRunsByTrainingInstanceId",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training runs in given training instance found.", response = TrainingRunDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/{instanceId}/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingRunsByTrainingInstanceId(
            @ApiParam(value = "Training Instance Id", required = true) @PathVariable Long instanceId,
            @ApiParam(value = "If only active or not active training runs should be returned.")
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @ApiParam(value = "Pagination support.") Pageable pageable,
            @ApiParam(value = "Parameters for filtering the objects.", required = false)
            @RequestParam MultiValueMap<String, String> parameters,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            PageResultResource<TrainingRunDTO> trainingRunResource =
                    trainingInstanceFacade.findTrainingRunsByTrainingInstance(instanceId, isActive, pageable);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingRunResource));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Delete sandboxes.
     *
     * @param instanceId the instance id
     * @param sandboxIds the sandbox ids
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete sandboxes from training instance",
            response = Void.class,
            nickname = "deleteSandboxes")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Sandboxes were removed from training instance"),
            @ApiResponse(code = 404, message = "Training instance with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered")
    })
    @DeleteMapping(path = "/{instanceId}/sandbox-instances")
    public ResponseEntity<Void> deleteSandboxes(
            @ApiParam(value = "Id of training instance for which sandboxes are deleted", required = true)
            @PathVariable(value = "instanceId") Long instanceId,
            @ApiParam(value = "Ids of sandboxes that will be deleted", required = true)
            @RequestParam(value = "sandboxIds", required = true) Set<Long> sandboxIds) {
        try {
            trainingInstanceFacade.deleteSandboxes(instanceId, sandboxIds);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "GET",
            value = "Check if training instance can be safely deleted",
            response = TrainingInstanceIsFinishedInfoDTO.class,
            nickname = "checkIfInstanceCanBeDeleted")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Information about safe deletion was gathered"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered")
    })
    @GetMapping(path = "/{instanceId}/deletion-info")
    public ResponseEntity<TrainingInstanceIsFinishedInfoDTO> checkIfInstanceCanBeDeleted(
            @ApiParam(value = "Id of training instance for which deletion info is gathered", required = true)
            @PathVariable(value = "instanceId") Long instanceId){
        return ResponseEntity.ok(trainingInstanceFacade.checkIfInstanceCanBeDeleted(instanceId));
    }

}
