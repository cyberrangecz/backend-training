package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;

/**
 * @author Pavel Seda
 */
public class SandboxDefinitionRoutersDTO {

    private String name;
    private String cidr;

    public SandboxDefinitionRoutersDTO(){}

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
        return "SandboxDefinitionRoutersDTO{" +
                "name='" + name + '\'' +
                ", cidr='" + cidr + '\'' +
                '}';
    }
}
