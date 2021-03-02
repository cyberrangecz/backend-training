package cz.muni.ics.kypo.training.api.dto.visualization.table;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.Objects;

public class TableLevelDTO extends VisualizationAbstractLevelDTO {
    private final Integer wrongFlags;
    private final Integer hintsTaken;
    private final int score;

    public TableLevelDTO(TableLevelBuilder builder) {
        super(builder);
        this.wrongFlags = builder.wrongFlags;
        this.hintsTaken = builder.hintsTaken;
        this.score = builder.score;
    }

    public Integer getWrongFlags() {
        return wrongFlags;
    }

    public Integer getHintsTaken() {
        return hintsTaken;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TableLevelDTO that = (TableLevelDTO) o;
        return getScore() == that.getScore();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getScore());
    }

    public static class TableLevelBuilder extends BaseBuilder<TableLevelDTO, TableLevelDTO.TableLevelBuilder> {
        private Integer wrongFlags;
        private Integer hintsTaken;
        private int score;

        @Override
        protected TableLevelDTO.TableLevelBuilder getActualBuilder() {
            return this;
        }

        public TableLevelDTO.TableLevelBuilder wrongFlags(Integer wrongFlags) {
            this.wrongFlags = wrongFlags;
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
