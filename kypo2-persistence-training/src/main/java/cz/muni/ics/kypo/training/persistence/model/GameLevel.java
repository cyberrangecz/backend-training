package cz.muni.ics.kypo.training.persistence.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Seda (441048)
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
    @Column(name = "solution_penalized", nullable = false)
    private boolean solutionPenalized;
    @Column(name = "estimated_duration")
    private int estimatedDuration;
    @Column(name = "attachments")
    private String[] attachments;
    @OneToMany(
            mappedBy = "gameLevel",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Hint> hints = new HashSet<>();
    @Column(name = "incorrect_flag_limit")
    private int incorrectFlagLimit;

    /**
     * Used to fix missing foreign key in the child (Hint) of @OneToMany association.
     *
     * Hint entity was missing foreign key to GameLevel after persisting it.
     */
    @PrePersist
    private void prePersist() {
        hints.forEach(hint -> hint.setGameLevel(this));
    }

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

    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
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
        if (!(o instanceof GameLevel)) return false;
        if (!super.equals(o)) return false;
        GameLevel gameLevel = (GameLevel) o;
        return Objects.equals(getContent(), gameLevel.getContent()) &&
                Objects.equals(getSolution(), gameLevel.getSolution());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getContent(), getSolution());
    }


    @Override
    public String toString() {
        return "GameLevel{" +
                "flag='" + flag + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", estimatedDuration=" + estimatedDuration +
                ", attachments=" + Arrays.toString(attachments) +
                ", hints=" + hints +
                ", incorrectFlagLimit=" + incorrectFlagLimit +
                '}';
    }
}
