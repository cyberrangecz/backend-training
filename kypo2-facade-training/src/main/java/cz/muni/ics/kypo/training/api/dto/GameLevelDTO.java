package cz.muni.ics.kypo.training.api.dto;

import cz.muni.ics.kypo.training.model.Hint;
import io.swagger.annotations.ApiModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "GameLevelDTO", description = ".")
public class GameLevelDTO extends AbstractLevelDTO {

    private String flag;
    private String content;
    private String solution;
    private int incorrectFlagPenalty;
    private int solutionPenalty = super.getMaxScore() - 1;
    private int estimatedDuration;
    private String[] attachments;
    private Set<Hint> hints = new HashSet<>();

    public GameLevelDTO() {}

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

    public int getIncorrectFlagPenalty() {
        return incorrectFlagPenalty;
    }

    public void setIncorrectFlagPenalty(int incorrectFlagPenalty) {
        this.incorrectFlagPenalty = incorrectFlagPenalty;
    }

    public int getSolutionPenalty() {
        return solutionPenalty;
    }

    public void setSolutionPenalty(int solutionPenalty) {
        this.solutionPenalty = solutionPenalty;
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

    public Set<Hint> getHints() {
        return hints;
    }

    public void setHints(Set<Hint> hints) {
        this.hints = hints;
    }

    @Override
    public String toString() {
        return "GameLevelDTO{" +
                "flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", incorrectFlagPenalty=" + incorrectFlagPenalty +
                ", solutionPenalty=" + solutionPenalty +
                ", estimatedDuration=" + estimatedDuration +
                ", attachments=" + Arrays.toString(attachments) +
                ", hints=" + hints +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", nextLevel=" + nextLevel +
                ", preHook=" + preHook +
                ", postHook=" + postHook +
                '}';
    }
}
