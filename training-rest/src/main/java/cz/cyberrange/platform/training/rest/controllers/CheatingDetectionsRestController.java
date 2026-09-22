package cz.cyberrange.platform.training.rest.controllers;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AbstractDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AnswerSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.LocationSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.MinimalSolveTimeDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.NoCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.TimeProximityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.detection.CheatingDetectionExportFacade;
import cz.cyberrange.platform.training.service.facade.detection.CheatingDetectionFacade;
import cz.cyberrange.platform.training.service.facade.detection.DetectionEventFacade;
import cz.cyberrange.platform.training.service.utils.AbstractFileExtensions;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The rest controller for Cheating detections */
@Tag(
    name = "Cheating detection",
    description =
        "Runs that look for signs of cheating in a training instance, and what they found")
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
@RequestMapping(value = "/cheating-detections", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CheatingDetectionsRestController {

  private final CheatingDetectionFacade cheatingDetectionFacade;
  private final DetectionEventFacade detectionEventFacade;
  private final CheatingDetectionExportFacade cheatingDetectionExportFacade;

  @Autowired
  public CheatingDetectionsRestController(
      CheatingDetectionFacade cheatingDetectionFacade,
      DetectionEventFacade detectionEventFacade,
      CheatingDetectionExportFacade cheatingDetectionExportFacade) {
    this.cheatingDetectionFacade = cheatingDetectionFacade;
    this.cheatingDetectionExportFacade = cheatingDetectionExportFacade;
    this.detectionEventFacade = detectionEventFacade;
  }

  /**
   * Creates a cheating detection from the given configuration and immediately executes it.
   *
   * @param cheatingDetectionDTO the cheating detection to create and execute
   * @return an empty response
   */
  @Operation(
      operationId = "createAndExecuteCheatingDetection",
      summary = "Create a cheating detection and run it",
      description =
          "A training administrator or an organizer of the training instance may call it. Only a"
              + " detection sent as QUEUED is run. Forbidden commands sent in the body are stored"
              + " with the detection and are what the forbidden commands sweep matches against.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The detection was created and executed."),
    @ApiResponse(
        responseCode = "400",
        description = "The request body failed validation.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/detection", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> createAndExecuteCheatingDetection(
      @RequestBody @Valid CheatingDetectionDTO cheatingDetectionDTO) {
    cheatingDetectionFacade.createAndExecute(cheatingDetectionDTO);
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes the detection events of a cheating detection and re-executes it. {@code
   * trainingInstanceId} plays no part in the operation or its authorization check, which is scoped
   * by {@code cheatingDetectionId} alone.
   *
   * @param cheatingDetectionId id of cheating detection.
   * @param trainingInstanceId id of training instance.
   * @return an empty response
   */
  @Operation(
      operationId = "rerunCheatingDetection",
      summary = "Run a cheating detection again from scratch",
      description =
          "A training administrator or an organizer of the instance the detection ran in may call"
              + " it. Every detection not disabled is queued again.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The detection was run again."),
    @ApiResponse(
        responseCode = "400",
        description = "An id in the path is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No cheating detection with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PatchMapping(
      path = "/{cheatingDetectionId}/rerun/{trainingInstanceId}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> rerunCheatingDetection(
      @PathVariable("cheatingDetectionId") Long cheatingDetectionId,
      @Parameter(description = "Not used; the rerun is scoped by the detection id.")
          @PathVariable("trainingInstanceId")
          Long trainingInstanceId) {
    cheatingDetectionFacade.rerunCheatingDetection(cheatingDetectionId, trainingInstanceId);
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes a cheating detection together with its detection events, their participants and their
   * forbidden commands, and clears the detection-event flag on every training run of {@code
   * trainingInstanceId} rather than only the runs the deleted detection covered.
   *
   * @param cheatingDetectionId id of cheating detection.
   * @param trainingInstanceId id of the training instance whose runs have their detection-event
   *     flag cleared.
   * @return the response entity
   */
  @Operation(
      operationId = "deleteDetectionEventsOfCheatingDetection",
      summary = "Delete a cheating detection and its findings",
      description =
          "A training administrator, or an organizer of both the detection and the instance, may"
              + " call it. Every training run of that instance loses its detection flag, not only"
              + " the implicated ones.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The detection was deleted."),
    @ApiResponse(
        responseCode = "400",
        description = "The training instance id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No cheating detection with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @DeleteMapping(path = "/{cheatingDetectionId}/delete")
  public ResponseEntity<Void> deleteDetectionEvents(
      @PathVariable("cheatingDetectionId") Long cheatingDetectionId,
      @Parameter(description = "Instance whose training runs lose their detection flag.")
          @RequestParam(value = "trainingInstanceId")
          Long trainingInstanceId) {
    cheatingDetectionFacade.deleteCheatingDetection(cheatingDetectionId, trainingInstanceId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Finds all detection events of a cheating detection, filtered by {@code predicate}. {@code
   * trainingInstanceId} plays no part in the query or the authorization check.
   *
   * @param predicate specifies query to database.
   * @param cheatingDetectionId id of cheating detection.
   * @param trainingInstanceId id of training instance.
   * @param pageable pageable parameter with information about pagination.
   * @return all Detection Events occurred in a cheating detection.
   */
  @Operation(
      operationId = "findAllDetectionEvents",
      summary = "List the findings of a cheating detection",
      description =
          "A training administrator or an organizer of the instance the detection ran in may call"
              + " it. Text filters match partially and ignore case.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching findings."),
    @ApiResponse(
        responseCode = "400",
        description = "The training instance id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No cheating detection with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{cheatingDetectionId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<AbstractDetectionEventDTO>>
      findAllDetectionEventsOfCheatingDetection(
          @QuerydslPredicate(root = AbstractDetectionEvent.class) Predicate predicate,
          @PathVariable("cheatingDetectionId") Long cheatingDetectionId,
          @Parameter(description = "Not used; the findings come from the cheating detection.")
              @RequestParam(value = "trainingInstanceId", required = true)
              Long trainingInstanceId,
          @ParameterObject Pageable pageable) {
    PageResultResource<AbstractDetectionEventDTO> detectionEventResource =
        detectionEventFacade.findAllDetectionEventsOfCheatingDetection(
            cheatingDetectionId, pageable, predicate, trainingInstanceId);
    return new ResponseEntity<>(detectionEventResource, HttpStatus.OK);
  }

  /**
   * Get all participants of Detection Event.
   *
   * @param eventId id of detection event.
   * @param pageable pageable parameter with information about pagination.
   * @return all participants of a detection event.
   */
  @Operation(
      operationId = "findAllParticipantsOfEvent",
      summary = "List the trainees implicated in a finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching trainees."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<DetectionEventParticipantDTO>>
      findAllParticipantsOfDetectionEvent(
          @RequestParam(value = "eventId", required = true) Long eventId,
          @ParameterObject Pageable pageable) {
    PageResultResource<DetectionEventParticipantDTO> participantsResource =
        detectionEventFacade.findAllParticipantsOfDetectionEvent(eventId, pageable);
    return new ResponseEntity<>(participantsResource, HttpStatus.OK);
  }

  /**
   * Get all forbidden commands of Detection Event.
   *
   * @param eventId id of detection event.
   * @param pageable pageable parameter with information about pagination.
   * @return all detected forbidden commands occurred in a detection event.
   */
  @Operation(
      operationId = "findAllForbiddenCommandsOfEvent",
      summary = "List the forbidden commands a finding caught",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching commands."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/forbidden-commands", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<DetectedForbiddenCommandDTO>>
      findAllForbiddenCommandsOfDetectionEvent(
          @RequestParam(value = "eventId", required = true) Long eventId,
          @ParameterObject Pageable pageable) {
    PageResultResource<DetectedForbiddenCommandDTO> participantsResource =
        detectionEventFacade.findAllForbiddenCommandsOfDetectionEvent(eventId, pageable);
    return new ResponseEntity<>(participantsResource, HttpStatus.OK);
  }

  /**
   * Get all forbidden commands of Detection Event, unpaged.
   *
   * @param eventId id of detection event.
   * @return every detected forbidden command of the event.
   */
  @Operation(
      operationId = "findDetectedForbiddenCommandsOfEvent",
      summary = "List every forbidden command a finding caught",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it. The whole list comes back unpaged.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Every command the finding caught."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/detected-commands/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DetectedForbiddenCommandDTO>> findAllForbiddenCommandsOfDetectionEvent(
      @PathVariable Long eventId) {
    ;
    return ResponseEntity.ok(
        detectionEventFacade.findAllForbiddenCommandsOfDetectionEvent(eventId));
  }

  /**
   * Builds and returns a zip archive holding the cheating detection's own configuration, two
   * entries per finding it produced, plus the trainee participant groups it evaluated.
   *
   * @param cheatingDetectionId the cheating detection id
   * @return the zip archive as a byte array response
   */
  @Operation(
      operationId = "archiveCheatingDetectionResults",
      summary = "Export a cheating detection and its findings",
      description =
          "A training administrator or an organizer of the instance the detection ran in may call"
              + " it. The archive holds the settings of the run, its findings by kind, and the"
              + " groups it evaluated.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The zip archive.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(
        responseCode = "400",
        description = "The cheating detection id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No cheating detection with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(
      path = "/exports/{cheatingDetectionId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> archiveCheatingDetectionResults(
      @PathVariable("cheatingDetectionId") Long cheatingDetectionId) {
    FileToReturnDTO file =
        cheatingDetectionExportFacade.archiveCheatingDetectionResults(cheatingDetectionId);
    HttpHeaders header = new HttpHeaders();
    header.setContentType(new MediaType("application", "octet-stream"));
    header.set(
        "Content-Disposition",
        "inline; filename=" + file.getTitle() + AbstractFileExtensions.ZIP_FILE_EXTENSION);
    header.setContentLength(file.getContent().length);
    return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
  }

  /**
   * Detection Event by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventById",
      summary = "Find one finding by id",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The requested finding.",
        content = @Content(schema = @Schema(implementation = AbstractDetectionEventDTO.class))),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/event", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AbstractDetectionEventDTO> findDetectionEventById(
      @RequestParam(value = "eventId", required = true) Long eventId) {
    AbstractDetectionEventDTO abstractDetectionEventDTO =
        detectionEventFacade.findDetectionEventById(eventId);
    return ResponseEntity.ok(abstractDetectionEventDTO);
  }

  /**
   * Detection Event of type Answer Similarity by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventOfAnswerSimilarityById",
      summary = "Find one answer similarity finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested finding."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/answer-similarity", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AnswerSimilarityDetectionEventDTO> findAnswerSimilarityDetectionEventById(
      @RequestParam(value = "eventId", required = true) Long eventId) {
    AnswerSimilarityDetectionEventDTO answerSimilarityDetectionEventDTO =
        detectionEventFacade.findAnswerSimilarityEventById(eventId);
    return ResponseEntity.ok(answerSimilarityDetectionEventDTO);
  }

  /**
   * Detection Event of type Location Similarity by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventOfLocationSimilarityById",
      summary = "Find one location similarity finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested finding."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/location-similarity", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LocationSimilarityDetectionEventDTO>
      findLocationSimilarityDetectionEventById(
          @RequestParam(value = "eventId", required = true) Long eventId) {
    LocationSimilarityDetectionEventDTO locationSimilarityDetectionEventDTO =
        detectionEventFacade.findLocationSimilarityEventById(eventId);
    return ResponseEntity.ok(locationSimilarityDetectionEventDTO);
  }

  /**
   * Detection Event of type Time Proximity by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventOfTimeProximityById",
      summary = "Find one time proximity finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested finding."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/time-proximity", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TimeProximityDetectionEventDTO> findTimeProximityDetectionEventById(
      @RequestParam(value = "eventId", required = true) Long eventId) {
    TimeProximityDetectionEventDTO timeProximityDetectionEventDTO =
        detectionEventFacade.findTimeProximityEventById(eventId);
    return ResponseEntity.ok(timeProximityDetectionEventDTO);
  }

  /**
   * Detection Event of type Minimal Solve Time by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventOfMinimalSolveTimeById",
      summary = "Find one minimal solve time finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested finding."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/minimal-solve-time", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MinimalSolveTimeDetectionEventDTO> findMinimalSolveTimeDetectionEventById(
      @RequestParam(value = "eventId", required = true) Long eventId) {
    MinimalSolveTimeDetectionEventDTO minimalSolveTimeDetectionEventDTO =
        detectionEventFacade.findMinimalSolveTimeEventById(eventId);
    return ResponseEntity.ok(minimalSolveTimeDetectionEventDTO);
  }

  /**
   * Detection Event of type No Commands by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventOfNoCommandsById",
      summary = "Find one no commands finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested finding."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/no-commands", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<NoCommandsDetectionEventDTO> findNoCommandsDetectionEventById(
      @RequestParam(value = "eventId", required = true) Long eventId) {
    NoCommandsDetectionEventDTO NoCommandsDetectionEventDTO =
        detectionEventFacade.findNoCommandsEventById(eventId);
    return ResponseEntity.ok(NoCommandsDetectionEventDTO);
  }

  /**
   * Detection Event of type Forbidden Commands by ID.
   *
   * @param eventId the detection event id
   * @return detection event.
   */
  @Operation(
      operationId = "findDetectionEventOfForbiddenCommandsById",
      summary = "Find one forbidden commands finding",
      description =
          "A training administrator or an organizer of the instance the finding came from may call"
              + " it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested finding."),
    @ApiResponse(
        responseCode = "400",
        description = "The event id is missing or not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No detection event with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/detected-forbidden-commands", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ForbiddenCommandsDetectionEventDTO> findForbiddenCommandsDetectionEventById(
      @RequestParam(value = "eventId", required = true) Long eventId) {
    ForbiddenCommandsDetectionEventDTO forbiddenCommandsDetectionEventDTO =
        detectionEventFacade.findForbiddenCommandsEventById(eventId);
    return ResponseEntity.ok(forbiddenCommandsDetectionEventDTO);
  }

  /**
   * Get all cheating detections of a training instance, ordered by their execution time.
   *
   * @param trainingInstanceId id of training instance.
   * @param pageable pageable parameter with information about pagination.
   * @return all cheating Detections occurred in a training instance.
   */
  @Operation(
      operationId = "findAllCheatingDetections",
      summary = "List the cheating detections of a training instance",
      description =
          "A training administrator or an organizer of the training instance may call it. The"
              + " oldest run comes first.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching detections."),
    @ApiResponse(
        responseCode = "400",
        description = "The training instance id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(
      path = "/{trainingInstanceId}/detections",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<CheatingDetectionDTO>>
      findAllCheatingDetectionsOfInstance(
          @PathVariable("trainingInstanceId") Long trainingInstanceId,
          @ParameterObject Pageable pageable) {
    PageResultResource<CheatingDetectionDTO> cheatingDetectionResource =
        cheatingDetectionFacade.findAllCheatingDetectionsOfTrainingInstance(
            trainingInstanceId, pageable);
    return new ResponseEntity<>(cheatingDetectionResource, HttpStatus.OK);
  }
}
