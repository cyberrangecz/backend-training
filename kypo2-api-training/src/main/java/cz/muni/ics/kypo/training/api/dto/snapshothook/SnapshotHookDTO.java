package cz.muni.ics.kypo.training.api.dto.snapshothook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about Snapshot hook.
 *
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "SnapshotHookDTO", description = ".")
public class SnapshotHookDTO {

    private Long id;
    private String snapshot;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of snapshotHook.", example = "8")
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
    @ApiModelProperty(value = "Content of snapshot. What should be executed to get to particular state.", example = "Snapshot 1")
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
