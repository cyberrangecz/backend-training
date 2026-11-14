package cz.cyberrange.platform.training.service.services.attachment;

import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import java.util.List;

/**
 * A definition archive read and checked for import.
 *
 * @param definition the definition document, parsed and validated
 * @param attachments the attachment files, each carrying its metadata
 */
public record DefinitionArchive(
    ImportTrainingDefinitionDTO definition, List<SuppliedAttachment> attachments) {}
