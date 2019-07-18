package cz.muni.ics.kypo.training.api.dto.archive;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "GameLevelArchiveDTO", description = "An assignment containing security tasks whose completion yields a flag.", parent = AbstractLevelArchiveDTO.class)
public class GameLevelArchiveDTO extends AbstractLevelArchiveDTO{

    private String flag;
    private String content;
    private String solution;
    private boolean solutionPenalized;
    private String[] attachments;
    private Set<HintArchiveDTO> hints = new HashSet<>();
    private int incorrectFlagLimit;

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

    @ApiModelProperty(value = "Downloadable files for level (pictures, source code...)", example = "")
    public String[] getAttachments() {
        return attachments;
    }

    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
    }

    @ApiModelProperty(value = "Information which helps player resolve the level.")
    public Set<HintArchiveDTO> getHints() {
        return hints;
    }

    public void setHints(Set<HintArchiveDTO> hints) {
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
        return "GameLevelArchiveDTO{" +
                "flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", attachments=" + Arrays.toString(attachments) +
                ", hints=" + hints +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                '}';
    }
}
