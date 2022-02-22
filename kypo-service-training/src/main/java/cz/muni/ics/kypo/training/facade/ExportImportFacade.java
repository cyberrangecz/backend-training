package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.events.trainings.LevelStarted;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.archive.*;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.LevelReferenceSolutionDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.SandboxDefinitionInfo;
import cz.muni.ics.kypo.training.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.exceptions.UnprocessableEntityException;
import cz.muni.ics.kypo.training.mapping.mapstruct.ExportImportMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.ReferenceSolutionNodeMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingDefinitionMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.QuestionType;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingOption;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionAnswer;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import cz.muni.ics.kypo.training.utils.AbstractFileExtensions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Export import facade.
 */
@Service
@Transactional
public class ExportImportFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ExportImportFacade.class);
    private static final String LOGS_FOLDER = "logs";
    private static final String EVENTS_FOLDER = "training_events";
    private static final String RUNS_FOLDER = "training_runs";
    private static final String ASSESSMENTS_ANSWERS_FOLDER = "assessments_answers";

    private final ExportImportService exportImportService;
    private final TrainingDefinitionService trainingDefinitionService;
    private final SandboxApiService sandboxApiService;
    private final ElasticsearchApiService elasticsearchApiService;
    private final TrainingFeedbackApiService trainingFeedbackApiService;
    private final ExportImportMapper exportImportMapper;
    private final LevelMapper levelMapper;
    private final TrainingDefinitionMapper trainingDefinitionMapper;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Export import facade.
     *
     * @param exportImportService       the export import service
     * @param exportImportMapper        the export import mapper
     * @param levelMapper               the level mapper
     * @param trainingDefinitionService the training definition service
     * @param trainingDefinitionMapper  the training definition mapper
     * @param objectMapper              the object mapper
     */
    @Autowired
    public ExportImportFacade(ExportImportService exportImportService,
                              TrainingDefinitionService trainingDefinitionService,
                              ElasticsearchApiService elasticsearchApiService,
                              TrainingFeedbackApiService trainingFeedbackApiService,
                              SandboxApiService sandboxApiService,
                              ExportImportMapper exportImportMapper,
                              LevelMapper levelMapper,
                              TrainingDefinitionMapper trainingDefinitionMapper,
                              ObjectMapper objectMapper) {
        this.exportImportService = exportImportService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.trainingFeedbackApiService = trainingFeedbackApiService;
        this.sandboxApiService = sandboxApiService;
        this.exportImportMapper = exportImportMapper;
        this.levelMapper = levelMapper;
        this.trainingDefinitionMapper = trainingDefinitionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports Training Definition to file
     *
     * @param trainingDefinitionId the id of the definition to be exported
     * @return the file containing definition, {@link FileToReturnDTO}
     */
    @IsDesignerOrOrganizerOrAdmin
    @TransactionalRO
    public FileToReturnDTO dbExport(Long trainingDefinitionId) {
        TrainingDefinition td = exportImportService.findById(trainingDefinitionId);
        ExportTrainingDefinitionAndLevelsDTO dbExport = exportImportMapper.mapToDTO(td);
        if (dbExport != null) {
            dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(trainingDefinitionId));
        }
        try {
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(objectMapper.writeValueAsBytes(dbExport));
            if(dbExport != null && dbExport.getTitle() != null){
                fileToReturnDTO.setTitle(dbExport.getTitle());
            } else {
                fileToReturnDTO.setTitle("");
            }
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    private List<AbstractLevelExportDTO> mapAbstractLevelToAbstractLevelDTO(Long trainingDefinitionId) {
        List<AbstractLevelExportDTO> abstractLevelExportDTOs = new ArrayList<>();
        List<AbstractLevel> abstractLevels = trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinitionId);
        abstractLevels.forEach(level ->
                abstractLevelExportDTOs.add(levelMapper.mapToExportDTO(level)));
        return abstractLevelExportDTOs;
    }

    /**
     * Imports training definition.
     *
     * @param importTrainingDefinitionDTO the training definition to be imported
     * @return the {@link TrainingDefinitionByIdDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO) {
        importTrainingDefinitionDTO.setState(TDState.UNRELEASED);
        if (importTrainingDefinitionDTO.getTitle() != null && !importTrainingDefinitionDTO.getTitle().startsWith("Uploaded")) {
            importTrainingDefinitionDTO.setTitle("Uploaded " + importTrainingDefinitionDTO.getTitle());
        }

        TrainingDefinition newDefinition = exportImportMapper.mapToEntity(importTrainingDefinitionDTO);
        newDefinition.setEstimatedDuration(computeEstimatedDuration(importTrainingDefinitionDTO));
        TrainingDefinition newTrainingDefinition = trainingDefinitionService.create(newDefinition, false);
        List<AbstractLevelImportDTO> levels = importTrainingDefinitionDTO.getLevels();
        List<AbstractLevel> createdLevels = new ArrayList<>();
        for (AbstractLevelImportDTO level : levels) {
            AbstractLevel newLevel;
            if (level.getLevelType().equals(LevelType.TRAINING_LEVEL)) {
                newLevel = levelMapper.mapImportToEntity((TrainingLevelImportDTO) level);
                checkSumOfHintPenalties((TrainingLevel) newLevel);
                setAnswerAndAnswerVariableNameToNullIfBlank((TrainingLevel) newLevel);
                checkAnswerAndAnswerVariableName((TrainingLevel) newLevel);
            } else if (level.getLevelType().equals(LevelType.INFO_LEVEL)) {
                newLevel = levelMapper.mapImportToEntity((InfoLevelImportDTO) level);
            } else if (level.getLevelType().equals(LevelType.ACCESS_LEVEL)) {
                newLevel = levelMapper.mapImportToEntity((AccessLevelImportDTO) level);
            } else {
                newLevel = levelMapper.mapImportToEntity((AssessmentLevelImportDTO) level);
                if (((AssessmentLevel) newLevel).getAssessmentType() == AssessmentType.TEST) {
                    this.checkAndSetCorrectOptionsOfStatements((AssessmentLevel) newLevel, (AssessmentLevelImportDTO) level);
                    newLevel.setMaxScore(computeAssessmentLevelMaxScore((AssessmentLevel) newLevel));
                }
            }
            exportImportService.createLevel(newLevel, newTrainingDefinition);
            createdLevels.add(newLevel);
        }
        createReferenceGraph(newTrainingDefinition, createdLevels);
        return trainingDefinitionMapper.mapToDTOById(newTrainingDefinition);
    }

    private void createReferenceGraph(TrainingDefinition trainingDefinition, List<AbstractLevel> createdLevels) {
        List<LevelReferenceSolutionDTO> referenceSolution = new ArrayList<>();
        boolean isAnyReferenceSolution = false;
        for (AbstractLevel level: createdLevels) {
            if (level.getClass() == TrainingLevel.class) {
                isAnyReferenceSolution = isAnyReferenceSolution || !((TrainingLevel) level).getReferenceSolution().isEmpty();
                referenceSolution.add(createLevelReferenceSolutionDTO((TrainingLevel) level));
            }
        }
        if(isAnyReferenceSolution) {
            this.trainingFeedbackApiService.createReferenceGraph(trainingDefinition.getId(), referenceSolution);
        }
    }

    private LevelReferenceSolutionDTO createLevelReferenceSolutionDTO(TrainingLevel trainingLevel) {
        return new LevelReferenceSolutionDTO(
                trainingLevel.getId(),
                trainingLevel.getOrder(),
                new ArrayList<>(ReferenceSolutionNodeMapper.INSTANCE.mapToSetDTO(trainingLevel.getReferenceSolution()))
        );
    }

    private void setAnswerAndAnswerVariableNameToNullIfBlank(TrainingLevel trainingLevel) {
        if (StringUtils.isBlank(trainingLevel.getAnswer())) {
            trainingLevel.setAnswer(StringUtils.isBlank(trainingLevel.getAnswer()) ? null : trainingLevel.getAnswer());
        }
        if (StringUtils.isBlank(trainingLevel.getAnswerVariableName())) {
            trainingLevel.setAnswerVariableName(null);
        }
    }

    private void checkAnswerAndAnswerVariableName(TrainingLevel trainingLevel) {
        if (trainingLevel.isVariantAnswers()) {
            this.checkAnswerVariableName(trainingLevel);
        } else {
            this.checkAnswer(trainingLevel);
        }
    }

    private void checkAnswer(TrainingLevel trainingLevel) {
        if (trainingLevel.getAnswerVariableName() != null) {
            throw new BadRequestException("Field Correct Answer - Variable Name must be null.");
        }
        if (StringUtils.isBlank(trainingLevel.getAnswer())) {
            throw new BadRequestException("Field Correct Answer - Static cannot be empty.");
        }
    }

    private void checkAnswerVariableName(TrainingLevel trainingLevel) {
        if (trainingLevel.getAnswer() != null) {
            throw new BadRequestException("Field Correct Answer - Static must be null.");
        }
        if (StringUtils.isBlank(trainingLevel.getAnswerVariableName())) {
            throw new BadRequestException("Field Correct Answer - Variable name cannot be empty.");
        }
    }

    private int computeAssessmentLevelMaxScore(AssessmentLevel assessmentLevel) {
        return assessmentLevel.getQuestions()
                .stream()
                .mapToInt(Question::getPoints)
                .sum();
    }

    private void checkAndSetCorrectOptionsOfStatements(AssessmentLevel assessmentLevel, AssessmentLevelImportDTO assessmentLevelImportDTO) {
        assessmentLevelImportDTO.getQuestions().stream()
                .filter(questionDTO -> questionDTO.getQuestionType() == cz.muni.ics.kypo.training.api.enums.QuestionType.EMI)
                .forEach(questionDTO -> questionDTO.getExtendedMatchingStatements()
                        .forEach(statementDTO -> {
                            if (statementDTO.getCorrectOptionOrder() == null) {
                                throw new BadRequestException("You must set the correct option for the each statement in the assessment of the type TEST");
                            }
                            Question question = assessmentLevel.getQuestions().get(questionDTO.getOrder());
                            ExtendedMatchingOption correctOption = question.getExtendedMatchingOptions().get(statementDTO.getCorrectOptionOrder());
                            ExtendedMatchingStatement statementToUpdate = question.getExtendedMatchingStatements().get(statementDTO.getOrder());
                            statementToUpdate.setExtendedMatchingOption(correctOption);
                        }));
    }

    /**
     * Exports Training Instance to file
     *
     * @param trainingInstanceId the id of the instance to be exported
     * @return the file containing instance, {@link FileToReturnDTO}
     */
    @IsOrganizerOrAdmin
    @TransactionalRO
    public FileToReturnDTO archiveTrainingInstance(Long trainingInstanceId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            TrainingInstance trainingInstance = exportImportService.findInstanceById(trainingInstanceId);

            TrainingInstanceArchiveDTO archivedInstance = exportImportMapper.mapToDTO(trainingInstance);
            archivedInstance.setDefinitionId(trainingInstance.getTrainingDefinition().getId());
            Set<Long> organizersRefIds = trainingInstance.getOrganizers().stream()
                    .map(UserRef::getUserRefId)
                    .collect(Collectors.toSet());
            archivedInstance.setOrganizersRefIds(new HashSet<>(organizersRefIds));

            writeTrainingInstanceGeneralInfo(zos, trainingInstance.getId(), archivedInstance);
            writeTrainingDefinitionInfo(zos, trainingInstance);
            writeTrainingRunsInfo(zos, trainingInstance);
            writeSandboxDefinitionInfo(zos, trainingInstance);

            zos.closeEntry();
            zos.close();
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(baos.toByteArray());
            fileToReturnDTO.setTitle(trainingInstance.getTitle());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException("The .zip file was not created since there were some processing error.", ex);
        }
    }

    private void writeTrainingInstanceGeneralInfo(ZipOutputStream zos, Long trainingInstanceId, TrainingInstanceArchiveDTO archivedInstance) throws IOException {
        ZipEntry instanceEntry = new ZipEntry("training_instance-id" + trainingInstanceId + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(instanceEntry);
        zos.write(objectMapper.writeValueAsBytes(archivedInstance));
    }

    private void writeTrainingRunsInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        Set<TrainingRun> runs = exportImportService.findRunsByInstanceId(trainingInstance.getId());
        Map<Long, Map<Long, QuestionAnswersDetailsDTO>> assessmentsDetails = new HashMap<>();
        for (TrainingRun run : runs) {
            TrainingRunArchiveDTO archivedRun = exportImportMapper.mapToArchiveDTO(run);
            archivedRun.setInstanceId(trainingInstance.getId());
            archivedRun.setParticipantRefId(run.getParticipantRef().getUserRefId());
            ZipEntry runEntry = new ZipEntry(RUNS_FOLDER + "/training_run-id" + run.getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(runEntry);
            zos.write(objectMapper.writeValueAsBytes(archivedRun));

            writeQuestionsAnswers(zos, run, assessmentsDetails);
            List<Map<String, Object>> events = elasticsearchApiService.findAllEventsFromTrainingRun(run);
            if (events.isEmpty()) {
                continue;
            }
            Map<Integer, Long> levelStartTimestampMapping = writeEventsAndGetLevelStartTimestampMapping(zos, run, events);
            writeEventsByLevels(zos, run, events);

            List<Map<String, Object>> consoleCommands = getConsoleCommands(trainingInstance, run);
            Integer sandboxId = events.get(0).get("sandbox_id") == null ?
                    run.getParticipantRef().getUserRefId().intValue() : (Integer) events.get(0).get("sandbox_id");
            writeConsoleCommands(zos, sandboxId, consoleCommands);
            writeConsoleCommandsDetails(zos, trainingInstance, run, sandboxId, levelStartTimestampMapping);
        }
        writeAssessmentsDetails(zos, assessmentsDetails);
    }

    private List<Map<String, Object>> getConsoleCommands(TrainingInstance instance, TrainingRun run) {
        if (instance.isLocalEnvironment()) {
            return elasticsearchApiService.findAllConsoleCommandsByAccessTokenAndUserId(instance.getAccessToken(), run.getParticipantRef().getUserRefId());
        }
        Long sandboxId = run.getSandboxInstanceRefId() == null ? run.getPreviousSandboxInstanceRefId() : run.getSandboxInstanceRefId();
        return elasticsearchApiService.findAllConsoleCommandsBySandbox(sandboxId);
    }

    private void writeAssessmentsDetails(ZipOutputStream zos, Map<Long, Map<Long, QuestionAnswersDetailsDTO>> assessmentsDetails) throws IOException {
        for(Map.Entry<Long, Map<Long, QuestionAnswersDetailsDTO>> assessmentDetails: assessmentsDetails.entrySet()) {
            ZipEntry assessmentDetailsEntry = new ZipEntry(ASSESSMENTS_ANSWERS_FOLDER + "/assessment-id-" + assessmentDetails.getKey() + "-details" + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(assessmentDetailsEntry);
            zos.write(objectMapper.writer().writeValueAsBytes(assessmentDetails.getValue().values()));
        }
    }

    private Map<Integer, Long> writeEventsAndGetLevelStartTimestampMapping(ZipOutputStream zos, TrainingRun run, List<Map<String, Object>> events) throws IOException {
        ZipEntry eventsEntry = new ZipEntry(EVENTS_FOLDER + "/training_run-id" + run.getId() + "-events" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(eventsEntry);
        //Obtain start timestamp of each level, so it can be used later
        Map<Integer, Long> levelStartTimestampMapping = new LinkedHashMap<>();

        for (Map<String, Object> event : events) {
            zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(event));
            zos.write(System.lineSeparator().getBytes());
            if (event.get("type").equals(LevelStarted.class.getCanonicalName())) {
                levelStartTimestampMapping.put(((Integer) event.get("level")), (Long) event.get("timestamp"));
            }
        }
        return levelStartTimestampMapping;
    }

    private void writeEventsByLevels(ZipOutputStream zos, TrainingRun run, List<Map<String, Object>> events) throws IOException {
        Integer currentLevel = ((Integer) events.get(0).get("level"));
        ZipEntry eventsDetailEntry = new ZipEntry(EVENTS_FOLDER + "/training_run-id" + run.getId() + "-details" + "/level" + currentLevel + "-events" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(eventsDetailEntry);
        for (Map<String, Object> event : events) {
            if (!event.get("level").equals(currentLevel)) {
                currentLevel = ((Integer) event.get("level"));
                eventsDetailEntry = new ZipEntry(EVENTS_FOLDER + "/training_run-id" + run.getId() + "-details" + "/level" + currentLevel + "-events" + AbstractFileExtensions.JSON_FILE_EXTENSION);
                zos.putNextEntry(eventsDetailEntry);
            }
            zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(event));
            zos.write(System.lineSeparator().getBytes());
        }
    }

    private void writeQuestionsAnswers(ZipOutputStream zos, TrainingRun run, Map<Long, Map<Long, QuestionAnswersDetailsDTO>> assessmentsDetails) throws IOException {
        Map<Long, List<QuestionAnswer>> questionsAnswersByAssessments = exportImportService.findQuestionsAnswersOfAssessment(run.getId());
        for (Map.Entry<Long, List<QuestionAnswer>> questionsAnswersByAssessment : questionsAnswersByAssessments.entrySet()) {
            ZipEntry eventsDetailEntry = new ZipEntry(ASSESSMENTS_ANSWERS_FOLDER + "/training_run-id-" + run.getId() + "-assessments" + "/assessment-id-" + questionsAnswersByAssessment.getKey() + "-answers" + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(eventsDetailEntry);

            Map<Long, QuestionAnswersDetailsDTO> questionAnswersDetails = assessmentsDetails.getOrDefault(questionsAnswersByAssessment.getKey(), new HashMap<>());
            for(QuestionAnswer questionAnswer : questionsAnswersByAssessment.getValue()) {
                Question question = questionAnswer.getQuestion();
                if (question.getQuestionType() == QuestionType.EMI) {
                    Set<QuestionEMIAnswer> emiAnswers = objectMapper.readValue(questionAnswer.getAnswers().toString(), new TypeReference<Set<QuestionEMIAnswer>>() {});
                    questionAnswer.setAnswers(emiAnswers.stream()
                            .map(emiAnswer -> this.mapEmiAnswerToString(question, emiAnswer))
                            .collect(Collectors.toSet()));
                }
                if (!questionAnswersDetails.containsKey(question.getId())) {
                    questionAnswersDetails.put(question.getId(), new QuestionAnswersDetailsDTO(questionAnswer.getQuestion().getText()));
                }
                questionAnswersDetails.get(question.getId()).addAnswers(questionAnswer.getAnswers());
                zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(new QuestionAnswerArchiveDTO(question.getText(), questionAnswer.getAnswers())));
                zos.write(System.lineSeparator().getBytes());
            }
            assessmentsDetails.putIfAbsent(questionsAnswersByAssessment.getKey(), questionAnswersDetails);

        }
    }

    private String mapEmiAnswerToString(Question question, QuestionEMIAnswer emiAnswer) {
        return "{ statement: '" + question.getExtendedMatchingStatements().get(emiAnswer.getStatementOrder()).getText()
             + "', option: '" + question.getExtendedMatchingOptions().get(emiAnswer.getOptionOrder()).getText()+ "' }";
    }

    private void writeConsoleCommands(ZipOutputStream zos, Integer sandboxId, List<Map<String, Object>> consoleCommands) throws IOException {
        ZipEntry consoleCommandsEntry = new ZipEntry(LOGS_FOLDER + "/sandbox-" + sandboxId + "-useractions" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(consoleCommandsEntry);
        for (Map<String, Object> command : consoleCommands) {
            zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(command));
            zos.write(System.lineSeparator().getBytes());
        }
    }

    private void writeConsoleCommandsDetails(ZipOutputStream zos, TrainingInstance instance, TrainingRun run, Integer sandboxId, Map<Integer, Long> levelStartTimestampMapping) throws IOException {
        List<Long> levelTimestampRanges = new ArrayList<>(levelStartTimestampMapping.values());
        List<Integer> levelIds = new ArrayList<>(levelStartTimestampMapping.keySet());
        levelTimestampRanges.add(Long.MAX_VALUE);

        for (int i = 0; i < levelIds.size(); i++) {
            List<Map<String, Object>> consoleCommandsByLevel = getConsoleCommandsWithinTimeRange(instance, run, sandboxId, levelTimestampRanges.get(i), levelTimestampRanges.get(i+1));
            ZipEntry consoleCommandsEntryDetails = new ZipEntry(LOGS_FOLDER + "/sandbox-" + sandboxId + "-details" + "/level" + levelIds.get(i)+ "-useractions" + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(consoleCommandsEntryDetails);
            for (Map<String, Object> command : consoleCommandsByLevel) {
                zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(command));
                zos.write(System.lineSeparator().getBytes());
            }
        }
    }

    private List<Map<String, Object>> getConsoleCommandsWithinTimeRange(TrainingInstance instance, TrainingRun run, Integer sandboxId, Long from, Long to) {
        if(instance.isLocalEnvironment()) {
            return elasticsearchApiService.findAllConsoleCommandsByAccessTokenAndUserIdAndTimeRange(instance.getAccessToken(), run.getParticipantRef().getUserRefId(), from, to);
        }
        return elasticsearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(sandboxId, from, to);
    }

    private void writeTrainingDefinitionInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        ExportTrainingDefinitionAndLevelsDTO tD = exportImportMapper.mapToDTO(exportImportService.findById(trainingDefinitionId));
        if (tD != null) {
            tD.setLevels(mapAbstractLevelToAbstractLevelDTO(trainingDefinitionId));
            ZipEntry definitionEntry = new ZipEntry("training_definition-id" + trainingInstance.getTrainingDefinition().getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(definitionEntry);
            zos.write(objectMapper.writeValueAsBytes(tD));
        }
    }

    private void writeSandboxDefinitionInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        if (trainingInstance.getPoolId() != null) {
            SandboxDefinitionInfo sandboxDefinitionInfo = sandboxApiService.getSandboxDefinitionId(trainingInstance.getPoolId());
            ZipEntry sandboxDefinitionEntry = new ZipEntry("sandbox_definition-id" + sandboxDefinitionInfo.getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(sandboxDefinitionEntry);
            zos.write(objectMapper.writeValueAsBytes(sandboxDefinitionInfo));
        }
    }

    private void checkSumOfHintPenalties(TrainingLevel trainingLevel) {
        int sumHintPenalties = 0;
        for (Hint hint : trainingLevel.getHints()) {
            sumHintPenalties += hint.getHintPenalty();
        }
        if(sumHintPenalties > trainingLevel.getMaxScore()) {
            throw new UnprocessableEntityException(new EntityErrorDetail(TrainingLevel.class, "title", String.class, trainingLevel.getTitle(),
                    "Sum of hints penalties cannot be greater than maximal score of the training level."));     }
    }

    private int computeEstimatedDuration(ImportTrainingDefinitionDTO importedTrainingDefinition) {
        return importedTrainingDefinition.getLevels().stream()
                .mapToInt(AbstractLevelImportDTO::getEstimatedDuration)
                .sum();
    }
}
