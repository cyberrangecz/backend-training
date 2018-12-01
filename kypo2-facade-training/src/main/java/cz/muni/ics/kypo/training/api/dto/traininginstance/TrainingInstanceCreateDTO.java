package cz.muni.ics.kypo.training.api.dto.traininginstance;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeDeserializer;
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
    @NotEmpty(message = "{traininginstancecreate.password.NotEmpty.message}")
    private String password;
    @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
    private long trainingDefinitionId;
    @NotNull(message = "{traininginstancecreate.orgIds.NotNull.message}")
    private Set<Long> orgIds;

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

    @ApiModelProperty(value = "Keyword which will be modified and then used for accessing training run.", required = true, example = "hunter")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true, example = "1")
    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    @ApiModelProperty(value = "Reference to users which organize training instance.", required = true, example = "[1]")
    public Set<Long> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(Set<Long> orgIds) {
        this.orgIds = orgIds;
    }

    @Override
    public String toString() {
        return "TrainingInstanceCreateDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\'' + ", poolSize="
                + poolSize + ", password='" + password + '\'' + ", trainingDefinitionId=" + trainingDefinitionId + ", orgIds=" + orgIds + '}';
    }
}
