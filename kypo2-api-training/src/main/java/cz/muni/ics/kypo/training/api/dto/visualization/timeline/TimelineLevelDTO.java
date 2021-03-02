package cz.muni.ics.kypo.training.api.dto.visualization.timeline;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.EventDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;

import java.util.List;
import java.util.Objects;

public class TimelineLevelDTO extends VisualizationAbstractLevelDTO {

    private final Long solutionDisplayedTime;
    private final Long correctFlagTime;
    private final Integer wrongFlags;
    private final Integer hintsTaken;
    private final long startTime;
    private final int score;
    private final List<EventDTO> events;
    private final AssessmentType assessmentType;


    public TimelineLevelDTO(TimelineLevelBuilder timelineLevelBuilder) {
        super(timelineLevelBuilder);
        this.solutionDisplayedTime = timelineLevelBuilder.solutionDisplayedTime;
        this.correctFlagTime = timelineLevelBuilder.correctFlagTime;
        this.wrongFlags = timelineLevelBuilder.wrongFlags;
        this.hintsTaken = timelineLevelBuilder.hintsTaken;
        this.assessmentType = timelineLevelBuilder.assessmentType;
        this.score = timelineLevelBuilder.score;
        this.events = timelineLevelBuilder.events;
        this.startTime = timelineLevelBuilder.startTime;
    }

    public Long getCorrectFlagTime() {
        return correctFlagTime;
    }

    public Long getSolutionDisplayedTime() {
        return solutionDisplayedTime;
    }

    public Integer getWrongFlags() {
        return wrongFlags;
    }

    public Integer getHintsTaken() {
        return hintsTaken;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public Long getStartTime() {
        return startTime;
    }

    public int getScore() {
        return score;
    }

    public List<EventDTO> getEvents() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TimelineLevelDTO that = (TimelineLevelDTO) o;
        return getScore() == that.getScore() &&
                Objects.equals(getEvents(), that.getEvents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getScore(), getEvents());
    }

    public static class TimelineLevelBuilder extends BaseBuilder<TimelineLevelDTO, TimelineLevelBuilder> {
        private Long solutionDisplayedTime;
        private Long correctFlagTime;
        private Integer wrongFlags;
        private Integer hintsTaken;
        private AssessmentType assessmentType;
        private long startTime;
        private int score;
        private List<EventDTO> events;

        @Override
        protected TimelineLevelBuilder getActualBuilder() {
            return this;
        }

        public TimelineLevelBuilder solutionDisplayedTime(Long solutionDisplayedTime) {
            this.solutionDisplayedTime = solutionDisplayedTime;
            return this;
        }

        public TimelineLevelBuilder wrongFlags(Integer wrongFlags) {
            this.wrongFlags = wrongFlags;
            return this;
        }

        public TimelineLevelBuilder hintsTaken(Integer hintsTaken) {
            this.hintsTaken = hintsTaken;
            return this;
        }

        public TimelineLevelBuilder correctFlagTime(Long correctFlagTime) {
            this.correctFlagTime = correctFlagTime;
            return this;
        }

        public TimelineLevelBuilder assessmentType(AssessmentType assessmentType) {
            this.assessmentType = assessmentType;
            return this;
        }

        public TimelineLevelBuilder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public TimelineLevelBuilder score(int score) {
            this.score = score;
            return this;
        }

        public TimelineLevelBuilder events(List<EventDTO> events) {
            this.events = events;
            return this;
        }

        @Override
        public TimelineLevelDTO build() {
            return new TimelineLevelDTO(this);
        }
    }
}
