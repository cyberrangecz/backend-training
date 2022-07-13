package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.auth.In;

import java.util.Objects;

public class QuestionEMIAnswer {

    @JsonProperty("statementOrder")
    private Integer statementOrder;
    @JsonProperty("optionOrder")
    private Integer optionOrder;

    public QuestionEMIAnswer() {
    }

    public QuestionEMIAnswer(Integer statementOrder, Integer optionOrder) {
        this.statementOrder = statementOrder;
        this.optionOrder = optionOrder;
    }

    public Integer getStatementOrder() {
        return statementOrder;
    }

    public void setStatementOrder(Integer statementOrder) {
        this.statementOrder = statementOrder;
    }

    public Integer getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(Integer optionOrder) {
        this.optionOrder = optionOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionEMIAnswer that = (QuestionEMIAnswer) o;
        return getStatementOrder().equals(that.getStatementOrder()) &&
                getOptionOrder().equals(that.getOptionOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatementOrder(), getOptionOrder());
    }
}
