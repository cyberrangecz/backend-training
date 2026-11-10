package cz.cyberrange.platform.training.rest.controllers;

import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.TrainingInstanceScoreReportDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.rest.utils.ArchiveRequestBodyReader;
import cz.cyberrange.platform.training.rest.utils.ZipMediaType;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.ApiMicroserviceError;
import cz.cyberrange.platform.training.service.export.ExportFormat;
import cz.cyberrange.platform.training.service.export.ExportFormats;
import cz.cyberrange.platform.training.service.export.YamlExportFormat;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.utils.AbstractFileExtensions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the endpoints that carry training content out of the service as a downloadable file and
 * back in from a submitted one
 */
@Tag(
    name = "Export Imports",
    description = "Training content taken out as a file, and definitions read back in")
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
public class ExportImportRestController {

  private static final String KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION =
      "When true, a reference attachment:<hash> in level content that names no file supplied or"
          + " stored is kept as it is and linked to nothing, instead of being refused with 400."
          + " Uploading a file with that hash to the definition later resolves it. Defaults to false.";

  private ExportImportFacade exportImportFacade;
  private ExportFormats exportFormats;
  private ArchiveRequestBodyReader archiveRequestBodyReader;

  /**
   * Instantiates a new Export import rest controller.
   *
   * @param exportImportFacade the export import facade
   * @param exportFormats the selector of the format an export is written in
   * @param archiveRequestBodyReader the bounded reader of an archive import body
   */
  @Autowired
  public ExportImportRestController(
      ExportImportFacade exportImportFacade,
      ExportFormats exportFormats,
      ArchiveRequestBodyReader archiveRequestBodyReader) {
    this.exportImportFacade = exportImportFacade;
    this.exportFormats = exportFormats;
    this.archiveRequestBodyReader = archiveRequestBodyReader;
  }

