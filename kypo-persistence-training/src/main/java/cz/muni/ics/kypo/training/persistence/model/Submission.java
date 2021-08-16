package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.model.enums.SubmissionType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
public class Submission extends AbstractEntity<Long> implements Serializable {

    @Column(name = "provided", nullable = false)
    private String provided;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SubmissionType type;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    @JoinColumn(name = "level_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AbstractLevel levelId;
    @JoinColumn(name = "training_run_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TrainingRun trainingRun;

    public String getProvided() {
        return provided;
    }

    public void setProvided(String provided) {
        this.provided = provided;
    }

    public SubmissionType getType() {
        return type;
    }

    public void setType(SubmissionType type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public AbstractLevel getLevelId() {
        return levelId;
    }

    public void setLevelId(AbstractLevel levelId) {
        this.levelId = levelId;
    }

    public TrainingRun getTrainingRun() {
        return trainingRun;
    }

    public void setTrainingRun(TrainingRun trainingRun) {
        this.trainingRun = trainingRun;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "provided='" + provided + '\'' +
                ", type=" + type +
                ", date=" + date +
                ", ipAddress='" + ipAddress + '\'' +
                "} " + super.toString();
    }
}
