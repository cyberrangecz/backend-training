package cz.muni.ics.kypo.training.mapping.mapstruct;

/**
 * The BasicLevelInfoMapper  is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type BasicLevelInfoMapper and
 * DTOs classes. Code is generated during compile time.
 *
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
