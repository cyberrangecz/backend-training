package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Copies training definitions and their nested levels, hints, attachments and assessment questions
 * into new detached entities, each leaving its identifier and owning association unset for the
 * caller to assign
 */
@Mapper(
    componentModel = "spring",
    uses = {},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CloneMapper {

  /**
   * Copies a training definition, resetting its state to {@code UNRELEASED}, clearing its authors,
   * and leaving its identifier and beta testing group unset.
   *
   * @param entity the training definition to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "state", constant = "UNRELEASED")
  @Mapping(target = "authors", expression = "java(new java.util.HashSet<>())")
  @Mapping(target = "betaTestingGroup", ignore = true)
  TrainingDefinition clone(TrainingDefinition entity);

  /**
   * Copies an info level, leaving its identifier and training definition unset for the caller to
   * assign.
   *
   * @param entity the info level to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingDefinition", ignore = true)
  InfoLevel clone(InfoLevel entity);

  /**
   * Copies an access level, leaving its identifier and training definition unset for the caller to
   * assign.
   *
   * @param entity the access level to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingDefinition", ignore = true)
  AccessLevel clone(AccessLevel entity);

  /**
   * Copies a training level, leaving its identifier, training definition, hints, and attachments
   * unset for the caller to assign.
   *
   * @param entity the training level to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingDefinition", ignore = true)
  @Mapping(target = "hints", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  TrainingLevel clone(TrainingLevel entity);

  /**
   * Copies a hint, leaving its identifier and training level unset.
   *
   * @param entity the hint to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingLevel", ignore = true)
  Hint clone(Hint entity);

  Set<Hint> cloneHints(Set<Hint> entity);

  /**
   * Copies an attachment, leaving its identifier and training level unset.
   *
   * @param entity the attachment to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingLevel", ignore = true)
  Attachment clone(Attachment entity);

  Set<Attachment> cloneAttachments(Set<Attachment> entity);

  /**
   * Copies an assessment level, leaving its identifier, training definition, and questions unset
   * for the caller to assign.
   *
   * @param entity the assessment level to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingDefinition", ignore = true)
  @Mapping(target = "questions", ignore = true)
  AssessmentLevel clone(AssessmentLevel entity);

  /**
   * Copies a question, leaving its identifier, assessment level, choices, extended matching
   * statements, and extended matching options unset for the caller to assign.
   *
   * @param entity the question to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "assessmentLevel", ignore = true)
  @Mapping(target = "choices", ignore = true)
  @Mapping(target = "extendedMatchingStatements", ignore = true)
  @Mapping(target = "extendedMatchingOptions", ignore = true)
  Question clone(Question entity);

  /**
   * Copies a question choice, leaving its identifier and question unset for the caller to assign.
   *
   * @param entity the question choice to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  QuestionChoice clone(QuestionChoice entity);

  List<QuestionChoice> cloneChoices(List<QuestionChoice> entities);

  /**
   * Copies an extended matching statement, leaving its identifier, question, and extended matching
   * option unset for the caller to assign.
   *
   * @param entity the extended matching statement to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  @Mapping(target = "extendedMatchingOption", ignore = true)
  ExtendedMatchingStatement clone(ExtendedMatchingStatement entity);

  List<ExtendedMatchingStatement> cloneExtendedMatchingStatements(
      List<ExtendedMatchingStatement> entities);

  /**
   * Copies an extended matching option, leaving its identifier and question unset for the caller to
   * assign.
   *
   * @param entity the extended matching option to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  ExtendedMatchingOption clone(ExtendedMatchingOption entity);

  List<ExtendedMatchingOption> cloneExtendedMatchingOptions(List<ExtendedMatchingOption> entities);
}
