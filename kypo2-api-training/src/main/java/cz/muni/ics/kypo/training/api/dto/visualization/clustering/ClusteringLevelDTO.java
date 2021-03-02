package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClusteringLevelDTO extends VisualizationAbstractLevelDTO {

    private final String title;
    private final long estimatedTime;
    private final int maxPoints;
    private final long maxTime;
    private final float averageTime;
    private final float averageScore;
    private final List<PlayerDataDTO> playerData;

    public ClusteringLevelDTO(ClusteringLevelBuilder builder) {
        super(builder);
        this.title = builder.title;
        this.estimatedTime = builder.estimatedTime;
        this.maxPoints = builder.maxPoints;
        this.maxTime = builder.maxTime;
        this.averageTime = builder.averageTime;
        this.averageScore = builder.averageScore;
        this.playerData = builder.playerData;
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

    public long getMaxTime() {
        return maxTime;
    }

    public float getAverageTime() {
        return averageTime;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public List<PlayerDataDTO> getPlayerData() {
        return playerData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClusteringLevelDTO that = (ClusteringLevelDTO) o;
        return getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTitle());
    }

    public static class ClusteringLevelBuilder extends VisualizationAbstractLevelDTO.BaseBuilder<ClusteringLevelDTO, ClusteringLevelBuilder> {
        private String title;
        private long estimatedTime;
        private int maxPoints;
        private long maxTime;
        private float averageTime;
        private float averageScore;
        private List<PlayerDataDTO> playerData = new ArrayList<>();

        @Override
        protected ClusteringLevelBuilder getActualBuilder() {
            return this;
        }

        public ClusteringLevelBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ClusteringLevelBuilder estimatedTime(long estimatedTime) {
            this.estimatedTime = estimatedTime;
            return this;
        }

        public ClusteringLevelBuilder maxPoints(int maxPoints) {
            this.maxPoints = maxPoints;
            return this;
        }

        public ClusteringLevelBuilder maxTime(long maxTime) {
            this.maxTime = maxTime;
            return this;
        }

        public ClusteringLevelBuilder averageTime(float averageTime) {
            this.averageTime = averageTime;
            return this;
        }

        public ClusteringLevelBuilder averageScore(float averageScore) {
            this.averageScore = averageScore;
            return this;
        }

        public ClusteringLevelBuilder playerData(List<PlayerDataDTO> playerData) {
            this.playerData = playerData;
            return this;
        }

        public ClusteringLevelBuilder addPlayerData(PlayerDataDTO playerData) {
            this.playerData.add(playerData);
            return this;
        }

        @Override
        public ClusteringLevelDTO build() {
            return new ClusteringLevelDTO(this);
        }
    }

}
