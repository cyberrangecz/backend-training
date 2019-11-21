package cz.muni.ics.kypo.training.api.dto.archive;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "GameLevelArchiveDTO", description = "Archived game level.", parent = AbstractLevelArchiveDTO.class)
public class GameLevelArchiveDTO extends AbstractLevelArchiveDTO{

    @ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretFlag")
    private String flag;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintArchiveDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "How many times player can submit incorrect flag before displaying solution.", example = "5")
    private int incorrectFlagLimit;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
    }

    public Set<HintArchiveDTO> getHints() {
        return hints;
    }

    public void setHints(Set<HintArchiveDTO> hints) {
        this.hints = hints;
    }

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
                ", hints=" + hints +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                '}';
    }
}
