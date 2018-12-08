package cz.muni.ics.kypo.training.rest.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IDMGroupRefDTO;
import cz.muni.ics.kypo.training.exception.CommonsFacadeException;
import cz.muni.ics.kypo.training.facade.IDMGroupRefFacade;
import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotModifiedException;
import cz.muni.ics.kypo.training.security.mapping.RoleDTO;
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

/**
 * @author Pavel Seda
 */
@Api(value = "/groups", consumes = "application/json", tags = "Groups")
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.")
})
@RestController
@RequestMapping(path = "/groups")
public class GroupsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(GroupsRestController.class);

    private IDMGroupRefFacade groupFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public GroupsRestController(IDMGroupRefFacade groupFacade, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.groupFacade = groupFacade;
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get all groups including their roles.",
            response = RoleDTO.class,
            responseContainer = "Page",
            nickname = "getAllGroups",
            produces = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All groups found.", response = IDMGroupRefDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllGroups(
            @QuerydslPredicate(root = IDMGroupRef.class) Predicate predicate,
            Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> parameters,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            PageResultResource<IDMGroupRefDTO> groupResource = groupFacade.getAllGroups(predicate, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, groupResource), HttpStatus.OK);
        } catch (CommonsFacadeException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Delete group reference.",
            nickname = "deleteGroupReference",
            produces = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group reference deleted."),
            @ApiResponse(code = 404, message = "Group reference cannot be found."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroupReference(
            @ApiParam(value = "Id of group whose reference to be deleted") @PathVariable("id") long id) {
        try {
            groupFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CommonsFacadeException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Remove role from group reference.",
            nickname = "removeRoleFromGroupRef",
            produces = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Role removed from group."),
            @ApiResponse(code = 404, message = "Group or role cannot be found ."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @DeleteMapping(value = "/{groupId}/roles/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> removeRoleFromGroupRef(@ApiParam(value = "Role Id", required = true) @PathVariable("roleId") long roleId,
                                                       @ApiParam(value = "IDMGroup Id", required = true) @PathVariable("groupId") long groupId,
                                                       @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                       @RequestParam(value = "fields", required = false) String fields) {
        try {
            groupFacade.removeRoleFromGroup(roleId, groupId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CommonsFacadeException ex) {
            throw new ResourceNotModifiedException(ex.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "POST",
            value = "Assign role to group reference.",
            nickname = "assignRoleToGroupRef",
            produces = "application/json"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Role assigned to group."),
            @ApiResponse(code = 404, message = "Group or role cannot be found ."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.")
    })
    @PostMapping(value = "/{groupId}/roles/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> assignRoleToGroupRef(@ApiParam(value = "Role Id") @PathVariable("roleId") long roleId,
                                                     @ApiParam(value = "IDMGroup Id") @PathVariable("groupId") long groupId) {
        try {
            groupFacade.assignRoleToGroup(roleId, groupId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CommonsFacadeException ex) {
            throw new ResourceNotModifiedException(ex.getLocalizedMessage());
        }
    }


}
