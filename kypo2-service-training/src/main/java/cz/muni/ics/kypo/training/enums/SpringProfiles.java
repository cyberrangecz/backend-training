package cz.muni.ics.kypo.training.enums;

/**
 * The enum Spring profiles.
 */
public enum SpringProfiles {

    /**
     * Prod spring profiles.
     */
    PROD("PROD", ""),
    /**
     * Dev spring profiles.
     */
    DEV("DEV", "");

    private String name;
    private String description;

    SpringProfiles(String name, String description) {
        this.name = name;
        this.description = description;
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
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}

