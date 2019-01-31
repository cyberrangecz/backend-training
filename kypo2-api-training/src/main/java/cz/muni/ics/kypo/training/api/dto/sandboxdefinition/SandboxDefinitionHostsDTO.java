package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;

import java.util.Arrays;

/**
 * @author Pavel Seda
 */
public class SandboxDefinitionHostsDTO {

    private String name;
    private String vagrantBox;
    private String openstackImage;
    private String flavor;
    private String[] roles;

    public SandboxDefinitionHostsDTO() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVagrantBox() {
        return vagrantBox;
    }

    public void setVagrantBox(String vagrantBox) {
        this.vagrantBox = vagrantBox;
    }

    public String getOpenstackImage() {
        return openstackImage;
    }

    public void setOpenstackImage(String openstackImage) {
        this.openstackImage = openstackImage;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "SandboxDefinitionHostsDTO{" +
                "name='" + name + '\'' +
                ", vagrantBox='" + vagrantBox + '\'' +
                ", openstackImage='" + openstackImage + '\'' +
                ", flavor='" + flavor + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}
