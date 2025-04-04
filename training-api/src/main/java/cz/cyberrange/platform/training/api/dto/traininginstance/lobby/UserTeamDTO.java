package cz.cyberrange.platform.training.api.dto.traininginstance.lobby;


import io.swagger.annotations.ApiModel;
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

    private Long teamId;
    private Long userId;

}
