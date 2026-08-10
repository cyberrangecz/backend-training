package cz.cyberrange.platform.training.api.dto.archive;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionAnswerArchiveDTO {

  private String question;
  private Set<String> answer = new HashSet<>();
}
