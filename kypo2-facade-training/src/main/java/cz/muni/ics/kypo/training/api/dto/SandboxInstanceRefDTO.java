package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class SandboxInstanceRefDTO {

    private Long id;
    private Long sandboxInstanceRef;

    @ApiModelProperty(value = "Main identifier of sandbox instance ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to sandbox instance in another microservice.", example = "1")
    public Long getSandboxInstanceRef() {
        return sandboxInstanceRef;
    }

    public void setSandboxInstanceRef(Long sandboxInstanceRef) {
        this.sandboxInstanceRef = sandboxInstanceRef;
    }

    @Override
    public String toString() {
        return "SandboxInstanceRefDTO{" + "id=" + id + ", sandboxInstanceRef=" + sandboxInstanceRef + '}';
    }
}
