package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;

import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "TrainingRun")
@Table(name = "training_run")
public class TrainingRun implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "event_log_reference", nullable = true)
    private String eventLogReference;
    @Column(name = "state", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private TRState state;
    @Column(name = "incorrect_flag_count", nullable = false)
    private int incorrectFlagCount;
    @Column(name = "solution_taken", nullable = false)
    private boolean solutionTaken;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AbstractLevel currentLevel;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TrainingInstance trainingInstance;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sandbox_instance_ref_id")
    private SandboxInstanceRef sandboxInstanceRef;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant_ref_id", nullable = false)
    private ParticipantRef participantRef;
    @Lob
    @Type(type ="org.hibernate.type.TextType")
    @Column(name = "assessment_responses", nullable = true)
    private String assessmentResponses;
    @Column(name = "total_score")
    private int totalScore;
    @Column(name = "current_score")
    private int currentScore;
    @Column(name = "level_answered")
    private boolean levelAnswered;

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

    public String getEventLogReference() {
        return eventLogReference;
    }

    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    public TRState getState() {
        return state;
    }

    public void setState(TRState state) {
        this.state = state;
    }

    public AbstractLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(AbstractLevel currentLevel) {
        this.totalScore += currentLevel.getMaxScore();
        this.currentScore = currentLevel.getMaxScore();
        this.levelAnswered = currentLevel instanceof InfoLevel;
        this.solutionTaken = false;
        this.currentLevel = currentLevel;
    }

    public TrainingInstance getTrainingInstance() {
        return trainingInstance;
    }

    public void setTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    public SandboxInstanceRef getSandboxInstanceRef() {
        return sandboxInstanceRef;
    }

    public int getIncorrectFlagCount() {
        return incorrectFlagCount;
    }

    public void setIncorrectFlagCount(int incorrectFlagCount) {
        this.incorrectFlagCount = incorrectFlagCount;
    }

    public boolean isSolutionTaken() {
        return solutionTaken;
    }

    public void setSolutionTaken(boolean solutionTaken) {
        this.solutionTaken = solutionTaken;
    }

    public void setSandboxInstanceRef(SandboxInstanceRef sandboxInstanceRef) {
        this.sandboxInstanceRef = sandboxInstanceRef;
    }

    public String getAssessmentResponses() {
        return assessmentResponses;
    }

    public void setAssessmentResponses(String assessmentResponses) {
        this.assessmentResponses = assessmentResponses;
    }

    public ParticipantRef getParticipantRef() {
        return participantRef;
    }

    public void setParticipantRef(ParticipantRef participantRef) {
        this.participantRef = participantRef;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void decreaseTotalScore(int penalty) {
        this.totalScore -= penalty;

    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void decreaseCurrentScore(int penalty) {
        this.currentScore -= penalty;
    }

    public boolean isLevelAnswered() {
        return levelAnswered;
    }

    public void setLevelAnswered(boolean levelAnswered) {
        this.levelAnswered = levelAnswered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentLevel, eventLogReference, startTime, endTime, state, trainingInstance, incorrectFlagCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TrainingRun))
            return false;
        TrainingRun other = (TrainingRun) obj;
        // @formatter:off
        return Objects.equals(currentLevel, other.getCurrentLevel())
                && Objects.equals(eventLogReference, other.getEventLogReference())
                && Objects.equals(startTime, other.getStartTime())
                && Objects.equals(endTime, other.getEndTime())
                && Objects.equals(state, other.getState())
                && Objects.equals(incorrectFlagCount, other.getIncorrectFlagCount())
                && Objects.equals(trainingInstance, other.getTrainingInstance())
                && Objects.equals(participantRef, other.getParticipantRef())
                && Objects.equals(solutionTaken, other.isSolutionTaken());
        // @formatter:on
    }

    @Override
    public String toString() {
        return "TrainingRun [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference=" + eventLogReference
                + ", state=" + state + ", incorrectFlagCount=" + incorrectFlagCount + ", currentLevel=" + currentLevel + ", trainingInstance="
                + trainingInstance + ", solutionTaken=" + solutionTaken + ", participantRef=" + participantRef + ", getClass()=" + getClass() + ", toString()=" + super.toString() + "]";
    }

}
