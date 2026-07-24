package cz.cyberrange.platform.training.api.dto.event;

import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(value = "TrainingRunStartedDTO", description = "Training run started event")
public class TrainingRunStartedDTO extends TrainingEventDTO {}
