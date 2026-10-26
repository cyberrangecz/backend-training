package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A training level of a training definition being created. Carries the {@code TRAINING_LEVEL}
 * discriminator that lets {@link AbstractLevelCreateDTO} resolve this subtype during
 * deserialization.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(
    description = "The content stored on a training level; a field left out clears what was there.")
public class TrainingLevelCreateDTO extends TrainingLevelContentDTO
    implements AbstractLevelCreateDTO {}
