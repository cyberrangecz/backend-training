package cz.cyberrange.platform.training.api.dto.snapshothook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about Snapshot hook.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "SnapshotHookDTO", description = ".")
public class SnapshotHookDTO {

    @ApiModelProperty(value = "Main identifier of snapshotHook.", example = "8")
    private Long id;
    @ApiModelProperty(value = "Content of snapshot. What should be executed to get to particular state.", example = "Snapshot 1")
    private String snapshot;
}
