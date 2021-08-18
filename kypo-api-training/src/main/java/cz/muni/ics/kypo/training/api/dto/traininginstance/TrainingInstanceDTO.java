package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about Training Instance
 *
 */
@ApiModel(value = "TrainingInstanceDTO", description = "A session of attending a concrete training, which involves a deployment of the training definition in one or more sandbox instances that are then assigned to participants. The instance comprises one or more training runs.")
public class TrainingInstanceDTO {

    @ApiModelProperty(value = "Main identifier of training instance.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Date when training instance starts.", example = "2016-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    @ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
    private String title;
    @ApiModelProperty(value = "Reference to training definition from which is training instance created.")
    private TrainingDefinitionByIdDTO trainingDefinition;
    @ApiModelProperty(value = "Token used to access training run.", required = true, example = "hunter")
    private String accessToken;
    @ApiModelProperty(value = "Id of sandbox pool belonging to training instance", example = "1")
    private Long poolId;
    @ApiModelProperty(value = "Ids of sandboxes which are assigned to training run.", example = "[3,15]")
    private List<Long> sandboxesWithTrainingRun = new ArrayList<>();
    @ApiModelProperty(value = "Time of last edit done to instance.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;
    @ApiModelProperty(value = "Name of the user who has done the last edit in instance.", example = "John Doe")
    private String lastEditedBy;


    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * Gets training definition.
     *
     * @return the training definition
     */
    public TrainingDefinitionByIdDTO getTrainingDefinition() {
        return trainingDefinition;
    }

    /**
     * Sets training definition.
     *
     * @param trainingDefinition the training definition
     */
    public void setTrainingDefinition(TrainingDefinitionByIdDTO trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
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

    /**
     * Gets sandboxes with training run.
     *
     * @return the sandboxes with training run
     */
    public List<Long> getSandboxesWithTrainingRun() {
        return sandboxesWithTrainingRun;
    }

    /**
     * Sets sandboxes with training run.
     *
     * @param sandboxesWithTrainingRun the sandboxes with training run
     */
    public void setSandboxesWithTrainingRun(List<Long> sandboxesWithTrainingRun) {
        this.sandboxesWithTrainingRun = sandboxesWithTrainingRun;
    }

    /**
     * Gets time of last edit.
     *
     * @return the last edited
     */
    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    /**
     * Sets time of last edit.
     *
     * @param lastEdited the last edited
     */
    public void setLastEdited(LocalDateTime lastEdited) {
        this.lastEdited = lastEdited;
    }

    /**
     * Gets the name of the user who has done the last edit in Training Instance
     *
     * @return the name of the user
     */
    public String getLastEditedBy() {
        return lastEditedBy;
    }

    /**
     * Sets the name of the user who has done the last edit in Training Instance
     *
     * @param lastEditedBy the name of the user
     */
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }



    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TrainingInstanceDTO)) return false;
        TrainingInstanceDTO that = (TrainingInstanceDTO) object;
        return  Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getAccessToken(), that.getAccessToken()) &&
                Objects.equals(getPoolId(), that.getPoolId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getAccessToken(), getPoolId());
    }

    @Override
    public String toString() {
        return "TrainingInstanceDTO{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", poolId=" + poolId +
                ", sandboxesWithTrainingRun=" + sandboxesWithTrainingRun +
                ", lastEdited=" + lastEdited +
                ", lastEditedBy='" + lastEditedBy + '\'' +
                '}';
    }
}
