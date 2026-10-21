package cz.cyberrange.platform.training.rest.controllers;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionMitreTechniquesDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.sort.SortableList;
import cz.cyberrange.platform.training.rest.utils.sort.SortableProperties;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import cz.cyberrange.platform.training.service.services.UserNameFilter;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The rest controller for Training definitions */
@Tag(
    name = "Training definitions",
    description = "Training definitions, the levels they hold and the users working on them")
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
@Validated
@RestController
@RequestMapping(path = "/training-definitions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingDefinitionsRestController {

  private TrainingDefinitionFacade trainingDefinitionFacade;

  /**
   * Instantiates a new Training Definitions rest controller.
   *
   * @param trainingDefinitionFacade the training definition facade
   */
  @Autowired
  public TrainingDefinitionsRestController(TrainingDefinitionFacade trainingDefinitionFacade) {
    this.trainingDefinitionFacade = trainingDefinitionFacade;
  }

  /**
   * Returns one training definition with the full detail of its levels.
   *
   * @param id id of the training definition to return
   * @return the {@link TrainingDefinitionWithLevelsDTO} matching the given id
   */
  @Operation(
      operationId = "findTrainingDefinitionById",
      summary = "Find one training definition with its levels",
      description =
          "A training administrator may read any definition. Anyone else has to be one of its"
              + " designers, or an organizer of a training instance created from it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The definition and its levels."),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> findTrainingDefinitionById(
      @PathVariable(value = "definitionId") Long id) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        trainingDefinitionFacade.findById(id);
    return ResponseEntity.ok(trainingDefinitionResource);
  }

  /**
   * Returns a page of training definitions. A training administrator receives every definition
   * matching the predicate, while any other caller receives only those they author.
   *
   * @param predicate restricts which definitions are considered, string comparisons matching
   *     partially and ignoring case
   * @param pageable pageable parameter with information about pagination
   * @return the page of {@link TrainingDefinitionDTO} matching the predicate
   */
  @Operation(
      operationId = "findAllTrainingDefinitions",
      summary = "List training definitions matching a filter",
      description =
          "A training administrator sees every definition. Anyone else sees only the definitions"
              + " they author. Text filters match partially and ignore case.")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "The matching definitions."))
  @SortableProperties(SortableList.TRAINING_DEFINITION)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<TrainingDefinitionDTO>> findAllTrainingDefinitions(
      @QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
      @ParameterObject Pageable pageable) {

    PageResultResource<TrainingDefinitionDTO> trainingDefinitionResource =
        trainingDefinitionFacade.findAll(predicate, pageable);
    return ResponseEntity.ok(trainingDefinitionResource);
  }

  /**
   * Returns a page of the training definitions in the given state that the caller may organize.
   * Released definitions are returned to every caller. Unreleased ones are returned in full to a
   * training administrator, narrowed to those the caller authors when the caller holds the designer
   * role, and none otherwise.
   *
   * @param state the state the definitions have to be in, which has to be released or unreleased
   * @param pageable pageable parameter with information about pagination
   * @return the page of {@link TrainingDefinitionInfoDTO} matching the given state
   */
  @Operation(
      operationId = "findAllTrainingDefinitionsForOrganizers",
      summary = "List training definitions available to organizers",
      description =
          "Released definitions are visible to every caller. A training administrator also sees"
              + " every unreleased definition. A designer sees the unreleased definitions they"
              + " author, and an organizer who is no designer sees none.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching definitions."),
    @ApiResponse(
        responseCode = "400",
        description = "The state is neither RELEASED nor UNRELEASED.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @SortableProperties(SortableList.TRAINING_DEFINITION)
  @GetMapping(path = "/for-organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<TrainingDefinitionInfoDTO>>
      findAllTrainingDefinitionsForOrganizers(
          @Parameter(required = true) @RequestParam(value = "state") TDState state,
          @ParameterObject Pageable pageable) {

    PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionResource =
        trainingDefinitionFacade.findAllForOrganizers(state, pageable);
    return ResponseEntity.ok(trainingDefinitionResource);
  }

  /**
   * Get MITRE techniques used by released Training Definitions.
   *
   * @return released Training Definitions using MITRE techniques, each flagged as played or not
   *     played by the requesting user.
   */
  @Operation(
      operationId = "findPlayedMitreTechniques",
      summary = "List released definitions using MITRE techniques",
      description =
          "Only a trainee or a training administrator may call it. A definition using no MITRE"
              + " technique is left out.")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "The matching definitions."))
  @GetMapping(path = "/played-mitre-techniques", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TrainingDefinitionMitreTechniquesDTO>> findPlayedMitreTechniques() {
    return ResponseEntity.ok(trainingDefinitionFacade.findPlayedMitreTechniques());
  }

  /**
   * Stores a new training definition together with its levels in the order given, listing the
   * calling user among its authors.
   *
   * @param trainingDefinitionCreateDTO the training definition to store, its levels included
   * @return the stored {@link TrainingDefinitionWithLevelsDTO}, carrying the ids of every created
   *     entity
   */
  @Operation(
      operationId = "createTrainingDefinition",
      summary = "Create a training definition with its levels",
      description =
          "The calling user is added as an author. Levels are stored in the order given and carry"
              + " no ids; the response holds the ids the server gave them, in the same order. The"
              + " estimated duration is the sum of the levels' estimated durations.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created definition and its levels."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The request body failed validation, a level carries an id, or a level's content is"
                + " inconsistent.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "422",
        description = "The hint penalties of a training level exceed its maximal score.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> createTrainingDefinition(
      @RequestBody @Valid TrainingDefinitionCreateDTO trainingDefinitionCreateDTO) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        trainingDefinitionFacade.create(trainingDefinitionCreateDTO);
    return ResponseEntity.ok(trainingDefinitionResource);
  }

  /**
   * Replaces the content and the whole level list of a training definition that is unreleased and
   * has no training instance yet, in one transaction, listing the calling user among its authors.
   *
   * @param definitionId id of the training definition to update
   * @param trainingDefinitionUpdateDTO the new content and every level the definition keeps, with
   *     the version the change was made on
   * @return the updated {@link TrainingDefinitionWithLevelsDTO}, carrying its new version and the
   *     ids of every created entity
   */
  @Operation(
      operationId = "updateTrainingDefinition",
      summary = "Update a training definition with all its levels",
      description =
          "Levels are kept in the order given. A level without an id is created, a level with"
              + " the id of one of the definition's levels replaces it, and a stored level left"
              + " out is deleted. New levels, hints, questions, choices and matching items carry"
              + " no ids; a hint, question, choice or matching item keeps its id only when it"
              + " belongs to the level or question it is sent under, any other is created with a"
              + " new id. The response holds every id in the order sent. The request must carry"
              + " the version last read; the state is not changed.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The updated definition, its levels and its new version."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The request body failed validation, a level id appears twice, a level would change"
                + " its type, or a level's content is inconsistent.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id, or a level id not of this definition.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description =
            "The definition is not unreleased, already has a training instance, is at another"
                + " version than the one sent, or was changed by another request at the same"
                + " time.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "422",
        description = "The hint penalties of a training level exceed its maximal score.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(
      path = "/{definitionId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> updateTrainingDefinition(
      @PathVariable("definitionId") Long definitionId,
      @RequestBody @Valid TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.update(definitionId, trainingDefinitionUpdateDTO));
  }

  /**
   * Stores a copy of a training definition together with copies of all its levels, under the given
   * title and with the calling user among its authors, and returns the copy.
   *
   * @param id id of the training definition to copy
   * @param title title the copy is stored under
   * @return the stored copy, its levels in presentation order
   */
  @Operation(
      operationId = "cloneTrainingDefinition",
      summary = "Copy a training definition under a new title",
      description =
          "The copy carries every level of the original. It starts unreleased, with no beta"
              + " testing group and the calling user as its only author.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created copy and its levels."),
    @ApiResponse(
        responseCode = "400",
        description = "The title is missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(path = "/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> cloneTrainingDefinition(
      @PathVariable("definitionId") Long id,
      @Parameter(required = true, description = "Title to give the copy.")
          @RequestParam(value = "title")
          String title) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionWithLevelsDTO =
        trainingDefinitionFacade.clone(id, title);
    return ResponseEntity.ok(trainingDefinitionWithLevelsDTO);
  }

  /**
   * Removes a training definition along with all of its levels. A released definition, or one that
   * already has a training instance, is refused rather than removed.
   *
   * @param id id of the training definition to remove
   * @return an empty successful response
   */
  @Operation(
      operationId = "deleteTrainingDefinition",
      summary = "Delete a training definition",
      description = "Every level of the definition is deleted with it.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The definition was deleted."),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The definition is released, or already has a training instance.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @DeleteMapping(path = "/{definitionId}")
  public ResponseEntity<Void> deleteTrainingDefinition(@PathVariable("definitionId") Long id) {
    trainingDefinitionFacade.delete(id);
    return ResponseEntity.ok().build();
  }

  /**
   * Returns one level in the full detail of whichever level type it turns out to be.
   *
   * @param levelId id of the level to return
   * @return the {@link AbstractLevelDTO} matching the given id
   */
  @Operation(
      operationId = "findLevelById",
      summary = "Find one level",
      description =
          "Any training designer may read any level, not only the levels of definitions they"
              + " design.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The requested level."),
    @ApiResponse(
        responseCode = "404",
        description = "No level with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AbstractLevelDTO> findLevelById(@PathVariable("levelId") Long levelId) {
    AbstractLevelDTO level = trainingDefinitionFacade.findLevelById(levelId);
    return ResponseEntity.ok(level);
  }

  /**
   * Returns a page of the users holding the training designer role who do not yet author the given
   * training definition.
   *
   * @param trainingDefinitionId id of the training definition whose current authors are left out
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     absent
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     absent
   * @param name restricts the result to users whose given name or family name matches every word of
   *     it, no restriction when absent
   * @param pageable pageable parameter with information about pagination
   * @return the page of {@link UserRefDTO} not authoring the definition
   */
  @Operation(
      operationId = "findDesignersNotInGivenTrainingDefinition",
      summary = "List designers who do not author a definition")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The page of designers."),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @SortableProperties(SortableList.USER)
  @GetMapping(
      path = "{definitionId}/designers-not-in-training-definition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserRefDTO>> getDesignersNotInGivenTrainingDefinition(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @RequestParam(value = "givenName", required = false) String givenName,
      @RequestParam(value = "familyName", required = false) String familyName,
      @Parameter(
              description =
                  "Restricts the result to users whose given name or family name matches every word of"
                      + " the text.")
          @RequestParam(value = "name", required = false)
          String name,
      @ParameterObject Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
            trainingDefinitionId, pageable, new UserNameFilter(givenName, familyName, name));
    return ResponseEntity.ok(designers);
  }

  /**
   * Returns a page of the users authoring the given training definition.
   *
   * @param trainingDefinitionId id of the training definition whose authors are read
   * @param givenName restricts the result to authors whose given name matches, no restriction when
   *     absent
   * @param familyName restricts the result to authors whose family name matches, no restriction
   *     when absent
   * @param name restricts the result to authors whose given name or family name matches every word
   *     of it, no restriction when absent
   * @param pageable pageable parameter with information about pagination
   * @return the page of {@link UserRefDTO} authoring the definition
   */
  @Operation(operationId = "getAuthors", summary = "List the authors of a training definition")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The page of authors."),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @SortableProperties(SortableList.USER)
  @GetMapping(path = "/{definitionId}/authors", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserRefDTO>> getAuthors(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @RequestParam(value = "givenName", required = false) String givenName,
      @RequestParam(value = "familyName", required = false) String familyName,
      @Parameter(
              description =
                  "Restricts the result to users whose given name or family name matches every word of"
                      + " the text.")
          @RequestParam(value = "name", required = false)
          String name,
      @ParameterObject Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getAuthors(
            trainingDefinitionId, pageable, new UserNameFilter(givenName, familyName, name));
    return ResponseEntity.ok(designers);
  }

  /**
   * Adds and removes authors of a training definition in one request, and records the edit on the
   * definition. The calling user is dropped from the removal set, so a caller cannot remove their
   * own authorship here.
   *
   * @param trainingDefinitionId id of the training definition whose authors change
   * @param authorsAddition cross-service user reference ids to add as authors
   * @param authorsRemoval cross-service user reference ids to remove from the authors
   * @return an empty response carrying no content
   */
  @Operation(
      operationId = "editAuthors",
      summary = "Add and remove authors of a training definition",
      description = "The calling user is never removed, even when listed for removal.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "The authors were changed."),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(path = "/{definitionId}/authors", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> editAuthors(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @Parameter(description = "User reference ids of the users to add as authors.")
          @RequestParam(value = "authorsAddition", required = false)
          Set<Long> authorsAddition,
      @Parameter(description = "User reference ids of the authors to remove.")
          @RequestParam(value = "authorsRemoval", required = false)
          Set<Long> authorsRemoval) {
    trainingDefinitionFacade.editAuthors(trainingDefinitionId, authorsAddition, authorsRemoval);
    return ResponseEntity.noContent().build();
  }

  /**
   * Moves a training definition to the given lifecycle state and records the edit on it. Only three
   * moves are allowed: unreleased to released, released to archived, and released back to
   * unreleased provided no training instance exists for the definition. Asking for the state it
   * already holds does nothing, and every other move is refused, an archived definition therefore
   * being final.
   *
   * @param definitionId id of the training definition to move
   * @param state the lifecycle state to move it to
   * @return an empty response carrying no content
   */
  @Operation(
      operationId = "switchDefinitionState",
      summary = "Change the state of a training definition",
      description =
          "Only three moves are allowed: unreleased to released, released to archived, and released"
              + " back to unreleased. Asking for the state the definition already holds changes"
              + " nothing.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "The state was changed."),
    @ApiResponse(
        responseCode = "400",
        description = "The state is not a recognized value.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition with this id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The move is not allowed, or a training instance blocks it.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PutMapping(path = "/{definitionId}/states/{state}")
  public ResponseEntity<Void> switchState(
      @PathVariable("definitionId") Long definitionId, @PathVariable("state") TDState state) {
    trainingDefinitionFacade.switchState(definitionId, state);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns the named training definitions, each carrying the basic information of its levels. An
   * id matching no definition is passed over rather than reported, so the result may be shorter
   * than the request.
   *
   * @param ids ids of the training definitions to return
   * @return the matching {@link TrainingDefinitionBasicDTO}s
   */
  @Operation(
      operationId = "findTrainingDefinitionsByIds",
      summary = "Find several training definitions by id",
      description =
          "Anyone but a training administrator has to be a trainee or an organizer in an"
              + " instance of every definition named. An id matching no definition is skipped, so"
              + " the result may be shorter than the request.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching definitions."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TrainingDefinitionBasicDTO>> findTrainingDefinitionsByIds(
      @RequestParam(value = "ids", required = true) List<Long> ids) {
    List<TrainingDefinitionBasicDTO> trainingDefinitions =
        trainingDefinitionFacade.findTrainingDefinitionsByIds(ids);
    return ResponseEntity.ok(trainingDefinitions);
  }

  /**
   * Returns the basic information of the named levels. An id matching no level is passed over
   * rather than reported, so the result may be shorter than the request.
   *
   * @param ids ids of the levels to return
   * @return the matching {@link AbstractLevelBasicDTO}s
   */
  @Operation(
      operationId = "findLevelsByIds",
      summary = "Find several levels by id",
      description =
          "Anyone but a training administrator has to be a trainee or an organizer in an"
              + " instance of every definition the levels belong to. An id matching no level is"
              + " skipped, so the result may be shorter than the request.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching levels."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/levels/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AbstractLevelBasicDTO>> findLevelsByIds(
      @RequestParam(value = "ids", required = true) List<Long> ids) {
    List<AbstractLevelBasicDTO> levels = trainingDefinitionFacade.findLevelsByIds(ids);
    return ResponseEntity.ok(levels);
  }

  /**
   * Returns the basic information of the named hints. An id matching no hint is passed over rather
   * than reported, so the result may be shorter than the request.
   *
   * @param ids ids of the hints to return
   * @return the matching {@link HintBasicDTO}s
   */
  @Operation(
      operationId = "findHintsByIds",
      summary = "Find several hints by id",
      description =
          "Anyone but a training administrator has to be a trainee or an organizer in an"
              + " instance of every definition the hints belong to. An id matching no hint is"
              + " skipped, so the result may be shorter than the request.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The matching hints."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids are missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/hints/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<HintBasicDTO>> findHintsByIds(
      @RequestParam(value = "ids", required = true) List<Long> ids) {
    List<HintBasicDTO> hints = trainingDefinitionFacade.findHintsByIds(ids);
    return ResponseEntity.ok(hints);
  }
}
