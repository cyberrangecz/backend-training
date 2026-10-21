package cz.cyberrange.platform.training.service.services;

/**
 * Restricts a user listing of the user-and-group service by name. Each component left null places
 * no restriction.
 *
 * @param givenName keeps users whose given name matches
 * @param familyName keeps users whose family name matches
 * @param name keeps users whose given name or family name matches every word of it
 */
public record UserNameFilter(String givenName, String familyName, String name) {

  /** The filter that places no restriction at all. */
  public static final UserNameFilter NONE = new UserNameFilter(null, null, null);
}
