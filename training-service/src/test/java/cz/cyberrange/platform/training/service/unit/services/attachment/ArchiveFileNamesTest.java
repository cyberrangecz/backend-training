package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.training.service.services.attachment.ArchiveFileNames;
import cz.cyberrange.platform.training.service.services.attachment.UniqueEntryNames;
import org.junit.jupiter.api.Test;

class ArchiveFileNamesTest {

  @Test
  void cleanRemovesPathSeparatorsControlCharactersAndWindowsForbiddenCharacters() {
    assertEquals("....etcpasswd", ArchiveFileNames.clean("../../etc/passwd"));
    assertEquals("ab", ArchiveFileNames.clean("a\u0000\n\u007fb"));
    assertEquals("abcdefgh", ArchiveFileNames.clean("a\\b<c>d:e\"f|g?h*"));
  }

  @Test
  void cleanTrimsAndKeepsInnerSpacesAndNonAsciiLetters() {
    assertEquals("Zpráva o testu.txt", ArchiveFileNames.clean("  Zpráva o testu.txt \t"));
  }

  @Test
  void cleanOfTextWithNothingUsableLeftIsEmpty() {
    assertEquals("", ArchiveFileNames.clean(null));
    assertEquals("", ArchiveFileNames.clean("   "));
    assertEquals("", ArchiveFileNames.clean("/\\:"));
    assertEquals("", ArchiveFileNames.clean(".."));
    assertEquals("", ArchiveFileNames.clean("../.."));
  }

  @Test
  void cleanOrElseFallsBackOnlyWhenNothingIsLeft() {
    assertEquals("fallback", ArchiveFileNames.cleanOrElse("..", "fallback"));
    assertEquals("kept", ArchiveFileNames.cleanOrElse("kept", "fallback"));
  }

  @Test
  void uniqueNamesAreComparedIgnoringCaseAndNumberedBeforeTheLastExtension() {
    UniqueEntryNames names = new UniqueEntryNames();

    assertEquals("diagram.png", names.claim("diagram.png"));
    assertEquals("diagram (1).png", names.claim("diagram.png"));
    assertEquals("Diagram (2).PNG", names.claim("Diagram.PNG"));
    assertEquals("diagram (1) (1).png", names.claim("diagram (1).png"));
  }

  @Test
  void uniqueNamesWithoutAnExtensionTakeTheNumberAtTheEnd() {
    UniqueEntryNames names = new UniqueEntryNames();

    assertEquals("notes", names.claim("notes"));
    assertEquals("NOTES (1)", names.claim("NOTES"));
    assertEquals(".hidden", names.claim(".hidden"));
    assertEquals(".hidden (1)", names.claim(".hidden"));
    assertEquals("a.tar.gz", names.claim("a.tar.gz"));
    assertEquals("a.tar (1).gz", names.claim("a.tar.gz"));
  }
}
