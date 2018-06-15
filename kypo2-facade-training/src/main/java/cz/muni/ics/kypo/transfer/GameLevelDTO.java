package cz.muni.ics.kypo.transfer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cz.muni.ics.kypo.model.Hint;
import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.model.TrainingRun;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
public class GameLevelDTO extends AbstractLevelDTO {

  private String flag;
  private byte[] content;
  private byte[] solution;
  private int incorrectFlagPenalty;
  private int solutionPenalty = super.getMaxScore() - 1;
  private int estimatedDuration;
  private String[] attachments;
  private Set<Hint> hints = new HashSet<>();

  public GameLevelDTO() {}

  public GameLevelDTO(Long id, String title, int maxScore, byte[] preHook, byte[] postHook, Long nextLevel, TrainingDefinition trainingDefinition,
      Set<TrainingRun> trainingRun, String flag, byte[] content, byte[] solution, int incorrectFlagPenalty, int solutionPenalty, int estimatedDuration,
      String[] attachments, Set<Hint> hints) {
    super(id, title, maxScore, preHook, postHook, nextLevel, trainingDefinition, trainingRun);
    this.flag = flag;
    this.content = content;
    this.solution = solution;
    this.incorrectFlagPenalty = incorrectFlagPenalty;
    this.solutionPenalty = solutionPenalty;
    this.estimatedDuration = estimatedDuration;
    this.attachments = attachments;
    this.hints = hints;
  }

  public String getFlag() {
    return flag;
  }

  public void setFlag(String flag) {
    this.flag = flag;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public byte[] getSolution() {
    return solution;
  }

  public void setSolution(byte[] solution) {
    this.solution = solution;
  }

  public int getIncorrectFlagPenalty() {
    return incorrectFlagPenalty;
  }

  public void setIncorrectFlagPenalty(int incorrectFlagPenalty) {
    this.incorrectFlagPenalty = incorrectFlagPenalty;
  }

  public int getSolutionPenalty() {
    return solutionPenalty;
  }

  public void setSolutionPenalty(int solutionPenalty) {
    this.solutionPenalty = solutionPenalty;
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

  public Set<Hint> getHints() {
    return hints;
  }

  public void setHints(Set<Hint> hints) {
    this.hints = hints;
  }

  @Override
  public String toString() {
    return "GameLevelDTO [flag=" + flag + ", content=" + Arrays.toString(content) + ", solution=" + Arrays.toString(solution) + ", incorrectFlagPenalty="
        + incorrectFlagPenalty + ", solutionPenalty=" + solutionPenalty + ", estimatedDuration=" + estimatedDuration + ", attachments="
        + Arrays.toString(attachments) + ", hints=" + hints + "]";
  }

}
