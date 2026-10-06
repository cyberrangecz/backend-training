package cz.cyberrange.platform.training.rest.controllers;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AccessTokenDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.event.AbstractEventDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.TrainingInstanceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** The rest controller for Training instances */
@Tag(
    name = "Training instances",
    description =
        "Scheduled occurrences of a training definition, which trainees enter with an access token")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
  @ApiResponse(
      responseCode = "401",
      description = "Missing or invalid bearer token.",
      content = @Content(schema = @Schema(implementation = ApiError.class))),
  @ApiResponse(
      responseCode = "403",
      description = "The caller lacks the required role or relationship.",
      content = @Content(schema = @Schema(implementation = ApiError.class))),
  @ApiResponse(
      responseCode = "500",
      description = "Unexpected server error.",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
})
@RestController
@RequestMapping(path = "/training-instances", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingInstancesRestController {

  private TrainingInstanceFacade trainingInstanceFacade;

  @Autowired
  public TrainingInstancesRestController(TrainingInstanceFacade trainingInstanceFacade) {
    this.trainingInstanceFacade = trainingInstanceFacade;
  }

  /**
   * Returns the training instance for the given id, including its training definition.
   *
   * @param id id of the training instance to return.
   * @return the requested training instance.
   */
  @Operation(
      operationId = "findTrainingInstanceById",
      summary = "Find one training instance with its definition",
      description =
          "A training administrator may read any instance. Anyone else has to be one of its"
              + " organizers.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The instance and its training definition."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingInstanceDTO> findTrainingInstanceById(
      @PathVariable("instanceId") Long id) {
    TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.findById(id);
    return ResponseEntity.ok(trainingInstanceResource);
  }

  /**
   * Get Training instance access token by pool id.
   *
   * @param poolId id of the assigned pool.
   * @return Requested access token by pool id.
   * @throws EntityNotFoundException no training instance holds this pool.
   */
  @Operation(
      operationId = "findTrainingInstanceAccessTokenByPoolId",
      summary = "Find the access token of the instance holding a pool",
      description =
          "Any training organizer or training administrator may call it, whether or not they run"
              + " that instance.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The access token."),
    @ApiResponse(
        responseCode = "400",
        description = "The pool id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance holds this pool.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/access/{poolId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTokenDTO> findInstanceAccessTokenByPoolId(
      @PathVariable("poolId") Long poolId) {
    String accessToken = trainingInstanceFacade.findInstanceAccessTokenByPoolId(poolId);
    return ResponseEntity.ok(new AccessTokenDTO(accessToken));
  }

  /**
   * Get all Training Instances.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @return all Training Instances.
   */
  @Operation(
      operationId = "findAllTrainingInstances",
      summary = "List training instances matching a filter",
      description =
          "A training administrator sees every instance. Anyone else sees only the instances they"
              + " organize. Text filters match partially and ignore case.")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "The matching instances."))
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<TrainingInstanceFindAllResponseDTO>>
      findAllTrainingInstances(
          @QuerydslPredicate(root = TrainingInstance.class) Predicate predicate,
          @ParameterObject Pageable pageable) {
    PageResultResource<TrainingInstanceFindAllResponseDTO> trainingInstanceResource =
        trainingInstanceFacade.findAll(predicate, pageable);
    return ResponseEntity.ok(trainingInstanceResource);
  }

  /**
   * Create new Training Instance.
   *
   * @param trainingInstanceCreateDTO the Training Instance to be created
   * @return the newly created instance
   */
  @Operation(
      operationId = "createTrainingInstance",
      summary = "Create a training instance",
      description =
          "A training organizer or a training administrator may call it, and is added as an"
              + " organizer. A generated pin is appended to the access token sent. A pool is"
              + " required unless the instance uses a local environment.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created instance."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The request body failed validation, or its pool and local environment settings"
                + " conflict.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description =
            "The times are out of order, or the sandbox lacks a variable the definition needs.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingInstanceDTO> createTrainingInstance(
      @Valid @RequestBody TrainingInstanceCreateDTO trainingInstanceCreateDTO) {
    TrainingInstanceDTO trainingInstanceResource =
        trainingInstanceFacade.create(trainingInstanceCreateDTO);
    return ResponseEntity.ok(trainingInstanceResource);
  }

  /**
   * Updates a training instance. Refuses the update if the instance has already started and the
   * assigned training definition would change, and validates that the referenced sandbox definition
   * or pool exposes every variable name the training definition requires.
   *
   * @param trainingInstanceUpdateDTO the training instance to be updated
   * @return the access token in effect after the update, whether it changed or was kept
   */
  @Operation(
      operationId = "updateTrainingInstance",
      summary = "Update a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. Once the"
              + " instance has started, its definition, start time, access token and pool are"
              + " fixed. Replacing the pool unlocks the old one and deletes its recorded console"
              + " commands.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The access token in effect after the update."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The request body failed validation, or its pool and local environment settings"
                + " conflict.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance, or no training definition, with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The instance has started and a fixed field changed.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTokenDTO> updateTrainingInstance(
      @RequestBody @Valid TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
    String newToken = trainingInstanceFacade.update(trainingInstanceUpdateDTO);
    return ResponseEntity.ok(new AccessTokenDTO(newToken));
  }

  /**
   * Deletes a training instance together with its training runs, cheating detections and audited
   * events. Unless {@code forceDelete} is set, refuses to delete an instance that has not finished
   * and still has training runs, and refuses to delete an instance with a pool still assigned.
   *
   * @param id id of the training instance to be deleted
   * @param forceDelete indicates if the instance should be deleted regardless of these checks.
   */
  @Operation(
      operationId = "deleteTrainingInstance",
      summary = "Delete a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. Its training"
              + " runs, cheating detections and audited events go with it. Forcing the delete also"
              + " unlocks the assigned pool.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The instance was deleted."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id or the force flag has an invalid value.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The instance is unfinished and has runs, or still has a pool assigned.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @DeleteMapping(path = "/{instanceId}")
  public ResponseEntity<Void> deleteTrainingInstance(
      @PathVariable("instanceId") Long id,
      @Parameter(description = "Delete even when the instance has runs or a pool assigned.")
          @RequestParam(value = "forceDelete", required = false)
          boolean forceDelete) {
    trainingInstanceFacade.delete(id, forceDelete);
    return ResponseEntity.ok().build();
  }

  /**
   * Assigns a sandbox pool to a training instance that currently has none. Refuses instances
   * running in a local environment and instances that already have a pool assigned.
   *
   * @param id id of the training instance to update
   * @param trainingInstanceAssignPoolIdDTO carries the id of the pool to assign
   * @return the training instance after the pool assignment
   */
  @Operation(
      operationId = "assignPool",
      summary = "Assign a sandbox pool to a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. The pool is"
              + " locked with the instance's access token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The instance after the pool was assigned."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance uses a local environment, or the pool id is missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The instance already has a pool assigned.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PatchMapping(path = "/{instanceId}/assign-pool", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingInstanceBasicInfoDTO> assignPool(
      @PathVariable("instanceId") Long id,
      @Valid @RequestBody TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
    return ResponseEntity.ok(
        trainingInstanceFacade.assignPoolToTrainingInstance(id, trainingInstanceAssignPoolIdDTO));
  }

  /**
   * Unassigns the sandbox pool currently assigned to a training instance, unlocking the pool and
   * deleting its recorded console commands. Refuses an instance with no pool assigned.
   *
   * @param id id of the training instance to update
   * @return the training instance after the pool removal
   */
  @Operation(
      operationId = "unassignPool",
      summary = "Remove the sandbox pool from a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. The pool is"
              + " unlocked and its recorded console commands are deleted.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The instance after the pool was removed."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The instance has no pool assigned.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PatchMapping(path = "/{instanceId}/unassign-pool")
  public ResponseEntity<TrainingInstanceBasicInfoDTO> unassignPool(
      @PathVariable("instanceId") Long id) {
    return ResponseEntity.ok(trainingInstanceFacade.unassignPoolInTrainingInstance(id));
  }

  /**
   * Get all Training Runs by Training Instance id.
   *
   * @param instanceId the Training Instance id
   * @param isActive if true, only active Training Runs are returned
   * @param pageable Pageable parameter with information about pagination.
   * @return all Training Runs in given Training Instance.
   */
  @Operation(
      operationId = "findAllTrainingRunsByTrainingInstanceId",
      summary = "List the training runs of a training instance",
      description = "A training administrator, or one of the instance's organizers, may call it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching runs."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id or the active flag has an invalid value.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{instanceId}/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<TrainingRunDTO>> findAllTrainingRunsByTrainingInstanceId(
      @PathVariable("instanceId") Long instanceId,
      @Parameter(description = "True keeps the runs that are not archived, false keeps the rest.")
          @RequestParam(value = "isActive", required = false)
          Boolean isActive,
      @ParameterObject Pageable pageable) {
    PageResultResource<TrainingRunDTO> trainingRunResource =
        trainingInstanceFacade.findTrainingRunsByTrainingInstance(instanceId, isActive, pageable);
    return ResponseEntity.ok(trainingRunResource);
  }

  /**
   * Get requested organizers of training instance.
   *
   * @param trainingInstanceId id of training instance for which to get the organizers
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return organizers already assigned to the training instance, filtered by name.
   */
  @Operation(
      operationId = "getOrganizersOfTrainingInstance",
      summary = "List the organizers of a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. The names"
              + " come from the user and group service.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching organizers."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{instanceId}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserRefDTO>> getOrganizersOfTrainingInstance(
      @PathVariable("instanceId") Long trainingInstanceId,
      @RequestParam(value = "givenName", required = false) String givenName,
      @RequestParam(value = "familyName", required = false) String familyName,
      @ParameterObject Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingInstanceFacade.getOrganizersOfTrainingInstance(
            trainingInstanceId, pageable, givenName, familyName);
    return ResponseEntity.ok(designers);
  }

  /**
   * Get requested organizers not in given training instance.
   *
   * @param trainingInstanceId id ot the training instance
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return organizers not in the given training instance, filtered by name.
   */
  @Operation(
      operationId = "findOrganizersNotInGivenTrainingInstance",
      summary = "List organizers not in a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. Only users"
              + " holding the training organizer role are returned.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching users."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(
      path = "{instanceId}/organizers-not-in-training-instance",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserRefDTO>> getOrganizersNotInGivenTrainingInstance(
      @PathVariable("instanceId") Long trainingInstanceId,
      @RequestParam(value = "givenName", required = false) String givenName,
      @RequestParam(value = "familyName", required = false) String familyName,
      @ParameterObject Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(
            trainingInstanceId, pageable, givenName, familyName);
    return ResponseEntity.ok(designers);
  }

  /**
   * Adds and removes organizers of the training instance in one call. The caller's own user
   * reference id is dropped from {@code organizersRemoval} before it is applied, so a caller cannot
   * remove itself as organizer through this endpoint.
   *
   * @param trainingInstanceId id of the training instance whose organizers are being edited
   * @param organizersAddition ids of the organizers to be added to the training instance.
   * @param organizersRemoval ids of the organizers to be removed from the training instance.
   */
  @Operation(
      operationId = "editOrganizers",
      summary = "Add and remove organizers of a training instance",
      description =
          "A training administrator, or one of the instance's organizers, may call it. The caller's"
              + " own id is dropped from the removals, so a caller never removes itself.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "The organizers were updated."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id or one of the user ids is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(path = "/{instanceId}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> editOrganizers(
      @PathVariable("instanceId") Long trainingInstanceId,
      @Parameter(description = "User reference ids to add as organizers.")
          @RequestParam(value = "organizersAddition", required = false)
          Set<Long> organizersAddition,
      @Parameter(description = "User reference ids to drop from the organizers.")
          @RequestParam(value = "organizersRemoval", required = false)
          Set<Long> organizersRemoval) {
    trainingInstanceFacade.editOrganizers(
        trainingInstanceId, organizersAddition, organizersRemoval);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get Training Instances by their ids.
   *
   * @param ids the ids of Training Instances to return.
   * @return List of requested Training Instances.
   */
  @Operation(
      operationId = "findTrainingInstancesByIds",
      summary = "Find training instances by their ids",
      description =
          "A training administrator may call it. Anyone else has to organize, or take part in,"
              + " every instance listed.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching instances."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing or not numbers.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TrainingInstanceBasicDTO>> findTrainingInstancesByIds(
      @RequestParam(value = "ids", required = true) List<Long> ids) {
    List<TrainingInstanceBasicDTO> trainingInstances =
        trainingInstanceFacade.findTrainingInstancesByIds(ids);
    return ResponseEntity.ok(trainingInstances);
  }

  /**
   * Returns training events for the given instance, filtered by event type and timestamp.
   *
   * @param instanceId the id of the Training Instance whose events to retrieve.
   * @param eventType the type of events to retrieve; passing {@code "COMMAND"} retrieves console
   *     command events read from the pool assigned to the instance instead of audit events.
   * @param sinceTimestamp lower bound timestamp in epoch milliseconds, exclusive.
   * @return list of events matching the given criteria.
   */
  @Operation(
      operationId = "getTrainingInstanceEvents",
      summary = "List the events recorded in a training instance",
      description =
          "An administrator, an organizer of the instance, or one of its participants may call it."
              + " A participant sees only their own answers and console commands. Every sandbox id"
              + " other than their own reaches them hashed.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching events."),
    @ApiResponse(
        responseCode = "400",
        description = "The instance id or the timestamp is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{instanceId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AbstractEventDTO>> getTrainingInstanceEvents(
      @PathVariable("instanceId") Long instanceId,
      @Parameter(
              description =
                  "Type of event to return; COMMAND returns console commands instead of audit"
                      + " events.")
          @RequestParam(value = "eventType")
          String eventType,
      @Parameter(description = "Lower bound in epoch milliseconds, exclusive.")
          @RequestParam(value = "sinceTimestamp")
          long sinceTimestamp) {
    List<AbstractEventDTO> events =
        trainingInstanceFacade.getTrainingInstanceEvents(instanceId, eventType, sinceTimestamp);
    return ResponseEntity.ok(events);
  }
}
