package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * This classes uses Builder pattern based on the following blog:
 *
 * @author Pavel Šeda
 * @see <a href="https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/">https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/</a>
 * <p>
 * Without that builder it is easy to mesh class parameters, e.g. trainingDefinitionId with trainingInstanceId.
 */
@ApiObject(name = "Hint Taken", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class HintTaken extends AbstractAuditPOJO {

    @ApiObjectField(description = "Sandbox ID.")
    @JsonProperty(value = "sandbox_id", required = true)
    private long sandboxId;
    @ApiObjectField(description = "Training definition ID.")
    @JsonProperty(value = "training_definition_id", required = true)
    private long trainingDefinitionId;
    @ApiObjectField(description = "Training instance ID.")
    @JsonProperty(value = "training_instance_id", required = true)
    private long trainingInstanceId;
    @ApiObjectField(description = "Training run ID.")
    @JsonProperty(value = "training_run_id", required = true)
    private long trainingRunId;
    @ApiObjectField(description = "ID of a player in the training run.")
    @JsonProperty(value = "player_login", required = true)
    private String playerLogin;
    @ApiObjectField(description = "Training run level.")
    @JsonProperty(value = "level", required = true)
    private long level;
    @ApiObjectField(description = "Hint ID.")
    @JsonProperty(value = "hint_id", required = true)
    private long hintId;
    @ApiObjectField(description = "Hint penalty points.")
    @JsonProperty(value = "hint_penalty_points", required = true)
    private int hintPenaltyPoints;
    @ApiObjectField(description = "Hint title.")
    @JsonProperty(value = "hint_title", required = true)
    private String hintTitle;

    public static SandboxIdBuilder builder() {
        return new HintTakenBuilder();
    }

    public static class HintTakenBuilder implements SandboxIdBuilder, TrainingDefinitionIdBuilder, TrainingInstanceIdBuilder, TrainingRunIdBuilder, PlayerLoginBuilder, LevelBuilder, HintIdBuilder, HintPenaltyPointsBuilder, HintTitleBuilder {
        private long sandboxId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private String playerLogin;
        private long level;
        private long hintId;
        private int hintPenaltyPoints;
        private String hintTitle;

        @Override
        public TrainingDefinitionIdBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        @Override
        public TrainingInstanceIdBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        @Override
        public TrainingRunIdBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        @Override
        public PlayerLoginBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        @Override
        public LevelBuilder playerLogin(String playerLogin) {
            this.playerLogin = playerLogin;
            return this;
        }

        @Override
        public HintIdBuilder level(long level) {
            this.level = level;
            return this;
        }

        @Override
        public HintPenaltyPointsBuilder hintId(long hintId) {
            this.hintId = hintId;
            return this;
        }

        @Override
        public HintTitleBuilder hintPenaltyPoints(int penaltyPoints) {
            this.hintPenaltyPoints = penaltyPoints;
            return this;
        }

        @Override
        public HintTakenBuilder hintTitle(String hintTitle) {
            this.hintTitle = hintTitle;
            return this;
        }

        public HintTaken build() {
            return new HintTaken(this);
        }

    }

    public interface SandboxIdBuilder {
        TrainingDefinitionIdBuilder sandboxId(long sandboxId);
    }

    public interface TrainingDefinitionIdBuilder {
        TrainingInstanceIdBuilder trainingDefinitionId(long trainingDefinitionId);
    }

    public interface TrainingInstanceIdBuilder {
        TrainingRunIdBuilder trainingInstanceId(long trainingInstanceId);
    }

    public interface TrainingRunIdBuilder {
        PlayerLoginBuilder trainingRunId(long trainingRunId);
    }

    public interface PlayerLoginBuilder {
        LevelBuilder playerLogin(String playerLogin);
    }

    public interface LevelBuilder {
        HintIdBuilder level(long level);
    }

    public interface HintIdBuilder {
        HintPenaltyPointsBuilder hintId(long hintId);
    }

    public interface HintPenaltyPointsBuilder {
        HintTitleBuilder hintPenaltyPoints(int penaltyPoints);
    }

    public interface HintTitleBuilder {
        HintTakenBuilder hintTitle(String hintTitle);
    }

    private HintTaken(HintTakenBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.playerLogin = builder.playerLogin;
        this.level = builder.level;
        this.hintId = builder.hintId;
        this.hintPenaltyPoints = builder.hintPenaltyPoints;
        this.hintTitle = builder.hintTitle;
    }

    public long getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(long sandboxId) {
        this.sandboxId = sandboxId;
    }

    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    public long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    public String getPlayerLogin() {
        return playerLogin;
    }

    public void setPlayerLogin(String playerLogin) {
        this.playerLogin = playerLogin;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public long getHintId() {
        return hintId;
    }

    public void setHintId(long hintId) {
        this.hintId = hintId;
    }

    public int getHintPenaltyPoints() {
        return hintPenaltyPoints;
    }

    public void setHintPenaltyPoints(int hintPenaltyPoints) {
        this.hintPenaltyPoints = hintPenaltyPoints;
    }

    public String getHintTitle() {
        return hintTitle;
    }

    public void setHintTitle(String hintTitle) {
        this.hintTitle = hintTitle;
    }

    @Override
    public String toString() {
        return "HintTaken{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", playerLogin='" + playerLogin + '\'' +
                ", level=" + level +
                ", hintId=" + hintId +
                ", hintPenaltyPoints=" + hintPenaltyPoints +
                ", hintTitle='" + hintTitle + '\'' +
                '}';
    }
}
