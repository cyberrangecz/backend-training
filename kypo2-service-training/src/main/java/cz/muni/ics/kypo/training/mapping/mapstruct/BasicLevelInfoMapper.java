package cz.muni.ics.kypo.training.mapping.mapstruct;

/**
 * @author Roman Oravec
 */

import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BasicLevelInfoMapper {

    BasicLevelInfoDTO mapTo(AssessmentLevel assesmentLevel);

    BasicLevelInfoDTO mapTo(InfoLevel infoLevel);

    BasicLevelInfoDTO mapTo(GameLevel gameLevel);

}
