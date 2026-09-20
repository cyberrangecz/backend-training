package cz.cyberrange.platform.training.rest.controllers;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.CorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.ValidatePasskeyDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessedTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunBasicDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunByIdDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.ValidateAnswerDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** The rest controller for Training runs */
@Tag(
    name = "Training runs",
    description = "One trainee's passage through the levels of a training instance")
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
@RequestMapping(value = "/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TrainingRunsRestController {

  private TrainingRunFacade trainingRunFacade;

  @Autowired
  public TrainingRunsRestController(TrainingRunFacade trainingRunFacade) {
    this.trainingRunFacade = trainingRunFacade;
  }

  /**
   * Deletes several training runs. A caller who is not an administrator must be an organizer of
   * every listed run. Deleting a run also removes its question answers and submissions, purges its
   * OpenSearch command and training-event data, and releases the concurrent-access lock held for
   * its participant.
   *
   * @param trainingRunIds ids of the training runs to delete; the call is a no-op when this list is
   *     empty
   * @param forceDelete whether a run still in the running state may be deleted anyway
   * @return the response entity
   */
  @Operation(
      operationId = "deleteTrainingRuns",
      summary = "Delete several training runs",
      description =
          "A training administrator may delete any run. A training organizer has to organize every"
              + " run listed. Each run's answers, submissions and recorded events go with it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The runs were deleted."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing or not numbers.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with one of these ids.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "A run is still running and the delete was not forced.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @DeleteMapping
  public ResponseEntity<Void> deleteTrainingRuns(
      @RequestParam(value = "trainingRunIds", required = true) List<Long> trainingRunIds,
      @Parameter(description = "Delete even a run that is still running.")
          @RequestParam(value = "forceDelete", required = false, defaultValue = "false")
          boolean forceDelete) {
    trainingRunFacade.deleteTrainingRuns(trainingRunIds, forceDelete);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Deletes a given training run, along with its question answers and submissions, purges its
   * OpenSearch command and training-event data, and releases the concurrent-access lock held for
   * its participant.
   *
   * @param runId the training run id
   * @param forceDelete whether a run still in the running state may be deleted anyway
   * @return the response entity
   */
  @Operation(
      operationId = "deleteTrainingRun",
      summary = "Delete one training run",
      description =
          "A training administrator, or an organizer of the run, may call it. The run's answers,"
              + " submissions and recorded events go with it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The run was deleted."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id or the force flag has an invalid value.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The run is still running and the delete was not forced.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @DeleteMapping(path = "/{runId}")
  public ResponseEntity<Void> deleteTrainingRun(
      @PathVariable("runId") Long runId,
      @Parameter(description = "Delete even a run that is still running.")
          @RequestParam(value = "forceDelete", required = false, defaultValue = "false")
          boolean forceDelete) {
    trainingRunFacade.deleteTrainingRun(runId, forceDelete);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets a training run by id, with its participant reference resolved from the user management
   * service.
   *
   * @param runId of Training Run to return.
   * @return Requested Training Run by id.
   */
  @Operation(
      operationId = "findTrainingRunById",
      summary = "Find one training run",
      description = "A training administrator, or the run's own participant, may call it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The training run."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingRunByIdDTO> findTrainingRunById(@PathVariable("runId") Long runId) {
    TrainingRunByIdDTO trainingRunResource = trainingRunFacade.findById(runId);
    return new ResponseEntity<>(trainingRunResource, HttpStatus.OK);
  }

  /**
   * Gets a page of all training runs matching the given predicate, each with its participant
   * reference resolved from the user management service.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @return all Training Runs.
   */
  @Operation(
      operationId = "findAllTrainingRuns",
      summary = "List training runs matching a filter",
      description =
          "Only a training administrator may call it. Text filters match partially and ignore"
              + " case.")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "The matching runs."))
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<TrainingRunDTO>> findAllTrainingRuns(
      @QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
      @ParameterObject Pageable pageable) {
    PageResultResource<TrainingRunDTO> trainingRunResource =
        trainingRunFacade.findAll(predicate, pageable);
    return new ResponseEntity<>(trainingRunResource, HttpStatus.OK);
  }

  /**
   * Accesses the training instance identified by the given access token on behalf of the logged in
   * user. Resumes the user's own already-running training run for that instance if one exists;
   * otherwise creates a new run, assigns it a sandbox unless the instance runs in a local
   * environment, and audits the run as started.
   *
   * @param accessToken the access token
   * @return the resumed or newly created run's current level and related run information.
   */
  @Operation(
      operationId = "createTrainingRun",
      summary = "Enter a training instance and start a run",
      description =
          "Only a trainee or a training administrator may call it. A run of the caller already"
              + " under way is resumed instead of a second one being started. A new run is given a"
              + " sandbox unless the instance uses a local environment.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The run's current level and its context."),
    @ApiResponse(
        responseCode = "400",
        description = "The access token is missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description =
            "No sandbox is free in the instance's pool, or the caller lacks the required role or"
                + " relationship.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No instance is open right now under this access token.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description =
            "The caller's existing run cannot be resumed, or the training instance has no sandbox"
                + " pool allocated yet.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "429",
        description = "The caller is already entering this instance.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(
      @Parameter(description = "Access token of the training instance to enter.")
          @RequestParam(value = "accessToken", required = true)
          String accessToken) {
    AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(accessToken);
    return ResponseEntity.ok(accessTrainingRunDTO);
  }

  /**
   * Gets a page of the training runs the logged in user is a participant of.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @param sortByTitle "asc" for ascending alphabetical sort by title, "desc" for descending, or
   *     omitted for no sort
   * @return all accessed Training Runs.
   */
  @Operation(
      operationId = "getAllAccessedTrainingRuns",
      summary = "List the caller's own training runs",
      description =
          "Only a trainee or a training administrator may call it. Text filters match partially and"
              + " ignore case.")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "The caller's runs."))
  @GetMapping(path = "/accessible", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<AccessedTrainingRunDTO>> getAllAccessedTrainingRuns(
      @QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @Parameter(
              description =
                  "asc or desc to order the returned page by title; any other value leaves the"
                      + " order alone.",
              schema = @Schema(example = "asc"))
          @RequestParam(value = "sortByTitle", required = false)
          String sortByTitle) {
    PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS =
        trainingRunFacade.findAllAccessedTrainingRuns(predicate, pageable, sortByTitle);
    return new ResponseEntity<>(accessedTrainingRunDTOS, HttpStatus.OK);
  }

  /**
   * Advances the given training run to its next level and returns that level.
   *
   * @param runId of Training Run for which to get next level.
   * @return Requested next level.
   */
  @Operation(
      operationId = "getNextLevel",
      summary = "Move the run on to its next level",
      description =
          "A training administrator, or the run's own participant, may call it. The run's current"
              + " level advances, and the new level is recorded as started.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The level the run moved to."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id, or the run is on its last level.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The current level has not been answered yet.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/next-levels", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AbstractLevelDTO> getNextLevel(@PathVariable("runId") Long runId) {
    AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(runId);
    return ResponseEntity.ok(levelDTO);
  }

  /**
   * Get solution of current training level.
   *
   * @param runId of Training Run for which to get solution.
   * @return Requested solution of training level.
   */
  @Operation(
      operationId = "getSolution",
      summary = "Reveal the solution of the run's current level",
      description =
          "A training administrator, or the run's own participant, may call it. The first reveal is"
              + " recorded and, where the level penalizes it, wipes out the score still on offer.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The solution text of the current level.",
        content =
            @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
    @ApiResponse(
        responseCode = "400",
        description = "The current level is not a training level.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/solutions", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getSolution(@PathVariable("runId") Long runId) {
    return ResponseEntity.ok(trainingRunFacade.getSolution(runId));
  }

  /**
   * Get hint of current training level.
   *
   * @param runId of Training Run for which to get hint.
   * @param hintId the hint id
   * @return Requested hint of training level.
   */
  @Operation(
      operationId = "getHint",
      summary = "Take a hint of the run's current level",
      description =
          "A training administrator, or the run's own participant, may call it. Taking the hint is"
              + " recorded and adds its penalty to the run.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The hint."),
    @ApiResponse(
        responseCode = "400",
        description = "The current level is not a training level.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run, or no hint, with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The hint belongs to another level than the run's current one.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/hints/{hintId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HintDTO> getHint(
      @PathVariable("runId") Long runId, @PathVariable Long hintId) {
    HintDTO hintDTO = trainingRunFacade.getHint(runId, hintId);
    return ResponseEntity.ok(hintDTO);
  }

  /**
   * Checks a submitted answer against the current level of the given training run.
   *
   * @param runId the run id
   * @param validateAnswerDTO submitted answer.
   * @return whether the answer was correct, the attempts remaining, and the solution once attempts
   *     are exhausted.
   */
  @Operation(
      operationId = "isCorrectAnswer",
      summary = "Check an answer against the run's current level",
      description =
          "A training administrator, or the run's own participant, may call it. Every submission is"
              + " recorded. Once no attempts remain, the response carries the solution.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200", description = "The verdict and the attempts still remaining."),
    @ApiResponse(
        responseCode = "400",
        description = "The current level is not a training level.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The current level has already been answered.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(path = "/{runId}/is-correct-answer", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IsCorrectAnswerDTO> isCorrectAnswer(
      @PathVariable("runId") Long runId, @RequestBody @Valid ValidateAnswerDTO validateAnswerDTO) {
    return ResponseEntity.ok(
        trainingRunFacade.isCorrectAnswer(runId, validateAnswerDTO.getAnswer()));
  }

  /**
   * Check if submitted passkey is correct.
   *
   * @param runId the run id
   * @param validatePasskeyDTO submitted passkey.
   * @return True if passkey is correct, false if passkey is wrong.
   */
  @Operation(
      operationId = "isCorrectPasskey",
      summary = "Check a passkey against the run's current level",
      description =
          "A training administrator, or the run's own participant, may call it. Every attempt is"
              + " recorded, the successful one included.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "True when the passkey matches."),
    @ApiResponse(
        responseCode = "400",
        description = "The current level is not an access level.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The current level has already been answered.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(path = "/{runId}/is-correct-passkey", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> isCorrectPasskey(
      @PathVariable("runId") Long runId,
      @RequestBody @Valid ValidatePasskeyDTO validatePasskeyDTO) {
    return ResponseEntity.ok(
        trainingRunFacade.isCorrectPasskey(runId, validatePasskeyDTO.getPasskey()));
  }

  /**
   * Re-enters an existing training run, refused once the run is finished or archived, its training
   * instance has ended, its pool assignment is missing, or its sandbox has been deleted on a
   * non-local instance. When the current level is a training level, the response also carries any
   * solution and hints the participant already took for it.
   *
   * @param runId id of training run.
   * @return current level of training run.
   */
  @Operation(
      operationId = "resumeTrainingRun",
      summary = "Resume a training run",
      description =
          "A training administrator, or the run's own participant, may call it. On a training"
              + " level, the response also carries the solution and hints already taken there.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The run's current level and its context."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description =
            "The run is finished or archived, its instance has ended, or its sandbox is gone.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/resumption", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTrainingRunDTO> resumeTrainingRun(@PathVariable("runId") Long runId) {
    AccessTrainingRunDTO resumedTrainingRunDTO = trainingRunFacade.resumeTrainingRun(runId);
    return ResponseEntity.ok(resumedTrainingRunDTO);
  }

  /**
   * Finishes the given training run. The call blocks for a fixed delay after finishing to let the
   * run's audited events propagate before returning.
   *
   * @param runId id of training run.
   * @return the response entity
   */
  @Operation(
      operationId = "finishTrainingRun",
      summary = "Finish a training run",
      description =
          "A training administrator, or the run's own participant, may call it. The call waits a"
              + " short while so the run's recorded events settle before it answers.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The run was finished."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The run is not on its last level, or that level is unanswered.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> finishTrainingRun(@PathVariable("runId") Long runId) {
    trainingRunFacade.finishTrainingRun(runId);
    return ResponseEntity.ok().build();
  }

  /**
   * Evaluate responses to assessment.
   *
   * @param runId id of training run.
   * @param responses to assessment
   * @return the response entity
   */
  @Operation(
      operationId = "evaluateResponsesToAssessment",
      summary = "Submit answers to the run's assessment level",
      description =
          "A training administrator, or the run's own participant, may call it. The answers are"
              + " stored and the level is marked answered. A test level is scored as well.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "The answers were stored."),
    @ApiResponse(
        responseCode = "400",
        description = "The current level is not an assessment level.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The current level has already been answered.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(
      value = "/{runId}/assessment-evaluations",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> evaluateResponsesToAssessment(
      @PathVariable("runId") Long runId, @Valid @RequestBody List<QuestionAnswerDTO> responses) {
    trainingRunFacade.evaluateResponsesToAssessment(runId, responses);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get requested participant of the given training run.
   *
   * @param trainingRunId id of training run for which to get participant
   * @return Participant of specific training run.
   */
  @Operation(
      operationId = "getParticipant",
      summary = "Find the participant of a training run",
      description =
          "A training administrator, or the run's own participant, may call it. The details come"
              + " from the user and group service.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The participant of the run."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/participant", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserRefDTO> getParticipant(@PathVariable("runId") Long trainingRunId) {
    UserRefDTO participant = trainingRunFacade.getParticipant(trainingRunId);
    return ResponseEntity.ok(participant);
  }

  /**
   * Archive training run.
   *
   * @param runId id of training run.
   * @return the response entity
   */
  @Operation(
      operationId = "archiveTrainingRun",
      summary = "Archive a training run",
      description =
          "A training administrator, or an organizer of the run, may call it. The run's sandbox"
              + " reference moves to its previous sandbox and is cleared.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The run was archived."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PatchMapping(path = "/{runId}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> archiveTrainingRun(@PathVariable("runId") Long runId) {
    trainingRunFacade.archiveTrainingRun(runId);
    return ResponseEntity.ok().build();
  }

  /**
   * Gets the correct answer of every training level in the definition backing the given training
   * run, ordered by level order. For a level using variant answers, the correct answer is looked up
   * per participant from the answer storage service.
   *
   * @param runId of Training Run for which to get correct answers.
   * @return Requested correct answers of the training run.
   */
  @Operation(
      operationId = "getCorrectAnswers",
      summary = "List the correct answers of the run's training levels",
      description =
          "A training organizer or a training administrator may call it. A level with variant"
              + " answers is resolved for this run's participant from the answer storage service.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One correct answer per training level."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/answers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<CorrectAnswerDTO>> getCorrectAnswers(
      @PathVariable("runId") Long runId) {
    List<CorrectAnswerDTO> correctAnswerDTOs = trainingRunFacade.getCorrectAnswers(runId);
    return ResponseEntity.ok(correctAnswerDTOs);
  }

  /**
   * Get previous or current level (any visited) of given Training Run.
   *
   * @param runId of Training Run for which to get previous or current level.
   * @param levelId ID of the visited level.
   * @return Requested level.
   */
  @Operation(
      operationId = "getVisitedLevel",
      summary = "Find a level the run has already reached",
      description =
          "A training administrator, or the run's own participant, may call it. The run's current"
              + " level counts as reached.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The level."),
    @ApiResponse(
        responseCode = "400",
        description = "The run id or the level id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training run, or no level, with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The level belongs to another definition, or the run has not reached it.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{runId}/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AbstractLevelDTO> getVisitedLevel(
      @PathVariable("runId") Long runId, @PathVariable("levelId") Long levelId) {
    AbstractLevelDTO levelDTO = trainingRunFacade.getVisitedLevel(runId, levelId);
    return ResponseEntity.ok(levelDTO);
  }

  /**
   * Gets training runs by their ids. The sandbox instance reference id of each run is masked
   * according to caller privilege: administrators and organizers of the runs see the plain sandbox
   * id for every run; any other caller sees the plain id only for their own run and a hash of it
   * for every other run.
   *
   * @param ids the ids of Training Runs to return.
   * @return List of requested Training Runs.
   */
  @Operation(
      operationId = "findTrainingRunsByIds",
      summary = "Find training runs by their ids",
      description =
          "A training administrator, an organizer of the runs, or their participant may call it."
              + " A caller who is neither administrator nor organizer sees the sandbox id of"
              + " another trainee's run only as a hash.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching runs."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing or not numbers.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TrainingRunBasicDTO>> findTrainingRunsByIds(
      @RequestParam(value = "ids", required = true) List<Long> ids) {
    List<TrainingRunBasicDTO> trainingRuns = trainingRunFacade.findTrainingRunsByIds(ids);
    return ResponseEntity.ok(trainingRuns);
  }

  /**
   * Get Users by their ids.
   *
   * @param ids the ids of Users to return.
   * @return List of requested Users.
   */
  @Operation(
      operationId = "findUsersByIds",
      summary = "Find users by their user reference ids",
      description =
          "A training administrator may call it. Anyone else has to share a training instance with"
              + " every user listed.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching users."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing or not numbers.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<UserRefDTO>> findUsersByIds(
      @RequestParam(value = "ids", required = true) List<Long> ids) {
    List<UserRefDTO> users = trainingRunFacade.findUsersByIds(ids);
    return ResponseEntity.ok(users);
  }
}
