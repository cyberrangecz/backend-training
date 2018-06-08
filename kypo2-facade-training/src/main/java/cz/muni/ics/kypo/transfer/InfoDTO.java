package cz.muni.ics.kypo.transfer;

import java.util.Arrays;
import java.util.Set;

import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.model.TrainingRun;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
public class InfoDTO extends AbstractLevelDTO {

	private byte[] content;

	public InfoDTO() {
	}

	public InfoDTO(byte[] content) {
		super();
		this.content = content;
	}

	public InfoDTO(Long id, String title, int maxScore, byte[] preHook, byte[] postHook, Long nextLevel,
			TrainingDefinition trainingDefinition, Set<TrainingRun> trainingRun, byte[] content) {
		super(id, title, maxScore, preHook, postHook, nextLevel, trainingDefinition, trainingRun);
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "InfoDTO [content=" + Arrays.toString(content) + ", getId()=" + getId() + ", getTitle()=" + getTitle()
				+ ", getMaxScore()=" + getMaxScore() + ", getPreHook()=" + Arrays.toString(getPreHook())
				+ ", getPostHook()=" + Arrays.toString(getPostHook()) + ", getNextLevel()=" + getNextLevel()
				+ ", getTrainingDefinition()=" + getTrainingDefinition() + ", getTrainingRun()=" + getTrainingRun()
				+ ", toString()=" + super.toString() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ "]";
	}

}
