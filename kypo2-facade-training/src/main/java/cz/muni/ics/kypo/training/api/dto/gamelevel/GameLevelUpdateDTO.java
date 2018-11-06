package cz.muni.ics.kypo.training.api.dto.gamelevel;

import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.*;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "GameLevelUpdateDTO", description = "Game Level to update.")
public class GameLevelUpdateDTO {
    @NotNull(message = "{gamelevelupdate.id.NotNull.message}")
    protected Long id;
    @NotEmpty(message = "{gamelevelupdate.title.NotEmpty.message}")
    protected String title;
    @NotNull(message = "{gamelevelupdate.maxScore.NotNull.message}")
    @Min(value = 0, message = "{gamelevelupdate.maxScore.Min.message}")
    @Max(value = 100, message = "{gamelevelupdate.maxScore.Max.message}")
    private Integer maxScore;
    @NotEmpty(message = "{gamelevelupdate.flag.NotEmpty.message}")
    @Size(max = 50, message = "{gamelevelupdate.flag.Size.message}")
    private String flag;
    private String content;
    private String solution;
    @NotNull(message = "{gamelevelupdate.solutionPenalized.NotNull.message}")
    private boolean solutionPenalized;
    private int estimatedDuration;
    private String[] attachments;//?
    @NotNull(message = "{gamelevelupdate.incorrectFlagLimit.NotEmpty.message}")
    @Min(value = 0, message = "{gamelevelupdate.incorrectFlagLimit.Min.message}")
    private int incorrectFlagLimit;
    private Set<HintDTO> hints;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIncorrectFlagLimit() {
        return incorrectFlagLimit;
    }

    public void setIncorrectFlagLimit(int incorrectFlagLimit) {
        this.incorrectFlagLimit = incorrectFlagLimit;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Set<HintDTO> getHints() {
        return hints;
    }

    public void setHints(Set<HintDTO> hints) {
        this.hints = hints;
    }

    @Override
    public String toString() {
        return "GameLevelUpdateDTO{" + "id=" + id + ", title='" + title + '\'' + ", maxScore=" + maxScore + ", flag='" + flag + '\''
                + ", content='" + content + '\'' + ", solution='" + solution + '\'' + ", solutionPenalized=" + solutionPenalized
                + ", estimatedDuration=" + estimatedDuration + ", attachments=" + Arrays.toString(attachments) + ", incorrectFlagLimit="
                + incorrectFlagLimit + ", hints=" + hints + '}';
    }
}
