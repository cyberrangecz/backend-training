package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Encapsulates information about Training Instance, intended for creation of new instance.
 *
 */
@ApiModel(value = "TrainingInstanceCreateDTO", description = "Training Instance to create.")
public class TrainingInstanceCreateDTO {

    @ApiModelProperty(value = "Date when training instance starts.", required = true, example = "2020-11-20T10:28:02.727Z")
    @NotNull(message = "{trainingInstance.startTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training instance ends.", required = true, example = "2020-11-25T10:26:02.727Z")
    @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime endTime;
    @ApiModelProperty(value = "Short textual description of the training instance.", required = true, example = "December instance")
    @NotEmpty(message = "{traininginstancecreate.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "AccessToken which will be modified and then used for accessing training run.", required = true, example = "hunter")
    @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
    private String accessToken;
    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true, example = "1")
    @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
    private long trainingDefinitionId;
    @ApiModelProperty(value = "Id of sandbox pool assigned to training instance", example = "1")
    private Long poolId;

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets end time.
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets access token.
     *
     * @param accessToken the access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets training definition id.
     *
     * @return the training definition id
     */
    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    /**
     * Sets training definition id.
     *
     * @param trainingDefinitionId the training definition id
     */
    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    /**
     * Gets pool id.
     *
     * @return the pool id
     */
    public Long getPoolId() {
        return poolId;
    }

    /**
     * Sets pool id.
     *
     * @param poolId the pool id
     */
    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    @Override
    public String toString() {
        return "TrainingInstanceCreateDTO{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", poolId=" + poolId +
                '}';
    }
}
