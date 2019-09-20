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
import java.util.Set;

/**
 * Encapsulates information about Training Instance, intended for creation of new instance.
 *
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
    @Max(value = 64, message = "{traininginstancecreate.poolSize.Max.message}")
    private int poolSize;
    @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
    private String accessToken;
    @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
    private long trainingDefinitionId;
    @NotNull(message = "{traininginstancecreate.organizersRefIds.NotNull.message}")
    private Set<Long> organizersRefIds;

    /**
     * Gets start time.
     *
     * @return the start time
     */
    @ApiModelProperty(value = "Date when training instance starts.", required = true, example = "2020-11-20T10:28:02.727Z")
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
    @ApiModelProperty(value = "Date when training instance ends.", required = true, example = "2020-11-25T10:26:02.727Z")
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
    @ApiModelProperty(value = "Short textual description of the training instance.", required = true, example = "December instance")
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
     * Gets pool size.
     *
     * @return the pool size
     */
    @ApiModelProperty(value = "Number of sandboxes that can be allocated.", required = true, example = "20")
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Sets pool size.
     *
     * @param poolSize the pool size
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * Gets access token.
     *
     * @return the access token
     */
    @ApiModelProperty(value = "AccessToken which will be modified and then used for accessing training run.", required = true, example = "hunter")
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
    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true, example = "1")
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
     * Gets organizers ids.
     *
     * @return the organizers ids
     */
    @ApiModelProperty(value = "Reference to users which organize training instance.", required = true)
    public Set<Long> getOrganizersRefIds() {
        return organizersRefIds;
    }

    /**
     * Sets organizers user ref ids.
     *
     * @param organizersRefIds the organizers user ref ids
     */
    public void setOrganizersRefIds(Set<Long> organizersRefIds) {
        this.organizersRefIds = organizersRefIds;
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
