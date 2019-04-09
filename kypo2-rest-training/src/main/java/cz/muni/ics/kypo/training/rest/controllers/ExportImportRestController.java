package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Pavel Seda
 */
@Api(value = "/", tags = "Export Imports", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExportImportRestController {

    private ExportImportFacade exportImportFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public ExportImportRestController(ExportImportFacade exportImportFacade, ObjectMapper objectMapper) {
        this.exportImportFacade = exportImportFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Get exported training definition and levels.
     *
     * @return Exported training definition and levels.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get exported training definitions and levels.",
            response = ExportTrainingDefinitionAndLevelsDTO.class,
            nickname = "findTrainingDefinitionById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definitions and levels found.", response = ExportTrainingDefinitionAndLevelsDTO.class),
            @ApiResponse(code = 404, message = "Training definition and levels not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/exports/training-definitions/{trainingDefinitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getExportedTrainingDefinitionAndLevels(
            @ApiParam(value = "Id of training definition", required = true)
            @PathVariable(value = "trainingDefinitionId") Long trainingDefinitionId,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, exportImportFacade.dbExport(trainingDefinitionId)));
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex);
        }
    }

    @ApiOperation(httpMethod = "POST",
            value = "Import training definition with levels.",
            response = TrainingDefinitionDTO.class,
            nickname = "importTrainingDefinition",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definition imported.", response = TrainingDefinitionDTO.class)
    })
    @PostMapping(path = "/imports/training-definitions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importTrainingDefinition(
            @ApiParam(value = "Training definition to be imported", required = true)
            @Valid @RequestBody ImportTrainingDefinitionDTO importTrainingDefinitionDTO,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        // by default set uploaded training definition to unrelease state
        importTrainingDefinitionDTO.setState(TDState.UNRELEASED);
        TrainingDefinitionDTO trainingDefinitionResource = exportImportFacade.dbImport(importTrainingDefinitionDTO);
        trainingDefinitionResource.setState(TDState.UNRELEASED);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
    }

    @ApiOperation(httpMethod = "GET",
            value = "Archive training instance",
            response = TrainingInstanceArchiveDTO.class,
            nickname = "archiveTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Training instance archived.", response = TrainingInstanceArchiveDTO.class),
        @ApiResponse(code = 404, message = "Training instance not found."),
        @ApiResponse(code = 409, message = "Cannot archive instance that is not finished."),
        @ApiResponse(code = 500, message = "Unexpected condition was encountered.")

    })
    @GetMapping(path = "/exports/training-instances/{trainingInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> archiveTrainingInstance(
            @ApiParam(value = "Id of training instance", required = true)
            @PathVariable(value = "trainingInstanceId") Long trainingInstanceId,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields){
        try{
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, exportImportFacade.archiveTrainingInstance(trainingInstanceId)));
        } catch (FacadeLayerException ex){
            throw ExceptionSorter.throwException(ex);
        }

    }

}
