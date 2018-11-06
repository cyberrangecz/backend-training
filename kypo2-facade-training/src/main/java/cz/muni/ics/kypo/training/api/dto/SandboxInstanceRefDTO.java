package cz.muni.ics.kypo.training.api.dto;

public class SandboxInstanceRefDTO {

    private Long id;
    private Long sandboxInstanceRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
