package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentReferenceExtractor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AttachmentReferenceExtractorTest {

  private static final String HASH_A = "a".repeat(64);
  private static final String HASH_B = "b".repeat(64);
  private static final String HASH_C = "c".repeat(64);
  private static final String HASH_D = "d".repeat(64);
  private static final String HASH_E = "e".repeat(64);
  private static final String HASH_F = "0123456789".repeat(6) + "abcd";

  private final AttachmentReferenceExtractor extractor = new AttachmentReferenceExtractor();

  private static String link(String hash) {
    return "![image](attachment:" + hash + ")";
  }

  @Test
  void extractsFromTrainingLevelContentSolutionAndHints() {
    TrainingLevel level = new TrainingLevel();
    level.setContent(link(HASH_A));
    level.setSolution("[file](attachment:" + HASH_B + ")");
    Hint hint = new Hint();
    hint.setContent(link(HASH_C));
    level.setHints(new LinkedHashSet<>(Set.of(hint)));

    assertEquals(Set.of(HASH_A, HASH_B, HASH_C), extractor.extract(List.of(level)));
  }

  @Test
  void extractsFromInfoLevelContent() {
    InfoLevel level = new InfoLevel();
    level.setContent(link(HASH_A));

    assertEquals(Set.of(HASH_A), extractor.extract(List.of(level)));
  }

  @Test
  void extractsFromBothAccessLevelContents() {
    AccessLevel level = new AccessLevel();
    level.setCloudContent(link(HASH_A));
    level.setLocalContent(link(HASH_B));

    assertEquals(Set.of(HASH_A, HASH_B), extractor.extract(List.of(level)));
  }

  @Test
  void extractsFromAssessmentInstructionsAndEveryQuestionText() {
    Question question = new Question();
    question.setText(link(HASH_B));
    QuestionChoice choice = new QuestionChoice();
    choice.setText(link(HASH_C));
    question.setChoices(List.of(choice));
    ExtendedMatchingStatement statement = new ExtendedMatchingStatement();
    statement.setText(link(HASH_D));
    question.setExtendedMatchingStatements(List.of(statement));
    ExtendedMatchingOption option = new ExtendedMatchingOption();
    option.setText(link(HASH_E));
    question.setExtendedMatchingOptions(List.of(option));
    AssessmentLevel level = new AssessmentLevel();
    level.setInstructions(link(HASH_A));
    level.setQuestions(new ArrayList<>(List.of(question)));

    assertEquals(Set.of(HASH_A, HASH_B, HASH_C, HASH_D, HASH_E), extractor.extract(List.of(level)));
  }

  @Test
  void collectsDuplicatesOnceAcrossLevels() {
    InfoLevel first = new InfoLevel();
    first.setContent(link(HASH_A) + " and again " + link(HASH_A));
    InfoLevel second = new InfoLevel();
    second.setContent(link(HASH_A));

    assertEquals(Set.of(HASH_A), extractor.extract(List.of(first, second)));
  }

  @Test
  void acceptsAnyLowercaseHexadecimalDigits() {
    InfoLevel level = new InfoLevel();
    level.setContent(link(HASH_F));

    assertEquals(Set.of(HASH_F), extractor.extract(List.of(level)));
  }

  @Test
  void ignoresUppercaseShortAndDifferentlyPrefixedLookAlikes() {
    InfoLevel level = new InfoLevel();
    level.setContent(
        String.join(
            " ",
            "attachment:" + HASH_A.toUpperCase(Locale.ROOT),
            "attachment:" + "a".repeat(63),
            "attachments:" + HASH_B,
            "attachment:" + "g".repeat(64),
            HASH_C));

    assertTrue(extractor.extract(List.of(level)).isEmpty());
  }

  @Test
  void toleratesLevelsWithoutContent() {
    assertTrue(extractor.extract(List.of(new InfoLevel(), new TrainingLevel())).isEmpty());
  }
}
