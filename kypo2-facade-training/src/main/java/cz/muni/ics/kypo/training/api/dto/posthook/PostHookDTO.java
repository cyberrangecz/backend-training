package cz.muni.ics.kypo.training.api.dto.posthook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "PostHookDTO", description = ".")
public class PostHookDTO {

    private Long id;

    @ApiModelProperty(value = "Main identifier of postHook.")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PostHookDTO{" + "id=" + id + '}';
    }
}
