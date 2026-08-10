package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEMIAnswer {

  @JsonProperty("statementOrder")
  private Integer statementOrder;

  @JsonProperty("optionOrder")
  private Integer optionOrder;
}
