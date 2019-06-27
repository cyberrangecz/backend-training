package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about Sandbox instance reference.
 *
 * @author Pavel Seda
 */
public class SandboxInstanceRefDTO {

    private Long id;
    private Long sandboxInstanceRef;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of sandbox instance ref.", example = "1")
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets sandbox instance ref.
     *
     * @return the sandbox instance ref
     */
    @ApiModelProperty(value = "Reference to sandbox instance in another microservice.", example = "1")
    public Long getSandboxInstanceRef() {
        return sandboxInstanceRef;
    }

    /**
     * Sets sandbox instance ref.
     *
     * @param sandboxInstanceRef the sandbox instance ref
     */
    public void setSandboxInstanceRef(Long sandboxInstanceRef) {
        this.sandboxInstanceRef = sandboxInstanceRef;
    }

    @Override
    public String toString() {
        return "SandboxInstanceRefDTO{" + "id=" + id + ", sandboxInstanceRef=" + sandboxInstanceRef + '}';
    }
}
