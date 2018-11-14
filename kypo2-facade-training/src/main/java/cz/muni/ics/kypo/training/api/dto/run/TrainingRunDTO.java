package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.dto.SandboxInstanceRefDTO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "TrainingRunDTO", description = "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunDTO {

    private Long id;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startTime;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endTime;
    private String eventLogReference;
    private TRState state;
    private SandboxInstanceRefDTO sandboxInstanceRef;

    @ApiModelProperty(value = "Main identifier of training run.")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Date when training run started.")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ApiModelProperty(value = "Date when training run ends.")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getEventLogReference() {
        return eventLogReference;
    }

    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    @ApiModelProperty(value = "Current state of training run.")
    public TRState getState() {
        return state;
    }

    public void setState(TRState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "Reference to the received sandbox.")
    public SandboxInstanceRefDTO getSandboxInstanceRef() {
        return sandboxInstanceRef;
    }

    public void setSandboxInstanceRef(SandboxInstanceRefDTO sandboxInstanceRef) {
        this.sandboxInstanceRef = sandboxInstanceRef;
    }

    @Override
    public String toString() {
        return "TrainingRunDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference='"
                + eventLogReference + '\'' + ", state=" + state + ", sandboxInstanceRef=" + sandboxInstanceRef + '}';
    }
}
