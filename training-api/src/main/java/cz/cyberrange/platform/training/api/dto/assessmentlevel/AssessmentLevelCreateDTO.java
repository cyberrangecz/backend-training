package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An assessment level of a training definition being created. Carries the {@code ASSESSMENT_LEVEL}
 * discriminator that lets {@link AbstractLevelCreateDTO} resolve this subtype during
 * deserialization.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The questions, instructions and type stored on an assessment level.")
public class AssessmentLevelCreateDTO extends AssessmentLevelContentDTO
    implements AbstractLevelCreateDTO {}
