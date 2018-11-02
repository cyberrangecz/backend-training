package cz.muni.ics.kypo.training.api.dto;

public class SandboxDefinitionRefDTO {

    private Long id;
    private Long sandboxDefinitionRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
