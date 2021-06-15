package cz.muni.ics.kypo.training.api.dto.archive;

import java.util.HashSet;
import java.util.Set;

public class QuestionAnswerArchiveDTO {
    private String question;
    private Set<String> answer = new HashSet<>();

    public QuestionAnswerArchiveDTO(String question, Set<String> answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Set<String> getAnswer() {
        return answer;
    }

    public void setAnswer(Set<String> answer) {
        this.answer = answer;
    }
}
