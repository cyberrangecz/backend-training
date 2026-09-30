package cz.cyberrange.platform.training.service.enums;

/**
 * The authority names this service authorizes against. A constant's own name is the authority
 * string compared against the caller's granted authorities, so the security annotations name these
 * constants rather than repeating the strings.
 */
public enum RoleTypeSecurity {
  ROLE_TRAINING_ADMINISTRATOR,
  ROLE_TRAINING_DESIGNER,
  ROLE_TRAINING_ORGANIZER,
  ROLE_TRAINING_TRAINEE
}
