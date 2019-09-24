package cz.muni.ics.kypo.training.api.dto.gamelevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;

/**
 * Encapsulates information about game level. Inherits from {@link AbstractLevelDTO}
 *
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "GameLevelDTO", description = "An assignment containing security tasks whose completion yields a flag.", parent = AbstractLevelDTO.class)
public class GameLevelDTO extends AbstractLevelDTO {

    private String flag;
    private String content;
    private String solution;
    private boolean solutionPenalized;
    private Set<HintDTO> hints = new HashSet<>();
    private int incorrectFlagLimit;

    /**
     * Gets flag.
     *
     * @return the flag
     */
    @ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretFlag")
    public String getFlag() {
        return flag;
    }

    /**
     * Sets flag.
     *
     * @param flag the flag
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
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
    @ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
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
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
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
    @ApiModelProperty(value = "Information which helps player resolve the level.")
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
     * Gets incorrect flag limit.
     *
     * @return the incorrect flag limit
     */
    @ApiModelProperty(value = "How many times player can submit incorrect flag before displaying solution.", example = "5")
    public int getIncorrectFlagLimit() {
        return incorrectFlagLimit;
    }

    /**
     * Sets incorrect flag limit.
     *
     * @param incorrectFlagLimit the incorrect flag limit
     */
    public void setIncorrectFlagLimit(int incorrectFlagLimit) {
        this.incorrectFlagLimit = incorrectFlagLimit;
    }

    @Override
    public String toString() {
        return "GameLevelDTO{" +
                "flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", hints=" + hints +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                '}';
    }
}
