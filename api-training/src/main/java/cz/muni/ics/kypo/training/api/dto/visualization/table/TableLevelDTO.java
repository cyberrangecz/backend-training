package cz.muni.ics.kypo.training.api.dto.visualization.table;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.Objects;

public class TableLevelDTO extends VisualizationAbstractLevelDTO {
    private final Integer wrongAnswers;
    private final Integer hintsTaken;
    private final int participantLevelScore;

    public TableLevelDTO(TableLevelBuilder builder) {
        super(builder);
        this.wrongAnswers = builder.wrongAnswers;
        this.hintsTaken = builder.hintsTaken;
        this.participantLevelScore = builder.score;
    }

    public Integer getWrongAnswers() {
        return wrongAnswers;
    }

    public Integer getHintsTaken() {
        return hintsTaken;
    }

    public int getParticipantLevelScore() {
        return participantLevelScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TableLevelDTO that = (TableLevelDTO) o;
        return getParticipantLevelScore() == that.getParticipantLevelScore();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParticipantLevelScore());
    }

    public static class TableLevelBuilder extends BaseBuilder<TableLevelDTO, TableLevelDTO.TableLevelBuilder> {
        private Integer wrongAnswers;
        private Integer hintsTaken;
        private int score;

        @Override
        protected TableLevelDTO.TableLevelBuilder getActualBuilder() {
            return this;
        }

        public TableLevelDTO.TableLevelBuilder wrongAnswers(Integer wrongAnswers) {
            this.wrongAnswers = wrongAnswers;
            return this;
        }

        public TableLevelDTO.TableLevelBuilder hintsTaken(Integer hintsTaken) {
            this.hintsTaken = hintsTaken;
            return this;
        }

        public TableLevelDTO.TableLevelBuilder score(int score) {
            this.score = score;
            return this;
        }

        @Override
        public TableLevelDTO build() {
            return new TableLevelDTO(this);
        }
    }
}
