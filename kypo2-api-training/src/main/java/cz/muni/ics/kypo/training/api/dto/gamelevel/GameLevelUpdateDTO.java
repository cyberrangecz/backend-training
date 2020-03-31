package cz.muni.ics.kypo.training.api.dto.gamelevel;

import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.*;

/**
 * Encapsulates information needed to update game level.
 *
 */
@ApiModel(value = "GameLevelUpdateDTO", description = "Game level to update.")
public class GameLevelUpdateDTO {

    @ApiModelProperty(value = "Main identifier of level.", required = true, example = "1")
    @NotNull(message = "{gamelevelupdate.id.NotNull.message}")
    protected Long id;
    @ApiModelProperty(value = "Short textual description of the level.", required = true, example = "Game Level1")
    @NotEmpty(message = "{gamelevelupdate.title.NotEmpty.message}")
    protected String title;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", required = true, example = "20")
    @NotNull(message = "{gamelevelupdate.maxScore.NotNull.message}")
    @Min(value = 0, message = "{gamelevelupdate.maxScore.Min.message}")
    @Max(value = 100, message = "{gamelevelupdate.maxScore.Max.message}")
    private int maxScore;
    @ApiModelProperty(value = "Keyword found in game, used for access next level.", required = true, example = "secretFlag")
    @NotEmpty(message = "{gamelevelupdate.flag.NotEmpty.message}")
    @Size(max = 50, message = "{gamelevelupdate.flag.Size.message}")
    private String flag;
    @ApiModelProperty(value = "The information and experiences that are directed towards an player.", example = "Play me")
    @NotEmpty(message = "{gamelevelimport.solution.NotEmpty.message}")
    private String content;
    @ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
    @NotEmpty(message = "{gamelevelimport.solution.NotEmpty.message}")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", required = true, example = "false")
    @NotNull(message = "{gamelevelupdate.solutionPenalized.NotNull.message}")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "20")
    private int estimatedDuration;
    @ApiModelProperty(value = "How many times participant can submit incorrect flag before displaying solution.", required = true, example = "5")
    @NotNull(message = "{gamelevelupdate.incorrectFlagLimit.NotEmpty.message}")
    @Min(value = 0, message = "{gamelevelupdate.incorrectFlagLimit.Min.message}")
    private int incorrectFlagLimit;
    @ApiModelProperty(value = "Information which helps participant resolve the level.")
    private Set<HintDTO> hints = new HashSet<>();

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets flag.
     *
     * @return the flag
     */
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
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
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
     * Gets max score.
     *
     * @return the max score
     */
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets max score.
     *
     * @param maxScore the max score
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
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

    @Override
    public String toString() {
        return "GameLevelUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", estimatedDuration=" + estimatedDuration +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                ", hints=" + hints +
                '}';
    }
}
