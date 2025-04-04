package cz.cyberrange.platform.training.rest.controllers;


import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.TrainingInstanceLobbyDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.UserTeamDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
import cz.cyberrange.platform.training.api.dto.visualization.VisualizationInfoDTO;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.LobbyManagementFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.util.Set;

@Api(value = "/instance-lobby",
        tags = "Training Instance Lobby",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/teams-management", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingInstanceLobbyRestController {

    LobbyManagementFacade teamsManagementFacade;

    @Autowired
    public TrainingInstanceLobbyRestController(LobbyManagementFacade teamsManagementFacade) {
        this.teamsManagementFacade = teamsManagementFacade;
    }


    @ApiOperation(httpMethod = "GET",
            value = "Get training instance lobby",
            notes = "This can only be done by organizer of training instance or administrator",
            response = TrainingInstanceLobbyDTO.class,
            nickname = "getInstanceLobby",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Lobby found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Instance lobby not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingInstanceLobbyDTO> getInstanceLobby(
            @ApiParam(value = "Training instance ID", required = true)
            @PathVariable("instanceId")
            Long instanceId) {
        return ResponseEntity.ok(
                teamsManagementFacade.getTrainingInstanceLobby(instanceId)
        );
    }


    @ApiOperation(httpMethod = "POST",
            value = "Create new team",
            notes = "This can only be done by organizer of training instance or administrator",
            response = TeamDTO.class,
            nickname = "createTeam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Team created.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Instance not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{instanceId}/team", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> createTeam(
            @ApiParam(value = "Training instance ID", required = true)
            @PathVariable("instanceId")
            Long instanceId,
            @ApiParam(value = "New team name", required = true)
            @QueryParam("name")
            String name
    ) {
        return ResponseEntity.ok(teamsManagementFacade.createTeam(instanceId, name));
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Lock team",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "lockTam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Team locked.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Team with given id not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Team with given name already exists.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/team/{teamId}/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> renameTeam(
            @ApiParam(value = "Team ID", required = true)
            @PathVariable("teamId")
            Long teamId,
            @ApiParam(value = "New name", required = true)
            @QueryParam("name")
            String name
    ) {
        teamsManagementFacade.renameTeam(teamId, name);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Update Team name",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "renameTeam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Team renamed.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Team with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/team/{teamId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> lockTeam(
            @ApiParam(value = "Team ID", required = true)
            @PathVariable("teamId")
            Long teamId
    ) {
        teamsManagementFacade.lockTeam(teamId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Disband team",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "deleteTeam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Team disbanded.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Instance lobby not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> disbandTeam(
            @ApiParam(value = "New team name", required = true)
            @PathVariable("teamId")
            Long teamId
    ) {
        teamsManagementFacade.disbandTeam(teamId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Transfer players between teams",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "transferBetweenTeams",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Players transferred.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/team/between-teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> transferPlayersBetweenTeams(
            @ApiParam(value = "Team to transfer players from", required = true)
            @QueryParam("from")
            Long idFrom,
            @ApiParam(value = "Team to transfer players to", required = true)
            @QueryParam("to")
            Long idTo,
            @ApiParam(value = "Player ids transferred", required = true)
            @RequestBody
            Set<Long> playerIds
    ) {
        teamsManagementFacade.moveUserBetweenTeams(idFrom, idTo, playerIds);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(httpMethod = "PUT",
            value = "Transfer players from queue to team",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "transferToTeam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Players transferred.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "{instanceId}/teams/to-teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> transferPlayersToTeams(
            @ApiParam(value = "Training instance ID", required = true)
            @PathVariable("instanceId")
            Long instanceId,
            @ApiParam(value = "Player ids to transfer", required = true)
            @RequestBody
            Set<UserTeamDTO> newRelations
    ) {
        teamsManagementFacade.assignToTeams(instanceId, newRelations);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Transfer players from teams to queue",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "transferToTeam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Players transferred.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "{instanceId}/team/to-queue", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> transferPlayersToQueue(
            @ApiParam(value = "Training instance ID", required = true)
            @PathVariable("instanceId")
            Long instanceId,
            @ApiParam(value = "Player ids to transfer", required = true)
            @RequestBody
            Set<UserTeamDTO> removedRelations
    ) {
        teamsManagementFacade.unassignFromTeams(instanceId, removedRelations);
        return ResponseEntity.ok().build();
    }


}
