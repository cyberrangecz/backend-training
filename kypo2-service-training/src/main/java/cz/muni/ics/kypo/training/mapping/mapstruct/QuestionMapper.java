package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingStatementDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionChoiceDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingOption;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionChoice;
import org.mapstruct.*;

/**
 * The InfoLevelMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type InfoLevelMapper and
 * DTOs classes. Code is generated during compile time.
 *
 */
@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper extends ParentMapper {
    Question mapToEntity(QuestionDTO dto);
    @AfterMapping
    default void setCorrectOption(@MappingTarget Question target, @Context QuestionDTO questionDTO) {
        questionDTO.getExtendedMatchingStatements().forEach(statementDTO -> {
            ExtendedMatchingOption correctOption = target.getExtendedMatchingOptions().get(statementDTO.getCorrectOptionOrder());
            target.getExtendedMatchingStatements().get(statementDTO.getOrder()).setExtendedMatchingOption(correctOption);
        });
    }
    QuestionChoice mapToEntity(QuestionChoiceDTO dto);
    ExtendedMatchingStatement mapToEntity(ExtendedMatchingStatementDTO dto);
    ExtendedMatchingOption mapToEntity(ExtendedMatchingOptionDTO dto);

    @Named("questionWithoutId")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "choices", target = "choices", qualifiedByName = "questionChoiceWithoutId")
    @Mapping(source = "extendedMatchingStatements", target = "extendedMatchingStatements", qualifiedByName = "extendedMatchingStatementWithoutId")
    @Mapping(source = "extendedMatchingOptions", target = "extendedMatchingOptions", qualifiedByName = "extendedMatchingOptionWithoutId")
    QuestionDTO mapToQuestionDTOWithoutId(Question entity);
    QuestionDTO mapToQuestionDTO(Question entity);

    @Named("questionChoiceWithoutId")
    @Mapping(target = "id", ignore = true)
    QuestionChoiceDTO mapToQuestionChoiceDTOWithoutId(QuestionChoice entity);
    QuestionChoiceDTO mapToQuestionChoiceDTO(QuestionChoice entity);

    @Named("extendedMatchingStatementWithoutId")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "extendedMatchingOption", target = "correctOptionOrder")
    ExtendedMatchingStatementDTO mapToExtendedMatchingStatementDTOWithoutId(ExtendedMatchingStatement entity);
    @Mapping(source = "extendedMatchingOption", target = "correctOptionOrder")
    ExtendedMatchingStatementDTO mapToExtendedMatchingStatementDTO(ExtendedMatchingStatement entity);

    @Named("extendedMatchingOptionWithoutId")
    @Mapping(target = "id", ignore = true)
    ExtendedMatchingOptionDTO mapToExtendedMatchingOptionDTOWithoutId(ExtendedMatchingOption entity);
    ExtendedMatchingOptionDTO mapToExtendedMatchingOptionDTO(ExtendedMatchingOption entity);

    default Integer mapToOptionOrder(ExtendedMatchingOption entity) {
        if (entity == null) {
            return null;
        } else {
            return entity.getOrder();
        }
    }
}
