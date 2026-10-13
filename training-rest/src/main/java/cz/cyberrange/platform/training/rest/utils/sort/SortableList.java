package cz.cyberrange.platform.training.rest.utils.sort;

import java.util.List;
import java.util.stream.Stream;

/**
 * The lists of properties a paged endpoint may be sorted by, as entity property paths. Each
 * constant is one list, selected through {@link SortableProperties}.
 */
public enum SortableList {
  TRAINING_DEFINITION(
      "id",
      "title",
      "description",
      "state",
      "estimatedDuration",
      "lastEdited",
      "lastEditedBy",
      "createdAt"),
  TRAINING_INSTANCE(
      "id",
      "title",
      "startTime",
      "endTime",
      "poolId",
      "accessToken",
      "lastEdited",
      "lastEditedBy",
      "localEnvironment",
      "sandboxDefinitionId",
      "showStepperBar",
      "backwardMode"),
  TRAINING_RUN(
      "id",
      "startTime",
      "endTime",
      "state",
      "incorrectAnswerCount",
      "solutionTaken",
      "totalTrainingScore",
      "totalAssessmentScore",
      "maxLevelScore",
      "currentPenalty",
      "hasDetectionEvent"),
  ACCESSIBLE_TRAINING_RUN(TRAINING_RUN, "trainingInstance.title"),
  CHEATING_DETECTION(
      "id",
      "executeTime",
      "executedBy",
      "proximityThreshold",
      "currentState",
      "results",
      "answerSimilarityState",
      "locationSimilarityState",
      "timeProximityState",
      "minimalSolveTimeState",
      "forbiddenCommandsState",
      "noCommandsState"),
  DETECTION_EVENT(
      "id",
      "detectedAt",
      "levelId",
      "levelOrder",
      "levelTitle",
      "participantCount",
      "detectionEventType",
      "trainingRunId"),
  DETECTION_EVENT_PARTICIPANT(
      "id", "occurredAt", "participantName", "ipAddress", "solvedInTime", "userId"),
  FORBIDDEN_COMMAND("id", "command", "type", "hostname", "occurredAt"),
  USER("id", "sub", "iss", "fullName", "givenName", "familyName", "mail", "status", "externalId");

  private final List<String> properties;

  SortableList(String... properties) {
    this.properties = List.of(properties);
  }

  SortableList(SortableList base, String... additionalProperties) {
    this.properties =
        Stream.concat(base.properties.stream(), Stream.of(additionalProperties)).toList();
  }

  /**
   * Returns the properties of this list.
   *
   * @return the sortable properties, in declaration order
   */
  public List<String> properties() {
    return properties;
  }
}
