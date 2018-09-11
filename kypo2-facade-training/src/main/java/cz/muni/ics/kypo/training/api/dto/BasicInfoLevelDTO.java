package cz.muni.ics.kypo.training.api.dto;

import cz.muni.ics.kypo.training.api.enums.LevelType;

public class BasicInfoLevelDTO {
    private Long id;
    private String title;
    private LevelType type;

    public BasicInfoLevelDTO(long id, String title, LevelType levelType) {
        this.id = id;
        this.title = title;
        this.type = levelType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LevelType getType() {
        return type;
    }

    public void setType(LevelType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicInfoLevelDTO that = (BasicInfoLevelDTO) o;

        if (!id.equals(that.id)) return false;
        if (!title.equals(that.title)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BasicInfoLevelDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                '}';
    }
}
