package cz.muni.ics.kypo.training.api.dto.traininglevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelDTO}
 *
 */
@ApiModel(value = "TrainingLevelDTO", description = "An assignment containing security tasks whose completion yields a answer.", parent = AbstractLevelDTO.class)
public class TrainingLevelDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String answer;
    @ApiModelProperty(value = "Identifier that is used to obtain answer from remote storage.", example = "username")
    private String answerVariableName;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "How many times player can submit incorrect answer before displaying solution.", example = "5")
    private int incorrectAnswerLimit;

    /**
     * Gets answer.
     *
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets answer.
     *
     * @param answer the answer
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Gets answer identifier.
     *
     * @return the answer identifier
     */
    public String getAnswerVariableName() {
        return answerVariableName;
    }

    /**
     * Sets answer identifier.
     *
     * @param answerVariableName the answer identifier
     */
    public void setAnswerVariableName(String answerVariableName) {
        this.answerVariableName = answerVariableName;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets solution.
     *
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets solution.
     *
     * @param solution the solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * Is solution penalized boolean.
     *
     * @return true if incorrect solution is penalized
     */
    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    /**
     * Sets solution penalized.
     *
     * @param solutionPenalized the solution penalized
     */
    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
    }

    /**
     * Gets hints.
     *
     * @return the set of {@link HintDTO}s
     */
    public Set<HintDTO> getHints() {
        return hints;
    }

    /**
     * Sets hints.
     *
     * @param hints the set of {@link HintDTO}s
     */
    public void setHints(Set<HintDTO> hints) {
        this.hints = hints;
    }

    /**
     * Gets incorrect answer limit.
     *
     * @return the incorrect answer limit
     */
    public int getIncorrectAnswerLimit() {
        return incorrectAnswerLimit;
    }

    /**
     * Sets incorrect answer limit.
     *
     * @param incorrectAnswerLimit the incorrect answer limit
     */
    public void setIncorrectAnswerLimit(int incorrectAnswerLimit) {
        this.incorrectAnswerLimit = incorrectAnswerLimit;
    }


    @Override
    public String toString() {
        return "TrainingLevelDTO{" +
                "answer='" + answer + '\'' +
                ", answerVariableName='" + answerVariableName + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", hints=" + hints +
                ", incorrectAnswerLimit=" + incorrectAnswerLimit +
                '}';
    }
}
