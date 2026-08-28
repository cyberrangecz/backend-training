package cz.cyberrange.platform.training.service.startup;

import javax.validation.Valid;

/**
 * The whole of the configured default level content: one starting shape per level kind that has
 * one. Both are validated along with this object, so a defect in either fails the load.
 */
public class DefaultLevels {
  @Valid private DefaultInfoLevel defaultInfoLevel;
  @Valid private DefaultAccessLevel defaultAccessLevel;

  public DefaultInfoLevel getDefaultInfoLevel() {
    return defaultInfoLevel;
  }

  public void setDefaultInfoLevel(DefaultInfoLevel defaultInfoLevel) {
    this.defaultInfoLevel = defaultInfoLevel;
  }

  public DefaultAccessLevel getDefaultAccessLevel() {
    return defaultAccessLevel;
  }

  public void setDefaultAccessLevel(DefaultAccessLevel defaultAccessLevel) {
    this.defaultAccessLevel = defaultAccessLevel;
  }
}
