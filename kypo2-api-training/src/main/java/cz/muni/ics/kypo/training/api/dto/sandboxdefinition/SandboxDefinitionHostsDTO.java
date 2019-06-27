package cz.muni.ics.kypo.training.api.dto.sandboxdefinition;

import java.util.Arrays;

/**
 * Encapsulates information about Sandbox definition hosts.
 *
 * @author Pavel Seda
 */
public class SandboxDefinitionHostsDTO {

    private String name;
    private String vagrantBox;
    private String openstackImage;
    private String flavor;
    private String[] roles;

    /**
     * Instantiates a new Sandbox definition hosts dto.
     */
    public SandboxDefinitionHostsDTO() {
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
     * Gets vagrant box.
     *
     * @return the vagrant box
     */
    public String getVagrantBox() {
        return vagrantBox;
    }

    /**
     * Sets vagrant box.
     *
     * @param vagrantBox the vagrant box
     */
    public void setVagrantBox(String vagrantBox) {
        this.vagrantBox = vagrantBox;
    }

    /**
     * Gets openstack image.
     *
     * @return the openstack image
     */
    public String getOpenstackImage() {
        return openstackImage;
    }

    /**
     * Sets openstack image.
     *
     * @param openstackImage the openstack image
     */
    public void setOpenstackImage(String openstackImage) {
        this.openstackImage = openstackImage;
    }

    /**
     * Gets flavor.
     *
     * @return the flavor
     */
    public String getFlavor() {
        return flavor;
    }

    /**
     * Sets flavor.
     *
     * @param flavor the flavor
     */
    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    /**
     * Get roles string [ ].
     *
     * @return the string [ ]
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * Sets roles.
     *
     * @param roles the roles
     */
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
