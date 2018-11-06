package cz.muni.ics.kypo.training.api.dto.gamelevel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "GameLevelDTO", description = ".")
public class GameLevelDTO extends AbstractLevelDTO {

    private String flag;
    private String content;
    private String solution;
    private boolean solutionPenalized;
    private int estimatedDuration;
    private String[] attachments;
    private Set<HintDTO> hints = new HashSet<>();
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

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String[] getAttachments() {
        return attachments;
    }

    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
    }

    public Set<HintDTO> getHints() {
        return hints;
    }

    public void setHints(Set<HintDTO> hints) {
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
        return "GameLevelDTO{" + "flag='" + flag + '\'' + ", content='" + content + '\'' + ", solution='" + solution + '\''
                + ", solutionPenalized=" + solutionPenalized + ", estimatedDuration=" + estimatedDuration + ", attachments="
                + Arrays.toString(attachments) + ", hints=" + hints + ", incorrectFlagLimit=" + incorrectFlagLimit + '}';
    }
}
