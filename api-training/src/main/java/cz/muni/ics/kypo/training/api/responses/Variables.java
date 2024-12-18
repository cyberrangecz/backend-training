package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

@Data
public class Variables {
    @ApiModelProperty(value = "Variables associated with sandbox definition of the pool", example = "['secret', 'port']")
    @JsonProperty("variables")
    private Set<String> variables;
}
