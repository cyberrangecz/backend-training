package cz.muni.ics.kypo.training.api.dto.visualization.timeline;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.EventDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;

import java.util.List;
import java.util.Objects;

public class TimelineLevelDTO extends VisualizationAbstractLevelDTO {

    private final Long solutionDisplayedTime;
    private final Long correctAnswerTime;
    private final Integer wrongAnswers;
    private final Integer hintsTaken;
    private final long startTime;
    private final int participantLevelScore;
    private final List<EventDTO> events;
    private final AssessmentType assessmentType;


    public TimelineLevelDTO(TimelineLevelBuilder timelineLevelBuilder) {
        super(timelineLevelBuilder);
        this.solutionDisplayedTime = timelineLevelBuilder.solutionDisplayedTime;
        this.correctAnswerTime = timelineLevelBuilder.correctAnswerTime;
        this.wrongAnswers = timelineLevelBuilder.wrongAnswers;
        this.hintsTaken = timelineLevelBuilder.hintsTaken;
        this.assessmentType = timelineLevelBuilder.assessmentType;
        this.participantLevelScore = timelineLevelBuilder.participantScore;
        this.events = timelineLevelBuilder.events;
        this.startTime = timelineLevelBuilder.startTime;
    }

    public Long getCorrectAnswerTime() {
        return correctAnswerTime;
    }

    public Long getSolutionDisplayedTime() {
        return solutionDisplayedTime;
    }

    public Integer getWrongAnswers() {
        return wrongAnswers;
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

    public int getParticipantLevelScore() {
        return participantLevelScore;
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
        return getParticipantLevelScore() == that.getParticipantLevelScore() &&
                Objects.equals(getEvents(), that.getEvents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParticipantLevelScore(), getEvents());
    }

    public static class TimelineLevelBuilder extends BaseBuilder<TimelineLevelDTO, TimelineLevelBuilder> {
        private Long solutionDisplayedTime;
        private Long correctAnswerTime;
        private Integer wrongAnswers;
        private Integer hintsTaken;
        private AssessmentType assessmentType;
        private long startTime;
        private int participantScore;
        private List<EventDTO> events;

        @Override
        protected TimelineLevelBuilder getActualBuilder() {
            return this;
        }

        public TimelineLevelBuilder solutionDisplayedTime(Long solutionDisplayedTime) {
            this.solutionDisplayedTime = solutionDisplayedTime;
            return this;
        }

        public TimelineLevelBuilder wrongAnswers(Integer wrongAnswers) {
            this.wrongAnswers = wrongAnswers;
            return this;
        }

        public TimelineLevelBuilder hintsTaken(Integer hintsTaken) {
            this.hintsTaken = hintsTaken;
            return this;
        }

        public TimelineLevelBuilder correctAnswerTime(Long correctAnswerTime) {
            this.correctAnswerTime = correctAnswerTime;
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

        public TimelineLevelBuilder participantScore(int participantScore) {
            this.participantScore = participantScore;
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
