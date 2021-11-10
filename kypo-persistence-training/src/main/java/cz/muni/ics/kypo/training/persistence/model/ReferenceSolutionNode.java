package cz.muni.ics.kypo.training.persistence.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

//@Entity
//@Table(name = "reference_solution_node")
public class ReferenceSolutionNode {


    @Column(name = "state_name")
//    @JsonProperty("state_name")
    private String stateName;
    @ElementCollection
    @CollectionTable(
            name = "reference_solution_node_prerequisites",
            joinColumns = @JoinColumn(name = "reference_solution_node_id")
    )
    @Column(name = "prerequisite_state")
    private List<String> prereqState;
    @Column(name = "command")
    private String cmd;
    @Column(name = "command_type")
    private String cmdType;
    @Column(name = "command_regex")
    private String cmdRegex;
    @Column(name = "optional")
    private boolean optional;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "training_level_id")
//    private TrainingLevel trainingLevel;

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public List<String> getPrereqState() {
        return prereqState;
    }

    public void setPrereqState(List<String> prereqState) {
        this.prereqState = prereqState;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public String getCmdRegex() {
        return cmdRegex;
    }

    public void setCmdRegex(String cmdRegex) {
        this.cmdRegex = cmdRegex;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

//    public TrainingLevel getTrainingLevel() {
//        return trainingLevel;
//    }
//
//    public void setTrainingLevel(TrainingLevel trainingLevel) {
//        this.trainingLevel = trainingLevel;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceSolutionNode)) return false;
        ReferenceSolutionNode that = (ReferenceSolutionNode) o;
        return isOptional() == that.isOptional() && Objects.equals(getStateName(), that.getStateName()) && Objects.equals(getPrereqState(), that.getPrereqState()) && Objects.equals(getCmd(), that.getCmd()) && Objects.equals(getCmdType(), that.getCmdType()) && Objects.equals(getCmdRegex(), that.getCmdRegex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStateName(), getPrereqState(), getCmd(), getCmdType(), getCmdRegex(), isOptional());
    }

    @Override
    public String toString() {
        return "ReferenceSolutionNode{" +
                "stateName='" + stateName + '\'' +
                ", prereqState=" + prereqState +
                ", cmd='" + cmd + '\'' +
                ", cmdType='" + cmdType + '\'' +
                ", cmdRegex='" + cmdRegex + '\'' +
                ", optional=" + optional +
                '}';
    }
}
