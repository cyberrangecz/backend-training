package cz.cyberrange.platform.training.api.dto.traininginstance.lobby;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "UserTeamDTO", description = "Link between team and user")
public class UserTeamDTO {

    @ApiModelProperty(value = "Team id")
    private Long teamId;
    
    @ApiModelProperty(value = "User id")
    private Long userId;

}
