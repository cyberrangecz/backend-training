package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
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
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Copies training definitions and their levels, together with every hint, attachment and assessment
 * question they hold, into new detached entities. Each copy leaves its identifier unset, and a
 * copied level leaves its training definition unset for the caller to assign.
 */
@Mapper(
    componentModel = "spring",
    uses = {},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CloneMapper {

  /**
   * Copies a training definition, resetting its state to {@code UNRELEASED}, clearing its authors,
   * and leaving its identifier and creation time unset.
   *
   * @param entity the training definition to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "state", constant = "UNRELEASED")
  @Mapping(target = "authors", expression = "java(new java.util.HashSet<>())")
  @Mapping(target = "createdAt", ignore = true)
  TrainingDefinition clone(TrainingDefinition entity);

  /**
   * Copies a level of any type together with everything it holds, by the copy method of its
   * concrete type.
   *
   * @param level the level to copy
   * @return the copy, of the same concrete type
   * @throws IllegalArgumentException when the level is of a type no copy method exists for
   */
  default AbstractLevel cloneLevel(AbstractLevel level) {
    return switch (level) {
      case InfoLevel infoLevel -> clone(infoLevel);
      case AccessLevel accessLevel -> clone(accessLevel);
      case TrainingLevel trainingLevel -> clone(trainingLevel);
      case AssessmentLevel assessmentLevel -> clone(assessmentLevel);
      default ->
          throw new IllegalArgumentException(
              "Level type " + level.getClass().getName() + " cannot be cloned.");
    };
  }

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
   * Copies a training level together with copies of its hints and attachments, each linked to the
   * copy, sharing its MITRE techniques, and leaving its identifier and training definition unset
   * for the caller to assign.
   *
   * @param entity the training level to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingDefinition", ignore = true)
  TrainingLevel clone(TrainingLevel entity);

  /**
   * Links each copied hint and attachment of the training level copy to that copy.
   *
   * @param clone the training level copy whose hints and attachments are linked
   */
  @AfterMapping
  default void linkHintsAndAttachments(@MappingTarget TrainingLevel clone) {
    clone.getHints().forEach(hint -> hint.setTrainingLevel(clone));
    clone.getAttachments().forEach(attachment -> attachment.setTrainingLevel(clone));
  }

  /**
   * Copies a hint, leaving its identifier and training level unset.
   *
   * @param entity the hint to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingLevel", ignore = true)
  Hint clone(Hint entity);

  /**
   * Copies each of the given hints.
   *
   * @param entities the hints to copy
   * @return the copies
   */
  Set<Hint> cloneHints(Set<Hint> entities);

  /**
   * Copies an attachment, leaving its identifier and training level unset.
   *
   * @param entity the attachment to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingLevel", ignore = true)
  Attachment clone(Attachment entity);

  /**
   * Copies each of the given attachments.
   *
   * @param entities the attachments to copy
   * @return the copies
   */
  Set<Attachment> cloneAttachments(Set<Attachment> entities);

  /**
   * Copies an assessment level together with copies of its questions, each linked to the copy,
   * leaving its identifier and training definition unset for the caller to assign.
   *
   * @param entity the assessment level to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "trainingDefinition", ignore = true)
  AssessmentLevel clone(AssessmentLevel entity);

  /**
   * Copies each of the given questions.
   *
   * @param entities the questions to copy
   * @return the copies, in the order of the given questions
   */
  List<Question> cloneQuestions(List<Question> entities);

  /**
   * Copies a question together with copies of its choices, extended matching statements and
   * extended matching options, each linked to the copy, leaving its identifier and assessment level
   * unset.
   *
   * @param entity the question to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "assessmentLevel", ignore = true)
  Question clone(Question entity);

  /**
   * Links each copied extended matching statement to the copied option holding the order of the
   * option its source statement names as correct. A statement whose source names no correct option,
   * or names one no copied option matches, stays without one.
   *
   * @param source the question that was copied
   * @param clone the copy whose statements are linked
   */
  @AfterMapping
  default void linkCorrectOptions(Question source, @MappingTarget Question clone) {
    List<ExtendedMatchingStatement> sourceStatements = source.getExtendedMatchingStatements();
    for (int position = 0; position < sourceStatements.size(); position++) {
      ExtendedMatchingOption sourceOption =
          sourceStatements.get(position).getExtendedMatchingOption();
      if (sourceOption == null) {
        continue;
      }
      ExtendedMatchingStatement clonedStatement =
          clone.getExtendedMatchingStatements().get(position);
      clone.getExtendedMatchingOptions().stream()
          .filter(option -> option.getOrder() == sourceOption.getOrder())
          .findFirst()
          .ifPresent(clonedStatement::setExtendedMatchingOption);
    }
  }

  /**
   * Copies a question choice, leaving its identifier and question unset for the caller to assign.
   *
   * @param entity the question choice to copy
   * @return the copy
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  QuestionChoice clone(QuestionChoice entity);

  /**
   * Copies each of the given question choices.
   *
   * @param entities the choices to copy
   * @return the copies, in the order of the given choices
   */
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

  /**
   * Copies each of the given extended matching statements.
   *
   * @param entities the statements to copy
   * @return the copies, in the order of the given statements
   */
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

  /**
   * Copies each of the given extended matching options.
   *
   * @param entities the options to copy
   * @return the copies, in the order of the given options
   */
  List<ExtendedMatchingOption> cloneExtendedMatchingOptions(List<ExtendedMatchingOption> entities);
}
