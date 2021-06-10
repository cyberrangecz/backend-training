package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlayerDataDTO {

    private Long id;
    private String name;
    private int gameScore;
    private int assessmentScore;
    private long trainingTime;
    private Boolean finished;
    private byte[] picture;
    private String avatarColor;
    private List<VisualizationAbstractLevelDTO> levels = new ArrayList<>();

    public PlayerDataDTO() {
    }

    public PlayerDataDTO(Long id,
                         String name,
                         int gameScore,
                         int assessmentScore,
                         long trainingTime,
                         Boolean finished,
                         byte[] picture) {
        this.id = id;
        this.name = name;
        this.gameScore = gameScore;
        this.assessmentScore = assessmentScore;
        this.trainingTime = trainingTime;
        this.finished = finished;
        this.picture = picture;
        this.avatarColor = extractAvatarColor(picture);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGameScore() {
        return gameScore;
    }

    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
    }

    public int getAssessmentScore() {
        return assessmentScore;
    }

    public void setAssessmentScore(int assessmentScore) {
        this.assessmentScore = assessmentScore;
    }

    public long getTrainingTime() {
        return trainingTime;
    }

    public void setTrainingTime(long trainingTime) {
        this.trainingTime = trainingTime;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
        this.avatarColor = extractAvatarColor(picture);
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public List<VisualizationAbstractLevelDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<VisualizationAbstractLevelDTO> levels) {
        this.levels = levels;
    }

    public void addTableLevel(VisualizationAbstractLevelDTO visualizationAbstractLevelDTO) {
        this.levels.add(visualizationAbstractLevelDTO);
    }

    private String extractAvatarColor(byte[] picture) {
        try {
            BufferedImage finalImage = ImageIO.read(new ByteArrayInputStream(picture));
            for(int y = 0; y < finalImage.getHeight(); y++) {
                for(int x = 0; x < finalImage.getWidth(); x++) {
                    int clr = finalImage.getRGB(x, y);
                    int red = (clr & 0x00ff0000) >> 16;
                    int green = (clr & 0x0000ff00) >> 8;
                    int blue = clr & 0x000000ff;
                    if (red != 255 && green != 255 && blue != 255) {
                        return String.format("#%02X%02X%02X", red, green, blue);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return String.format("#%02X%02X%02X", 255, 255, 255);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerDataDTO that = (PlayerDataDTO) o;
        return getGameScore() == that.getGameScore() &&
                getAssessmentScore() == that.getAssessmentScore() &&
                getTrainingTime() == that.getTrainingTime() &&
                getFinished().equals(that.getFinished()) &&
                getId().equals(that.getId()) &&
                getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getGameScore(), getAssessmentScore(), getTrainingTime(), getFinished());
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gameScore=" + gameScore +
                ", assessmentScore=" + assessmentScore +
                ", trainingTime=" + trainingTime +
                ", finished=" + finished +
                '}';
    }
}
