package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelViewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentQuestionsSource;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.AssessmentLevelPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.dto.export.AbstractLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.export.AccessLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.export.AssessmentLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.export.InfoLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.export.TrainingLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AccessLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.TrainingLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelPreviewDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelViewDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.ExpectedCommand;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

/**
 * The InfoLevelMapper is an utility class to map items into data transfer objects. It provides the
 * implementation of mappings between Java bean type InfoLevelMapper and DTOs classes. Code is
 * generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {
      EnumMapper.class,
      HintMapper.class,
      AttachmentMapper.class,
      QuestionMapper.class,
      MitreTechniqueMapper.class
    },
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LevelMapper extends ParentMapper {
  // INFO LEVEL

  /**
   * Maps an imported info level into a new entity, copying title, order, estimated duration,
   * minimal possible solve time, and content. Score, primary key, and training definition
   * association carry no matching source field and are left unset.
   *
   * @param dto the imported info level to map
   * @return the mapped info level entity
   */
  InfoLevel mapImportToEntity(InfoLevelImportDTO dto);

  /**
   * Maps an info create payload into a new entity.
   *
   * @param dto the payload to map
   * @return the mapped info level
   */
  InfoLevel mapCreateToEntity(InfoLevelCreateDTO dto);

  /**
   * Maps an info update payload into a new entity carrying the payload's id.
   *
   * @param dto the payload to map
   * @return the mapped info level
   */
  InfoLevel mapUpdateToEntity(InfoLevelUpdateDTO dto);

  @Mapping(target = "levelType", constant = "INFO_LEVEL")
  InfoLevelDTO mapToInfoLevelDTO(InfoLevel entity);

  @Mapping(target = "levelType", constant = "INFO_LEVEL")
  InfoLevelBasicDTO mapToInfoLevelBasicDTO(InfoLevel entity);

  /**
   * Maps an info level to its export DTO, labelled with the info level type, copying title, order,
   * estimated duration, minimal possible solve time, and content.
   *
   * @param entity the info level to map
   * @return the exported info level
   */
  @Mapping(target = "levelType", constant = "INFO_LEVEL")
  InfoLevelExportDTO mapToExportInfoLevelDTO(InfoLevel entity);

  // ASSESSMENT LEVEL

  /**
   * Maps an assessment level create payload into a new entity, converting its {@code type} field
   * into the entity's assessment type.
   *
   * @param dto the payload to map
   * @return the mapped assessment level
   */
  @Mapping(source = "type", target = "assessmentType")
  AssessmentLevel mapCreateToEntity(AssessmentLevelCreateDTO dto);

  /**
   * Maps an assessment level update payload into a new entity carrying the payload's id, converting
   * its {@code type} field into the entity's assessment type.
   *
   * @param dto the payload to map
   * @return the mapped assessment level
   */
  @Mapping(source = "type", target = "assessmentType")
  AssessmentLevel mapUpdateToEntity(AssessmentLevelUpdateDTO dto);

  /**
   * Maps an imported assessment level into a new entity, copying title, order, estimated duration,
   * minimal possible solve time, instructions, assessment type, and questions. Score, primary key,
   * and training definition association carry no matching source field and are left unset.
   *
   * @param dto the imported assessment level to map
   * @return the mapped assessment level entity
   */
  AssessmentLevel mapImportToEntity(AssessmentLevelImportDTO dto);

  /**
   * Binds the statements of a mapped TEST assessment level to the options their question payloads
   * name as correct, pairing the payload questions with the level's questions in ascending question
   * order. A level of another assessment type is left as mapped.
   *
   * @param assessmentLevel the mapped assessment level, bound in place
   * @param source the payload the level was mapped from
   */
  @AfterMapping
  default void bindCorrectOptions(
      @MappingTarget AssessmentLevel assessmentLevel, AssessmentQuestionsSource source) {
    if (assessmentLevel.getAssessmentType()
            != cz.cyberrange.platform.training.persistence.model.enums.AssessmentType.TEST
        || source.getQuestions() == null) {
      return;
    }
    List<QuestionDTO> questionDTOs =
        source.getQuestions().stream()
            .sorted(Comparator.comparingInt(QuestionDTO::getOrder))
            .toList();
    for (int questionIndex = 0; questionIndex < questionDTOs.size(); questionIndex++) {
      if (questionDTOs.get(questionIndex).getQuestionType() == QuestionType.EMI) {
        bindCorrectOptions(
            assessmentLevel.getQuestions().get(questionIndex), questionDTOs.get(questionIndex));
      }
    }
  }

  /**
   * Sets on each extended matching statement of the question the option its payload names as
   * correct, taking the statement and the option at the list position of the payload's statement
   * and option index. A statement naming no correct option, or an index outside the question's
   * options, is left unbound.
   *
   * @param question the mapped question, bound in place
   * @param questionDTO the payload the question was mapped from
   */
  private void bindCorrectOptions(Question question, QuestionDTO questionDTO) {
    List<ExtendedMatchingOption> options = question.getExtendedMatchingOptions();
    for (int statementIndex = 0;
        statementIndex < questionDTO.getExtendedMatchingStatements().size();
        statementIndex++) {
      Integer correctOptionIndex =
          questionDTO.getExtendedMatchingStatements().get(statementIndex).getCorrectOptionOrder();
      if (correctOptionIndex == null
          || correctOptionIndex < 0
          || correctOptionIndex >= options.size()) {
        continue;
      }
      question
          .getExtendedMatchingStatements()
          .get(statementIndex)
          .setExtendedMatchingOption(options.get(correctOptionIndex));
    }
  }

  @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
  AssessmentLevelDTO mapToAssessmentLevelDTO(AssessmentLevel entity);

  @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
  AssessmentLevelBasicDTO mapToAssessmentLevelBasicDTO(AssessmentLevel entity);

  @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
  AssessmentLevelPreviewDTO mapToAssessmentLevelPreviewDTO(AssessmentLevel entity);

  /**
   * Maps an assessment level to its export DTO, labelled with the assessment level type, copying
   * title, order, estimated duration, minimal possible solve time, instructions, and assessment
   * type. Each question is mapped with its own id, and its nested choices', statements', and
   * options' ids, left unset.
   *
   * @param entity the assessment level to map
   * @return the exported assessment level
   */
  @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
  @Mapping(source = "questions", target = "questions", qualifiedByName = "questionWithoutId")
  AssessmentLevelExportDTO mapToExportAssessmentLevelDTO(AssessmentLevel entity);

  AssessmentType mapToApiType(
      cz.cyberrange.platform.training.persistence.model.enums.AssessmentType assessmentType);

  // TRAINING LEVEL

  /**
   * Maps a training level create payload into a new entity.
   *
   * @param dto the payload to map
   * @return the mapped training level
   */
  TrainingLevel mapCreateToEntity(TrainingLevelCreateDTO dto);

  /**
   * Maps a training level update payload into a new entity carrying the payload's id.
   *
   * @param dto the payload to map
   * @return the mapped training level
   */
  TrainingLevel mapUpdateToEntity(TrainingLevelUpdateDTO dto);

  /**
   * Reduces a blank answer and a blank answer variable name of a mapped training level to {@code
   * null}, so that an empty submission reads as an absent one.
   *
   * @param trainingLevel the mapped training level, normalized in place
   */
  @AfterMapping
  default void clearBlankAnswers(@MappingTarget TrainingLevel trainingLevel) {
    if (StringUtils.isBlank(trainingLevel.getAnswer())) {
      trainingLevel.setAnswer(null);
    }
    if (StringUtils.isBlank(trainingLevel.getAnswerVariableName())) {
      trainingLevel.setAnswerVariableName(null);
    }
  }

  /**
   * Maps an imported training level into a new entity, copying title, order, estimated duration,
   * minimal possible solve time, score, answer, answer variable name, content, solution, solution
   * penalty flag, incorrect answer limit, variant answers flag, commands-required flag, hints,
   * attachments, MITRE techniques, and expected commands. Primary key and training definition
   * association carry no matching source field and are left unset.
   *
   * @param dto the imported training level to map
   * @return the mapped training level entity
   */
  TrainingLevel mapImportToEntity(TrainingLevelImportDTO dto);

  @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
  TrainingLevelDTO mapToTrainingLevelDTO(TrainingLevel entity);

  @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
  @Mapping(source = "hints", target = "hints", qualifiedByName = "hintsToBasicDtoSet")
  TrainingLevelBasicDTO mapToTrainingLevelBasicDTO(TrainingLevel entity);

  /**
   * Maps a training level to its export DTO, labelled with the training level type, copying title,
   * order, estimated duration, minimal possible solve time, score, answer, answer variable name,
   * content, solution, solution penalty flag, incorrect answer limit, variant answers flag,
   * commands-required flag, hints, attachments, and expected commands. Each MITRE technique is
   * mapped with its id left unset.
   *
   * @param entity the training level to map
   * @return the exported training level
   */
  @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
  @Mapping(source = "mitreTechniques", target = "mitreTechniques", qualifiedByName = "ignoreIds")
  TrainingLevelExportDTO mapToExportTrainingLevelDTO(TrainingLevel entity);

  @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
  TrainingLevelViewDTO mapToViewDTO(TrainingLevel entity);

  @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
  @Mapping(target = "hints", ignore = true)
  TrainingLevelPreviewDTO mapToPreviewDTO(TrainingLevel entity);

  default String mapExpectedCommandToString(ExpectedCommand entity) {
    return entity.getCommand();
  }

  /**
   * Wraps a command string into a new expected command, leaving its identifier unset.
   *
   * @param command the command text to wrap
   * @return the resulting expected command
   */
  default ExpectedCommand mapStringToExpectedCommand(String command) {
    ExpectedCommand expectedCommand = new ExpectedCommand();
    expectedCommand.setCommand(command);
    return expectedCommand;
  }

  // ACCESS LEVEL

  /**
   * Maps an access create payload into a new entity.
   *
   * @param dto the payload to map
   * @return the mapped access level
   */
  AccessLevel mapCreateToEntity(AccessLevelCreateDTO dto);

  /**
   * Maps an access update payload into a new entity carrying the payload's id.
   *
   * @param dto the payload to map
   * @return the mapped access level
   */
  AccessLevel mapUpdateToEntity(AccessLevelUpdateDTO dto);

  /**
   * Maps an imported access level into a new entity, copying title, order, estimated duration,
   * minimal possible solve time, passkey, cloud content, and local content. Score, primary key, and
   * training definition association carry no matching source field and are left unset.
   *
   * @param dto the imported access level to map
   * @return the mapped access level entity
   */
  AccessLevel mapImportToEntity(AccessLevelImportDTO dto);

  @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
  AccessLevelDTO mapToAccessLevelDTO(AccessLevel entity);

  @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
  AccessLevelBasicDTO mapToAccessLevelBasicDTO(AccessLevel entity);

  /**
   * Maps an access level to its export DTO, labelled with the access level type, copying title,
   * order, estimated duration, minimal possible solve time, passkey, cloud content, and local
   * content.
   *
   * @param entity the access level to map
   * @return the exported access level
   */
  @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
  AccessLevelExportDTO mapToExportAccessLevelDTO(AccessLevel entity);

  @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
  AccessLevelViewDTO mapToViewDTO(AccessLevel entity);

  // ABSTRACT

  /**
   * Maps a level entity to the full {@link AbstractLevelDTO} subtype matching its concrete type.
   *
   * @param entity the level entity to map
   * @return the DTO of the subtype corresponding to the entity's concrete type
   */
  @SubclassMapping(source = TrainingLevel.class, target = TrainingLevelDTO.class)
  @SubclassMapping(source = InfoLevel.class, target = InfoLevelDTO.class)
  @SubclassMapping(source = AssessmentLevel.class, target = AssessmentLevelDTO.class)
  @SubclassMapping(source = AccessLevel.class, target = AccessLevelDTO.class)
  AbstractLevelDTO mapToDTO(AbstractLevel entity);

  /**
   * Maps a level entity to the {@link AbstractLevelBasicDTO} subtype matching its concrete type.
   *
   * @param entity the level entity to map
   * @return the basic DTO of the subtype corresponding to the entity's concrete type
   */
  @SubclassMapping(source = TrainingLevel.class, target = TrainingLevelBasicDTO.class)
  @SubclassMapping(source = InfoLevel.class, target = InfoLevelBasicDTO.class)
  @SubclassMapping(source = AssessmentLevel.class, target = AssessmentLevelBasicDTO.class)
  @SubclassMapping(source = AccessLevel.class, target = AccessLevelBasicDTO.class)
  AbstractLevelBasicDTO mapToBasicDTO(AbstractLevel entity);

  /**
   * Maps a level entity to the {@link AbstractLevelExportDTO} subtype matching its concrete type.
   *
   * @param entity the level entity to map
   * @return the export DTO of the subtype corresponding to the entity's concrete type
   */
  @SubclassMapping(source = TrainingLevel.class, target = TrainingLevelExportDTO.class)
  @SubclassMapping(source = InfoLevel.class, target = InfoLevelExportDTO.class)
  @SubclassMapping(source = AssessmentLevel.class, target = AssessmentLevelExportDTO.class)
  @SubclassMapping(source = AccessLevel.class, target = AccessLevelExportDTO.class)
  AbstractLevelExportDTO mapToExportDTO(AbstractLevel entity);

  /**
   * Maps a level create payload to a new entity of the level type matching the payload's concrete
   * type, through the mapping of that type.
   *
   * @param dto the level create payload to map
   * @return the entity of the level type corresponding to the payload's concrete type
   */
  @SubclassMapping(source = TrainingLevelCreateDTO.class, target = TrainingLevel.class)
  @SubclassMapping(source = InfoLevelCreateDTO.class, target = InfoLevel.class)
  @SubclassMapping(source = AssessmentLevelCreateDTO.class, target = AssessmentLevel.class)
  @SubclassMapping(source = AccessLevelCreateDTO.class, target = AccessLevel.class)
  AbstractLevel mapCreateToEntity(AbstractLevelCreateDTO dto);

  /**
   * Maps an imported level to a new entity of the level type matching the import's concrete type,
   * through the mapping of that type.
   *
   * @param dto the imported level to map
   * @return the entity of the level type corresponding to the import's concrete type
   */
  @SubclassMapping(source = TrainingLevelImportDTO.class, target = TrainingLevel.class)
  @SubclassMapping(source = InfoLevelImportDTO.class, target = InfoLevel.class)
  @SubclassMapping(source = AssessmentLevelImportDTO.class, target = AssessmentLevel.class)
  @SubclassMapping(source = AccessLevelImportDTO.class, target = AccessLevel.class)
  AbstractLevel mapImportToEntity(AbstractLevelImportDTO dto);

  /**
   * Maps a level update to a new entity of the level type matching the update's concrete type,
   * through the mapping of that type.
   *
   * @param dto the level update to map
   * @return the entity of the level type corresponding to the update's concrete type
   */
  @SubclassMapping(source = TrainingLevelUpdateDTO.class, target = TrainingLevel.class)
  @SubclassMapping(source = InfoLevelUpdateDTO.class, target = InfoLevel.class)
  @SubclassMapping(source = AssessmentLevelUpdateDTO.class, target = AssessmentLevel.class)
  @SubclassMapping(source = AccessLevelUpdateDTO.class, target = AccessLevel.class)
  AbstractLevel mapUpdateToEntity(AbstractLevelUpdateDTO dto);

  /**
   * Maps level entities to their basic DTOs, preserving the order of the input.
   *
   * @param entities the level entities to map
   * @return the basic DTOs, each of the subtype corresponding to its entity's concrete type
   */
  default List<AbstractLevelBasicDTO> mapToBasicDtoList(List<AbstractLevel> entities) {
    return entities.stream().map(this::mapToBasicDTO).collect(Collectors.toList());
  }
}
