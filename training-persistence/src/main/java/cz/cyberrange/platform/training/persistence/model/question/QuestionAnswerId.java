package cz.cyberrange.platform.training.persistence.model.question;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuestionAnswerId implements Serializable {

    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "training_run_id")
    private Long trainingRunId;
}