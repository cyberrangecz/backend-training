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
@ApiModel(value = "TrainingInstanceCreateDTO", description = "Training Instance to create.")
public class TrainingInstanceCreateDTO {
    @ApiModelProperty(required = true)
    @NotNull(message = "{traininginstancecreate.startTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(required = true)
    @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    @ApiModelProperty(required = true)
    @NotEmpty(message = "{traininginstancecreate.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(required = true)
    @NotNull(message = "{traininginstancecreate.poolSize.NotNull.message}")
    @Min(value = 1, message = "{traininginstancecreate.poolSize.Min.message}")
    @Max(value = 100, message = "{traininginstancecreate.poolSize.Max.message}")
    private int poolSize;
    @ApiModelProperty(required = true)
    @NotEmpty(message = "{traininginstancecreate.keyword.NotEmpty.message}")
    private String keyword;
    @ApiModelProperty(required = true)
    @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
    private TrainingDefinitionDTO trainingDefinition;
    @ApiModelProperty(required = true)
    @NotNull(message = "{traininginstancecreate.organizers.NotNull.message}")
    private Set<UserRefDTO> organizers;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public TrainingDefinitionDTO getTrainingDefinition() {
        return trainingDefinition;
    }

    public void setTrainingDefinition(TrainingDefinitionDTO trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    public Set<UserRefDTO> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserRefDTO> organizers) {
        this.organizers = organizers;
    }

    @Override
    public String toString() {
        return "TrainingInstanceCreateDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\'' + ", poolSize="
                + poolSize + ", keyword='" + keyword + '\'' + ", trainingDefinition=" + trainingDefinition + ", organizers=" + organizers + '}';
    }
}
