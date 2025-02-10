package cz.cyberrange.platform.training.persistence.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Class represents information of hint associated with current level of training run
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SolutionInfo {

    @Column(name = "training_level_id", nullable = false)
    private long trainingLevelId;
    @Column(name = "solution_content", nullable = false)
    private String solutionContent;
}
