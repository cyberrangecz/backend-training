package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about Sandbox definition.
 *
 * @author Pavel Seda
 */
public class SandboxDefinitionCreateDTO {

    private String name;
    private List<SandboxDefinitionHostsDTO> hosts;
    private List<SandboxDefinitionRoutersDTO> routers;
    private List<SandboxDefinitionNetworksDTO> networks;
    private List<List<String>> routerMappings = new ArrayList<>();
    private List<List<String>> netMappings = new ArrayList<>();
    private List<List<String>> blockInternet = new ArrayList<>();
    private List<String> includeUserAccess = new ArrayList<>();

    /**
     * Instantiates a new Sandbox definition create dto.
     */
    public SandboxDefinitionCreateDTO() {
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
     * Gets hosts.
     *
     * @return the hosts
     */
    public List<SandboxDefinitionHostsDTO> getHosts() {
        return hosts;
    }

    /**
     * Sets hosts.
     *
     * @param hosts the hosts
     */
    public void setHosts(List<SandboxDefinitionHostsDTO> hosts) {
        this.hosts = hosts;
    }

    /**
     * Gets routers.
     *
     * @return the routers
     */
    public List<SandboxDefinitionRoutersDTO> getRouters() {
        return routers;
    }

    /**
     * Sets routers.
     *
     * @param routers the routers
     */
    public void setRouters(List<SandboxDefinitionRoutersDTO> routers) {
        this.routers = routers;
    }

    /**
     * Gets networks.
     *
     * @return the networks
     */
    public List<SandboxDefinitionNetworksDTO> getNetworks() {
        return networks;
    }

    /**
     * Sets networks.
     *
     * @param networks the networks
     */
    public void setNetworks(List<SandboxDefinitionNetworksDTO> networks) {
        this.networks = networks;
    }

    /**
     * Gets router mappings.
     *
     * @return the router mappings
     */
    public List<List<String>> getRouterMappings() {
        return routerMappings;
    }

    /**
     * Sets router mappings.
     *
     * @param routerMappings the router mappings
     */
    public void setRouterMappings(List<List<String>> routerMappings) {
        this.routerMappings = routerMappings;
    }

    /**
     * Gets net mappings.
     *
     * @return the net mappings
     */
    public List<List<String>> getNetMappings() {
        return netMappings;
    }

    /**
     * Sets net mappings.
     *
     * @param netMappings the net mappings
     */
    public void setNetMappings(List<List<String>> netMappings) {
        this.netMappings = netMappings;
    }

    /**
     * Gets block internet.
     *
     * @return the block internet
     */
    public List<List<String>> getBlockInternet() {
        return blockInternet;
    }

    /**
     * Sets block internet.
     *
     * @param blockInternet the block internet
     */
    public void setBlockInternet(List<List<String>> blockInternet) {
        this.blockInternet = blockInternet;
    }

    /**
     * Sets include user access.
     *
     * @param includeUserAccess the include user access
     */
    public void setIncludeUserAccess(List<String> includeUserAccess) {
        this.includeUserAccess = includeUserAccess;
    }

    @Override
    public String toString() {
        return "SandboxDefinitionCreateDTO{" +
                "name='" + name + '\'' +
                ", hosts=" + hosts +
                ", routers=" + routers +
                ", networks=" + networks +
                ", routerMappings=" + routerMappings +
                ", netMappings=" + netMappings +
                ", blockInternet=" + blockInternet +
                ", includeUserAccess=" + includeUserAccess +
                '}';
    }
}
