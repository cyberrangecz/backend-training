package cz.cyberrange.platform.training.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Classifies the kind of level held by a training definition */
public enum LevelType {
  INFO_LEVEL,
  TRAINING_LEVEL,
  ACCESS_LEVEL,
  ASSESSMENT_LEVEL;

  /**
   * Resolves a JSON string to its matching constant, accepting {@code "GAME_LEVEL"} as an alias for
   * {@link #TRAINING_LEVEL}.
   *
   * @param levelType the level type name to resolve
   * @return the matching constant, or {@code null} when none matches
   * @throws NullPointerException when the given name is null
   */
  @JsonCreator
  public static LevelType createLevelType(String levelType) {
    if (levelType.equals("GAME_LEVEL")) {
      levelType = "TRAINING_LEVEL";
    }
    for (LevelType type : LevelType.values()) {
      if (type.name().equals(levelType)) {
        return type;
      }
    }
    return null;
  }

  public String getName() {
    return this.name();
  }
}
