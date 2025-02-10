package cz.cyberrange.platform.training.persistence.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Class representing attachments of Training Level
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attachment")
public class Attachment extends AbstractEntity<Long> {

    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;
}

