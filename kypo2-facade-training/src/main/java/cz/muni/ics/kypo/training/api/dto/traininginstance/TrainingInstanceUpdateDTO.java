package cz.muni.ics.kypo.training.api.dto.traininginstance;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeDeserializer;
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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @NotNull(message = "{traininginstanceupdate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    @NotEmpty(message = "{traininginstanceupdate.title.NotEmpty.message}")
    private String title;
    @NotNull(message = "{traininginstanceupdate.poolSize.NotNull.message}")
    @Min(value = 1, message = "{traininginstanceupdate.poolSize.Min.message}")
    @Max(value = 100, message = "{traininginstanceupdate.poolSize.Max.message}")
    private int poolSize;
    private String password;
    @NotNull(message = "{traininginstanceupdate.trainingDefinition.NotNull.message}")
    private TrainingDefinitionDTO trainingDefinition;
    @NotNull(message = "{traininginstanceupdate.organizers.NotNull.message}")
    private Set<UserRefDTO> organizers;

    @ApiModelProperty(value = "Main identifier of training instance.", required = true, example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Date when training instance starts.", required = true, example = "2016-10-19 10:23:54+02")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ApiModelProperty(value = "Date when training instance ends.", required = true, example = "2017-10-19 10:23:54+02")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @ApiModelProperty(value = "Short textual description of the training instance.", required = true, example = "Concluded Instance")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Number of sandboxes that can be allocated.", required = true, example = "10")
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

    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true)
    public TrainingDefinitionDTO getTrainingDefinition() {
        return trainingDefinition;
    }

    public void setTrainingDefinition(TrainingDefinitionDTO trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    @ApiModelProperty(value = "Reference to users which organize training instance.", required = true)
    public Set<UserRefDTO> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserRefDTO> organizers) {
        this.organizers = organizers;
    }

    @Override public String toString() {
        return "TrainingInstanceUpdateDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\''
            + ", poolSize=" + poolSize + ", password='" + password + '\'' + ", trainingDefinition=" + trainingDefinition + ", organizers="
            + organizers + '}';
    }
}
