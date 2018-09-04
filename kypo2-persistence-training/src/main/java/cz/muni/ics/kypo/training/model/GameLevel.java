package cz.muni.ics.kypo.training.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.*;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "GameLevel")
@Table(name = "game_level")
@PrimaryKeyJoinColumn(name = "id")
public class GameLevel extends AbstractLevel implements Serializable {

  @Column(name = "flag", nullable = false)
  private String flag;
  @Lob
  @Column(name = "content", nullable = false)
  private String content;
  @Lob
  @Column(name = "solution", nullable = false)
  private String solution;
  @Column(name = "incorrect_flag_penalty", nullable = false)
  private int incorrectFlagPenalty;
  @Column(name = "solution_penalty", nullable = false)
  private int solutionPenalty = super.getMaxScore() - 1;
  @Column(name = "estimated_duration")
  private int estimatedDuration;
  @Column(name = "attachments")
  private String[] attachments;
  @OneToMany(fetch = FetchType.LAZY, targetEntity = Hint.class, mappedBy = "gameLevel", cascade = CascadeType.ALL)
  private Set<Hint> hints = new HashSet<>();

  public GameLevel() {}

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

  public void addHint(Hint hint) {
    this.hints.add(hint);
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
        && Objects.equals(content, other.getContent())
        && Objects.equals(estimatedDuration, other.getEstimatedDuration()) 
        && Objects.equals(flag, other.getFlag()) 
        && Objects.equals(hints, other.getHints())
        && Objects.equals(incorrectFlagPenalty, other.getIncorrectFlagPenalty()) 
        && Objects.equals(solution, other.getSolution())
        && Objects.equals(solutionPenalty, other.getSolutionPenalty());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "GameLevel [flag=" + flag + ", content=" + content + ", solution=" + solution + ", incorrectFlagPenalty=" + incorrectFlagPenalty
        + ", solutionPenalty=" + solutionPenalty + ", estimatedDuration=" + estimatedDuration + ", attachments=" + Arrays.toString(attachments) + ", hints="
        + hints + ", getId()=" + getId() + ", getTitle()=" + getTitle() + ", getMaxScore()=" + getMaxScore() + ", getPreHook()="
        + getPreHook() + ", getPostHook()=" + getPostHook() + ", getNextLevel()=" + getNextLevel() + ", getTrainingDefinition()="
        + ", toString()=" + super.toString() + "]";
  }

}
