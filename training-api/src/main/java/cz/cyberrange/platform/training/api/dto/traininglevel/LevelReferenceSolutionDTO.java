package cz.cyberrange.platform.training.api.dto.traininglevel;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LevelReferenceSolutionDTO {

  private Long id;
  private Integer order;
  private List<ReferenceSolutionNodeDTO> referenceSolution;
}
