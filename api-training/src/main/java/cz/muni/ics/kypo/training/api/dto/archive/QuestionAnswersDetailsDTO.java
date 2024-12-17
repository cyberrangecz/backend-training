package cz.muni.ics.kypo.training.api.dto.archive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QuestionAnswersDetailsDTO {
    private String question;
    private Map<String, Integer> answers = new HashMap<>();
    private int totalAnswers;

    public QuestionAnswersDetailsDTO(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Integer> answers) {
        this.answers = answers;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public void addAnswers(Set<String> answers) {
        totalAnswers++;
        for (String answer : answers) {
            Integer numberOfAnswers = this.answers.getOrDefault(answer, 0) + 1;
            this.answers.put(answer, numberOfAnswers);
        }
    }
}
