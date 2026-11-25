package cz.cyberrange.platform.training.service.services.attachment;

/** Turns arbitrary text into a name that is safe as a file or archive entry name. */
public final class ArchiveFileNames {

  private static final String FORBIDDEN_CHARACTERS = "\\/<>:\"|?*";

  private ArchiveFileNames() {}

  /**
   * Removes path separators, control characters and the characters Windows forbids in file names,
   * then trims. A result made only of dots is empty, because it would name a directory reference.
   *
   * @param name the text to clean, may be {@code null}
   * @return the cleaned name, empty when nothing usable is left
   */
  public static String clean(String name) {
    if (name == null) {
      return "";
    }
    StringBuilder cleaned = new StringBuilder(name.length());
    name.codePoints().filter(ArchiveFileNames::isAllowed).forEach(cleaned::appendCodePoint);
    String trimmed = cleaned.toString().strip();
    return trimmed.chars().allMatch(character -> character == '.') ? "" : trimmed;
  }

  /**
   * Cleans a name and substitutes a fallback when nothing usable is left.
   *
   * @param name the text to clean, may be {@code null}
   * @param fallback the name used when the cleaned text is empty
   * @return the cleaned name, or {@code fallback}
   */
  public static String cleanOrElse(String name, String fallback) {
    String cleaned = clean(name);
    return cleaned.isEmpty() ? fallback : cleaned;
  }

  private static boolean isAllowed(int codePoint) {
    return !Character.isISOControl(codePoint) && FORBIDDEN_CHARACTERS.indexOf(codePoint) < 0;
  }
}
