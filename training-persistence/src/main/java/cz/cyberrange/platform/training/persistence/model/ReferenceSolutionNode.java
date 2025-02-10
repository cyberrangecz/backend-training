package cz.cyberrange.platform.training.persistence.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ReferenceSolutionNode {

    private String stateName;
    private List<String> prereqState;
    private String cmd;
    private String cmdType;
    private String cmdRegex;
    private boolean optional;
}
