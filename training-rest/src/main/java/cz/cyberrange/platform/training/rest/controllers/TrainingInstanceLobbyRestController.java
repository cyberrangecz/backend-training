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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/instance-lobby", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TrainingInstanceLobbyRestController {

    LobbyManagementFacade teamsManagementFacade;
    private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceLobbyRestController.class);

    @Autowired
    public TrainingInstanceLobbyRestController(LobbyManagementFacade teamsManagementFacade) {
        this.teamsManagementFacade = teamsManagementFacade;
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get number of users waiting in lobby",
            notes = "This can only be done by trainee or organizer of an instance",
            response = Integer.class,
            nickname = "getLobbyWaitingCount",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Count returned.", response = Integer.class),
            @ApiResponse(code = 404, message = "Instance lobby not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{accessToken}/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getUsersWaiting(
            @ApiParam(value = "Training instance access token", required = true)
            @PathVariable("accessToken")
            String accessToken,
            @ApiParam(value = "Count only unassigned users", defaultValue = "false")
            @RequestParam(value = "unassignedOnly", required = false, defaultValue = "false")
            Boolean unassignedOnly
    ) {
        return ResponseEntity.ok(
                teamsManagementFacade.getTrainingInstanceLobbyWaitingCount(accessToken, unassignedOnly)
        );
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get datetime of run start",
            notes = "This can only be done by trainee or organizer of an instance",
            response = String.class,
            nickname = "getStartDate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Time returned.", response = String.class),
            @ApiResponse(code = 404, message = "Instance not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{accessToken}/start-date", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getStartDate(
            @ApiParam(value = "Training instance access token", required = true)
            @PathVariable("accessToken")
            String accessToken
    ) {
        return ResponseEntity.ok(
                teamsManagementFacade.getTrainingInstanceStartDate(accessToken)
        );
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get team info",
            notes = "This can only be done by trainee or organizer of an instance",
            response = TeamDTO.class,
            nickname = "getTeamInfo",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Time returned.", response = Integer.class),
            @ApiResponse(code = 404, message = "Instance not found.", response = ApiError.class),
            @ApiResponse(code = 425, message = "Not yet assigned to team", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{accessToken}/team-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> getTeamInfo(
            @ApiParam(value = "Training instance access token", required = true)
            @PathVariable("accessToken")
            String accessToken
    ) {
        return ResponseEntity.ok(
                teamsManagementFacade.getTeamInfo(accessToken)
        );
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
            Long instanceId
    ) {
        LOG.error("REST - Getting Lobby Instance Lobby by ID: {}", instanceId);
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
    @PostMapping(path = "/{instanceId}/team", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> createTeam(
            @ApiParam(value = "Training instance ID", required = true)
            @PathVariable("instanceId")
            Long instanceId,
            @ApiParam(value = "New team name", required = true)
            @RequestParam("name")
            String name
    ) {
        return ResponseEntity.ok(teamsManagementFacade.createTeam(instanceId, name));
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Rename team",
            notes = "This can only be done by organizer of training instance or administrator",
            nickname = "renameTeam",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Team locked.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Team with given id not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Team with given name already exists.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PutMapping(path = "/team/{teamId}/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renameTeam(
            @ApiParam(value = "Team ID", required = true)
            @PathVariable("teamId")
            Long teamId,
            @ApiParam(value = "New name", required = true)
            @RequestParam("name")
            String name
    ) {
        return ResponseEntity.ok(teamsManagementFacade.renameTeam(teamId, name));
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
    @PutMapping(path = "/team/{teamId}/lock", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @DeleteMapping(path = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PutMapping(path = "/team/between-teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> transferPlayersBetweenTeams(
            @ApiParam(value = "Team to transfer players from", required = true)
            @RequestParam("from")
            Long idFrom,
            @ApiParam(value = "Team to transfer players to", required = true)
            @RequestParam("to")
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
    @PutMapping(path = "{instanceId}/team/to-teams", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PutMapping(path = "{instanceId}/team/to-queue", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> transferPlayersToQueue(
            @ApiParam(value = "Training instance ID", required = true)
            @PathVariable("instanceId")
            Long instanceId,
            @ApiParam(value = "Player ids to transfer", required = true)
            @RequestBody
            Set<UserTeamDTO> removedRelations
    ) {
        teamsManagementFacade.returnToQueue(instanceId, removedRelations);
        return ResponseEntity.ok().build();
    }


}
