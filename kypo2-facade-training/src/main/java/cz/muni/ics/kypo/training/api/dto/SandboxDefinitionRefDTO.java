package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
public class SandboxDefinitionRefDTO {

    private Long id;
    private Long sandboxDefinitionRef;

    @ApiModelProperty(value = "Main identifier of sandbox instance ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to sandbox definition in another microservice.", example = "1")
    public Long getSandboxDefinitionRef() {
        return sandboxDefinitionRef;
    }

    public void setSandboxDefinitionRef(Long sandboxDefinitionRef) {
        this.sandboxDefinitionRef = sandboxDefinitionRef;
    }

    @Override
    public String toString() {
        return "SandboxDefinitionRefDTO{" +
                "id=" + id +
                ", sandboxDefinitionRef=" + sandboxDefinitionRef +
                '}';
    }
}
