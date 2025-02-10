package cz.cyberrange.platform.training.persistence.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Class specifying Abstract level as Info level.
 * Info levels contain information for trainees.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "info_level")
@PrimaryKeyJoinColumn(name = "id")
public class InfoLevel extends AbstractLevel {

    @Lob
    @Column(name = "content", nullable = false)
    private String content;
}
