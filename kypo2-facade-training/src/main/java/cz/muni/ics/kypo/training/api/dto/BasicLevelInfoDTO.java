package cz.muni.ics.kypo.training.api.dto;

import cz.muni.ics.kypo.training.model.enums.LevelType;

import java.util.Objects;

/**
 * @author Boris Jadus
 */

public class BasicLevelInfoDTO {

  private Long id;
  private String title;
  private int order;
  private LevelType levelType;

  public BasicLevelInfoDTO() {
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

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public LevelType getLevelType() {
    return levelType;
  }

  public void setLevelType(LevelType levelType) {
    this.levelType = levelType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasicLevelInfoDTO that = (BasicLevelInfoDTO) o;
    return order == that.order &&
            Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            levelType == that.levelType;
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, title, order, levelType);
  }

  @Override
  public String toString() {
    return "BasicLevelInfoDTO{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", order=" + order +
            ", levelType=" + levelType +
            '}';
  }
}
