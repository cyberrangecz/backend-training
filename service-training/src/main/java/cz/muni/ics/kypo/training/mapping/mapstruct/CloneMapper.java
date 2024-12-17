package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingOption;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionChoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CloneMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "UNRELEASED")
    @Mapping(target = "authors", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "betaTestingGroup", ignore = true)
    TrainingDefinition clone(TrainingDefinition entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingDefinition", ignore = true)
    InfoLevel clone(InfoLevel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingDefinition", ignore = true)
    AccessLevel clone(AccessLevel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingDefinition", ignore = true)
    @Mapping(target = "hints", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    TrainingLevel clone(TrainingLevel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingLevel", ignore = true)
    Hint clone(Hint entity);

    Set<Hint> cloneHints(Set<Hint> entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingLevel", ignore = true)
    Attachment clone(Attachment entity);

    Set<Attachment> cloneAttachments(Set<Attachment> entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingDefinition", ignore = true)
    @Mapping(target = "questions", ignore = true)
    AssessmentLevel clone(AssessmentLevel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assessmentLevel", ignore = true)
    @Mapping(target = "choices", ignore = true)
    @Mapping(target = "extendedMatchingStatements", ignore = true)
    @Mapping(target = "extendedMatchingOptions", ignore = true)
    Question clone(Question entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    QuestionChoice clone(QuestionChoice entity);

    List<QuestionChoice> cloneChoices(List<QuestionChoice> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Mapping(target = "extendedMatchingOption", ignore = true)
    ExtendedMatchingStatement clone(ExtendedMatchingStatement entity);

    List<ExtendedMatchingStatement> cloneExtendedMatchingStatements(List<ExtendedMatchingStatement> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    ExtendedMatchingOption clone(ExtendedMatchingOption entity);

    List<ExtendedMatchingOption> cloneExtendedMatchingOptions(List<ExtendedMatchingOption> entities);




}
