package cz.muni.ics.kypo.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonAlias;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.ReferenceSolutionNodeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@ApiModel(value = "TrainingLevelImportDTO", description = "Imported training level.", parent = AbstractLevelExportDTO.class)
public class TrainingLevelImportDTO extends AbstractLevelImportDTO{

	@ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
	@Size(max = 50, message = "{trainingLevel.answer.Size.message}")
	@JsonAlias({"flag"})
	private String answer;
	@ApiModelProperty(value = "Identifier that is used to obtain answer from remote storage.", example = "username")
	private String answerVariableName;
	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
	@NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
	private String content;
	@ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
	@NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
	private String solution;
	@ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
	@NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
	private boolean solutionPenalized;
	@Valid
	@ApiModelProperty(value = "Information which helps player resolve the level.")
	private Set<HintImportDTO> hints = new HashSet<>();
	@ApiModelProperty(value = "How many times player can submit incorrect answer before displaying solution.", example = "5")
	@NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
	@Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
	@Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
	@JsonAlias({"incorrect_flag_limit"})
	private int incorrectAnswerLimit;
	@Valid
	@ApiModelProperty(value = "List of attachments.", example = "[]")
	private List<AttachmentImportDTO> attachments;
	@ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
	@NotNull(message = "{abstractLevel.maxScore.NotNull.message}")
	@Min(value = 0, message = "{abstractLevel.maxScore.Min.message}")
	@Max(value = 100, message = "{abstractLevel.maxScore.Max.message}")
	protected int maxScore;
	@ApiModelProperty(value = "Marking if flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
	private boolean variantAnswers;
	@Valid
	@ApiModelProperty(value = "Sequence of commands that leads to the level answer.", example = "false")
	private List<ReferenceSolutionNodeDTO> referenceSolution = new ArrayList<>();

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
	 * @return the set of {@link HintImportDTO}
	 */
	public Set<HintImportDTO> getHints() {
		return hints;
	}

	/**
	 * Sets hints.
	 *
	 * @param hints the set of {@link HintImportDTO}
	 */
	public void setHints(Set<HintImportDTO> hints) {
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
	 * @param attachments the attachments
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
	 * Is variant answers boolean.
	 *
	 * @return the boolean
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

	@Override
	public String toString() {
		return "TrainingLevelImportDTO{" +
				"answer='" + answer + '\'' +
				", answerVariableName='" + answerVariableName + '\'' +
				", content='" + content + '\'' +
				", solution='" + solution + '\'' +
				", solutionPenalized=" + solutionPenalized +
				", hints=" + hints +
				", incorrectAnswerLimit=" + incorrectAnswerLimit +
				", attachments=" + attachments +
				", maxScore=" + maxScore +
				", variantAnswers=" + variantAnswers +
				'}';
	}
}
