package cz.muni.ics.kypo.training.api.dto.visualization;/**
     * Gets assessment type.
     *
     * @return the {@link AssessmentType}
     */

import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AbstractLevelImportDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@ApiModel(value = "AssessmentLevelVisualizationDTO", description = "Information about assessment level needed for visualizations.", parent = AbstractLevelImportDTO.class)
public class AssessmentLevelVisualizationDTO extends AbstractLevelVisualizationDTO{

    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;
    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private List<QuestionDTO> questions;

    /**
     * Instantiates a new Assessment level visualization dto.
     */
    public AssessmentLevelVisualizationDTO() {
    }

    /**
     * Gets assessment type.
     *
     * @return the {@link AssessmentType}
     */
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
    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    /**
     * Sets questions.
     *
     * @param questions the questions
     */
    public void setQuestions(List<QuestionDTO> questions) {
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
                '}';
    }
}
