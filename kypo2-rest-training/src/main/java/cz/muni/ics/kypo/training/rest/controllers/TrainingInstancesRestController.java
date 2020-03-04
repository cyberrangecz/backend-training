package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
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
 */
@Api(value = "/training-instances", tags = "Training instances", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = JavaApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = JavaApiError.class)
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
     * @param id     id of the Training Instance to return.
     * @param fields attributes of the object to be returned as the result.
     * @return Requested Training Instance by id.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get training instance by id.",
            response = TrainingInstanceDTO.class,
            nickname = "findTrainingInstanceById",
            notes = "Returns training instance by id and also contains particular training definition in it.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance found", response = TrainingInstanceDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findTrainingInstanceById(
            @ApiParam(value = "Training instance ID", required = true) @PathVariable Long id,
            @ApiParam(value = "Fields which should be returned in REST API response") @RequestParam(value = "fields", required = false) String fields) {
        TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.findById(id);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
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
            nickname = "findAllTrainingInstances",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training instances found.", response = TrainingInstanceRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingInstances(@QuerydslPredicate(root = TrainingInstance.class) Predicate predicate,
                                                           @ApiParam(value = "Pagination support.", required = false)
                                                                   Pageable pageable,
                                                           @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                           @RequestParam MultiValueMap<String, String> parameters,
                                                           @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                           @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<TrainingInstanceFindAllResponseDTO> trainingInstanceResource = trainingInstanceFacade.findAll(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
    }

    /**
     * Create new Training Instance.
     *
     * @param trainingInstanceCreateDTO the Training Instance to be created
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
            @ApiResponse(code = 200, message = "Training instance created.", response = TrainingInstanceCreateDTO.class),
            @ApiResponse(code = 400, message = "Given training instance is not valid.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTrainingInstance(@ApiParam(value = "Training instance to be created", required = true) @Valid @RequestBody TrainingInstanceCreateDTO trainingInstanceCreateDTO,
                                                         @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                         @RequestParam(value = "fields", required = false) String fields) {
        TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.create(trainingInstanceCreateDTO);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
    }

    /**
     * Update Training Instance.
     *
     * @param trainingInstanceUpdateDTO the Training Instance to be updated
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Update training instance",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "updateTrainingInstance",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance updated."),
            @ApiResponse(code = 404, message = "The requested resource was not found", response = JavaApiError.class),
            @ApiResponse(code = 409, message = "The requested resource was not deleted because of its finish time", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateTrainingInstance(@ApiParam(value = "Training instance to be updated")
                                                         @RequestBody @Valid TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        String newToken = trainingInstanceFacade.update(trainingInstanceUpdateDTO);
        return new ResponseEntity<>(newToken, HttpStatus.OK);
    }

    /**
     * Delete Training Instance.
     *
     * @param id id of the Training Instance to be deleted
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete training instance",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "deleteTrainingInstance"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance updated."),
            @ApiResponse(code = 400, message = "Given training instance is not valid.", response = JavaApiError.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = JavaApiError.class),
            @ApiResponse(code = 409, message = "The training instance cannot be deleted for the specific reason stated in the error message.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTrainingInstance(@ApiParam(value = "Id of training instance to be deleted", required = true)
                                                       @PathVariable(value = "id") Long id) {
        trainingInstanceFacade.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Allocate Sandboxes.
     *
     * @param instanceId the Training Instance id
     * @param count      the number of Sandboxes tobe allocated
     */
    @ApiOperation(httpMethod = "POST",
            value = "Allocate sandboxes",
            nickname = "allocateSandboxes"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sandboxes have been allocated."),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @PostMapping(path = "/{instanceId}/sandbox-instances")
    public ResponseEntity<Void> allocateSandboxes(
            @ApiParam(value = "Id of training instance for which sandboxes are allocated", required = true)
            @PathVariable(value = "instanceId") Long instanceId,
            @ApiParam(value = "Number of sandboxes that will be created", required = false)
            @RequestParam(value = "count", required = false) Integer count) {
        trainingInstanceFacade.allocateSandboxes(instanceId, count);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    /**
     * Get all Training Runs by Training Instance id.
     *
     * @param instanceId the Training Instance id
     * @param isActive   if true, only active Training Runs are returned
     * @param pageable   Pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @return all Training Runs in given Training Instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all training runs of specific training instance",
            response = TrainingRunsRestController.TrainingRunRestResource.class,
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "findAllTrainingRunsByTrainingInstanceId",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training runs in given training instance found.", response = TrainingRunsRestController.TrainingRunRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @GetMapping(path = "/{instanceId}/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingRunsByTrainingInstanceId(@ApiParam(value = "Training Instance Id", required = true) @PathVariable Long instanceId,
                                                                          @ApiParam(value = "If only active or not active training runs should be returned.")
                                                                          @RequestParam(value = "isActive", required = false) Boolean isActive,
                                                                          @ApiParam(value = "Pagination support.") Pageable pageable,
                                                                          @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                                          @RequestParam MultiValueMap<String, String> parameters,
                                                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                                          @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<TrainingRunDTO> trainingRunResource =
                trainingInstanceFacade.findTrainingRunsByTrainingInstance(instanceId, isActive, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingRunResource));
    }

    /**
     * Delete sandboxes.
     *
     * @param instanceId the instance id
     * @param sandboxIds set of the sandbox ids
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete sandboxes in OpenStack",
            nickname = "deleteSandboxes",
            notes = "Possible errors, when calling PythonAPI, are only logged into syslog."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Sandboxes were removed from training instance"),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered", response = JavaApiError.class)
    })
    @DeleteMapping(path = "/{instanceId}/sandbox-instances")
    public ResponseEntity<Void> deleteSandboxes(@ApiParam(value = "Id of training instance for which sandboxes are deleted", required = true)
                                                @PathVariable(value = "instanceId") Long instanceId,
                                                @ApiParam(value = "Ids of sandboxes that will be deleted", required = true)
                                                @RequestParam(value = "sandboxIds", required = true) Set<Long> sandboxIds) {
        trainingInstanceFacade.deleteSandboxes(instanceId, sandboxIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Get requested organizers of training instance.
     *
     * @param pageable   pageable parameter with information about pagination.
     * @param trainingInstanceId id of training instance for which to get the organizers
     * @return List of users login and full name with role designer.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get organizers of training instance.",
            response = TrainingDefinitionsRestController.UserInfoRestResource.class,
            nickname = "getOrganizersOfTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Organizers for training instance found.", response = TrainingDefinitionsRestController.UserInfoRestResource.class),
            @ApiResponse(code = 404, message = "Training instance not found.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/{id}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOrganizersOfTrainingInstance(@ApiParam(value = "ID of training instance for which to retrieve the organizers.", required = true)
                                                                   @PathVariable(value = "id", required = true) Long trainingInstanceId,
                                                                   @ApiParam(value = "Given name filter.", required = true)
                                                                   @RequestParam(value = "givenName", required = false) String givenName,
                                                                   @ApiParam(value = "Family name filter.", required = true)
                                                                   @RequestParam(value = "familyName", required = false) String familyName,
                                                                   Pageable pageable) {
        PageResultResource<UserRefDTO> designers = trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstanceId, pageable, givenName, familyName);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
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
            response = TrainingDefinitionsRestController.UserInfoRestResource.class,
            nickname = "findOrganizersNotInGivenTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Organizers found.", response = TrainingDefinitionsRestController.UserInfoRestResource.class),
            @ApiResponse(code = 404, message = "Training instance not found.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = JavaApiError.class)

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
        PageResultResource<UserRefDTO> designers = trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstanceId, pageable, givenName, familyName);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
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
            nickname = "editOrganizers"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Organizers edited."),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = JavaApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered. Probably error during calling other microservice.", response = JavaApiError.class)
    })
    @ApiPageableSwagger
    @PutMapping(path = "/{id}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> editOrganizers(@ApiParam(value = "ID of training instance to be updated.", required = true)
                                            @PathVariable(value = "id", required = true) Long trainingInstanceId,
                                            @ApiParam(value = "Ids of the organizers to be added to the training instance.")
                                            @RequestParam(value = "organizersAddition", required = false) Set<Long> organizersAddition,
                                            @ApiParam(value = "Ids of the organizers to be removed from the training instance.")
                                            @RequestParam(value = "organizersRemoval", required = false) Set<Long> organizersRemoval) {
        trainingInstanceFacade.editOrganizers(trainingInstanceId, organizersAddition, organizersRemoval);
        return ResponseEntity.noContent().build();
    }

    @ApiModel(value = "TrainingInstanceRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class TrainingInstanceRestResource extends PageResultResource<TrainingInstanceFindAllResponseDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Training Instances from databases.")
        private List<TrainingInstanceFindAllResponseDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }
}
