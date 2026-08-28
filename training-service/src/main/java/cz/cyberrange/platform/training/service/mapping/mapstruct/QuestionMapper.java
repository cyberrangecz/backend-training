package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.ExtendedMatchingStatementPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.QuestionChoicePreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.QuestionPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingStatementDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionBasicDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionChoiceDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Converts an assessment question and its extended matching statements, options and choices between
 * entity form and their various DTOs: the full editing DTO, the id-stripped export DTO and the
 * trainee-facing preview DTO
 */
@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper extends ParentMapper {

  /**
   * Maps a question into a new entity, copying id, question type, text, order, points, penalty,
   * answer-required flag, choices, extended matching statements and extended matching options. The
   * owning assessment level carries no matching source field and is left unset.
   *
   * @param dto the question to map
   * @return the mapped question entity
   */
  Question mapToEntity(QuestionDTO dto);

  /**
   * Resolves each mapped extended matching statement's correct option by indexing the target
   * question's mapped options at the statement's {@code correctOptionOrder}, and sets it on the
   * target statement found at the same statement's {@code order}. Nothing checks that either order
   * value stays within the mapped lists' bounds.
   *
   * @param target the question whose statements and options have already been mapped
   * @param questionDTO the source question carrying the correct option orders
   */
  @AfterMapping
  default void setCorrectOption(@MappingTarget Question target, @Context QuestionDTO questionDTO) {
    questionDTO
        .getExtendedMatchingStatements()
        .forEach(
            statementDTO -> {
              ExtendedMatchingOption correctOption =
                  target.getExtendedMatchingOptions().get(statementDTO.getCorrectOptionOrder());
              target
                  .getExtendedMatchingStatements()
                  .get(statementDTO.getOrder())
                  .setExtendedMatchingOption(correctOption);
            });
  }

  /**
   * Maps a question choice into a new entity, copying id, text, correct flag and order. The owning
   * question carries no matching source field and is left unset.
   *
   * @param dto the question choice to map
   * @return the mapped question choice entity
   */
  QuestionChoice mapToEntity(QuestionChoiceDTO dto);

  /**
   * Maps an extended matching statement into a new entity, copying id, text and order. The
   * statement's chosen option is resolved separately by {@link #setCorrectOption}; the owning
   * question carries no matching source field. Both are left unset here.
   *
   * @param dto the extended matching statement to map
   * @return the mapped extended matching statement entity
   */
  ExtendedMatchingStatement mapToEntity(ExtendedMatchingStatementDTO dto);

  /**
   * Maps an extended matching option into a new entity, copying id, text and order. The owning
   * question carries no matching source field and is left unset.
   *
   * @param dto the extended matching option to map
   * @return the mapped extended matching option entity
   */
  ExtendedMatchingOption mapToEntity(ExtendedMatchingOptionDTO dto);

  /**
   * Maps a question to its DTO for an exported training level, copying question type, text, order,
   * points, penalty and answer-required flag, and mapping choices, extended matching statements and
   * extended matching options with each of their own ids left unset. The question's own id is
   * likewise left unset.
   *
   * @param entity the question to map
   * @return the exported question, with no ids anywhere in it
   */
  @Named("questionWithoutId")
  @Mapping(target = "id", ignore = true)
  @Mapping(source = "choices", target = "choices", qualifiedByName = "questionChoiceWithoutId")
  @Mapping(
      source = "extendedMatchingStatements",
      target = "extendedMatchingStatements",
      qualifiedByName = "extendedMatchingStatementWithoutId")
  @Mapping(
      source = "extendedMatchingOptions",
      target = "extendedMatchingOptions",
      qualifiedByName = "extendedMatchingOptionWithoutId")
  QuestionDTO mapToQuestionDTOWithoutId(Question entity);

  /**
   * Maps a question to the DTO carrying only its id, type, points, penalty, order and
   * answer-required flag, without its text, choices or extended matching content.
   *
   * @param entity the question to map
   * @return the mapped basic DTO
   */
  QuestionBasicDTO mapToBasicDTO(Question entity);

  /**
   * Maps a question to the DTO shown to a trainee attempting it, copying id, question type, text,
   * order, answer-required flag, choices, extended matching options and extended matching
   * statements. The trainee's own answers carry no matching source field and are left unset for the
   * caller to fill in from the recorded submissions.
   *
   * @param entity the question to map
   * @return the mapped preview DTO
   */
  QuestionPreviewDTO mapToQuestionPreviewDTO(Question entity);

  /**
   * Maps a question choice to its DTO, copying text, correct flag and order, and leaving the id
   * unset.
   *
   * @param entity the question choice to map
   * @return the mapped DTO, with no id
   */
  @Named("questionChoiceWithoutId")
  @Mapping(target = "id", ignore = true)
  QuestionChoiceDTO mapToQuestionChoiceDTOWithoutId(QuestionChoice entity);

  /**
   * Maps a question choice to its DTO, copying id, text, correct flag and order.
   *
   * @param entity the question choice to map
   * @return the mapped DTO
   */
  QuestionChoiceDTO mapToQuestionChoiceDTO(QuestionChoice entity);

  /**
   * Maps a question choice to the DTO shown to a trainee, copying id, text and order and omitting
   * the correct flag.
   *
   * @param entity the question choice to map
   * @return the mapped preview DTO
   */
  QuestionChoicePreviewDTO mapToQuestionChoicePreviewDTO(QuestionChoice entity);

  /**
   * Maps an extended matching statement to its DTO, copying text and order, resolving {@code
   * correctOptionOrder} from the chosen option's own order via {@link
   * #mapToOptionOrder(ExtendedMatchingOption)}, and leaving the id unset.
   *
   * @param entity the extended matching statement to map
   * @return the mapped DTO, with no id
   */
  @Named("extendedMatchingStatementWithoutId")
  @Mapping(target = "id", ignore = true)
  @Mapping(source = "extendedMatchingOption", target = "correctOptionOrder")
  ExtendedMatchingStatementDTO mapToExtendedMatchingStatementDTOWithoutId(
      ExtendedMatchingStatement entity);

  /**
   * Maps an extended matching statement to its DTO, copying id, text and order, and resolving
   * {@code correctOptionOrder} from the chosen option's own order via {@link
   * #mapToOptionOrder(ExtendedMatchingOption)}.
   *
   * @param entity the extended matching statement to map
   * @return the mapped DTO
   */
  @Mapping(source = "extendedMatchingOption", target = "correctOptionOrder")
  ExtendedMatchingStatementDTO mapToExtendedMatchingStatementDTO(ExtendedMatchingStatement entity);

  /**
   * Maps an extended matching statement to the DTO shown to a trainee, copying id, text and order.
   * The trainee's chosen option order carries no matching source field and is left unset for the
   * caller to fill in from the recorded submission.
   *
   * @param entity the extended matching statement to map
   * @return the mapped preview DTO
   */
  ExtendedMatchingStatementPreviewDTO mapToExtendedMatchingStatementPreviewDTO(
      ExtendedMatchingStatement entity);

  /**
   * Maps an extended matching option to its DTO, copying text and order, and leaving the id unset.
   *
   * @param entity the extended matching option to map
   * @return the mapped DTO, with no id
   */
  @Named("extendedMatchingOptionWithoutId")
  @Mapping(target = "id", ignore = true)
  ExtendedMatchingOptionDTO mapToExtendedMatchingOptionDTOWithoutId(ExtendedMatchingOption entity);

  /**
   * Maps an extended matching option to its DTO, copying id, text and order.
   *
   * @param entity the extended matching option to map
   * @return the mapped DTO
   */
  ExtendedMatchingOptionDTO mapToExtendedMatchingOptionDTO(ExtendedMatchingOption entity);

  /**
   * Reads the order of an extended matching option.
   *
   * @param entity the option to read, or null
   * @return the option's order, or null when the argument is null
   */
  default Integer mapToOptionOrder(ExtendedMatchingOption entity) {
    if (entity == null) {
      return null;
    } else {
      return entity.getOrder();
    }
  }

  /**
   * Converts the persistence question type to its API counterpart by matching constant name.
   *
   * @param questionType the question type to convert
   * @return the matching API question type
   */
  QuestionType mapToApiType(
      cz.cyberrange.platform.training.persistence.model.enums.QuestionType questionType);
}
