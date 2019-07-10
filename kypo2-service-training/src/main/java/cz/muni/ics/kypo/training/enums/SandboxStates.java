package cz.muni.ics.kypo.training.enums;

/**
 * The enum Sandbox states.
 *
 * @author Pavel Seda
 */
public enum SandboxStates {

    /**
     * The Create in progress.
     */
    CREATE_IN_PROGRESS("CREATE_IN_PROGRESS", "Stack CREATE started"),
    /**
     * The Create complete.
     */
    CREATE_COMPLETE("CREATE_COMPLETE", "Stack CREATE completed successfully"),
    /**
     * The Delete in progress.
     */
    DELETE_IN_PROGRESS("DELETE_IN_PROGRESS", "Stack DELETE started"),
    /**
     * The Snapshot in progress.
     */
    SNAPSHOT_IN_PROGRESS("SNAPSHOT_IN_PROGRESS", "Stack SNAPSHOT started"),
    /**
     * The Restore in progress.
     */
    RESTORE_IN_PROGRESS("RESTORE_IN_PROGRESS", "Stack RESTORE started"),
    /**
     * The Ansible in progress.
     */
    ANSIBLE_IN_PROGRESS("ANSIBLE_IN_PROGRESS", "Ansible is running"),
    /**
     * The Ansible failed.
     */
    ANSIBLE_FAILED("ANSIBLE_FAILED", "Ansible failed, check it's output for details"),
    /**
     * The Ansible complete.
     */
    ANSIBLE_COMPLETE("ANSIBLE_COMPLETE", "Ansible successfully finished"),
    /**
     * The Full build in progress.
     */
    FULL_BUILD_IN_PROGRESS("FULL_BUILD_IN_PROGRESS", "Stack CREATE started"),
    /**
     * The Full build complete.
     */
    FULL_BUILD_COMPLETE("FULL_BUILD_COMPLETE", "Full build finished successfully"),
    /**
     * The Full build failed.
     */
    FULL_BUILD_FAILED("FULL_BUILD_FAILED", "Full build failed"),
    /**
     * The Bootstrap in progress.
     */
    BOOTSTRAP_IN_PROGRESS("BOOTSTRAP_IN_PROGRESS", "Installing necessary packages"),
    /**
     * The Bootstrap complete.
     */
    BOOTSTRAP_COMPLETE("BOOTSTRAP_COMPLETE", "All necessary packages installed");

    private String name;
    private String description;

    SandboxStates(String name, String description) {
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
