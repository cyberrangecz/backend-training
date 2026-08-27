package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnswerRepository
    extends JpaRepository<QuestionAnswer, QuestionAnswerId>,
        QuerydslPredicateExecutor<QuestionAnswer> {

  @Modifying
  void deleteAllByTrainingRunId(Long trainingRunId);

  /**
   * Finds all question answers recorded for a training run, derived from the method name with no
   * {@code @NamedQuery} override, so it matches on the training run's primary key ({@code
   * trainingRun.id}, mapped to the {@code training_run_id} column via {@code @MapsId}). Neither a
   * {@code JOIN FETCH} nor an {@code @EntityGraph} backs it, so the associated question and
   * training run load on demand. The result carries no guaranteed order.
   *
   * @param trainingRunId the primary key of the training run
   * @return the question answers for that training run, or an empty list if none exist
   */
  List<QuestionAnswer> getAllByTrainingRunId(Long trainingRunId);

  List<QuestionAnswer> getAllByQuestionIdAndInstanceId(Long questionId, Long instanceId);
}
