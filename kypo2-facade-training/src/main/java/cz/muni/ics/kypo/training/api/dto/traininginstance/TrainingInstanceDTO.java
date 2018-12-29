package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "TrainingInstanceDTO", description = "A session of attending a concrete training, which involves a deployment of the training definition in one or more sandbox instances that are then assigned to participants. The instance comprises one or more game runs.")
public class TrainingInstanceDTO {

    private Long id;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    private String title;
    private int poolSize;
    private TrainingDefinitionDTO trainingDefinition;
    private Set<UserRefDTO> organizers;
    private String accessToken;

    @ApiModelProperty(value = "Main identifier of training instance.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Date when training instance starts.", example = "2016-10-19 10:23:54+02")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Number of sandboxes that can be allocated.", example = "5")
    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @ApiModelProperty(value = "Reference to training definition from which is training instance created.")
    public TrainingDefinitionDTO getTrainingDefinition() {
        return trainingDefinition;
    }

    public void setTrainingDefinition(TrainingDefinitionDTO trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    @ApiModelProperty(value = "Reference to organizers which organize training instance.")
    public Set<UserRefDTO> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserRefDTO> organizers) {
        this.organizers = organizers;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "TrainingInstanceDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\''
                + ", poolSize=" + poolSize + ", trainingDefinition=" + trainingDefinition + ", organizers=" + organizers + ", accessToken='"
                + accessToken + '\'' + '}';
    }
}
