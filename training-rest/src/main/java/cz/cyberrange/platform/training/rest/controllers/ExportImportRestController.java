package cz.cyberrange.platform.training.rest.controllers;

import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.TrainingInstanceScoreReportDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
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
import jakarta.validation.Valid;
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

  private ExportImportFacade exportImportFacade;
  private ExportFormats exportFormats;

  /**
   * Instantiates a new Export import rest controller.
   *
   * @param exportImportFacade the export import facade
   * @param exportFormats the selector of the format an export is written in
   */
  @Autowired
  public ExportImportRestController(
      ExportImportFacade exportImportFacade, ExportFormats exportFormats) {
    this.exportImportFacade = exportImportFacade;
    this.exportFormats = exportFormats;
  }

  /**
   * Returns the given training definition and its levels as a file offered for inline display,
   * named after the definition. The file is YAML when the {@code Accept} header asks for YAML, JSON
   * otherwise. A training administrator may export any definition, anyone else only one they
   * author.
   *
   * @param trainingDefinitionId the training definition id
   * @param requestHeaders the request's headers, whose {@code Accept} selects the file format
   * @return the definition and its levels as file bytes
   */
  @Operation(
      operationId = "getExportedTrainingDefinitionAndLevels",
      summary = "Export a training definition as a file",
      description =
          "A training administrator may export any definition, anyone else only one they author."
              + " The file holds the definition with its levels in order, and is named after the"
              + " definition. It is a .yaml file served as application/yaml when Accept prefers"
              + " application/yaml, text/yaml or text/yml, and a .json file served as"
              + " application/json otherwise.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The definition and its levels, as a JSON or YAML file.",
        content = {
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(type = "string", format = "binary")),
          @Content(
              mediaType = MediaType.APPLICATION_YAML_VALUE,
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
        YamlExportFormat.TEXT_YML_VALUE
      })
  public ResponseEntity<byte[]> getExportedTrainingDefinitionAndLevels(
      @PathVariable("definitionId") Long trainingDefinitionId,
      @Parameter(hidden = true) @RequestHeader HttpHeaders requestHeaders) {
    ExportFormat format = exportFormats.byAcceptedMediaTypes(requestHeaders.getAccept());
    FileToReturnDTO file = exportImportFacade.dbExport(trainingDefinitionId, format);
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
              + " sent as JSON or YAML.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The created definition and its levels."),
    @ApiResponse(
        responseCode = "400",
        description = "The submitted definition could not be read, or a field was refused.",
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
      @Valid @RequestBody ImportTrainingDefinitionDTO importTrainingDefinitionDTO) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        exportImportFacade.dbImport(importTrainingDefinitionDTO);
    return ResponseEntity.ok(trainingDefinitionResource);
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
