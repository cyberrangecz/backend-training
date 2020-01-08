package cz.muni.ics.kypo.training.api.dto.snapshothook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about Snapshot hook.
 *
 */
@ApiModel(value = "SnapshotHookDTO", description = ".")
public class SnapshotHookDTO {

    @ApiModelProperty(value = "Main identifier of snapshotHook.", example = "8")
    private Long id;
    @ApiModelProperty(value = "Content of snapshot. What should be executed to get to particular state.", example = "Snapshot 1")
    private String snapshot;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets snapshot.
     *
     * @return the snapshot
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets snapshot.
     *
     * @param snapshot the snapshot
     */
    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String toString() {
        return "SnapshotHookDTO{" + "id=" + id + ", snapshot=" + snapshot + '}';
    }
}
