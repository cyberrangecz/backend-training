package cz.muni.ics.kypo.training.api.dto;

import cz.muni.ics.kypo.training.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.UserRef;
import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "TrainingInstanceDTO", description = ".")
public class TrainingInstanceDTO {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String title;
    private int poolSize;
    private String keyword;
    private TrainingDefinition trainingDefinition;
    private Set<UserRef> organizers = new HashSet<>();
    private Set<SandboxInstanceRef> sandboxInstanceRef = new HashSet<>();

    public TrainingInstanceDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public TrainingDefinition getTrainingDefinition() {
        return trainingDefinition;
    }

    public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    public Set<UserRef> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserRef> organizers) {
        this.organizers = organizers;
    }

    public Set<SandboxInstanceRef> getSandboxInstanceRef() {
        return sandboxInstanceRef;
    }

    public void setSandboxInstanceRef(Set<SandboxInstanceRef> sandboxInstanceRef) {
        this.sandboxInstanceRef = sandboxInstanceRef;
    }

    @Override
    public String toString() {
        return "TrainingInstanceDTO{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", poolSize=" + poolSize +
                ", keyword='" + keyword + '\'' +
                ", trainingDefinition=" + trainingDefinition +
                ", organizers=" + organizers +
                ", sandboxInstanceRef=" + sandboxInstanceRef +
                '}';
    }
}
