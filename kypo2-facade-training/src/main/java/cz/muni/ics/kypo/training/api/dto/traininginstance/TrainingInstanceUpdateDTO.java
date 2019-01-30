package cz.muni.ics.kypo.training.api.dto.traininginstance;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeUTCDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "TrainingInstanceUpdateDTO", description = "Training Instance to update.")
public class TrainingInstanceUpdateDTO {

    @NotNull(message = "{traininginstanceupdate.id.NotNull.message}")
    private Long id;
    @NotNull(message = "{traininginstanceupdate.startTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime startTime;
    @NotNull(message = "{traininginstanceupdate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime endTime;
    @NotEmpty(message = "{traininginstanceupdate.title.NotEmpty.message}")
    private String title;
    @NotNull(message = "{traininginstanceupdate.poolSize.NotNull.message}")
    @Min(value = 1, message = "{traininginstanceupdate.poolSize.Min.message}")
    @Max(value = 100, message = "{traininginstanceupdate.poolSize.Max.message}")
    private int poolSize;
    @NotEmpty(message = "{traininginstanceupdate.accessToken.NotEmpty.message}")
    private String accessToken;
    @NotNull(message = "{traininginstanceupdate.trainingDefinition.NotNull.message}")
    private Long trainingDefinitionId;
    @NotNull(message = "{traininginstanceupdate.organizerLogins.NotNull.message}")
    private Set<String> organizerLogins;

    @ApiModelProperty(value = "Main identifier of training instance.", required = true, example = "2")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Date when training instance starts.", required = true, example = "2019-10-19T10:28:02.727Z")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ApiModelProperty(value = "Date when training instance ends.", required = true, example = "2019-10-25T10:28:02.727Z")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @ApiModelProperty(value = "Short textual description of the training instance.", required = true, example = "Current Instance")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Number of sandboxes that can be allocated.", required = true, example = "8")
    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @ApiModelProperty(value = "AccessToken which will be modified and then used for accessing training run.", required = true, example = "hello-6578")
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true, example = "1")
    public Long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(Long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    @ApiModelProperty(value = "Reference to users which organize training instance.", required = true, example = "[1]")
    public Set<String> getOrganizerLogins() {
        return organizerLogins;
    }

    public void setOrganizerLogins(Set<String> organizerLogins) {
        this.organizerLogins = organizerLogins;
    }

    @Override
    public String toString() {
        return "TrainingInstanceUpdateDTO{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", poolSize=" + poolSize +
                ", accessToken='" + accessToken + '\'' +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", organizerLogins=" + organizerLogins +
                '}';
    }
}
