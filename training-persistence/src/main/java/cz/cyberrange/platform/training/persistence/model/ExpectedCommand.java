package cz.cyberrange.platform.training.persistence.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Class specifying command to be executed during the training level.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Embeddable
public class ExpectedCommand {

    @Column(name = "command")
    private String command;
}
