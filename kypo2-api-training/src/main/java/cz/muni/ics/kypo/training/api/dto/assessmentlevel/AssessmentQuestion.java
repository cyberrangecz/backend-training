package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.api.enums.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class AssessmentQuestion {

    private boolean answerRequired;
    private int order;
    private int penalty;
    private int points;
    private String text;
    private QuestionType questionType = QuestionType.FFQ;
    private List<Integer> correctChoices = new ArrayList<>();

    public AssessmentQuestion() {
        this.text = "Example Question";

    }

    public boolean isAnswerRequired() {
        return answerRequired;
    }

    public void setAnswerRequired(boolean answerRequired) {
        this.answerRequired = answerRequired;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public List<Integer> getCorrectChoices() {
        return correctChoices;
    }

    public void setCorrectChoices(List<Integer> correctChoices) {
        this.correctChoices = correctChoices;
    }
}
