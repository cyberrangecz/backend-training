package cz.cyberrange.platform.training.api.dto;

import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Encapsulates information about abstract level. Extended by {@link AssessmentLevelDTO}, {@link
 * TrainingLevelDTO}, {@link AccessLevelDTO} and {@link InfoLevelDTO}
 */
@Schema(
    description = "One level of a training definition, in full detail.",
    subTypes = {
      TrainingLevelDTO.class,
      AccessLevelDTO.class,
      InfoLevelDTO.class,
      AssessmentLevelDTO.class
    })
public abstract class AbstractLevelDTO extends AbstractLevelBasicDTO {}
