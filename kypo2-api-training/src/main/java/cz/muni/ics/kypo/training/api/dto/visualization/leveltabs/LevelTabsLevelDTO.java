package cz.muni.ics.kypo.training.api.dto.visualization.leveltabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.EventDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.timeline.TimelineLevelDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LevelTabsLevelDTO extends VisualizationAbstractLevelDTO {

    private final String title;
    private final long estimatedTime;
    private final int maxPoints;
    private final String content;
    private final String correctFlag;
    private final List<LevelTabsHintDTO> hints;
    private final List<LevelTabsPlayerDTO> players;
    private final AssessmentType assessmentType;


    public LevelTabsLevelDTO(LevelTabsLevelBuilder builder) {
        super(builder);
        this.title = builder.title;
        this.estimatedTime = builder.estimatedTime;
        this.maxPoints = builder.maxPoints;
        this.content = builder.content;
        this.correctFlag = builder.correctFlag;
        this.hints = builder.hints;
        this.players = builder.players;
        this.assessmentType = builder.assessmentType;
    }

    public String getTitle() {
        return title;
    }

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public String getContent() {
        return content;
    }

    public String getCorrectFlag() {
        return correctFlag;
    }

    public List<LevelTabsHintDTO> getHints() {
        return hints;
    }

    public List<LevelTabsPlayerDTO> getPlayers() {
        return players;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LevelTabsLevelDTO that = (LevelTabsLevelDTO) o;
        return getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTitle());
    }

    public static class LevelTabsLevelBuilder extends VisualizationAbstractLevelDTO.BaseBuilder<LevelTabsLevelDTO, LevelTabsLevelBuilder> {

        private String title;
        private long estimatedTime;
        private String content;
        private int maxPoints;
        private String correctFlag;
        private List<LevelTabsHintDTO> hints;
        private List<LevelTabsPlayerDTO> players = new ArrayList<>();
        private AssessmentType assessmentType;


        @Override
        protected LevelTabsLevelBuilder getActualBuilder() {
            return this;
        }



        public LevelTabsLevelBuilder assessmentType(AssessmentType assessmentType) {
            this.assessmentType = assessmentType;
            return actualClassBuilder;
        }

        public LevelTabsLevelBuilder title(String title) {
            this.title = title;
            return actualClassBuilder;
        }

        public LevelTabsLevelBuilder estimatedTime(long estimatedTime) {
            this.estimatedTime = estimatedTime;
            return actualClassBuilder;
        }

        public LevelTabsLevelBuilder maxPoints(int maxPoints) {
            this.maxPoints = maxPoints;
            return actualClassBuilder;
        }
        public LevelTabsLevelBuilder content(String content) {
            this.content = content;
            return actualClassBuilder;
        }

        public LevelTabsLevelBuilder correctFlag(String correctFlag) {
            this.correctFlag = correctFlag;
            return this;
        }

        public LevelTabsLevelBuilder hints(List<LevelTabsHintDTO> hints) {
            this.hints = hints;
            return this;
        }

        public LevelTabsLevelBuilder players(List<LevelTabsPlayerDTO> players) {
            this.players = players;
            return this;
        }

        @Override
        public LevelTabsLevelDTO build() {
            return new LevelTabsLevelDTO(this);
        }
    }
}
