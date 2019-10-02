package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.UserDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.commons.security.mapping.UserInfoDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
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
            response = TrainingInstanceDTO.class,
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

    /**
     * Get requested organizers for training instance.
     *
     * @param pageable   pageable parameter with information about pagination.
     * @param trainingInstanceId id of training instance for which to get the organizers
     * @return List of users login and full name with role designer.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get organizer for training instance.",
            response = UserInfoRestResource.class,
            nickname = "getOrganizersForTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Organizers for training instance found."),
            @ApiResponse(code = 404, message = "Training instance not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(path = "/{id}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOrganizersForTrainingInstance(@ApiParam(value = "ID of training instance for which to retrieve the organizers.", required = true)
                                                                   @PathVariable(value = "id", required = true) Long trainingInstanceId,
                                                                   @ApiParam(value = "Given name filter.", required = true)
                                                                   @RequestParam(value = "givenName", required = false) String givenName,
                                                                   @ApiParam(value = "Family name filter.", required = true)
                                                                   @RequestParam(value = "familyName", required = false) String familyName,
                                                                   Pageable pageable) {
        try {
            PageResultResource<UserRefDTO> designers = trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstanceId, pageable, givenName, familyName);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiModel(value = "UserInfoRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class UserInfoRestResource extends PageResultResource<UserInfoDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Training Instances from databases.")
        private List<UserInfoDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    /**
     * Get requested organizers not in given training instance.
     *
     * @param trainingInstanceId id ot the training instance
     * @param pageable   pageable parameter with information about pagination.
     * @return List of users login and full name with role organizer.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get organizers not in given training instance.",
            response = UserInfoRestResource.class,
            nickname = "findOrganizersNotInGivenTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Organizers found.", response = UserInfoRestResource.class),
            @ApiResponse(code = 404, message = "Training instance not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")

    })
    @ApiPageableSwagger
    @GetMapping(path = "{id}/organizers-not-in-training-instance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOrganizersNotInGivenTrainingInstance(@ApiParam(value = "ID of the training instance which do not contains organizers you want to retrieve.", required = true)
                                                                           @PathVariable(value = "id") Long trainingInstanceId,
                                                                          @ApiParam(value = "Given name filter.", required = false)
                                                                          @RequestParam(value = "givenName", required = false) String givenName,
                                                                          @ApiParam(value = "Family name filter.", required = false)
                                                                          @RequestParam(value = "familyName", required = false) String familyName,
                                                                           Pageable pageable) {
        try {
            PageResultResource<UserRefDTO> designers = trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstanceId, pageable, givenName, familyName);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Concurrently add/remove organizers with given ids to/from the training instance.
     *
     * @param trainingInstanceId id of training instance for which to retrieve organizers
     * @param organizersAddition ids of the organizers to be added to the training instance.
     * @param organizersRemoval ids of the organizers to be removed from the training instance.
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Edit organizers.",
            response = Void.class,
            nickname = "editOrganizers",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Organizers edited."),
            @ApiResponse(code = 404, message = "Training instance with given id has not been found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @PutMapping(path = "/{id}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> editOrganizers(@ApiParam(value = "ID of training instance to be updated.", required = true)
                                            @PathVariable(value = "id", required = true) Long trainingInstanceId,
                                            @ApiParam(value = "Ids of the organizers to be added to the training instance.")
                                            @RequestParam(value = "organizersAddition", required = false) Set<Long> organizersAddition,
                                            @ApiParam(value = "Ids of the organizers to be removed from the training instance.")
                                            @RequestParam(value = "organizersRemoval", required = false) Set<Long> organizersRemoval) {
        try {
            trainingInstanceFacade.editOrganizers(trainingInstanceId, organizersAddition, organizersRemoval);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }
}
