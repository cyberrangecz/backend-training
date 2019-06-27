package cz.muni.ics.kypo.training.api.dto;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates basic information about level.
 *
 * @author Boris Jadus
 */
public class BasicLevelInfoDTO {

    private Long id;
    private String title;
    private int order;
    private LevelType levelType;

    /**
     * Instantiates a new Basic level info dto.
     */
    public BasicLevelInfoDTO() {
    }

    /**
     * Instantiates a new Basic level info dto.
     *
     * @param id        the id
     * @param title     the title
     * @param levelType the level type
     * @param order     the order
     */
    public BasicLevelInfoDTO(Long id, String title, LevelType levelType, int order) {
        this.id = id;
        this.title = title;
        this.levelType = levelType;
        this.order = order;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @return the order
     */
    @ApiModelProperty(value = "Order of level among levels in training definition.", example = "1")
    public int getOrder() {
        return order;
    }

    /**
     * Sets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Gets level type.
     *
     * @return the {@link LevelType}
     */
    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    public LevelType getLevelType() {
        return levelType;
    }

    /**
     * Sets level type.
     *
     * @param levelType the {@link LevelType}
     */
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
