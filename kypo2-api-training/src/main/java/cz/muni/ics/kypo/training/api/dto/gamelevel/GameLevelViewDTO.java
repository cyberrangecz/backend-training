package cz.muni.ics.kypo.training.api.dto.gamelevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintForGameLevelViewDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Encapsulates basic information about game level.
 */

@ApiModel(value = "GameLevelDTO", description = "An assignment containing security tasks whose completion yields a flag.", parent = AbstractLevelDTO.class)
public class GameLevelViewDTO extends AbstractLevelDTO {

    private String content;
    private boolean solutionPenalized;
    private int estimatedDuration;
    private String[] attachments;
    private int incorrectFlagLimit;
    private Set<HintForGameLevelViewDTO> hints = new HashSet<>();


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

    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "25")
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Get attachments.
     *
     * @return the attachments
     */
    @ApiModelProperty(value = "Downloadable files for level (pictures, source code...)", example = "")
    public String[] getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param attachments the attachments
     */
    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
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

    /**
     * Gets hints.
     *
     * @return the set of {@link HintForGameLevelViewDTO}s
     */
    @ApiModelProperty(value = "Information which helps player resolve the level.")
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
        return "GameLevelDTO{" + '\'' + ", content='" + content + '\''
                + ", solutionPenalized=" + solutionPenalized + ", estimatedDuration=" + estimatedDuration + ", attachments="
                + Arrays.toString(attachments)  + ", incorrectFlagLimit=" + incorrectFlagLimit + '}';
    }
}
