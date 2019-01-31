package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "GameLevelExportDTO", description = "An assignment containing security tasks whose completion yields a flag.", parent = AbstractLevelExportDTO.class)
public class GameLevelExportDTO extends AbstractLevelExportDTO {

    private String flag;
    private String content;
    private String solution;
    private boolean solutionPenalized;
    private int estimatedDuration;
    private String[] attachments;
    private Set<HintExportDTO> hints = new HashSet<>();
    private int incorrectFlagLimit;

    public GameLevelExportDTO() {
    }

    @ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretFlag")
    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

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

    @ApiModelProperty(value = "Downloadable files for level (pictures, source code...)", example = "")
    public String[] getAttachments() {
        return attachments;
    }

    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
    }

    @ApiModelProperty(value = "Information which helps player resolve the level.")
    public Set<HintExportDTO> getHints() {
        return hints;
    }

    public void setHints(Set<HintExportDTO> hints) {
        this.hints = hints;
    }

    @ApiModelProperty(value = "How many times player can submit incorrect flag before displaying solution.", example = "5")
    public int getIncorrectFlagLimit() {
        return incorrectFlagLimit;
    }

    public void setIncorrectFlagLimit(int incorrectFlagLimit) {
        this.incorrectFlagLimit = incorrectFlagLimit;
    }

    @Override
    public String toString() {
        return "GameLevelExportDTO{" +
                "flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", estimatedDuration=" + estimatedDuration +
                ", attachments=" + Arrays.toString(attachments) +
                ", hints=" + hints +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                ", title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", levelType=" + levelType +
                '}';
    }
}
