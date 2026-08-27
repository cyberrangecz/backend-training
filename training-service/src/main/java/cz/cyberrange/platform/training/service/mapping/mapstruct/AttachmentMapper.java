package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.export.AttachmentExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AttachmentImportDTO;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Converts a training level's attachments between their entity form and the import and export DTOs
 * used when a training definition is transferred as a file
 */
@Mapper(
    componentModel = "spring",
    uses = {},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttachmentMapper {

  /**
   * Maps an imported attachment into a new entity, copying its content. The primary key, creation
   * time and owning training level carry no matching source field and are left unset.
   *
   * @param dto the imported attachment to map
   * @return the mapped attachment entity
   */
  Attachment mapImportDTOToEntity(AttachmentImportDTO dto);

  /**
   * Maps an attachment to its export DTO, copying its content.
   *
   * @param entity the attachment to map
   * @return the exported attachment
   */
  AttachmentExportDTO mapToExportDTO(Attachment entity);

  /**
   * Maps each imported attachment into a new entity, as {@link
   * #mapImportDTOToEntity(AttachmentImportDTO)}.
   *
   * @param dtos the imported attachments to map
   * @return the mapped attachment entities
   */
  List<Attachment> mapImportDTOsToList(Collection<AttachmentImportDTO> dtos);

  /**
   * Maps each attachment to its export DTO, as {@link #mapToExportDTO(Attachment)}.
   *
   * @param entities the attachments to map
   * @return the exported attachments
   */
  List<AttachmentExportDTO> mapToListExportDTO(Collection<Attachment> entities);
}
