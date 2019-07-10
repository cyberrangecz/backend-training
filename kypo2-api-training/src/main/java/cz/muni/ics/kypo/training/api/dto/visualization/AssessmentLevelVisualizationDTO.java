package cz.muni.ics.kypo.training.api.dto.visualization;/**
     * Gets assessment type.
     *
     * @return the {@link AssessmentType}
     */

import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class AssessmentLevelVisualizationDTO extends AbstractLevelVisualizationDTO{

    private AssessmentType assessmentType;
    private String questions;

    public AssessmentLevelVisualizationDTO() {
    }

    /**
     * Gets assessment type.
     *
     * @return the {@link AssessmentType}
     */
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }
    /**
     * Sets assessment type.
     *
     * @param assessmentType the {@link AssessmentType}
     */
    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }
    /**
     * Gets questions.
     *
     * @return the questions
     */
    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    public String getQuestions() {
        return questions;
    }
    /**
     * Sets questions.
     *
     * @param questions the questions
     */
    public void setQuestions(String questions) {
        this.questions = questions;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractLevelVisualizationDTO)) return false;
        if (!super.equals(object)) return false;
        AssessmentLevelVisualizationDTO that = (AssessmentLevelVisualizationDTO) object;
        return getAssessmentType() == that.getAssessmentType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getAssessmentType());
    }

    @Override
    public String toString() {
        return "AssessmentLevelVisualizationDTO{" +
                "id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", levelType=" + this.getLevelType() +
                ", order=" + this.getOrder() +
                ", maxScore=" + this.getMaxScore() +
                ", assessmentType=" + assessmentType +
                ", questions='" + questions + '\'' +
                '}';
    }
}
