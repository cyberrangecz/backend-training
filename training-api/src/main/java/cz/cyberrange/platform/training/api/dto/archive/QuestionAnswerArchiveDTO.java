package cz.cyberrange.platform.training.api.dto.archive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class QuestionAnswerArchiveDTO {

    private String question;
    private Set<String> answer = new HashSet<>();
}
