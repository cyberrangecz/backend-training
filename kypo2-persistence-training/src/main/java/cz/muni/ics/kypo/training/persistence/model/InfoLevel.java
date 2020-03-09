package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class specifying Abstract level as Info level.
 * Info levels contain information for trainees.
 *
 */
@Entity
@Table(name = "info_level")
@PrimaryKeyJoinColumn(name = "id")
public class InfoLevel extends AbstractLevel implements Serializable {

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * Instantiates a new Info level
     */
    public InfoLevel() {
        super();
    }

    /**
     * Instantiates a new Info level
     *
     * @param content text content of Info level
     */
    public InfoLevel(String content) {
        super();
        this.content = content;
    }

    /**
     * Gets text content of Info level.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets text content of Info level.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof InfoLevel))
            return false;
        InfoLevel other = (InfoLevel) obj;
        return Objects.equals(content, other.getContent());
    }

    @Override
    public String toString() {
        return "InfoLevel{" +
                "content='" + content + '\'' +
                '}';
    }
}
