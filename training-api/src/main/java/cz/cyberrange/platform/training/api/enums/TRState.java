package cz.cyberrange.platform.training.api.enums;

/** Classifies the lifecycle state of a training run. */
public enum TRState {

  /** Set from creation until the run is finished or archived. */
  RUNNING,
  /** Set once the run's current level is the last one and has been answered. */
  FINISHED,
  /** Set when the run is archived; its sandbox instance reference is cleared at the same time. */
  ARCHIVED;
}
