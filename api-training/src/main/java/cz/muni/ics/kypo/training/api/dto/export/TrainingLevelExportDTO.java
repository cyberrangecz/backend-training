package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.dto.imports.AttachmentImportDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.ReferenceSolutionNodeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelExportDTO}
 *
 */
@ApiModel(value = "TrainingLevelExportDTO", description = "Exported training level.", parent = AbstractLevelExportDTO.class)
public class TrainingLevelExportDTO extends AbstractLevelExportDTO {

    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String answer;
    @ApiModelProperty(value = "Identifier that is used to obtain answer from remote storage.", example = "username")
    private String answerVariableName;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintExportDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "How many times player can submit incorrect answer before displaying solution.", example = "5")
    private int incorrectAnswerLimit;
    @ApiModelProperty(value = "List of attachments.", example = "[]")
    private List<AttachmentImportDTO> attachments;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    private int maxScore;
    @ApiModelProperty(value = "Indicates if flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
    private boolean variantAnswers;
    @ApiModelProperty(value = "Sequence of commands that leads to the level answer.", example = "false")
    private List<ReferenceSolutionNodeDTO> referenceSolution = new ArrayList<>();
    @ApiModelProperty(value = "Set of mitre techniques used in the training level.")
    private Set<MitreTechniqueDTO> mitreTechniques;
    @ApiModelProperty(value = "Set of the expected commands to be executed during the training level.")
    private Set<String> expectedCommands;
    @ApiModelProperty(value = "Indicates if at least one command has to be executed to complete the level. Default is true.", example = "true")
    private boolean commandsRequired;


    /**
     * Instantiates a new Training level export dto.
     */
    public TrainingLevelExportDTO() {
    }

    /**
     * Gets answer.
     *
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets answer.
     *
     * @param answer the answer
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }


    /**
     * Gets answer identifier.
     *
     * @return the answer identifier
     */
    public String getAnswerVariableName() {
        return answerVariableName;
    }

    /**
     * Sets answer identifier.
     *
     * @param answerVariableName the answer identifier
     */
    public void setAnswerVariableName(String answerVariableName) {
        this.answerVariableName = answerVariableName;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets solution.
     *
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets solution.
     *
     * @param solution the solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * Is solution penalized boolean.
     *
     * @return true if incorrect solution is penalized
     */
    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    /**
     * Sets solution penalized.
     *
     * @param solutionPenalized the solution penalized
     */
    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
    }

    /**
     * Gets hints.
     *
     * @return the set of {@link HintExportDTO}
     */
    public Set<HintExportDTO> getHints() {
        return hints;
    }

    /**
     * Sets hints.
     *
     * @param hints the set of {@link HintExportDTO}
     */
    public void setHints(Set<HintExportDTO> hints) {
        this.hints = hints;
    }

    /**
     * Gets incorrect answer limit.
     *
     * @return the incorrect answer limit
     */
    public int getIncorrectAnswerLimit() {
        return incorrectAnswerLimit;
    }

    /**
     * Sets incorrect answer limit.
     *
     * @param incorrectAnswerLimit the incorrect answer limit
     */
    public void setIncorrectAnswerLimit(int incorrectAnswerLimit) {
        this.incorrectAnswerLimit = incorrectAnswerLimit;
    }

    /**
     * Gets attachments.
     *
     * @return the list of attachments
     */
    public List<AttachmentImportDTO> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param attachments the list of attachments
     */
    public void setAttachments(List<AttachmentImportDTO> attachments) {
        this.attachments = attachments;
    }

    /**
     * Gets max score.
     *
     * @return the max score
     */
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets max score.
     *
     * @param maxScore the max score
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    /**
     * Gets reference solution fo the training level.
     *
     * @return the list of {@link ReferenceSolutionNodeDTO}s
     */
    public List<ReferenceSolutionNodeDTO> getReferenceSolution() {
        return referenceSolution;
    }


    /**
     * Sets reference solution of the training level.
     *
     * @param referenceSolution list of the reference solution node
     */
    public void setReferenceSolution(List<ReferenceSolutionNodeDTO> referenceSolution) {
        this.referenceSolution = referenceSolution;
    }

    /**
     * Are answers variant.
     *
     * @return true if answers are variant and unique for each training run
     */
    public boolean isVariantAnswers() {
        return variantAnswers;
    }

    /**
     * Sets variant answers.
     *
     * @param variantAnswers the variant answers
     */
    public void setVariantAnswers(boolean variantAnswers) {
        this.variantAnswers = variantAnswers;
    }

    /**
     * Gets set of MITRE techniques used in the training level
     *
     * @return set of MITRE techniques
     */
    public Set<MitreTechniqueDTO> getMitreTechniques() {
        return mitreTechniques;
    }

    /**
     * Sets set of MITRE techniques used in the training level
     *
     * @param mitreTechniques set of MITRE techniques
     */
    public void setMitreTechniques(Set<MitreTechniqueDTO> mitreTechniques) {
        this.mitreTechniques = mitreTechniques;
    }

    /**
     * Gets set of expected commands executed in the training level
     *
     * @return set of expected commands
     */
    public Set<String> getExpectedCommands() {
        return expectedCommands;
    }

    /**
     * Sets set of expected commands executed in the training level
     *
     * @param expectedCommands set of expected commands
     */
    public void setExpectedCommands(Set<String> expectedCommands) {
        this.expectedCommands = expectedCommands;
    }

    /**
     * Gets boolean if at least one command has to be executed to complete the training level
     *
     * @return true if commands are required, false otherwise
     */
    public boolean isCommandsRequired() {
        return commandsRequired;
    }

    /**
     * Sets a boolean if at least one command has to be executed to complete the training level
     *
     * @param commandsRequired boolean value
     */
    public void setCommandsRequired(boolean commandsRequired) {
        this.commandsRequired = commandsRequired;
    }

    @Override
    public String toString() {
        return "TrainingLevelExportDTO{" +
                "answer='" + answer + '\'' +
                ", answerVariableName='" + answerVariableName + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", hints=" + hints +
                ", incorrectAnswerLimit=" + incorrectAnswerLimit +
                ", maxScore=" + maxScore +
                ", variantAnswers=" + variantAnswers +
                ", commandsRequired=" + commandsRequired +
                '}';
    }
}
