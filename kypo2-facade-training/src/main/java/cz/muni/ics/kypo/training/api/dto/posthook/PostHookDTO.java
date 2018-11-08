package cz.muni.ics.kypo.training.api.dto.posthook;

import io.swagger.annotations.ApiModel;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "PostHookDTO", description = ".")
public class PostHookDTO {

    private Long id;

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
