package cz.muni.ics.kypo.training.api.dto.traininginstance;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "TrainingInstanceCreateDTO", description = "Training Instance to create.")
public class TrainingInstanceCreateDTO {

    @NotNull(message = "{traininginstancecreate.startTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime startTime;
    @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime endTime;
    @NotEmpty(message = "{traininginstancecreate.title.NotEmpty.message}")
    private String title;
    @NotNull(message = "{traininginstancecreate.poolSize.NotNull.message}")
    @Min(value = 1, message = "{traininginstancecreate.poolSize.Min.message}")
    @Max(value = 100, message = "{traininginstancecreate.poolSize.Max.message}")
    private int poolSize;
    @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
    private String accessToken;
    @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
    private long trainingDefinitionId;
    @NotNull(message = "{traininginstancecreate.organizersLogin.NotNull.message}")
    private Set<String> organizersLogin;

    @ApiModelProperty(value = "Date when training instance starts.", required = true, example = "2020-11-20T10:28:02.727Z")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ApiModelProperty(value = "Date when training instance ends.", required = true, example = "2020-11-25T10:26:02.727Z")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @ApiModelProperty(value = "Short textual description of the training instance.", required = true, example = "December instance")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Number of sandboxes that can be allocated.", required = true, example = "20")
    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @ApiModelProperty(value = "AccessToken which will be modified and then used for accessing training run.", required = true, example = "hunter")
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true, example = "1")
    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    @ApiModelProperty(value = "Reference to users which organize training instance.", required = true)
    public Set<String> getOrganizersLogin() {
        return organizersLogin;
    }

    public void setOrganizersLogin(Set<String> organizersLogin) {
        this.organizersLogin = organizersLogin;
    }

    @Override
    public String toString() {
        return "TrainingInstanceCreateDTO{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", poolSize=" + poolSize +
                ", accessToken='" + accessToken + '\'' +
                ", trainingDefinitionId=" + trainingDefinitionId +
                '}';
    }
}
