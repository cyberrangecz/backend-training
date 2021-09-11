package cz.muni.ics.kypo.training.startup;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class GetAccessLevel {
    @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
    private String title;
    private Long estimatedDuration;
    @NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
    private String content;
    @Size(max = 50, message = "{trainingLevel.answer.Size.message}")
    private String answer;
    @NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
    private String solution;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}