  /**
   * Returns the given training definition and its levels as a file offered for inline display,
   * named after the definition. A definition that links no attachment comes as one document, YAML
   * when the {@code Accept} header asks for YAML and JSON otherwise. A definition that links
   * attachments comes as a ZIP archive holding the document in that format, the attachment metadata
   * and the attachment files. A training administrator may export any definition, anyone else only
   * one they author.
   *
   * @param trainingDefinitionId the training definition id
   * @param requestHeaders the request's headers, whose {@code Accept} selects the document format
   * @return the definition and its levels as file bytes
   */
  @Operation(
      operationId = "getExportedTrainingDefinitionAndLevels",
      summary = "Export a training definition as a file",
      description =
          "A training administrator may export any definition, anyone else only one they author."
              + " The response type tells the two shapes apart. A definition with no linked file"
              + " is a .yaml file served as application/yaml when Accept prefers"
              + " application/yaml, text/yaml or text/yml, and a .json file served as"
              + " application/json otherwise. A definition that links files is a .zip file served"
              + " as application/zip, holding <name>.json or <name>.yaml in the format Accept"
              + " selects, attachments.json with the original name, duration, size in pixels and"
              + " poster of each file, and each file as attachments/<entry name>. A name clash"
              + " inside the archive is told apart by a number in parentheses before the last"
              + " extension.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description =
            "The definition and its levels, as a JSON or YAML file, or as a ZIP archive when the"
                + " definition links files.",
        content = {
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(type = "string", format = "binary")),
          @Content(
              mediaType = MediaType.APPLICATION_YAML_VALUE,
              schema = @Schema(type = "string", format = "binary")),
          @Content(
              mediaType = ZipMediaType.APPLICATION_ZIP_VALUE,
              schema = @Schema(type = "string", format = "binary"))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training definition has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "406",
        description = "Accept allows neither JSON nor YAML.",
        content = @Content)
  })
  @GetMapping(
      path = "/exports/training-definitions/{definitionId}",
      produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        YamlExportFormat.TEXT_YAML_VALUE,
        YamlExportFormat.TEXT_YML_VALUE,
        ZipMediaType.APPLICATION_ZIP_VALUE
      })
  public ResponseEntity<byte[]> getExportedTrainingDefinitionAndLevels(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @Parameter(hidden = true) @RequestHeader HttpHeaders requestHeaders) {
    ExportFormat format = exportFormats.byAcceptedMediaTypes(requestHeaders.getAccept());
    FileToReturnDTO file = exportImportFacade.dbExport(trainingDefinitionId, format);
    if (file.isArchive()) {
      return asInlineFile(
          file, ZipMediaType.APPLICATION_ZIP, AbstractFileExtensions.ZIP_FILE_EXTENSION);
    }
    return asInlineFile(file, format.getContentType(), format.getFileExtension());
  }

  /**
   * Creates a new training definition from the submitted one, levels included. The new definition
   * starts out unreleased whatever state was submitted, and its estimated duration is the sum of
   * its levels'. Only a training administrator or a designer may import.
   *
   * @param importTrainingDefinitionDTO the training definition to be imported
   * @return the created definition with its levels
   */
  @Operation(
      operationId = "importTrainingDefinition",
      summary = "Import a training definition",
      description =
          "Only a training administrator or a training designer may import. The new definition"
              + " starts unreleased, and its estimated duration is the sum of its levels'. Each"
              + " level takes its position from the order it is sent in. The definition may be"
              + " sent as JSON or YAML, which carries no files, so level content that references a"
              + " file is refused unless keepUnresolvedAttachments=true. A definition with files is sent as a ZIP archive to the"
              + " same path with Content-Type application/zip.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created definition and its levels."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The submitted definition could not be read, a field was refused, or the content"
                + " references a file and keepUnresolvedAttachments is not true.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "422",
        description = "The hints of a training level penalize more than its maximum score.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(
      path = "/imports/training-definitions",
      produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        YamlExportFormat.TEXT_YAML_VALUE,
        YamlExportFormat.TEXT_YML_VALUE
      },
      consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        YamlExportFormat.TEXT_YAML_VALUE,
        YamlExportFormat.TEXT_YML_VALUE
      })
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> importTrainingDefinition(
      @Valid @RequestBody ImportTrainingDefinitionDTO importTrainingDefinitionDTO,
      @Parameter(description = KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION)
          @RequestParam(name = "keepUnresolvedAttachments", defaultValue = "false")
          boolean keepUnresolvedAttachments) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        exportImportFacade.dbImport(importTrainingDefinitionDTO, keepUnresolvedAttachments);
    return ResponseEntity.ok(trainingDefinitionResource);
  }

  /**
   * Creates a new training definition from an exported ZIP archive: the definition document, the
   * attachment metadata and the attachment files. Otherwise it behaves as the import of a JSON or
   * YAML document.
   *
   * @param request the request whose body is the ZIP archive
   * @return the created definition with its levels
   * @throws IOException when the request body cannot be read
   */
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content =
          @Content(
              mediaType = ZipMediaType.APPLICATION_ZIP_VALUE,
              schema = @Schema(type = "string", format = "binary")))
  @Operation(
      operationId = "importTrainingDefinitionArchive",
      summary = "Import a training definition from a ZIP archive",
      description =
          "Only a training administrator or a training designer may import. The archive holds one"
              + " definition document (.json, .yaml or .yml) at its root, attachments.json that"
              + " lists every file with its original name, duration, size in pixels and poster,"
              + " and each file as attachments/<entry name>. Nothing else is accepted, and the"
              + " entry names are never used as paths. The distinct files may not exceed the"
              + " attachment budget of a definition, and byte size, hash and media type are"
              + " computed from the bytes. Level content may reference only files the archive"
              + " holds, unless keepUnresolvedAttachments=true keeps a reference to any other"
              + " file unlinked.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created definition and its levels."),
    @ApiResponse(
        responseCode = "400",
        description =
            "The archive breaks its layout or entry limit, its metadata or definition document is"
                + " invalid, or the content references a file the archive does not hold and"
                + " keepUnresolvedAttachments is not true.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "413",
        description =
            "The request body or the distinct files exceed the attachment budget of a definition.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "422",
        description = "The hints of a training level penalize more than its maximum score.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(
      path = "/imports/training-definitions",
      produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        YamlExportFormat.TEXT_YAML_VALUE,
        YamlExportFormat.TEXT_YML_VALUE
      },
      consumes = ZipMediaType.APPLICATION_ZIP_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> importTrainingDefinitionArchive(
      HttpServletRequest request,
      @Parameter(description = KEEP_UNRESOLVED_ATTACHMENTS_DESCRIPTION)
          @RequestParam(name = "keepUnresolvedAttachments", defaultValue = "false")
          boolean keepUnresolvedAttachments) throws IOException {
    return ResponseEntity.ok(
        exportImportFacade.dbImportArchive(
            archiveRequestBodyReader.read(request), keepUnresolvedAttachments));
  }

  /**
   * Returns the given training instance as a zip file offered for inline display, named after the
   * instance. The archive holds the instance, the training definition it runs, and one entry per
   * training run with that run's assessment answers; a run that recorded audit events also
   * contributes those events and its console commands, each broken out per level. The sandbox
   * definition is included only for an instance with a pool assigned. Every entry is written in the
   * requested format. A training administrator may archive any instance, anyone else only one they
   * organize.
   *
   * @param trainingInstanceId the training instance id
   * @param formatName the name of the format the archive's entries are written in
   * @return the archive as file bytes
   */
  @Operation(
      operationId = "archiveTrainingInstance",
      summary = "Archive a training instance as a zip file",
      description =
          "A training administrator may archive any instance, anyone else only one they organize."
              + " The archive holds the instance, the definition it runs, and one entry per"
              + " training run. A run also contributes its audit events, console commands and"
              + " assessment answers.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The archive, as a zip file.",
        content =
            @Content(
                mediaType = ZipMediaType.APPLICATION_ZIP_VALUE,
                schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number, or the format is not json or yaml.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "default",
        description =
            "The sandbox or user service failed; carries that service's status and error.",
        content = @Content(schema = @Schema(implementation = ApiMicroserviceError.class)))
  })
  @GetMapping(
      path = "/exports/training-instances/{instanceId}",
      produces = ZipMediaType.APPLICATION_ZIP_VALUE)
  public ResponseEntity<byte[]> archiveTrainingInstance(
      @PathVariable("instanceId") Long trainingInstanceId,
      @Parameter(
              description =
                  "Format of the entries inside the archive; each entry carries its extension.",
              schema = @Schema(allowableValues = {"json", "yaml"}))
          @RequestParam(name = "format", defaultValue = "json")
          String formatName) {
    FileToReturnDTO file =
        exportImportFacade.archiveTrainingInstance(
            trainingInstanceId, exportFormats.byName(formatName));
    return asInlineFile(
        file, ZipMediaType.APPLICATION_ZIP, AbstractFileExtensions.ZIP_FILE_EXTENSION);
  }

  /**
   * Returns the standing of every training run of the given instance as one ranked row apiece,
   * carrying that trainee's per-level scores and the counts of hints taken, solutions displayed and
   * wrong answers submitted, alongside the levels the ranking scores over. A training administrator
   * may report on any instance, anyone else only on one they organize.
   *
   * @param trainingInstanceId id of the training instance
   * @return the ranked standing of the instance's runs
   */
  @Operation(
      operationId = "exportTrainingInstanceScores",
      summary = "Report the scores of a training instance",
      description =
          "A training administrator may report on any instance, anyone else only one they"
              + " organize. Every run of the instance yields one ranked row, even a run whose"
              + " trainee scored nothing.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The ranked rows and the levels they are scored over."),
    @ApiResponse(
        responseCode = "400",
        description = "The id in the path is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No training instance has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
    @ApiResponse(
        responseCode = "default",
        description =
            "The sandbox or user service failed; carries that service's status and error.",
        content = @Content(schema = @Schema(implementation = ApiMicroserviceError.class)))
  })
  @GetMapping(
      path = "/exports/training-instances/{instanceId}/scores",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingInstanceScoreReportDTO> exportTrainingInstanceScores(
      @PathVariable("instanceId") Long trainingInstanceId) {
    return ResponseEntity.ok(
        exportImportFacade.exportUserScoreFromTrainingInstance(trainingInstanceId));
  }

  /**
   * Wraps the file's bytes in a response of the given content type offered for inline display,
   * named after the file's title followed by {@code fileExtension}.
   *
   * @param file the file to return
   * @param contentType the media type the file is served under
   * @param fileExtension the extension appended to the file's title
   * @return the file response
   */
  private ResponseEntity<byte[]> asInlineFile(
      FileToReturnDTO file, MediaType contentType, String fileExtension) {
    HttpHeaders header = new HttpHeaders();
    header.setContentType(contentType);
    header.setContentDisposition(
        ContentDisposition.inline()
            .filename(file.getTitle() + fileExtension, StandardCharsets.UTF_8)
            .build());
    header.setContentLength(file.getContent().length);
    return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
  }
}
