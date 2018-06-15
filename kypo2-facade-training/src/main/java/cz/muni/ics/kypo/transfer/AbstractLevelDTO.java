package cz.muni.ics.kypo.transfer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.model.TrainingRun;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
public class AbstractLevelDTO {

  private Long id;
  private String title;
  private int maxScore;
  private byte[] preHook;
  private byte[] postHook;
  private Long nextLevel;
  private TrainingDefinition trainingDefinition;
  private Set<TrainingRun> trainingRun = new HashSet<>(0);

  public AbstractLevelDTO() {}

  public AbstractLevelDTO(Long id, String title, int maxScore, byte[] preHook, byte[] postHook, Long nextLevel, TrainingDefinition trainingDefinition,
      Set<TrainingRun> trainingRun) {
    super();
    this.id = id;
    this.title = title;
    this.maxScore = maxScore;
    this.preHook = preHook;
    this.postHook = postHook;
    this.nextLevel = nextLevel;
    this.trainingDefinition = trainingDefinition;
    this.trainingRun = trainingRun;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(int maxScore) {
    this.maxScore = maxScore;
  }

  public byte[] getPreHook() {
    return preHook;
  }

  public void setPreHook(byte[] preHook) {
    this.preHook = preHook;
  }

  public byte[] getPostHook() {
    return postHook;
  }

  public void setPostHook(byte[] postHook) {
    this.postHook = postHook;
  }

  public Long getNextLevel() {
    return nextLevel;
  }

  public void setNextLevel(Long nextLevel) {
    this.nextLevel = nextLevel;
  }

  public TrainingDefinition getTrainingDefinition() {
    return trainingDefinition;
  }

  public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
    this.trainingDefinition = trainingDefinition;
  }

  public Set<TrainingRun> getTrainingRun() {
    return trainingRun;
  }

  public void setTrainingRun(Set<TrainingRun> trainingRun) {
    this.trainingRun = trainingRun;
  }

  @Override
  public String toString() {
    return "AbstractLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", preHook=" + Arrays.toString(preHook) + ", postHook="
        + Arrays.toString(postHook) + ", nextLevel=" + nextLevel + ", trainingDefinition=" + trainingDefinition + ", trainingRun=" + trainingRun + "]";
  }

}
