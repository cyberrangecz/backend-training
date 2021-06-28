package cz.muni.ics.kypo.training.persistence.model.question;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionAnswerId implements Serializable {

    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "training_run_id")
    private Long trainingRunId;

    public QuestionAnswerId() {
    }

    public QuestionAnswerId(Long questionId, Long trainingRunId) {
        this.questionId = questionId;
        this.trainingRunId = trainingRunId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionAnswerId)) return false;
        QuestionAnswerId that = (QuestionAnswerId) o;
        return Objects.equals(getQuestionId(), that.getQuestionId()) &&
                Objects.equals(getTrainingRunId(), that.getTrainingRunId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestionId(), getTrainingRunId());
    }


    @Override
    public String toString() {
        return "QuestionAnswerId{" +
                "questionId=" + this.getQuestionId() +
                ", trainingRunId=" + this.getTrainingRunId() +
                '}';
    }
}