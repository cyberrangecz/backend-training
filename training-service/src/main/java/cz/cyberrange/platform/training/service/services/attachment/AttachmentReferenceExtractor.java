package cz.cyberrange.platform.training.service.services.attachment;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Finds the attachment references written in the markdown of training levels. A reference is the
 * text {@code attachment:} followed by the 64 lowercase hexadecimal digits of a file's SHA-256.
 */
@Component
public class AttachmentReferenceExtractor {

  private static final Pattern REFERENCE = Pattern.compile("attachment:([0-9a-f]{64})");

  /**
   * Collects the hashes referenced from every markdown field of the given levels: the content and
   * solution of a training level and its hints, the content of an info level, both contents of an
   * access level, and the instructions of an assessment level with the texts of its questions,
   * choices, statements and options. Reads lazy hints and questions, so it has to run inside the
   * transaction that loaded the levels.
   *
   * @param levels the levels to scan
   * @return the distinct referenced hashes, in order of first appearance
   */
  public Set<String> extract(Collection<? extends AbstractLevel> levels) {
    Set<String> hashes = new LinkedHashSet<>();
    levels.stream()
        .flatMap(this::markdownFields)
        .forEach(markdown -> collectInto(markdown, hashes));
    return hashes;
  }

  private Stream<String> markdownFields(AbstractLevel level) {
    return switch (level) {
      case TrainingLevel training ->
          Stream.concat(
              Stream.of(training.getContent(), training.getSolution()),
              training.getHints().stream().map(Hint::getContent));
      case InfoLevel info -> Stream.of(info.getContent());
      case AccessLevel access -> Stream.of(access.getCloudContent(), access.getLocalContent());
      case AssessmentLevel assessment ->
          Stream.concat(
              Stream.of(assessment.getInstructions()),
              assessment.getQuestions().stream().flatMap(this::questionFields));
      default -> Stream.empty();
    };
  }

  private Stream<String> questionFields(Question question) {
    return Stream.of(
            Stream.of(question.getText()),
            question.getChoices().stream().map(QuestionChoice::getText),
            question.getExtendedMatchingStatements().stream()
                .map(ExtendedMatchingStatement::getText),
            question.getExtendedMatchingOptions().stream().map(ExtendedMatchingOption::getText))
        .flatMap(stream -> stream);
  }

  private void collectInto(String markdown, Set<String> hashes) {
    if (markdown == null) {
      return;
    }
    Matcher matcher = REFERENCE.matcher(markdown);
    while (matcher.find()) {
      hashes.add(matcher.group(1));
    }
  }
}
