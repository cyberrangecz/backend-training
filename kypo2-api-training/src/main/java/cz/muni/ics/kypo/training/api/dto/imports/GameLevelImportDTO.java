package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates information about game level. Inherits from {@link AbstractLevelImportDTO}
 *
 * @author Boris Jadus(445343)
 */
public class GameLevelImportDTO extends AbstractLevelImportDTO{

	@NotEmpty(message = "{gamelevelimport.flag.NotEmpty.message}")
	@Size(max = 50, message = "{gamelevelimport.flag.Size.message}")
	private String flag;
	private String content;
	private String solution;
	@NotNull(message = "{gamelevelimport.solutionPenalized.NotNull.message}")
	private boolean solutionPenalized;
	private String[] attachments;
	private Set<HintDTO> hints = new HashSet<>();
	@NotNull(message = "{gamelevelimport.incorrectFlagLimit.NotEmpty.message}")
	@Min(value = 0, message = "{gamelevelimport.incorrectFlagLimit.Min.message}")
	private int incorrectFlagLimit;

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
	 * Get attachments.
	 *
	 * @return attachments
	 */
	public String[] getAttachments() {
		return attachments;
	}

	/**
	 * Sets attachments.
	 *
	 * @param attachments the attachments
	 */
	public void setAttachments(String[] attachments) {
		this.attachments = attachments;
	}

	/**
	 * Gets hints.
	 *
	 * @return the set of {@link HintImportDTO}
	 */
	public Set<HintDTO> getHints() {
		return hints;
	}

	/**
	 * Sets hints.
	 *
	 * @param hints the set of {@link HintImportDTO}
	 */
	public void setHints(Set<HintDTO> hints) {
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

	@Override public String toString() {
		return "GameLevelImportDTO{" + "flag='" + flag + '\'' + ", content='" + content + '\'' + ", solution='" + solution + '\''
				+ ", solutionPenalized=" + solutionPenalized + ", attachments=" + Arrays
				.toString(attachments) + ", hints=" + hints + ", incorrectFlagLimit=" + incorrectFlagLimit + '}';
	}
}
