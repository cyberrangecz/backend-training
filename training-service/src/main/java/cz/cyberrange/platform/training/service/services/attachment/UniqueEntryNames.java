package cz.cyberrange.platform.training.service.services.attachment;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Hands out names that are unique among everything it handed out, compared without regard to case.
 * A name already taken receives a counter in parentheses before its last extension.
 */
public final class UniqueEntryNames {

  private final Set<String> taken = new HashSet<>();

  /**
   * Reserves a name, or the first free numbered variant of it.
   *
   * @param name the wanted name
   * @return {@code name} when free, else {@code name} with " (n)" before its last extension, for
   *     the smallest n that is free
   */
  public String claim(String name) {
    String candidate = name;
    for (int counter = 1; !taken.add(candidate.toLowerCase(Locale.ROOT)); counter++) {
      candidate = withCounter(name, counter);
    }
    return candidate;
  }

  private static String withCounter(String name, int counter) {
    int lastDot = name.lastIndexOf('.');
    int insertAt = lastDot > 0 ? lastDot : name.length();
    return name.substring(0, insertAt) + " (" + counter + ")" + name.substring(insertAt);
  }
}
