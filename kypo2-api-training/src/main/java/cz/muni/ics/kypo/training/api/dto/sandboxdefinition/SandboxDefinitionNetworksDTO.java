package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;

/**
 * Encapsulates information about Sandbox definition networks.
 *
 * @author Pavel Seda
 */
public class SandboxDefinitionNetworksDTO {
    
    private String name;
    private String cidr;

    /**
     * Instantiates a new Sandbox definition networks dto.
     */
    public SandboxDefinitionNetworksDTO() {
    }

    /**
     * Instantiates a new Sandbox definition networks dto.
     *
     * @param name the name
     * @param cidr the cidr
     */
    public SandboxDefinitionNetworksDTO(String name, String cidr) {
        this.name = name;
        this.cidr = cidr;
    }

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
        return "SandboxDefinitionNetworksDTO{" +
                "name='" + name + '\'' +
                ", cidr='" + cidr + '\'' +
                '}';
    }
}
