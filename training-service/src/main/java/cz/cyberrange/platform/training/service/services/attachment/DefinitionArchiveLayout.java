package cz.cyberrange.platform.training.service.services.attachment;

/** The names that give an exported definition archive its structure. */
final class DefinitionArchiveLayout {

  /** Entry holding the metadata of every attachment file. */
  static final String METADATA_ENTRY = "attachments.json";

  /** Folder prefix of the attachment file entries. */
  static final String ATTACHMENT_FOLDER = "attachments/";

  /** Most entries an archive may hold, the document and the metadata file included. */
  static final int MAXIMAL_ENTRIES = 1000;

  /** Largest metadata file an archive may hold, in bytes. */
  static final int MAXIMAL_METADATA_BYTES = 16 * 1024 * 1024;

  /** Largest definition document an archive may hold, in bytes. */
  static final int MAXIMAL_DOCUMENT_BYTES = 16 * 1024 * 1024;

  private DefinitionArchiveLayout() {}
}
