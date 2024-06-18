package cz.muni.ics.kypo.training.service.clustering;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.CorrectAnswerSubmitted;
import cz.muni.csirt.kypo.events.trainings.HintTaken;
import cz.muni.csirt.kypo.events.trainings.SolutionDisplayed;
import cz.muni.csirt.kypo.events.trainings.WrongAnswerSubmitted;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeAfterHintClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.TimeSolutionDisplayedClusterableDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterableDTO;
import cz.muni.ics.kypo.training.api.enums.NormalizationStrategy;
import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClusterableDataTransformer {

    public List<WrongAnswersClusterableDTO> transformToWrongAnswersAndTimePlayedClusterable(
            Map<Long, List<AbstractAuditPOJO>> eventsByUser,
            NormalizationStrategy normalizationStrategy) {
        List<WrongAnswersClusterableDTO> result = eventsByUser.entrySet().stream()
                .map(userEvs -> {
                    long wrongFlags = userEvs.getValue().stream().filter(WrongAnswerSubmitted.class::isInstance).count();
                    List<Long> totalEvents = userEvs.getValue().stream()
                            .map(AbstractAuditPOJO::getTimestamp).toList();
                    return new WrongAnswersClusterableDTO(userEvs.getKey(), (double) wrongFlags,
                            (double) (totalEvents.get(totalEvents.size() - 1) - totalEvents.get(0)));
                }).toList();

        return ClusterMathUtils.normalize(normalizationStrategy, result,
                Pair.of(WrongAnswersClusterableDTO::getTimePlayed, WrongAnswersClusterableDTO::setTimePlayedNormalized),
                Pair.of(WrongAnswersClusterableDTO::getWrongAnswersSubmitted, WrongAnswersClusterableDTO::setWrongAnswersSubmittedNormalized));
    }

    public List<TimeAfterHintClusterableDTO> transformToTimeAfterHintAndWrongAnswers(
            Map<Long, List<AbstractAuditPOJO>> eventsByUser,
            NormalizationStrategy normalizationStrategy) {

        List<TimeAfterHintClusterableDTO> result = new ArrayList<>();
        eventsByUser.forEach((userRefId, userEvs) -> {
            Map<Long, List<AbstractAuditPOJO>> collect = userEvs.stream()
                    .collect(Collectors.groupingBy(AbstractAuditPOJO::getLevel, Collectors.toList()));

            collect.forEach((level, trainingEvents) -> {
                final Optional<AbstractAuditPOJO> hint = trainingEvents.stream()
                        .filter(HintTaken.class::isInstance)
                        .findFirst();

                if (hint.isPresent()) {
                    long timeAfterHint =
                            trainingEvents.get(trainingEvents.size() - 1).getTimestamp() - hint.get().getTimestamp();

                    long wrongFlags = trainingEvents.stream()
                            .filter(trainingEvent -> trainingEvent instanceof WrongAnswerSubmitted answerSubmitted
                                    && answerSubmitted.getTimestamp() <= hint.get().getTimestamp()).count();
                    result.add(new TimeAfterHintClusterableDTO(userRefId, level, (double) timeAfterHint, (double) wrongFlags));
                }
            });
        });
        return ClusterMathUtils.normalize(normalizationStrategy, result,
                Pair.of(TimeAfterHintClusterableDTO::getTimeSpentAfterHint, TimeAfterHintClusterableDTO::setTimeSpentAfterHintNormalized),
                Pair.of(TimeAfterHintClusterableDTO::getWrongFlagsAfterHint, TimeAfterHintClusterableDTO::setWrongFlagsAfterHintNormalized));
    }

    public List<TimeSolutionDisplayedClusterableDTO> transformToTimeSolutionAndTimeAfterDisplayed(Map<Long, List<AbstractAuditPOJO>> eventsByUser,
                                                                                                  NormalizationStrategy normalizationStrategy) {

        List<TimeSolutionDisplayedClusterableDTO> result = new ArrayList<>();
        eventsByUser.forEach((userRefId, userEvs) -> {
            Map<Long, List<AbstractAuditPOJO>> collect = userEvs.stream()
                    .collect(Collectors.groupingBy(AbstractAuditPOJO::getLevel, Collectors.toList()));

            collect.forEach((level, trainingEvents) -> {
                Optional<AbstractAuditPOJO> solutionDisplayed = trainingEvents.stream()
                        .filter(SolutionDisplayed.class::isInstance)
                        .findFirst();

                if (solutionDisplayed.isPresent()) {
                    long timeAfterSolutionDisplayed = trainingEvents.get(trainingEvents.size() - 1).getTimestamp() // timestamp of the last event of the level
                            - solutionDisplayed.get().getTimestamp();                                               // timestamp of the solution displayed event

                    long solutionDisplayedAt = solutionDisplayed.get().getTimestamp() - trainingEvents.get(0).getTimestamp();
                    result.add(new TimeSolutionDisplayedClusterableDTO(userRefId, level, (double) solutionDisplayedAt, (double) timeAfterSolutionDisplayed));
                }
            });
        });
        return ClusterMathUtils.normalize(normalizationStrategy, result,
                Pair.of(TimeSolutionDisplayedClusterableDTO::getSolutionDisplayedAt, TimeSolutionDisplayedClusterableDTO::setSolutionDisplayedAtNormalized),
                Pair.of(TimeSolutionDisplayedClusterableDTO::getTimeSpentAfterSolutionDisplayed, TimeSolutionDisplayedClusterableDTO::setTimeSpentAfterSolutionDisplayedNormalized));
    }

    public List<EuclideanDoublePoint> transformToNDimensionalClusterables(Map<Long, List<AbstractAuditPOJO>> userEvents,
                                                                          NormalizationStrategy normalizationStrategy) {

        List<EuclideanDoublePoint> result = new ArrayList<>();

        List<Double> maxTimeAfterHint = ClusterMathUtils.normalize(normalizationStrategy,
                extractMaxTimeAfterHint(userEvents));
        List<Double> wrongFlags = ClusterMathUtils.normalize(normalizationStrategy, extractNumberOf(WrongAnswerSubmitted.class, userEvents));
        List<Double> totalScore = ClusterMathUtils.normalize(normalizationStrategy, extractTotalScore(userEvents));
        List<Double> playtime = ClusterMathUtils.normalize(normalizationStrategy, extractPlaytime(userEvents));
        List<Double> hintsTaken = ClusterMathUtils.normalize(normalizationStrategy, extractNumberOf(HintTaken.class, userEvents));
        List<Double> wrongFlagsAfterHint = ClusterMathUtils.normalize(normalizationStrategy, extractWrongAnswersAfterHint(userEvents));
        List<Double> displayedSolutions = ClusterMathUtils.normalize(normalizationStrategy, extractNumberOf(SolutionDisplayed.class, userEvents));

        for (int i = 0; i < userEvents.size(); i++) {
            result.add(new EuclideanDoublePoint(new double[]{
                    maxTimeAfterHint.get(i),
                    wrongFlags.get(i),
                    totalScore.get(i),
                    playtime.get(i),
                    hintsTaken.get(i),
                    wrongFlagsAfterHint.get(i),
                    displayedSolutions.get(i)
            }));
        }
        return result;
    }

    private List<Double> extractMaxTimeAfterHint(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(events -> {
                    Map<Long, List<AbstractAuditPOJO>> eventsByLevel = events.stream()
                            .filter(trainingEvent -> trainingEvent instanceof HintTaken
                                    || trainingEvent instanceof CorrectAnswerSubmitted)
                            .collect(Collectors.groupingBy(AbstractAuditPOJO::getLevel, Collectors.toList()));

                    return eventsByLevel.values().stream()
                            .mapToDouble(events1 ->
                                    events1.get(events1.size() - 1).getTimestamp() - events1.get(0).getTimestamp())
                            .max()
                            .orElse(0L);
                }).toList();
    }

    private List<Double> extractTotalScore(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> userEvs.stream()
                        .mapToDouble(AbstractAuditPOJO::getActualScoreInLevel)
                        .max()
                        .orElse(0L)
                ).toList();
    }

    private List<Double> extractPlaytime(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> {
                    double[] timestamps = userEvs.stream()
                            .mapToDouble(AbstractAuditPOJO::getTimestamp).toArray();
                    return timestamps[timestamps.length - 1] - timestamps[0];
                }).toList();
    }

    private List<Double> extractWrongAnswersAfterHint(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        List<Double> result = new ArrayList<>();
        userEvents.values().forEach(events -> {

            Map<Long, List<AbstractAuditPOJO>> groupedByLevel = events.stream()
                    .collect(Collectors.groupingBy(AbstractAuditPOJO::getLevel, Collectors.toList()));

            groupedByLevel.values().forEach(levelEvents -> levelEvents.stream()
                    .filter(HintTaken.class::isInstance)
                    .findFirst()
                    .ifPresentOrElse(
                            hint -> result.add(
                                    (double) levelEvents.stream()
                                            .filter(trainingEvent -> trainingEvent instanceof WrongAnswerSubmitted
                                                    && trainingEvent.getTimestamp() <= hint.getTimestamp())
                                            .count()),
                            () -> result.add(0.0)
                    ));
        });
        return result;
    }

    private <T extends AbstractAuditPOJO>
    List<Double> extractNumberOf(Class<T> clazz, Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .mapToDouble(userEvs -> userEvs.stream()
                        .filter(clazz::isInstance)
                        .count()).boxed().toList();
    }
}
