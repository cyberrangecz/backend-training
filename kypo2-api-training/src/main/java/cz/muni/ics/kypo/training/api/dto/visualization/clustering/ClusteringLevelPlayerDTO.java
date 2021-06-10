package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

public class ClusteringLevelPlayerDTO {

    private Long id;
    private String name;
    private int participantLevelScore;
    private long trainingTime;
    private Boolean finished;
    private byte[] picture;
    private String avatarColor;

    public ClusteringLevelPlayerDTO() {
    }

    public ClusteringLevelPlayerDTO(Long id,
                                    String name,
                                    int participantLevelScore,
                                    long trainingTime,
                                    Boolean finished,
                                    byte[] picture) {
        this.id = id;
        this.name = name;
        this.participantLevelScore = participantLevelScore;
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

    public int getParticipantLevelScore() {
        return participantLevelScore;
    }

    public void setParticipantLevelScore(int participantLevelScore) {
        this.participantLevelScore = participantLevelScore;
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
        return getParticipantLevelScore() == that.getGameScore() &&
                getTrainingTime() == that.getTrainingTime() &&
                getFinished().equals(that.getFinished()) &&
                getId().equals(that.getId()) &&
                getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getParticipantLevelScore(), getTrainingTime(), getFinished());
    }

    @Override
    public String toString() {
        return "ClusteringPlayerDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gameScore=" + participantLevelScore +
                ", trainingTime=" + trainingTime +
                ", finished=" + finished +
                '}';
    }
}
