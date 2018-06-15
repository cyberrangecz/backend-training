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

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.InfoLevelFacade;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.transfer.InfoLevelDTO;
import cz.muni.ics.kypo.transfer.resource.InfoLevelsDTOResource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Pavel Šeda
 *
 */
@RestController
@RequestMapping(value = "/info-levels")
public class InfoLevelsRestController {

  private InfoLevelFacade infoFacade;
  private ObjectMapper objectMapper;

  @Autowired
  public InfoLevelsRestController(InfoLevelFacade infoFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
    this.infoFacade = infoFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Get requested Info by id.
   * 
   * @param id of info to return.
   * @return Requested Info by id.
   */
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(httpMethod = "GET", value = "Get info by Id.", produces = "application/json")
  public ResponseEntity<Object> findInfoLevelById(@ApiParam(name = "info ID") @PathVariable Long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) @RequestParam(value = "fields",
          required = false) String fields) {
    try {
      InfoLevelsDTOResource<InfoLevelDTO> infoResource = infoFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }

  /**
   * Get all info Levels.
   * 
   * @return all info levels.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(httpMethod = "GET", value = "Get all info levels.", produces = "application/json")
  public ResponseEntity<Object> findAllInfoLevels(@QuerydslPredicate(root = InfoLevel.class) Predicate predicate, Pageable pageable,
      @RequestParam MultiValueMap<String, String> parameters, @ApiParam(value = "Fields which should be returned in REST API response",
          required = false) @RequestParam(value = "fields", required = false) String fields) {
    try {
      InfoLevelsDTOResource<InfoLevelDTO> infoResource = infoFacade.findAll(predicate, pageable);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
}
