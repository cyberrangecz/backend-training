package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * Which kind of level to create, named in the path of the level-creation endpoint and used to pick
 * the kind to append to a training definition. Its constants are named differently from the
 * like-named enumeration the DTOs carry, and the two are not interchangeable.
 */
public enum LevelType {
  ASSESSMENT,
  INFO,
  TRAINING,
  ACCESS;
}
