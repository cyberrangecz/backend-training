package cz.muni.ics.kypo.training.api.dto.traininginstance;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeDeserializer;
import io.swagger.annotations.ApiModel;

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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
    private TrainingDefinitionDTO trainingDefinition;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Override public String toString() {
        return "TrainingInstanceCreateDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\'' + ", poolSize="
            + poolSize + ", password='" + password + '\'' + ", trainingDefinition=" + trainingDefinition + ", organizers=" + organizers
            + '}';
    }
}
