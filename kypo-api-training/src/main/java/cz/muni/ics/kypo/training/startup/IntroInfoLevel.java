package cz.muni.ics.kypo.training.startup;

import javax.validation.constraints.NotEmpty;

public class IntroInfoLevel {
    @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
    private String title;
    private Long estimatedDuration;
    @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
    private String content;

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
}
