package cz.muni.ics.kypo.training.model;

import org.hibernate.annotations.Type;

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
  @Type(type = "org.hibernate.type.StringType")
  @Column(name = "content", nullable = false)
  private String content;
  @Lob
  @Type(type = "org.hibernate.type.StringType")
  @Column(name = "solution", nullable = false)
  private String solution;
  @Column(name = "solution_penalty", nullable = false)
  private int solutionPenalty = super.getMaxScore() - 1;
  @Column(name = "estimated_duration")
  private int estimatedDuration;
  @Column(name = "attachments")
  private String[] attachments;
  @OneToMany(fetch = FetchType.LAZY, targetEntity = Hint.class, mappedBy = "gameLevel", cascade = CascadeType.ALL)
  private Set<Hint> hints = new HashSet<>();
  @Column(name = "incorrect_flag_limit")
  private int incorrectFlagLimit;

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

  public int getIncorrectFlagLimit() {
    return incorrectFlagLimit;
  }

  public void setIncorrectFlagLimit(int incorrectFlagLimit) {
    this.incorrectFlagLimit = incorrectFlagLimit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    GameLevel gameLevel = (GameLevel) o;
    return  solutionPenalty == gameLevel.solutionPenalty &&
            estimatedDuration == gameLevel.estimatedDuration &&
            incorrectFlagLimit == gameLevel.incorrectFlagLimit &&
            Objects.equals(flag, gameLevel.flag) &&
            Objects.equals(content, gameLevel.content) &&
            Objects.equals(solution, gameLevel.solution) &&
            Arrays.equals(attachments, gameLevel.attachments) &&
            Objects.equals(hints, gameLevel.hints);
  }

  @Override
  public int hashCode() {

    int result = Objects.hash(super.hashCode(), flag, content, solution, solutionPenalty, estimatedDuration, hints, incorrectFlagLimit);
    result = 31 * result + Arrays.hashCode(attachments);
    return result;
  }

  @Override
  public String toString() {
    return "GameLevel{" +
            "flag='" + flag + '\'' +
            ", content='" + content + '\'' +
            ", solution='" + solution + '\'' +
            ", solutionPenalty=" + solutionPenalty +
            ", estimatedDuration=" + estimatedDuration +
            ", attachments=" + Arrays.toString(attachments) +
            ", hints=" + hints +
            ", incorrectFlagLimit=" + incorrectFlagLimit +
            '}';
  }
}
