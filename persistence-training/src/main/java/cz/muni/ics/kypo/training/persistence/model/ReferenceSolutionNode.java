package cz.muni.ics.kypo.training.persistence.model;


import java.util.List;
import java.util.Objects;

public class ReferenceSolutionNode {


    private String stateName;
    private List<String> prereqState;
    private String cmd;
    private String cmdType;
    private String cmdRegex;
    private boolean optional;

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
