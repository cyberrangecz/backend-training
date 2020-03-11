package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.dto.imports.AttachmentImportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates information about game level. Inherits from {@link AbstractLevelExportDTO}
 *
 */
@ApiModel(value = "GameLevelExportDTO", description = "Exported game level.", parent = AbstractLevelExportDTO.class)
public class GameLevelExportDTO extends AbstractLevelExportDTO {

    @ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretFlag")
    private String flag;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintExportDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "How many times player can submit incorrect flag before displaying solution.", example = "5")
    private int incorrectFlagLimit;
    @ApiModelProperty(value = "List of attachments.", example = "[]")
    private List<AttachmentImportDTO> attachments;


    /**
     * Instantiates a new Game level export dto.
     */
    public GameLevelExportDTO() {
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
     * Gets hints.
     *
     * @return the set of {@link HintExportDTO}
     */
    public Set<HintExportDTO> getHints() {
        return hints;
    }

    /**
     * Sets hints.
     *
     * @param hints the set of {@link HintExportDTO}
     */
    public void setHints(Set<HintExportDTO> hints) {
        this.hints = hints;
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
     * Gets attachments.
     *
     * @return the list of attachments
     */
    public List<AttachmentImportDTO> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param attachments the list of attachments
     */
    public void setAttachments(List<AttachmentImportDTO> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "GameLevelExportDTO{" +
                "flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", hints=" + hints +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                ", title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", levelType=" + levelType +
                '}';
    }
}
