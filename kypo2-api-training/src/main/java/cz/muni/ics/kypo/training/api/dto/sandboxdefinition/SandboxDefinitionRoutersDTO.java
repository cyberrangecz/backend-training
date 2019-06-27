package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;

/**
 * Encapsulates information about Sandbox definition routers.
 *
 * @author Pavel Seda
 */
public class SandboxDefinitionRoutersDTO {

    private String name;
    private String cidr;

    /**
     * Instantiates a new Sandbox definition routers dto.
     */
    public SandboxDefinitionRoutersDTO(){}

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets cidr.
     *
     * @return the cidr
     */
    public String getCidr() {
        return cidr;
    }

    /**
     * Sets cidr.
     *
     * @param cidr the cidr
     */
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
