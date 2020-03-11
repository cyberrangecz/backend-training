package cz.muni.ics.kypo.training.api.dto.gamelevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintForGameLevelViewDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;


/**
 * Encapsulates basic information about game level.
 */

@ApiModel(value = "GameLevelViewDTO", description = "An assignment containing security tasks whose completion yields a flag.", parent = AbstractLevelDTO.class)
public class GameLevelViewDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "25")
    private int estimatedDuration;
    @ApiModelProperty(value = "How many times player can submit incorrect flag before displaying solution.", example = "5")
    private int incorrectFlagLimit;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintForGameLevelViewDTO> hints = new HashSet<>();


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

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets incorrect flag limit.
     *
     * @return the incorrect flag limit
     */
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

    /**
     * Gets hints.
     *
     * @return the set of {@link HintForGameLevelViewDTO}s
     */
    public Set<HintForGameLevelViewDTO> getHints() {
        return hints;
    }

    /**
     * Sets hints.
     *
     * @param hints the set of {@link HintForGameLevelViewDTO}s
     */
    public void setHints(Set<HintForGameLevelViewDTO> hints) {
        this.hints = hints;
    }

    @Override
    public String toString() {
        return "GameLevelViewDTO{" +
                "content='" + content + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", estimatedDuration=" + estimatedDuration +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                ", hints=" + hints +
                '}';
    }
}
