package cz.cyberrange.platform.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.cyberrange.platform.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.TrainingInstanceScoreReportDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.utils.AbstractFileExtensions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The controller for export/import. */
@Api(
    value = "/",
    tags = "Export Imports",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(
    value = {
      @ApiResponse(
          code = 401,
          message = "Full authentication is required to access this resource.",
          response = ApiError.class),
      @ApiResponse(
          code = 403,
          message = "The necessary permissions are required for a resource.",
          response = ApiError.class)
    })
@RestController
public class ExportImportRestController {

  private ExportImportFacade exportImportFacade;
  private ObjectMapper objectMapper;

  /**
   * Instantiates a new Export import rest controller.
   *
   * @param exportImportFacade the export import facade
   * @param objectMapper the object mapper
   */
  @Autowired
  public ExportImportRestController(
      ExportImportFacade exportImportFacade, ObjectMapper objectMapper) {
    this.exportImportFacade = exportImportFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Exports training definition and levels.
   *
   * @param trainingDefinitionId the training definition id
   * @return Exported training definition and levels.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get exported training definitions and levels.",
      response = ExportTrainingDefinitionAndLevelsDTO.class,
      nickname = "getExportedTrainingDefinitionAndLevels",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Training definitions and levels found and exported.",
            response = ExportTrainingDefinitionAndLevelsDTO.class),
        @ApiResponse(
            code = 404,
            message = "Training definition not found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(
      path = "/exports/training-definitions/{definitionId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> getExportedTrainingDefinitionAndLevels(
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("definitionId")
          Long trainingDefinitionId) {
    FileToReturnDTO file = exportImportFacade.dbExport(trainingDefinitionId);
    HttpHeaders header = new HttpHeaders();
    header.setContentType(new MediaType("application", "octet-stream"));
    header.set(
        "Content-Disposition",
        "inline; filename=" + file.getTitle() + AbstractFileExtensions.JSON_FILE_EXTENSION);
    header.setContentLength(file.getContent().length);
    return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
  }

  /**
   * Import training definition response entity.
   *
   * @param importTrainingDefinitionDTO the training definition to be imported
   * @param fields attributes of the object to be returned as the result.
   * @return the new imported definition
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Import training definition with levels.",
      response = TrainingDefinitionWithLevelsDTO.class,
      nickname = "importTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Training definition imported.",
            response = TrainingDefinitionWithLevelsDTO.class),
        @ApiResponse(
            code = 422,
            message =
                "Sum of hints penalties in imported training level is greater than maximal score.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(
      path = "/imports/training-definitions",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> importTrainingDefinition(
      @ApiParam(value = "Training definition to be imported", required = true) @Valid @RequestBody
          ImportTrainingDefinitionDTO importTrainingDefinitionDTO,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        exportImportFacade.dbImport(importTrainingDefinitionDTO);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Archive training instance
   *
   * @param trainingInstanceId the training instance id
   * @return file containing wanted training instance
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Archive training instance",
      response = TrainingInstanceArchiveDTO.class,
      nickname = "archiveTrainingInstance",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Training instance archived.",
            response = TrainingInstanceArchiveDTO.class),
        @ApiResponse(
            code = 404,
            message = "Training instance not found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot archive instance that is not finished.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(
      path = "/exports/training-instances/{instanceId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> archiveTrainingInstance(
      @ApiParam(value = "Id of training instance", required = true) @PathVariable("instanceId")
          Long trainingInstanceId) {
    FileToReturnDTO file = exportImportFacade.archiveTrainingInstance(trainingInstanceId);
    HttpHeaders header = new HttpHeaders();
    header.setContentType(new MediaType("application", "octet-stream"));
    header.set(
        "Content-Disposition",
        "inline; filename=" + file.getTitle() + AbstractFileExtensions.ZIP_FILE_EXTENSION);
    header.setContentLength(file.getContent().length);
    return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
  }

  /**
   * Export user scores from a specific training instance
   *
   * @param trainingInstanceId id of the training instance
   * @return the standing of every participant of the instance
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Export training instance scores",
      response = TrainingInstanceScoreReportDTO.class,
      nickname = "exportTrainingInstanceScores",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Training instance score exported",
            response = TrainingInstanceScoreReportDTO.class),
        @ApiResponse(
            code = 404,
            message = "Training instance not found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(
      path = "/exports/training-instances/{instanceId}/scores",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingInstanceScoreReportDTO> exportTrainingInstanceScores(
      @ApiParam(value = "Id of training instance", required = true) @PathVariable("instanceId")
          Long trainingInstanceId) {
    return ResponseEntity.ok(
        exportImportFacade.exportUserScoreFromTrainingInstance(trainingInstanceId));
  }
}
