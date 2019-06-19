package cz.muni.ics.kypo.training.persistence.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * /**
 * Class specifying Abstract level as game level.
 * Game levels contain tasks for trainees to solve.
 *
 * @author Pavel Seda (441048)
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
    @Column(name = "solution_penalized", nullable = false)
    private boolean solutionPenalized;
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

    /**
     * Gets flag that needs to be found by trainee to complete level
     *
     * @return the flag
     */
    public String getFlag() {
        return flag;
    }

    /**
     * Sets flag that needs to be found by trainee to complete level
     *
     * @param flag the flag
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Gets text assignment of task that needs to be performed by trainee
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets text assignment of task that needs to be performed by trainee
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets solution to the level that is shown if trainee fails or if they request it
     *
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets solution to the level that is shown if trainee fails or if they request it
     *
     * @param solution the solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * Gets if solution is penalized. If true, points for solving level will be decreased to 1 after trainee displays solution
     *
     * @return the boolean
     */
    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    /**
     * Sets if solution is penalized. If true, points for solving level will be decreased to 1 after trainee displays solution
     *
     * @param solutionPenalized the solution penalized
     */
    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
    }

    /**
     * Gets attachments to level, for example picture or script
     *
     * @return the string [ ]
     */
    public String[] getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments to level, for example picture or script
     *
     * @param attachments the attachments
     */
    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
    }

    /**
     * Gets hints associated with game level
     *
     * @return the hints
     */
    public Set<Hint> getHints() {
        return Collections.unmodifiableSet(hints);
    }

    /**
     * Adds hint to be associated with game level
     *
     * @param hint the hint
     */
    public void addHint(Hint hint) {
        this.hints.add(hint);
    }

    /**
     * Sets hints associated with game level
     *
     * @param hints the hints
     */
    public void setHints(Set<Hint> hints) {
        this.hints = hints;
    }

    /**
     * Gets number of attempts available to trainee to input incorrect flag before the solution is displayed
     *
     * @return the incorrect flag limit
     */
    public int getIncorrectFlagLimit() {
        return incorrectFlagLimit;
    }

    /**
     * Sets number of attempts available to trainee to input incorrect flag before the solution is displayed
     *
     * @param incorrectFlagLimit the incorrect flag limit
     */
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

    @Override public String toString() {
        return "GameLevel{" + "flag='" + flag + '\'' + ", content='" + content + '\'' + ", solution='" + solution + '\''
            + ", solutionPenalized=" + solutionPenalized + ", attachments=" + Arrays.toString(attachments) + ", hints=" + hints
            + ", incorrectFlagLimit=" + incorrectFlagLimit + '}';
    }
}
