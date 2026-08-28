package cz.cyberrange.platform.training.api.dto.event;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Training run started event, carrying the {@code type} value {@code training_run_started} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingRunStartedDTO", description = "Training run started event")
public class TrainingRunStartedDTO extends TrainingEventDTO {}
