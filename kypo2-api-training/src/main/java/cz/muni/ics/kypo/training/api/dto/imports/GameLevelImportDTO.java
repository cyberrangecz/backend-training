package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates information about game level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@ApiModel(value = "GameLevelImportDTO", description = "Imported game level.", parent = AbstractLevelExportDTO.class)
public class GameLevelImportDTO extends AbstractLevelImportDTO{

	@ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretFlag")
	@NotEmpty(message = "{gameLevel.flag.NotEmpty.message}")
	@Size(max = 50, message = "{gameLevel.flag.Size.message}")
	private String flag;
	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
	@NotEmpty(message = "{gameLevel.content.NotEmpty.message}")
	private String content;
	@ApiModelProperty(value = "Instruction how to get flag in game.", example = "This is how you do it")
	@NotEmpty(message = "{gameLevel.solution.NotEmpty.message}")
	private String solution;
	@ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
	@NotNull(message = "{gameLevel.solutionPenalized.NotNull.message}")
	private boolean solutionPenalized;
	@Valid
	@ApiModelProperty(value = "Information which helps player resolve the level.")
	private Set<HintImportDTO> hints = new HashSet<>();
	@ApiModelProperty(value = "How many times player can submit incorrect flag before displaying solution.", example = "5")
	@NotNull(message = "{gameLevel.incorrectFlagLimit.NotNull.message}")
	@Min(value = 0, message = "{gameLevel.incorrectFlagLimit.Min.message}")
	@Max(value = 100, message = "{gameLevel.incorrectFlagLimit.Max.message}")
	private int incorrectFlagLimit;
	@Valid
	@ApiModelProperty(value = "List of attachments.", example = "[]")
	private List<AttachmentImportDTO> attachments;

	/**
	 * Gets flag.
	 *
	 * @return the flag
	 */
	public String getFlag() {
		return flag;
	}

	/**
	 * Sets flag.
	 *
	 * @param flag the flag
	 */
	public void setFlag(String flag) {
		this.flag = flag;
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
	 * Gets incorrect flag limit.
	 *
	 * @return the incorrect flag limit
	 */
	public int getIncorrectFlagLimit() {
		return incorrectFlagLimit;
	}

	/**
	 * Sets incorrect flag limit.
	 *
	 * @param incorrectFlagLimit the incorrect flag limit
	 */
	public void setIncorrectFlagLimit(int incorrectFlagLimit) {
		this.incorrectFlagLimit = incorrectFlagLimit;
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

	@Override
	public String toString() {
		return "GameLevelImportDTO{" +
				"flag='" + flag + '\'' +
				", content='" + content + '\'' +
				", solution='" + solution + '\'' +
				", solutionPenalized=" + solutionPenalized +
				", hints=" + hints +
				", incorrectFlagLimit=" + incorrectFlagLimit +
				'}';
	}
}
