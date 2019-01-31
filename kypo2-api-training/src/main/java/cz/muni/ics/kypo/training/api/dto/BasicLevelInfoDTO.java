package cz.muni.ics.kypo.training.api.dto;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

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

    public BasicLevelInfoDTO(Long id, String title, LevelType levelType, int order) {
        this.id = id;
        this.title = title;
        this.levelType = levelType;
        this.order = order;
    }

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Order of level among levels in training definition.", example = "1")
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @ApiModelProperty(value = "Type of the level.", example = "GAME")
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
