package cz.cyberrange.platform.training.service.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** This class is a representation of a logged command retrieved by the openSearch search api */
@Data
public class OpenSearchCommand {
  private String hostname;
  private String ip;

  @JsonProperty(value = "timestamp_str")
  private String timestampStr;

  @JsonProperty(value = "sandbox_id")
  private String sandboxId;

  private String cmd;

  @JsonProperty(value = "pool_id")
  private Long poolId;

  private String wd;

  @JsonProperty(value = "cmd_type")
  private String cmdType;

  private String username;
}
