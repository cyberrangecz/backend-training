package cz.cyberrange.platform.training.api.dto.event;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingRunResumedDTO", description = "Training run resumed event")
public class TrainingRunResumedDTO extends TrainingEventDTO {}
