package cz.muni.ics.kypo.training.api.dto.snapshothook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "SnapshotHookDTO", description = ".")
public class SnapshotHookDTO {

    private Long id;
    private String snapshot;

    @ApiModelProperty(value = "Main identifier of snapshotHook.", example = "8")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Content of snapshot. What should be executed to get to particular state.", example = "Snapshot 1")
    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String toString() {
        return "SnapshotHookDTO{" + "id=" + id + ", snapshot=" + snapshot + '}';
    }
}
