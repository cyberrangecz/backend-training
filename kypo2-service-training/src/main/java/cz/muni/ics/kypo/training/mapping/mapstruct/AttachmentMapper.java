package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.export.AttachmentExportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AttachmentImportDTO;
import cz.muni.ics.kypo.training.persistence.model.Attachment;
import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttachmentMapper {

    Attachment mapImportDTOToEntity(AttachmentImportDTO dto);

    AttachmentExportDTO mapToExportDTO(Attachment entity);

    List<Attachment> mapImportDTOsToList(Collection<AttachmentImportDTO> dtos);

    List<AttachmentExportDTO> mapToListExportDTO(Collection<Attachment> entities);




}
