package cz.cyberrange.platform.training.service.utils;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import java.util.Set;

/**
 * Shared steps every cheating detection takes when it turns suspicious submissions into a finding:
 * recognising a trainee already gathered, building the participant record for one submission, and
 * rendering a set of participants as the text a finding carries.
 */
public enum CheatingDetectionUtils {
  ;

  /**
   * Reports whether the set already holds the same trainee as the given participant, comparing on
   * the trainee alone and disregarding the submission details each record carries.
   *
   * @param participants the participants gathered for a finding so far
   * @param participant the participant to look for
   * @return whether that trainee is already among them
   */
  public static boolean checkIfContainsParticipant(
      Set<DetectionEventParticipant> participants, DetectionEventParticipant participant) {
    return participants.stream().anyMatch(elem -> elem.getUserId().equals(participant.getUserId()));
  }

  /**
   * Builds a participant record from the submission without a solving time, for a finding that does
   * not turn on how long the trainee took.
   *
   * @param s the submission that implicates the trainee
   * @param participantName the trainee's display name
   * @return the participant record, its solving time left unset
   */
  public static DetectionEventParticipant extractParticipant(Submission s, String participantName) {
    return extractParticipant(s, false, 0L, participantName);
  }

  /**
   * Builds a participant record from the submission, carrying the address it came from, the moment
   * it was made, and the trainee's {@code userRefId} rather than the local key of the user row. The
   * solving time is attached only when asked for, so a finding that does not turn on solving speed
   * leaves it unset.
   *
   * @param s the submission that implicates the trainee
   * @param isMinimal whether to attach the solving time
   * @param solvedInTime the solving time to attach, ignored otherwise
   * @param participantName the trainee's display name
   * @return the participant record
   */
  public static DetectionEventParticipant extractParticipant(
      Submission s, boolean isMinimal, Long solvedInTime, String participantName) {
    DetectionEventParticipant participant = new DetectionEventParticipant();
    participant.setIpAddress(s.getIpAddress());
    participant.setUserId(s.getTrainingRun().getParticipantRef().getUserRefId());
    participant.setOccurredAt(s.getDate());
    participant.setParticipantName(participantName);
    if (isMinimal) {
      participant.setSolvedInTime(solvedInTime);
    }
    return participant;
  }

  /**
   * Joins the participants' display names into the single comma-separated string a finding stores.
   * The set's own iteration order decides the order of the names, so it is not stable between runs.
   *
   * @param participants the participants to render
   * @return their names separated by a comma and a space, empty when there are none
   */
  public static String generateParticipantString(Set<DetectionEventParticipant> participants) {
    StringBuilder participantString = new StringBuilder();
    for (var participant : participants) {
      participantString.append(',').append(' ').append(participant.getParticipantName());
    }
    participantString.delete(0, 2);
    return participantString.toString();
  }
}
