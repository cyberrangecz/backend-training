package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates basic information about Training Instance
 *
 */
public class TrainingInstanceFindAllResponseDTO {

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
    @ApiModelProperty(value = "Number of sandboxes that can be allocated.", example = "5")
    private int poolSize;
    @ApiModelProperty(value = "Reference to training definition from which is training instance created.")
    private TrainingDefinitionByIdDTO trainingDefinition;
    @ApiModelProperty(value = "Token used to access training run.", required = true, example = "hunter")
    private String accessToken;
    @ApiModelProperty(value = "Id of sandbox pool belonging to training instance", example = "1")
    private Long poolId;

    /**
     * Instantiates a new Training instance find all response dto.
     */
    public TrainingInstanceFindAllResponseDTO(){}

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
     * Gets pool size.
     *
     * @return the pool size
     */
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

    @Override
    public String toString() {
        return "TrainingInstanceFindAllResponseDTO{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", poolSize=" + poolSize +
                ", accessToken='" + accessToken + '\'' +
                ", poolId=" + poolId +
                '}';
    }
}
