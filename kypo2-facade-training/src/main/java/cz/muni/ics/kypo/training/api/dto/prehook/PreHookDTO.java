package cz.muni.ics.kypo.training.api.dto.prehook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "PreHookDTO", description = ".")
public class PreHookDTO {

    private Long id;

    @ApiModelProperty(value = "Main identifier of preHook.")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PreHookDTO{" + "id=" + id + '}';
    }
}
