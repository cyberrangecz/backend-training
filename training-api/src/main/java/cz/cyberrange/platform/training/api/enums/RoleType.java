package cz.cyberrange.platform.training.api.enums;

/**
 * Classifies a user role, sent by name as the {@code roleType} query parameter when this service
 * asks the user-and-group service for the users holding a role
 */
public enum RoleType {
  ROLE_TRAINING_ADMINISTRATOR,
  ROLE_TRAINING_DESIGNER,
  ROLE_TRAINING_ORGANIZER,
  ROLE_TRAINING_TRAINEE;
}
