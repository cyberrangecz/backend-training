package cz.muni.ics.kypo.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(catalog = "training", schema = "public", name = "game_level")
@PrimaryKeyJoinColumn(name = "id")
public class GameLevel extends AbstractLevel {

  @Column(name = "flag", nullable = false)
  private String flag;
  @Lob
  @Column(name = "content", nullable = false)
  private byte[] content;
  @Lob
  @Column(name = "solution", nullable = false)
  private byte[] solution;
  @Column(name = "incorrect_flag_penalty", nullable = false)
  private int incorrectFlagPenalty;
  @Column(name = "solution_penalty", nullable = false)
  private int solutionPenalty = super.getMaxScore() - 1;
  @Column(name = "estimated_duration")
  private int estimatedDuration;
  @Column(name = "attachments")
  private String[] attachments;
  @OneToMany(fetch = FetchType.LAZY, targetEntity = Hint.class, mappedBy = "gameLevel")
  private Set<Hint> hints = new HashSet<>();

  public GameLevel() {}

  public GameLevel(String flag, byte[] content, byte[] solution, int incorrectFlagPenalty, int solutionPenalty, int estimatedDuration, String[] attachments,
      Set<Hint> hints) {
    super();
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
    return Collections.unmodifiableSet(hints);
  }

  public void setHints(Set<Hint> hints) {
    this.hints = hints;
  }


  @Override
  public int hashCode() {
    return Objects.hash(attachments, content, estimatedDuration, flag, hints, incorrectFlagPenalty, solution, solutionPenalty);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof GameLevel))
      return false;
    GameLevel other = (GameLevel) obj;
    // @formatter:off
    return Arrays.equals(attachments, other.getAttachments()) 
        && Arrays.equals(content, other.getContent())
        && Objects.equals(estimatedDuration, other.getEstimatedDuration()) 
        && Objects.equals(flag, other.getFlag()) 
        && Objects.equals(hints, other.getHints())
        && Objects.equals(incorrectFlagPenalty, other.getIncorrectFlagPenalty()) 
        && Arrays.equals(solution, other.getSolution())
        && Objects.equals(solutionPenalty, other.getSolutionPenalty());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "GameLevel [flag=" + flag + ", content=" + Arrays.toString(content) + ", solution=" + Arrays.toString(solution) + ", incorrectFlagPenalty="
        + incorrectFlagPenalty + ", solutionPenalty=" + solutionPenalty + ", estimatedDuration=" + estimatedDuration + ", attachments="
        + Arrays.toString(attachments) + ", hints=" + hints + ", getId()=" + getId() + ", getTitle()=" + getTitle() + ", getMaxScore()=" + getMaxScore()
        + ", getOrder()=" + getOrder() + ", getPreHook()=" + Arrays.toString(getPreHook()) + ", getPostHook()=" + Arrays.toString(getPostHook())
        + ", getNextLevel()=" + getNextLevel() + ", getTrainingDefinition()=" + getTrainingDefinition() + ", getTrainingRun()=" + getTrainingRun()
        + ", toString()=" + super.toString() + "]";
  }

}
