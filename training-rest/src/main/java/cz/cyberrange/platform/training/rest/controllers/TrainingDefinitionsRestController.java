package cz.cyberrange.platform.training.rest.controllers;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentDownloadDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentListDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentMetadataDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentPosterDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentSummaryDTO;
import cz.cyberrange.platform.training.api.dto.attachment.TrainingMaterialDTO;
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
import cz.cyberrange.platform.training.rest.utils.MultipartAttachments;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.sort.SortableList;
import cz.cyberrange.platform.training.rest.utils.sort.SortableProperties;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import cz.cyberrange.platform.training.service.services.UserNameFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

  private static final String KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION =
      "When true, a reference attachment:<hash> in level content that names no file supplied or"
          + " stored is kept as it is and linked to nothing, instead of being refused with 400."
          + " Uploading a file with that hash to the definition later resolves it. Defaults to false.";

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
                + " inconsistent, or a reference names no stored file and keepUnresolvedAttachments"
                + " is not true.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "413",
        description = "The files the definition links to exceed the budget of a definition.",
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
      @RequestBody @Valid TrainingDefinitionCreateDTO trainingDefinitionCreateDTO,
      @Parameter(description = KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION)
          @RequestParam(name = "keepUnresolvedAttachments", defaultValue = "false")
          boolean keepUnresolvedAttachments) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        trainingDefinitionFacade.create(
            trainingDefinitionCreateDTO, List.of(), keepUnresolvedAttachments);
    return ResponseEntity.ok(trainingDefinitionResource);
  }

  /**
   * Stores a new training definition together with its levels and the files its content references,
   * in one transaction.
   *
   * @param trainingDefinitionCreateDTO the training definition to store, its levels included
   * @param files the files the level content references, possibly none
   * @param fileMetadata the media metadata of those files keyed by hash, possibly none
   * @return the stored {@link TrainingDefinitionWithLevelsDTO}, carrying the ids of every created
   *     entity
   */
  @Operation(
      operationId = "createTrainingDefinitionWithFiles",
      summary = "Create a training definition with its levels and files",
      description =
          "Takes the part definition, holding the same JSON as the plain request, and any number"
              + " of parts files, and optionally the JSON part fileMetadata, a list of objects with"
              + " hash, duration_seconds, width, height and poster (Base64 JPEG, WebP or PNG of at"
              + " most 1 MiB) that describe the supplied files by hash. Level content refers to a file as attachment:<sha-256 in"
              + " lowercase hex>; the definition part lists its materials by hash and file name, and a"
              + " file part that is neither a material nor referenced is ignored. A material or a"
              + " reference may also name a file already stored. With keepUnresolvedAttachments=true a"
              + " reference naming no supplied or stored file is kept unlinked instead of refused."
              + " A failure stores nothing.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created definition and its levels."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The definition part failed validation, a level carries an id, a level's content is"
                + " inconsistent, a material names no supplied or stored file, a reference names none and"
                + " keepUnresolvedAttachments is not true, or a file name is"
                + " longer than 255 characters, or the media metadata of a file is invalid.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "413",
        description = "A file, the request or the files the definition links to are too large.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "422",
        description = "The hint penalties of a training level exceed its maximal score.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content =
          @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              encoding = {
                @Encoding(name = "definition", contentType = "application/json"),
                @Encoding(name = "fileMetadata", contentType = "application/json")
              }))
  @PostMapping(
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> createTrainingDefinitionWithFiles(
      @RequestPart("definition") @Valid TrainingDefinitionCreateDTO trainingDefinitionCreateDTO,
      @RequestPart(name = "files", required = false) List<MultipartFile> files,
      @RequestPart(name = "fileMetadata", required = false)
          List<AttachmentFileMetadataDTO> fileMetadata,
      @Parameter(description = KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION)
          @RequestParam(name = "keepUnresolvedAttachments", defaultValue = "false")
          boolean keepUnresolvedAttachments) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.create(
            trainingDefinitionCreateDTO,
            MultipartAttachments.toSuppliedAttachments(files, fileMetadata),
            keepUnresolvedAttachments));
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
                + " its type, a level's content is inconsistent, or a reference names no stored"
                + " file and keepUnresolvedAttachments is not true.",
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
        responseCode = "413",
        description = "The files the definition links to exceed the budget of a definition.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
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
      @RequestBody @Valid TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO,
      @Parameter(description = KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION)
          @RequestParam(name = "keepUnresolvedAttachments", defaultValue = "false")
          boolean keepUnresolvedAttachments) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.update(
            definitionId, trainingDefinitionUpdateDTO, List.of(), keepUnresolvedAttachments));
  }

  /**
   * Replaces the content and the whole level list of a training definition, and stores the files
   * the new content references, in one transaction.
   *
   * @param definitionId id of the training definition to update
   * @param trainingDefinitionUpdateDTO the new content and every level the definition keeps, with
   *     the version the change was made on
   * @param files the files the level content references, possibly none
   * @param fileMetadata the media metadata of those files keyed by hash, possibly none
   * @return the updated {@link TrainingDefinitionWithLevelsDTO}, carrying its new version and the
   *     ids of every created entity
   */
  @Operation(
      operationId = "updateTrainingDefinitionWithFiles",
      summary = "Update a training definition with all its levels and files",
      description =
          "Takes the part definition, holding the same JSON as the plain request, and any number"
              + " of parts files, and optionally the JSON part fileMetadata, a list of objects with"
              + " hash, duration_seconds, width, height and poster (Base64 JPEG, WebP or PNG of at"
              + " most 1 MiB) that describe the supplied files by hash. The definition links exactly to its materials and the files"
              + " its new content refers to as attachment:<sha-256 in lowercase hex>; a material or"
              + " a reference may name a file supplied now or already stored, and a file no"
              + " definition links any more is deleted. With keepUnresolvedAttachments=true a reference"
              + " naming no supplied or stored file is kept unlinked instead of refused. A failure"
              + " changes nothing.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The updated definition, its levels and its new version."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The definition part failed validation, a level id appears twice, a level would"
                + " change its type, a level's content is inconsistent, a material names no"
                + " supplied or stored file, a reference names none and keepUnresolvedAttachments is"
                + " not true, or a file name is longer than 255 characters.",
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
        responseCode = "413",
        description = "A file, the request or the files the definition links to are too large.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "422",
        description = "The hint penalties of a training level exceed its maximal score.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content =
          @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              encoding = {
                @Encoding(name = "definition", contentType = "application/json"),
                @Encoding(name = "fileMetadata", contentType = "application/json")
              }))
  @PutMapping(
      path = "/{definitionId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> updateTrainingDefinitionWithFiles(
      @PathVariable("definitionId") Long definitionId,
      @RequestPart("definition") @Valid TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO,
      @RequestPart(name = "files", required = false) List<MultipartFile> files,
      @RequestPart(name = "fileMetadata", required = false)
          List<AttachmentFileMetadataDTO> fileMetadata,
      @Parameter(description = KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION)
          @RequestParam(name = "keepUnresolvedAttachments", defaultValue = "false")
          boolean keepUnresolvedAttachments) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.update(
            definitionId,
            trainingDefinitionUpdateDTO,
            MultipartAttachments.toSuppliedAttachments(files, fileMetadata),
            keepUnresolvedAttachments));
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
          "The copy carries every level of the original and links the same attachment files."
              + " It starts unreleased, with no beta"
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
      description =
          "Every level of the definition is deleted with it, and so are the attachment files no"
              + " other definition links.")
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

  /**
   * Lists the training materials of a training definition with the storage budget.
   *
   * @param trainingDefinitionId id of the training definition
   * @return the materials, the budget of a definition and the bytes the definition uses
   */
  @Operation(
      operationId = "listTrainingDefinitionAttachments",
      summary = "List the training materials of a training definition",
      description =
          "Answers with each material's hash, file name, media type, size, duration, width,"
              + " height and whether it has a poster, ordered by file name, together with the"
              + " budget of a definition and the bytes it uses. Open to the same callers as the"
              + " download.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The materials, the budget and the bytes used."),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{definitionId}/attachments", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AttachmentListDTO> listAttachments(
      @PathVariable("definitionId") Long trainingDefinitionId) {
    return ResponseEntity.ok(trainingDefinitionFacade.listAttachments(trainingDefinitionId));
  }

  /**
   * Returns the bytes of an attachment file the given training definition uses, as a download named
   * after the file.
   *
   * @param trainingDefinitionId id of the training definition using the file
   * @param hash the hash of the file
   * @return the file bytes, typed with the stored media type
   */
  @Operation(
      operationId = "downloadTrainingDefinitionAttachment",
      summary = "Download an attachment file of a training definition",
      description =
          "A training administrator, a designer or organizer who may see the definition, and a"
              + " user with a training run in an instance of the definition may download. The"
              + " response is typed with the stored media type, named after the file as the"
              + " definition knows it, and sent with X-Content-Type-Options: nosniff.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The file bytes.",
        content =
            @Content(
                mediaType = MediaType.ALL_VALUE,
                schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description =
            "No training definition has that id, or it does not use a file with that hash.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{definitionId}/attachments/{hash}", produces = MediaType.ALL_VALUE)
  public ResponseEntity<byte[]> downloadAttachment(
      @PathVariable("definitionId") Long trainingDefinitionId, @PathVariable("hash") String hash) {
    AttachmentDownloadDTO download =
        trainingDefinitionFacade.downloadAttachment(trainingDefinitionId, hash);
    return ResponseEntity.ok()
        .headers(attachmentHeaders(download.metadata()))
        .body(download.content());
  }

  /**
   * Returns the poster image of an attachment file the given training definition uses.
   *
   * @param trainingDefinitionId id of the training definition using the file
   * @param hash the hash of the file
   * @return the poster image, typed with its media type
   */
  @Operation(
      operationId = "downloadTrainingDefinitionAttachmentPoster",
      summary = "Download the poster image of an attachment file",
      description =
          "The download endpoint with the query parameter poster answers with the poster image the"
              + " designer's browser captured at upload: the first frame of a video or a"
              + " downscaled image. Open to the same callers as the download.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The poster image.",
        content =
            @Content(
                mediaType = MediaType.ALL_VALUE,
                schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description =
            "No training definition has that id, it does not use a file with that hash, or the"
                + " file has no poster.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(
      path = "/{definitionId}/attachments/{hash}",
      params = "poster",
      produces = MediaType.ALL_VALUE)
  public ResponseEntity<byte[]> downloadAttachmentPoster(
      @PathVariable("definitionId") Long trainingDefinitionId, @PathVariable("hash") String hash) {
    AttachmentPosterDTO poster =
        trainingDefinitionFacade.downloadAttachmentPoster(trainingDefinitionId, hash);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(poster.mediaType()));
    headers.set("X-Content-Type-Options", "nosniff");
    return ResponseEntity.ok().headers(headers).body(poster.content());
  }

  /**
   * Returns the headers of {@link #downloadAttachment(Long, String)} without reading the file
   * bytes, which gives a file's name, media type and size.
   *
   * @param trainingDefinitionId id of the training definition using the file
   * @param hash the hash of the file
   * @return the headers of the download, with no body
   */
  @Operation(
      operationId = "headTrainingDefinitionAttachment",
      summary = "Describe an attachment file of a training definition",
      description =
          "Answers with the headers of the download, Content-Length, Content-Type and"
              + " Content-Disposition included, and no body. When stored, X-Attachment-Duration"
              + " (seconds), X-Attachment-Width and X-Attachment-Height (pixels) are sent too, on"
              + " the download as well. Open to the same callers as the"
              + " download.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The headers of the download.",
        content = @Content),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number.",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description =
            "No training definition has that id, or it does not use a file with that hash.",
        content = @Content)
  })
  @RequestMapping(
      path = "/{definitionId}/attachments/{hash}",
      method = RequestMethod.HEAD,
      produces = MediaType.ALL_VALUE)
  public ResponseEntity<Void> describeAttachment(
      @PathVariable("definitionId") Long trainingDefinitionId, @PathVariable("hash") String hash) {
    AttachmentMetadataDTO metadata =
        trainingDefinitionFacade.findAttachmentMetadata(trainingDefinitionId, hash);
    return ResponseEntity.ok().headers(attachmentHeaders(metadata)).build();
  }

  /**
   * Links a file the service already stores to a training definition as a material, sending no
   * bytes.
   *
   * @param trainingDefinitionId id of the training definition
   * @param material the hash of the stored file and the name the definition gives it
   * @return the linked material
   */
  @Operation(
      operationId = "linkTrainingDefinitionAttachment",
      summary = "Link a stored attachment file to a training definition",
      description =
          "Takes the SHA-256 and the file name of a file that any definition already stored and"
              + " links it to this definition as a material, or renames the link the definition"
              + " already has. Answers 404 for a hash nobody stored, which tells the caller to"
              + " upload the bytes. Reachable by the callers who may save the definition.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The file is linked; its material."),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number, or the hash or file name is invalid.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition has that id, or no file with that hash is stored.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The definition is not unreleased or already has a training instance.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "413",
        description = "The files the definition links to would exceed its budget.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(
      path = "/{definitionId}/attachments/link",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AttachmentSummaryDTO> linkAttachment(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @RequestBody @Valid TrainingMaterialDTO material) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.linkAttachment(trainingDefinitionId, material));
  }

  /**
   * Stores one uploaded file and links it to a training definition as a material.
   *
   * @param trainingDefinitionId id of the training definition
   * @param file the file bytes, named by the part's file name
   * @param metadata the hash of the file and its media metadata
   * @return the linked material
   */
  @Operation(
      operationId = "uploadTrainingDefinitionAttachment",
      summary = "Upload an attachment file to a training definition",
      description =
          "Takes one file part and a metadata part carrying the SHA-256 the caller computed and the"
              + " optional duration, width, height and poster. Answers 400 when the hash is not"
              + " the hash of the bytes. Stores the bytes once for every definition, detects the"
              + " media type from them, and links the file as a material. Reachable by the callers"
              + " who may save the definition.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The file is stored and linked; its material."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The id in the path is not a number, the hash is not the hash of the bytes, or the"
                + " media metadata is invalid.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "The definition is not unreleased or already has a training instance.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "413",
        description = "The file or the files the definition links to exceed its budget.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content =
          @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              encoding = @Encoding(name = "metadata", contentType = "application/json")))
  @PostMapping(
      path = "/{definitionId}/attachments",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AttachmentSummaryDTO> uploadAttachment(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @RequestPart("file") MultipartFile file,
      @RequestPart("metadata") AttachmentFileMetadataDTO metadata) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.uploadAttachment(
            trainingDefinitionId,
            MultipartAttachments.toSuppliedAttachment(file, metadata),
            metadata.hash()));
  }

  private static HttpHeaders attachmentHeaders(AttachmentMetadataDTO metadata) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(metadata.mediaType()));
    headers.setContentLength(metadata.sizeBytes());
    headers.setContentDisposition(
        ContentDisposition.attachment()
            .filename(metadata.fileName(), StandardCharsets.UTF_8)
            .build());
    headers.set("X-Content-Type-Options", "nosniff");
    if (metadata.durationSeconds() != null) {
      headers.set("X-Attachment-Duration", metadata.durationSeconds().toString());
    }
    if (metadata.width() != null) {
      headers.set("X-Attachment-Width", metadata.width().toString());
    }
    if (metadata.height() != null) {
      headers.set("X-Attachment-Height", metadata.height().toString());
    }
    return headers;
  }
}
