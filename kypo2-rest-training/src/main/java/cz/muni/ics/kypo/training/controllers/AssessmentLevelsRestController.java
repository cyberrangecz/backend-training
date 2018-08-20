package cz.muni.ics.kypo.training.controllers;


import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.training.exceptions.ResourceNotModifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.facade.AssessmentLevelFacade;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * @author Pavel Šeda
 */
//@formatter:off
@Api(value = "/assessment-levels",
        consumes = "application/json",
        authorizations = {
                @Authorization(value = "sampleoauth",
                        scopes = {
                                @AuthorizationScope(
                                        scope = "HTTP operations on Assessment Level Resource",
                                        description = "allows operations on Assessment Level Resource."
                                )
                        }
                )
        }
)
//@formatter:on
@RestController
@RequestMapping(value = "/assessment-levels")
public class AssessmentLevelsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(AssessmentLevelsRestController.class);

    private AssessmentLevelFacade assessmentLevelFacade;

    private ObjectMapper objectMapper;

    private BeanMapping dtoMapper;

    private Logger log = LoggerFactory.getLogger(AssessmentLevelsRestController.class);

    @Autowired
    public AssessmentLevelsRestController(AssessmentLevelFacade assessmentLevelFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper, BeanMapping dtoMapper) {
        this.assessmentLevelFacade = assessmentLevelFacade;
        this.objectMapper = objectMapper;
        this.dtoMapper = dtoMapper;
    }

    /**
     * Get requested Assessment Level by id.
     *
     * @param id of Assessment Level to return.
     * @return Requested Assessment Level by id.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get Assessment Level by Id.",
            response = AssessmentLevelDTO.class,
            nickname = "findAssessmentLevelById",
            produces = "application/json",
            authorizations = {
                    @Authorization(value = "sampleoauth",
                            scopes = {
                                    @AuthorizationScope(
                                            scope = "find Assessment Level by ID",
                                            description = "allows returning Assessment Level by ID."
                                    )
                            }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAssessmentLevelById(@ApiParam(name = "AssessmentLevel ID") @PathVariable long id,
                                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                          @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("findAssessmentLevelById({},{})", id, fields);
        try {
            AssessmentLevelDTO assessmentLevelResource = assessmentLevelFacade.findById(id);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, assessmentLevelResource), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }
    //@formatter:on

    /**
     * Get all Assessment Levels.
     *
     * @return all Assessment levels.
     */
    //@formatter:off
    @ApiOperation(httpMethod = "GET",
            value = "Get all Assessment Levels.",
            response = AssessmentLevelDTO.class,
            responseContainer = "Page",
            nickname = "findAllAssessmentLevels",
            produces = "application/json",
            authorizations = {
                    @Authorization(value = "sampleoauth",
                            scopes = {
                                    @AuthorizationScope(
                                            scope = "find all Assessment Levels",
                                            description = "allows returning Assessment Levels."
                                    )
                            }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllAssessmentLevels(@QuerydslPredicate(root = AssessmentLevel.class) Predicate predicate, Pageable pageable,
                                                          @RequestParam MultiValueMap<String, String> parameters,
                                                          @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                          @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("findAllAssessmentLevels({})", fields);
        try {
            PageResultResource<AssessmentLevelDTO> assessmentLevelResource = assessmentLevelFacade.findAll(predicate, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, assessmentLevelResource), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }
    //@formatter:on


    //@formatter:off


    @ApiOperation(httpMethod = "POST",
            value = "Create Assessment Level.",
            response = AssessmentLevelDTO.class,
            nickname = "createAssessmentLevel",
            produces = "application/json",
            consumes = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAssessmentLevel(@ApiParam(name = "Assessment level to be created") @RequestBody AssessmentLevelDTO assessmentLevel,
                                                        @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                        @RequestParam(value = "fields", required = false) String fields) {
        try {
            AssessmentLevel al = dtoMapper.mapTo(assessmentLevel, AssessmentLevel.class);
            AssessmentLevelDTO assessmentLevelResource = assessmentLevelFacade.create(al);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, assessmentLevelResource), HttpStatus.OK);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotCreatedException(ex.getLocalizedMessage());
        }
    }

    //@formatter:on


    //@formatter:off
    @ApiOperation(httpMethod = "PUT",
            value = "Update Assessment Level.",
            response = AssessmentLevelDTO.class,
            nickname = "updateAssessmentLevel",
            consumes = "application/json",
            authorizations = {
                    @Authorization(value = "sampleoauth",
                            scopes = {
                                    @AuthorizationScope(
                                            scope = "update Assessment Level",
                                            description = "allows returning created Assessment Level."
                                    )
                            }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The requested resource was not found.")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAssessmentLevel(@ApiParam(name = "Assessment level to be updated") @RequestBody AssessmentLevelDTO assessmentLevel) {
        try {
            AssessmentLevel al = dtoMapper.mapTo(assessmentLevel, AssessmentLevel.class);
            assessmentLevelFacade.update(al);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (FacadeLayerException ex) {
            throw new ResourceNotModifiedException(ex.getLocalizedMessage());
        }
    }

    //@formatter:on



}
