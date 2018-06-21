package cz.muni.ics.kypo.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.InfoLevelFacade;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * @author Pavel Šeda
 *
 */
//@formatter:off
@Api(value = "/info-levels", 
  consumes = "application/json", 
  authorizations = {
    @Authorization(value = "sampleoauth", 
      scopes = {
        @AuthorizationScope(
          scope = "HTTP operations on Info Level Resource", 
          description = "allows operations on Info Level Resource."
        )
      }
    )
  }
)
//@formatter:on
@RestController
@RequestMapping(value = "/info-levels")
public class InfoLevelsRestController {

  private InfoLevelFacade infoLevelFacade;
  private ObjectMapper objectMapper;

  @Autowired
  public InfoLevelsRestController(InfoLevelFacade infoLevelFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
    this.infoLevelFacade = infoLevelFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Get requested Info Level by id.
   * 
   * @param id of Info Level to return.
   * @return Requested Info by id.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET", 
      value = "Get Info level by Id.", 
      response = InfoLevelDTO.class,
      nickname = "findInfoLevelById",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find Info level by ID", 
                      description = "allows returning Info level by ID."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findInfoLevelById(@ApiParam(name = "InfoLevel ID") @PathVariable long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    try {
      InfoLevelDTO infoLevelResource = infoLevelFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoLevelResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on

  /**
   * Get all Info Levels.
   * 
   * @return all Info levels.
   */
  //@formatter:off
  @ApiOperation(httpMethod = "GET",
      value = "Get all info levels.",
      response = InfoLevelDTO.class,
      responseContainer = "Page",
      nickname = "findAllInfoLevels",
      produces = "application/json",
      authorizations = {
          @Authorization(value = "sampleoauth", 
              scopes = {
                  @AuthorizationScope(
                      scope = "find all Info levels", 
                      description = "allows returning Info levels."
                  )
              }
          )
      }
  )
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "The requested resource was not found.") 
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllInfoLevels(@QuerydslPredicate(root = InfoLevel.class) Predicate predicate, Pageable pageable,
      @RequestParam MultiValueMap<String, String> parameters, 
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) 
      @RequestParam(value = "fields", required = false) String fields) {
    try {
      PageResultResource<InfoLevelDTO> infoLevelResource = infoLevelFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoLevelResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
  //@formatter:on
}
