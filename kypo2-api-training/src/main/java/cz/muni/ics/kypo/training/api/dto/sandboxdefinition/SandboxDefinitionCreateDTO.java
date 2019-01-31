package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;
import java.util.ArrayList;
import java.util.List;

/**
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

    public SandboxDefinitionCreateDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SandboxDefinitionHostsDTO> getHosts() {
        return hosts;
    }

    public void setHosts(List<SandboxDefinitionHostsDTO> hosts) {
        this.hosts = hosts;
    }

    public List<SandboxDefinitionRoutersDTO> getRouters() {
        return routers;
    }

    public void setRouters(List<SandboxDefinitionRoutersDTO> routers) {
        this.routers = routers;
    }

    public List<SandboxDefinitionNetworksDTO> getNetworks() {
        return networks;
    }

    public void setNetworks(List<SandboxDefinitionNetworksDTO> networks) {
        this.networks = networks;
    }

    public List<List<String>> getRouterMappings() {
        return routerMappings;
    }

    public void setRouterMappings(List<List<String>> routerMappings) {
        this.routerMappings = routerMappings;
    }

    public List<List<String>> getNetMappings() {
        return netMappings;
    }

    public void setNetMappings(List<List<String>> netMappings) {
        this.netMappings = netMappings;
    }

    public List<List<String>> getBlockInternet() {
        return blockInternet;
    }

    public void setBlockInternet(List<List<String>> blockInternet) {
        this.blockInternet = blockInternet;
    }

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
