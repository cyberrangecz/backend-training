package cz.muni.ics.kypo.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.facade.InfoFacade;
import cz.muni.ics.kypo.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.transfer.InfoDTO;
import cz.muni.ics.kypo.transfer.resource.LevelDTOResource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Pavel Šeda
 *
 */
@RestController
@RequestMapping(value = "/info")
public class InfoRestController {

  private InfoFacade infoFacade;
  private ObjectMapper objectMapper;

  @Autowired
  public InfoRestController(InfoFacade infoFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
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
  public ResponseEntity<Object> findInfoById(@ApiParam(name = "info ID") @PathVariable Long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false) @RequestParam(value = "fields",
          required = false) String fields) {
    try {
      LevelDTOResource<InfoDTO> infoResource = infoFacade.findById(id);
      Squiggly.init(objectMapper, fields);
      return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, infoResource), HttpStatus.OK);
    } catch (FacadeLayerException ex) {
      throw new ResourceNotFoundException(ex.getLocalizedMessage());
    }
  }
}
