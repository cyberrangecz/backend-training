package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.dto.export.HintExportDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GameLevelImportDTO extends AbstractLevelImportDTO{

	@NotEmpty(message = "{gamelevelimport.flag.NotEmpty.message}")
	@Size(max = 50, message = "{gamelevelimport.flag.Size.message}")
	private String flag;
	private String content;
	private String solution;
	@NotNull(message = "{gamelevelimport.solutionPenalized.NotNull.message}")
	private boolean solutionPenalized;
	private int estimatedDuration;
	private String[] attachments;
	private Set<HintDTO> hints;
	@NotNull(message = "{gamelevelimport.incorrectFlagLimit.NotEmpty.message}")
	@Min(value = 0, message = "{gamelevelimport.incorrectFlagLimit.Min.message}")
	private int incorrectFlagLimit;

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public boolean isSolutionPenalized() {
		return solutionPenalized;
	}

	public void setSolutionPenalized(boolean solutionPenalized) {
		this.solutionPenalized = solutionPenalized;
	}

	public int getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(int estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public String[] getAttachments() {
		return attachments;
	}

	public void setAttachments(String[] attachments) {
		this.attachments = attachments;
	}

	public Set<HintDTO> getHints() {
		return hints;
	}

	public void setHints(Set<HintDTO> hints) {
		this.hints = hints;
	}

	public int getIncorrectFlagLimit() {
		return incorrectFlagLimit;
	}

	public void setIncorrectFlagLimit(int incorrectFlagLimit) {
		this.incorrectFlagLimit = incorrectFlagLimit;
	}

	@Override public String toString() {
		return "GameLevelImportDTO{" + "flag='" + flag + '\'' + ", content='" + content + '\'' + ", solution='" + solution + '\''
				+ ", solutionPenalized=" + solutionPenalized + ", estimatedDuration=" + estimatedDuration + ", attachments=" + Arrays
				.toString(attachments) + ", hints=" + hints + ", incorrectFlagLimit=" + incorrectFlagLimit + '}';
	}
}
