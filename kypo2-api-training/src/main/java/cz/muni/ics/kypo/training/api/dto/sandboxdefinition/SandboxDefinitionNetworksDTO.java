package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;

/**
 * @author Pavel Seda
 */
public class SandboxDefinitionNetworksDTO {
    
    private String name;
    private String cidr;

    public SandboxDefinitionNetworksDTO() {
    }

    public SandboxDefinitionNetworksDTO(String name, String cidr) {
        this.name = name;
        this.cidr = cidr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    @Override
    public String toString() {
        return "SandboxDefinitionNetworksDTO{" +
                "name='" + name + '\'' +
                ", cidr='" + cidr + '\'' +
                '}';
    }
}
