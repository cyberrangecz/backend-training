package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;

/**
 * Deserialization target for the variables the sandbox service lists for one pool. Only the names
 * are carried; no value accompanies them.
 */
@Data
public class Variables {
  @Schema(example = "[\"secret\", \"port\"]")
  @JsonProperty("variables")
  private Set<String> variables;
}
