package cz.cyberrange.platform.training.api.dto.traininglevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LevelReferenceSolutionDTO {

    private Long id;
    private Integer order;
    private List<ReferenceSolutionNodeDTO> referenceSolution;
}
