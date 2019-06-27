package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import cz.muni.ics.kypo.training.rest.ExceptionSorter;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

/**
 * @author Pavel Šeda & Boris Jadus
 */
@Api(value = "/training-definitions", tags = "Training definitions", consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource."),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.")
})
@RestController
@RequestMapping(path = "/training-definitions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingDefinitionsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionsRestController.class);

    private TrainingDefinitionFacade trainingDefinitionFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public TrainingDefinitionsRestController(TrainingDefinitionFacade trainingDefinitionFacade, ObjectMapper objectMapper) {
        this.trainingDefinitionFacade = trainingDefinitionFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Get requested Training Definition by id.
     *
     * @param id of Training Definition to return.
     * @return Requested Training Definition by id.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get Training Definition by Id.",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "findTrainingDefinitionById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definition found.", response = TrainingDefinitionByIdDTO.class),
            @ApiResponse(code = 404, message = "Training definition with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")

    })
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findTrainingDefinitionById(
            @ApiParam(value = "ID of training definition to be retrieved.", required = true)
            @PathVariable Long id,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            TrainingDefinitionByIdDTO trainingDefinitionResource = trainingDefinitionFacade.findById(id);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
        } catch (FacadeLayerException ex) {
            LOG.error("Training definition with id {} not found.", id);
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    @ApiModel(description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    private static class TrainingDefinitionRestResource extends PageResultResource<TrainingDefinitionByIdDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Training Definitions from databases.")
        private List<TrainingDefinitionByIdDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    /**
     * Get all Training Definitions.
     *
     * @return all Training Definitions.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all Training Definitions.",
            response = TrainingDefinitionRestResource.class,
            nickname = "findAllTrainingDefinitions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training definitions found.", response = TrainingDefinitionByIdDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingDefinitions(
            @QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
            Pageable pageable,
            @ApiParam(value = "Parameters for filtering the objects.", required = false)
            @RequestParam MultiValueMap<String, String> parameters,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {

        PageResultResource<TrainingDefinitionDTO> trainingDefinitionResource = trainingDefinitionFacade.findAll(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
    }

    /**
     * Get all Training Definitions for organizers.
     *
     * @return all Training Definitions for organizers.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all Training Definitions for organizers.",
            response = TrainingDefinitionRestResource.class,
            nickname = "findAllTrainingDefinitionsForOrganizers",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training definitions for organizers found.", response = TrainingDefinitionInfoDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(path = "/for-organizers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingDefinitionsForOrganizers(
            @QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
            Pageable pageable,
            @ApiParam(value = "Parameters for filtering the objects.", required = false)
            @RequestParam MultiValueMap<String, String> parameters,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {

        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionResource = trainingDefinitionFacade.findAllForOrganizers(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get all training definition by sandbox definition id",
            response = TrainingDefinitionRestResource.class,
            nickname = "findAllTrainingDefinitionsBySandboxDefinitionId",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All training definitions by sandbox definition found.", response = TrainingDefinitionByIdDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @ApiPageableSwagger
    @GetMapping(path = "/sandbox-definitions/{sandboxDefinitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllTrainingDefinitionsBySandboxDefinitionId(
            @ApiParam(value = "Id of sandbox definition", required = true)
            @PathVariable(value = "sandboxDefinitionId") Long sandboxDefinitionId,
            Pageable pageable) {
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionResource = trainingDefinitionFacade.findAllBySandboxDefinitionId(sandboxDefinitionId, pageable);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
    }

    @ApiOperation(httpMethod = "POST",
            value = "Create Training Definition",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "createTrainingDefinition",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The requested resource has been created.", response = TrainingDefinitionCreateDTO.class),
            @ApiResponse(code = 400, message = "Given training definition is not valid"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTrainingDefinition(
            @ApiParam(value = "Training Definition to be created")
            @RequestBody @Valid TrainingDefinitionCreateDTO trainingDefinitionCreateDTO,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {

        TrainingDefinitionByIdDTO trainingDefinitionResource = trainingDefinitionFacade.create(trainingDefinitionCreateDTO);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Update Training Definition",
            notes = "Only unreleased training definition can be updated",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "updateTrainingDefinition",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The requested resource has been updated."),
            @ApiResponse(code = 400, message = "Given training definition is not valid"),
            @ApiResponse(code = 404, message = "Training definition with given id not found."),
            @ApiResponse(code = 409, message = "Cannot edit released or archived training definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateTrainingDefinition(
            @ApiParam(value = "Training definition to be updated")
            @RequestBody @Valid TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
        try {
            trainingDefinitionFacade.update(trainingDefinitionUpdateDTO);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "POST",
            value = "Clone training definition",
            notes = "Only released and archived training definitions can be cloned",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "cloneTrainingDefinition",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definition cloned.", response = TrainingDefinitionByIdDTO.class),
            @ApiResponse(code = 404, message = "Training definition with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingDefinitionByIdDTO> cloneTrainingDefinition(
            @ApiParam(value = "Id of training definition to be cloned", required = true)
            @PathVariable("id") Long id,
            @ApiParam(value = "Title of cloned definition", required = true)
            @RequestParam(value = "title") String title) {
        try {
            TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionFacade.clone(id, title);
            return ResponseEntity.ok(trainingDefinitionByIdDTO);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Swap levels",
            notes = "The first one level cannot be swapped to the left",
            nickname = "swapLevels",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = BasicLevelInfoDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The level has been swapped to the left."),
            @ApiResponse(code = 404, message = "Training definition with given id not found."),
            @ApiResponse(code = 409, message = "Cannot edit released or archived training definition or cannot swap first level to the left."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(path = "/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> swapLevels(
            @ApiParam(value = "Id of training definition", required = true)
            @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "Id of training definition", required = true)
            @PathVariable("levelIdFrom") Long levelIdFrom,
            @ApiParam(value = "Id of training definition", required = true)
            @PathVariable("levelIdTo") Long levelIdTo) {
        try {
            return ResponseEntity.ok(trainingDefinitionFacade.swapLevels(definitionId, levelIdFrom, levelIdTo));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Delete training definition",
            notes = "Released training definition canont be deleted",
            nickname = "deleteTrainingDefinition",
            response = Void.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definition deleted."),
            @ApiResponse(code = 404, message = "Training definition or one of levels cannot be found."),
            @ApiResponse(code = 409, message = "Cannot delete released training definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTrainingDefinition(
            @ApiParam(value = "Id of training definition to be deleted")
            @PathVariable("id") Long id) {
        try {
            trainingDefinitionFacade.delete(id);
            return ResponseEntity.ok().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Delete specific level from definition",
            notes = "Level can be deleted only in unreleased training definition",
            nickname = "deleteOneLevel",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = Void.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The level deleted."),
            @ApiResponse(code = 404, message = "Level with given id not found."),
            @ApiResponse(code = 409, message = "Cannot edit released or archived training definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @DeleteMapping(path = "/{definitionId}/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteOneLevel(
            @ApiParam(value = "Id of training definition from which level is deleted")
            @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "Id of level to be deleted")
            @PathVariable("levelId") Long levelId) {
        try {
            return ResponseEntity.ok(trainingDefinitionFacade.deleteOneLevel(definitionId, levelId));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Update specific game level in given training definition",
            notes = "Level can be updated only in unreleased training definition",
            nickname = "updateGameLevel",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Game level updated."),
            @ApiResponse(code = 400, message = "Given game level is not valid."),
            @ApiResponse(code = 409, message = "Cannot edit released or archived training definition."),
            @ApiResponse(code = 404, message = "Level not found in definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(path = "/{definitionId}/game-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGameLevel(
            @ApiParam(value = "Id of definition to which level is assigned", required = true)
            @PathVariable(value = "definitionId") Long definitionId,
            @ApiParam(value = "Game level to be updated")
            @RequestBody @Valid GameLevelUpdateDTO gameLevelUpdateDTO) {
        try {
            trainingDefinitionFacade.updateGameLevel(definitionId, gameLevelUpdateDTO);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Update specific info level in given training definition",
            notes = "Level can be deleted only in unreleased training definition",
            nickname = "updateInfoLevel",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Info level updated."),
            @ApiResponse(code = 400, message = "Given info level is not valid."),
            @ApiResponse(code = 409, message = "Cannot edit released or archived training definition."),
            @ApiResponse(code = 404, message = "Level not found in definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(path = "/{definitionId}/info-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateInfoLevel(
            @ApiParam(value = "Id of definition to which level is assigned", required = true)
            @PathVariable(value = "definitionId") Long definitionId,
            @ApiParam(value = "Info level to be updated")
            @RequestBody @Valid InfoLevelUpdateDTO infoLevelUpdateDTO) {
        try {
            trainingDefinitionFacade.updateInfoLevel(definitionId, infoLevelUpdateDTO);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Update specific assessment level in given training definition",
            notes = "Level can be deleted only in unreleased training definition",
            nickname = "updateAssessmentLevel",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Assessment level updated."),
            @ApiResponse(code = 400, message = "Given assessment level is not valid."),
            @ApiResponse(code = 409, message = "Cannot edit released or archived training definition."),
            @ApiResponse(code = 404, message = "Level not found in definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(path = "/{definitionId}/assessment-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAssessmentLevel(
            @ApiParam(value = "Id of definition to which level is assigned", required = true)
            @PathVariable(value = "definitionId") Long definitionId,
            @ApiParam(value = "Assessment level to be updated")
            @RequestBody @Valid AssessmentLevelUpdateDTO assessmentLevelUpdateDTO) {
        try {
            trainingDefinitionFacade.updateAssessmentLevel(definitionId, assessmentLevelUpdateDTO);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "GET",
            value = "Find level by ID",
            response = AbstractLevelDTO.class,
            nickname = "findLevelById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Game level has been found.", response = AbstractLevelDTO.class),
            @ApiResponse(code = 404, message = "Level with given id not found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findLevelById(
            @ApiParam(value = "Id of wanted level", required = true)
            @PathVariable(value = "levelId") Long levelId,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            AbstractLevelDTO level = trainingDefinitionFacade.findLevelById(levelId);
            Squiggly.init(objectMapper, fields);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, level));
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "POST",
            value = "Create level",
            notes = "Creates only default level for given training definition",
            response = BasicLevelInfoDTO.class,
            nickname = "createLevel",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Level created.", response = AbstractLevelDTO.class),
            @ApiResponse(code = 404, message = "Training definition with given id not found."),
            @ApiResponse(code = 409, message = "Cannot create level in released or archived training definition."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(path = "/{definitionId}/levels/{levelType}")
    public ResponseEntity<Object> createLevel(
            @ApiParam(value = "Id of definition for which is level created", required = true)
            @PathVariable(value = "definitionId") Long definitionId,
            @ApiParam(value = "Level type", allowableValues = "GAME, ASSESSMENT, INFO", required = true)
            @PathVariable(value = "levelType") LevelType levelType,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            BasicLevelInfoDTO basicLevelInfoDTO;
            if (levelType.equals(LevelType.GAME)) {
                basicLevelInfoDTO = trainingDefinitionFacade.createGameLevel(definitionId);
            } else if (levelType.equals(LevelType.ASSESSMENT)) {
                basicLevelInfoDTO = trainingDefinitionFacade.createAssessmentLevel(definitionId);
            } else {
                basicLevelInfoDTO = trainingDefinitionFacade.createInfoLevel(definitionId);
            }
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, basicLevelInfoDTO), HttpStatus.CREATED);
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Get requested designers.
     *
     * @return List of users login and full name with role designer.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get designers.",
            response = String[].class,
            nickname = "findTrainingDefinitionById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Designers found.", response = String[].class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")

    })
    @GetMapping(path = "/designers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDesigners(@QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
                                               Pageable pageable) {
        try {
            List<UserInfoDTO> designers = trainingDefinitionFacade.getUsersWithGivenRole(cz.muni.ics.kypo.training.api.enums.RoleType.ROLE_TRAINING_DESIGNER, pageable);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    /**
     * Get requested designers.
     *
     * @return List of users login and full name with role designer.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get organizers.",
            response = String[].class,
            nickname = "getOrganizers",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Organizers found.", response = String[].class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(path = "/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOrganizers(@QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
                                                Pageable pageable) {
        try {
            List<UserInfoDTO> designers = trainingDefinitionFacade.getUsersWithGivenRole(RoleType.ROLE_TRAINING_ORGANIZER, pageable);
            return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Switch state of training definition",
            response = Void.class,
            nickname = "switchDefinitionState")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definition updated."),
            @ApiResponse(code = 404, message = "Training definition with given id not found."),
            @ApiResponse(code = 409, message = "Cannot edit definition with created instances."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PutMapping(path = "/{definitionId}/states/{state}")
    public ResponseEntity<Void> switchState(
            @ApiParam(value = "Id of definition", required = true)
            @PathVariable(value = "definitionId") Long definitionId,
            @ApiParam(value = "New state of definition", allowableValues = "RELEASED, UNRELEASED, ARCHIVED", required = true)
            @PathVariable(value = "state") TDState state) {
        try {
            trainingDefinitionFacade.switchState(definitionId, state);
            return ResponseEntity.noContent().build();
        } catch (FacadeLayerException ex) {
            throw ExceptionSorter.throwException(ex);
        }
    }

}
