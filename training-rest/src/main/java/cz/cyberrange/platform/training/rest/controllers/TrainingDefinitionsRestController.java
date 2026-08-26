package cz.cyberrange.platform.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.commons.security.mapping.UserInfoDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionMitreTechniquesDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.enums.LevelType;
import cz.cyberrange.platform.training.rest.utils.annotations.ApiPageableSwagger;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
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

/** The rest controller for Training definitions. */
@Api(
    value = "/training-definitions",
    tags = "Training definitions",
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
@Validated
@RestController
@RequestMapping(path = "/training-definitions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingDefinitionsRestController {

  private TrainingDefinitionFacade trainingDefinitionFacade;
  private ObjectMapper objectMapper;

  /**
   * Instantiates a new Training Definitions rest controller.
   *
   * @param trainingDefinitionFacade the training definition facade
   * @param objectMapper the object mapper
   */
  @Autowired
  public TrainingDefinitionsRestController(
      TrainingDefinitionFacade trainingDefinitionFacade, ObjectMapper objectMapper) {
    this.trainingDefinitionFacade = trainingDefinitionFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Returns one training definition with the full detail of its levels, serialized to JSON narrowed
   * to the requested attributes.
   *
   * @param id id of the training definition to return
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole
   *     definition being returned when absent
   * @return the JSON body of the {@link TrainingDefinitionWithLevelsDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get Training Definition by Id.",
      response = TrainingDefinitionWithLevelsDTO.class,
      nickname = "findTrainingDefinitionById",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definition has been found.",
            response = TrainingDefinitionWithLevelsDTO.class),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingDefinitionById(
      @ApiParam(value = "ID of training definition to be retrieved.", required = true)
          @PathVariable(value = "definitionId")
          Long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        trainingDefinitionFacade.findById(id);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Returns a page of training definitions, serialized to JSON narrowed to the requested
   * attributes. A training administrator receives every definition matching the predicate, while
   * any other caller receives only those they author or organize the beta testing group of.
   *
   * @param predicate restricts which definitions are considered, string comparisons matching
   *     partially and ignoring case
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link TrainingDefinitionDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all Training Definitions.",
      response = TrainingDefinitionRestResource.class,
      nickname = "findAllTrainingDefinitions",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The requested resources have been found.",
            response = TrainingDefinitionWithLevelsDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitions(
      @QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
      Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {

    PageResultResource<TrainingDefinitionDTO> trainingDefinitionResource =
        trainingDefinitionFacade.findAll(predicate, pageable);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Returns a page of the training definitions in the given state that the caller may organize,
   * serialized to JSON narrowed to the requested attributes. Released definitions are returned to
   * every caller. Unreleased ones are returned in full to a training administrator, narrowed to
   * those the caller either authors or beta tests when the caller holds both the designer and the
   * organizer role, and narrowed to those the caller beta tests otherwise, authorship granting no
   * visibility in that last case.
   *
   * @param state the state the definitions have to be in, which has to be released or unreleased
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link TrainingDefinitionInfoDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all Training Definitions for organizers.",
      response = TrainingDefinitionRestResource.class,
      nickname = "findAllTrainingDefinitionsForOrganizers",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definitions have been found.",
            response = TrainingDefinitionInfoDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/for-organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitionsForOrganizers(
      @ApiParam(value = "State of the training definition", required = true)
          @RequestParam(value = "state")
          TDState state,
      Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {

    PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionResource =
        trainingDefinitionFacade.findAllForOrganizers(state, pageable);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Get MITRE techniques used by released Training Definitions.
   *
   * @return released Training Definitions using MITRE techniques, each flagged as played or not
   *     played by the requesting user.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get MITRE techniques of released Training Definitions.",
      response = TrainingDefinitionMitreTechniquesDTO.class,
      responseContainer = "List",
      nickname = "findPlayedMitreTechniques",
      notes =
          "Returns released training definitions that use at least one MITRE technique, each with"
              + " its distinct technique keys and a flag telling whether the requesting user has"
              + " played it.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The MITRE techniques have been found.",
            response = TrainingDefinitionMitreTechniquesDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/played-mitre-techniques", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TrainingDefinitionMitreTechniquesDTO>> findPlayedMitreTechniques() {
    return ResponseEntity.ok(trainingDefinitionFacade.findPlayedMitreTechniques());
  }

  /**
   * Stores a new training definition, listing the calling user among its authors, and returns it
   * serialized to JSON narrowed to the requested attributes. When the payload asks for default
   * content, the definition is created with a starting set of levels.
   *
   * @param trainingDefinitionCreateDTO the training definition to store
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole
   *     definition being returned when absent
   * @return the JSON body of the stored {@link TrainingDefinitionWithLevelsDTO} as a string
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Create Training Definition",
      response = TrainingDefinitionWithLevelsDTO.class,
      nickname = "createTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definition has been created.",
            response = TrainingDefinitionWithLevelsDTO.class),
        @ApiResponse(
            code = 400,
            message = "The provided training definition is not valid",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createTrainingDefinition(
      @ApiParam(value = "Training Definition to be created") @RequestBody @Valid
          TrainingDefinitionCreateDTO trainingDefinitionCreateDTO,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionResource =
        trainingDefinitionFacade.create(trainingDefinitionCreateDTO);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Overwrites a training definition that is unreleased and has no training instance yet, listing
   * the calling user among its authors and keeping the estimated duration already stored rather
   * than the submitted one. A payload that omits the beta testing group while the stored definition
   * has one is refused, since the group can only have its organizers emptied and not be removed.
   *
   * @param trainingDefinitionUpdateDTO the training definition to overwrite, identified by the id
   *     it carries
   * @return an empty response carrying no content
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update Training Definition",
      notes = "Only unreleased training definition can be updated",
      nickname = "updateTrainingDefinition",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training definition has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided training definition is not valid",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateTrainingDefinition(
      @ApiParam(value = "Training definition to be updated") @RequestBody @Valid
          TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
    trainingDefinitionFacade.update(trainingDefinitionUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Stores a copy of a training definition together with copies of all its levels, under the given
   * title and with the calling user among its authors, and returns the copy.
   *
   * @param id id of the training definition to copy
   * @param title title the copy is stored under
   * @return the stored copy, its levels in presentation order
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Clone training definition",
      notes = "Only released and archived training definitions can be cloned",
      response = TrainingDefinitionWithLevelsDTO.class,
      nickname = "cloneTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definition has been cloned.",
            response = TrainingDefinitionWithLevelsDTO.class),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(path = "/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionWithLevelsDTO> cloneTrainingDefinition(
      @ApiParam(value = "Id of training definition to be cloned", required = true)
          @PathVariable("definitionId")
          Long id,
      @ApiParam(value = "Title of cloned definition", required = true)
          @RequestParam(value = "title")
          String title) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionWithLevelsDTO =
        trainingDefinitionFacade.clone(id, title);
    return ResponseEntity.ok(trainingDefinitionWithLevelsDTO);
  }

  /**
   * Exchanges the positions of two levels of a training definition that is neither released nor
   * archived, and returns the definition's levels in their resulting order.
   *
   * @param definitionId id of the training definition holding both levels
   * @param levelIdFrom id of one level to exchange
   * @param levelIdTo id of the other level to exchange
   * @return the basic information of every level of the definition, in presentation order
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Swap levels",
      notes = "The first one level cannot be swapped to the left",
      nickname = "swapLevels",
      produces = MediaType.APPLICATION_JSON_VALUE,
      response = BasicLevelInfoDTO[].class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The level has been swapped to the left.",
            response = BasicLevelInfoDTO[].class),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message =
                "Cannot edit released or archived training definition or cannot swap first level to the left.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      path = "/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> swapLevels(
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("levelIdFrom")
          Long levelIdFrom,
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("levelIdTo")
          Long levelIdTo) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.swapLevels(definitionId, levelIdFrom, levelIdTo));
  }

  /**
   * Moves one level of a training definition that is unreleased and has no training instance yet
   * to the given position, shifting the levels in between, and returns the definition's levels in
   * their resulting order. A position outside the definition's range is pulled to the nearest end
   * rather than refused.
   *
   * @param definitionId id of the training definition holding the level
   * @param levelIdToBeMoved id of the level to move
   * @param newPosition position to move the level to
   * @return the basic information of every level of the definition, in presentation order
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Move level",
      nickname = "moveLevel",
      produces = MediaType.APPLICATION_JSON_VALUE,
      response = BasicLevelInfoDTO.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The level has been moved to the given position."),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      path = "/{definitionId}/levels/{levelIdToBeMoved}/move-to/{newPosition}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> moveLevel(
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Id of training definition", required = true)
          @PathVariable("levelIdToBeMoved")
          Long levelIdToBeMoved,
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("newPosition")
          Integer newPosition) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.moveLevel(definitionId, levelIdToBeMoved, newPosition));
  }

  /**
   * Removes a training definition along with all of its levels. A released definition, or one that
   * already has a training instance, is refused rather than removed.
   *
   * @param id id of the training definition to remove
   * @return an empty successful response
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete training definition",
      notes = "Released training definition cannot be deleted",
      nickname = "deleteTrainingDefinition")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The Training definition has been deleted."),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot delete released training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @DeleteMapping(path = "/{definitionId}")
  public ResponseEntity<Void> deleteTrainingDefinition(
      @ApiParam(value = "Id of training definition to be deleted", required = true)
          @PathVariable("definitionId")
          Long id) {
    trainingDefinitionFacade.delete(id);
    return ResponseEntity.ok().build();
  }

  /**
   * Removes one level from an unreleased training definition, closing the gap in the level order
   * and reducing the definition's estimated duration by the removed level's own, then returns the
   * levels that remain.
   *
   * @param definitionId id of the training definition holding the level
   * @param levelId id of the level to remove
   * @return the basic information of every remaining level of the definition, in presentation order
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete specific level from training definition",
      notes = "Level can be deleted only in unreleased training definition",
      nickname = "deleteOneLevel",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The level has been deleted."),
        @ApiResponse(
            code = 404,
            message = "The level has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @DeleteMapping(
      path = "/{definitionId}/levels/{levelId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> deleteOneLevel(
      @ApiParam(value = "Id of training definition from which level is deleted", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Id of level to be deleted", required = true) @PathVariable("levelId")
          Long levelId) {
    return ResponseEntity.ok(trainingDefinitionFacade.deleteOneLevel(definitionId, levelId));
  }

  /**
   * Overwrites one training level of a training definition that is unreleased and has no training
   * instance yet, and records the edit on the definition.
   *
   * @param definitionId id of the training definition the level has to belong to
   * @param trainingLevelUpdateDTO the training level to overwrite, identified by the id it carries
   * @return an empty response carrying no content
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update training level",
      notes = "Level can be updated only in unreleased training definition",
      nickname = "updateTrainingLevel",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The training level has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided training level is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training level has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/training-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateTrainingLevel(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Training level to be updated") @RequestBody @Valid
          TrainingLevelUpdateDTO trainingLevelUpdateDTO) {
    trainingDefinitionFacade.updateTrainingLevel(definitionId, trainingLevelUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Overwrites one info level of a training definition that is unreleased and has no training
   * instance yet, and records the edit on the definition.
   *
   * @param definitionId id of the training definition the level has to belong to
   * @param infoLevelUpdateDTO the info level to overwrite, identified by the id it carries
   * @return an empty response carrying no content
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update info level",
      notes = "Level can be deleted only in unreleased training definition",
      nickname = "updateInfoLevel",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The info level has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided info level is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The info level has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/info-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateInfoLevel(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Info level to be updated") @RequestBody @Valid
          InfoLevelUpdateDTO infoLevelUpdateDTO) {
    trainingDefinitionFacade.updateInfoLevel(definitionId, infoLevelUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Overwrites one assessment level of a training definition that is unreleased and has no training
   * instance yet, and records the edit on the definition. For a level scored as a test, the correct
   * option of every extended matching statement is resolved and checked before the level is stored.
   *
   * @param definitionId id of the training definition the level has to belong to
   * @param assessmentLevelUpdateDTO the assessment level to overwrite, identified by the id it
   *     carries
   * @return an empty response carrying no content
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update assessment level",
      notes = "Level can be deleted only in unreleased training definition",
      nickname = "updateAssessmentLevel",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The assessment level has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided assessment level is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The level has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      path = "/{definitionId}/assessment-levels",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateAssessmentLevel(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Assessment level to be updated") @RequestBody @Valid
          AssessmentLevelUpdateDTO assessmentLevelUpdateDTO) {
    trainingDefinitionFacade.updateAssessmentLevel(definitionId, assessmentLevelUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Overwrites several levels of a training definition that is unreleased and has no training
   * instance yet in one request, each level handled according to its own type, and records the edit
   * on the definition. A submitted level that does not belong to the definition aborts the whole
   * request.
   *
   * @param definitionId id of the training definition every submitted level has to belong to
   * @param levelUpdateDTOS the levels to overwrite, each identified by the id it carries
   * @return an empty response carrying no content
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update levels",
      notes = "Levels can be updated only in unreleased training definition.",
      nickname = "updateLevels",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The levels has been updated."),
        @ApiResponse(
            code = 400,
            message = "One of the provided levels is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "One of the provided levels has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/levels", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateLevels(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Levels to be updated") @RequestBody @Valid
          List<AbstractLevelUpdateDTO> levelUpdateDTOS) {
    trainingDefinitionFacade.updateLevels(definitionId, levelUpdateDTOS);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns one level in the full detail of whichever level type it turns out to be, serialized to
   * JSON narrowed to the requested attributes.
   *
   * @param levelId id of the level to return
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole level
   *     being returned when absent
   * @return the JSON body of the level as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Find level by ID",
      response = AbstractLevelDTO.class,
      nickname = "findLevelById",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The level has been found.",
            response = AbstractLevelDTO.class),
        @ApiResponse(
            code = 404,
            message = "The level has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findLevelById(
      @ApiParam(value = "Id of wanted level", required = true) @PathVariable("levelId")
          Long levelId,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    AbstractLevelDTO level = trainingDefinitionFacade.findLevelById(levelId);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, level));
  }

  /**
   * Appends a new level of the given type, filled with placeholder content, to the end of a
   * training definition that is unreleased and has no training instance yet, raising the
   * definition's estimated duration by the new level's own, and returns the level serialized to
   * JSON narrowed to the requested attributes.
   *
   * @param definitionId id of the training definition to append the level to
   * @param levelType which kind of level to append
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole level
   *     information being returned when absent
   * @return the JSON body of the new level's basic information as a string
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Create level",
      notes = "Creates only default level for given training definition",
      response = BasicLevelInfoDTO.class,
      nickname = "createLevel",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 201,
            message = "The level has been created.",
            response = AbstractLevelDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been not found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot create level in released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(path = "/{definitionId}/levels/{levelType}")
  public ResponseEntity<Object> createLevel(
      @ApiParam(value = "Id of definition for which is level created", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(
              value = "Level type",
              allowableValues = "TRAINING, ASSESSMENT, INFO",
              required = true)
          @PathVariable("levelType")
          LevelType levelType,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    BasicLevelInfoDTO basicLevelInfoDTO;
    if (levelType.equals(LevelType.TRAINING)) {
      basicLevelInfoDTO = trainingDefinitionFacade.createTrainingLevel(definitionId);
    } else if (levelType.equals(LevelType.ASSESSMENT)) {
      basicLevelInfoDTO = trainingDefinitionFacade.createAssessmentLevel(definitionId);
    } else if (levelType.equals(LevelType.ACCESS)) {
      basicLevelInfoDTO = trainingDefinitionFacade.createAccessLevel(definitionId);
    } else {
      basicLevelInfoDTO = trainingDefinitionFacade.createInfoLevel(definitionId);
    }
    Squiggly.init(objectMapper, fields);
    return new ResponseEntity<>(
        SquigglyUtils.stringify(objectMapper, basicLevelInfoDTO), HttpStatus.CREATED);
  }

  /**
   * Returns a page of the users the user-and-group service reports as holding the training designer
   * role, serialized to JSON narrowed to the requested attributes.
   *
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     absent
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     absent
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link UserRefDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get designers.",
      response = UserInfoRestResource.class,
      nickname = "getDesigners",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The designers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/designers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getDesigners(
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getUsersWithGivenRole(
            RoleType.ROLE_TRAINING_DESIGNER, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Returns a page of the users the user-and-group service reports as holding the training
   * organizer role, serialized to JSON narrowed to the requested attributes.
   *
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     absent
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     absent
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link UserRefDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get organizers.",
      response = UserInfoRestResource.class,
      nickname = "getOrganizers",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The organizers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getOrganizers(
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    PageResultResource<UserRefDTO> organizers =
        trainingDefinitionFacade.getUsersWithGivenRole(
            RoleType.ROLE_TRAINING_ORGANIZER, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, organizers));
  }

  /**
   * Returns a page of the users holding the training designer role who do not yet author the given
   * training definition, serialized to JSON narrowed to the requested attributes.
   *
   * @param trainingDefinitionId id of the training definition whose current authors are left out
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     absent
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     absent
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link UserRefDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get designers not in given training definition.",
      response = UserInfoRestResource.class,
      nickname = "findDesignersNotInGivenTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The designers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(
      path = "{definitionId}/designers-not-in-training-definition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getDesignersNotInGivenTrainingDefinition(
      @ApiParam(
              value =
                  "ID of the training definition which do not contains authors you want to retrieve.",
              required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
            trainingDefinitionId, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Returns a page of the organizers making up the given training definition's beta testing group,
   * serialized to JSON narrowed to the requested attributes. A definition without such a group, or
   * with an empty one, yields an empty page.
   *
   * @param trainingDefinitionId id of the training definition whose beta testing group is read
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link UserRefDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get beta testers.",
      response = UserInfoRestResource.class,
      nickname = "getBetaTesters",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The beta testers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/{definitionId}/beta-testers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getBetaTesters(
      @ApiParam(
              value =
                  "ID of the training definition which contains beta testers you want to retrieve",
              required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getBetaTesters(trainingDefinitionId, pageable);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Returns a page of the users authoring the given training definition, serialized to JSON
   * narrowed to the requested attributes.
   *
   * @param trainingDefinitionId id of the training definition whose authors are read
   * @param givenName restricts the result to authors whose given name matches, no restriction when
   *     absent
   * @param familyName restricts the result to authors whose family name matches, no restriction
   *     when absent
   * @param pageable pageable parameter with information about pagination
   * @param fields squiggly filter selecting the attributes to keep in the response, the whole page
   *     being returned when absent
   * @return the JSON body of the page of {@link UserRefDTO} as a string
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get authors.",
      response = UserInfoRestResource.class,
      nickname = "getAuthors",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The authors have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/{definitionId}/authors", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getAuthors(
      @ApiParam(
              value = "ID of the training definition which contains authors you want to retrieve.",
              required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getAuthors(trainingDefinitionId, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
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
  @ApiOperation(
      httpMethod = "PUT",
      value = "Edit authors.",
      response = UserInfoRestResource.class,
      nickname = "editAuthors",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The authors have been updated."),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @PutMapping(path = "/{definitionId}/authors", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> editAuthors(
      @ApiParam(value = "ID of training definition to be updated.", required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Ids of the users to be added to the training definition.")
          @RequestParam(value = "authorsAddition", required = false)
          Set<Long> authorsAddition,
      @ApiParam(value = "Ids of the users to be removed from the training definition.")
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
  @ApiOperation(
      httpMethod = "PUT",
      value = "Switch state of training definition",
      nickname = "switchDefinitionState")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training definition has been updated."),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit definition with created instances.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/states/{state}")
  public ResponseEntity<Void> switchState(
      @ApiParam(value = "Id of definition", required = true) @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(
              value = "New state of definition",
              allowableValues = "RELEASED, UNRELEASED, ARCHIVED",
              required = true)
          @PathVariable("state")
          TDState state) {
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
  @ApiOperation(
      httpMethod = "GET",
      value = "Get training definitions by ids.",
      response = TrainingDefinitionBasicDTO.class,
      nickname = "findTrainingDefinitionsByIds",
      notes = "Returns training definitions matching the given ids.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training definitions have been found.",
            response = TrainingDefinitionBasicDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TrainingDefinitionBasicDTO>> findTrainingDefinitionsByIds(
      @ApiParam(value = "Ids of training definitions", required = true)
          @RequestParam(value = "ids", required = true)
          List<Long> ids) {
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
  @ApiOperation(
      httpMethod = "GET",
      value = "Get levels by ids.",
      response = AbstractLevelBasicDTO.class,
      nickname = "findLevelsByIds",
      notes = "Returns levels matching the given ids.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The levels have been found.",
            response = AbstractLevelBasicDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/levels/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AbstractLevelBasicDTO>> findLevelsByIds(
      @ApiParam(value = "Ids of levels", required = true)
          @RequestParam(value = "ids", required = true)
          List<Long> ids) {
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
  @ApiOperation(
      httpMethod = "GET",
      value = "Get hints by ids.",
      response = HintBasicDTO.class,
      nickname = "findHintsByIds",
      notes = "Returns hints matching the given ids.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The hints have been found.",
            response = HintBasicDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/hints/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<HintBasicDTO>> findHintsByIds(
      @ApiParam(value = "Ids of hints", required = true)
          @RequestParam(value = "ids", required = true)
          List<Long> ids) {
    List<HintBasicDTO> hints = trainingDefinitionFacade.findHintsByIds(ids);
    return ResponseEntity.ok(hints);
  }

  @ApiModel(
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  private static class TrainingDefinitionRestResource
      extends PageResultResource<TrainingDefinitionWithLevelsDTO> {

    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Training Definitions from databases.")
    private List<TrainingDefinitionWithLevelsDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }

  /** The type User info rest resource. */
  @ApiModel(
      value = "UserInfoRestResource",
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  public static class UserInfoRestResource extends PageResultResource<UserInfoDTO> {
    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Training Instances from databases.")
    private List<UserRefDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }
}
