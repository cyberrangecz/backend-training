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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Class representing hints associated with training level that can be displayed by trainee if they are in need of help with
 * solving given level
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hint")
@NamedQueries({
        @NamedQuery(
                name = "Hint.deleteHintsByLevelId",
                query = "DELETE FROM Hint h WHERE h.trainingLevel.id = :levelId"
        ),
})
public class Hint extends AbstractEntity<Long> {

    @Column(name = "title", nullable = false)
    private String title;
    @Lob
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "hint_penalty", nullable = false)
    private Integer hintPenalty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;
    @Column(name = "order_in_level", nullable = false)
    private int order;
}
