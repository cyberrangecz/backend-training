package cz.muni.ics.kypo.training.api.dto.visualization;

import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about game level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@ApiModel(value = "GameLevelVisualizationDTO", description = "Information about game level needed for visualizations.", parent = AbstractLevelExportDTO.class)
public class GameLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretFlag")
    private String flag;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private List<HintDTO> hints;

    /**
     * Instantiates a new Game level visualization dto.
     */
    public GameLevelVisualizationDTO() {
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
    public List<HintDTO> getHints() {
        return hints;
    }

    /**
     * Sets hints.
     *
     * @param hints the set of {@link HintDTO}s
     */
    public void setHints(List<HintDTO> hints) {
        this.hints = hints;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GameLevelVisualizationDTO)) return false;
        if (!super.equals(object)) return false;
        GameLevelVisualizationDTO that = (GameLevelVisualizationDTO) object;
        return Objects.equals(this.getFlag(), that.getFlag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFlag());
    }
    @Override
    public String toString() {
        return "GameLevelVisualizationDTO{" +
                "id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", levelType=" + this.getLevelType() +
                ", order=" + this.getOrder() +
                ", maxScore=" + this.getMaxScore() +
                ", estimatedDuration=" + this.getEstimatedDuration() +
                ", solution='" + solution + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }
}
