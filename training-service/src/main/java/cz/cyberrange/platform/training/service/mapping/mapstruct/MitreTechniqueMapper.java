package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Converts a MITRE ATT&amp;CK technique between its entity form and its DTO, both of which carry
 * the primary key and technique key as their only directly mapped data; the entity also holds an
 * inverse relation to its training levels that no method here touches. Used by {@code LevelMapper}
 * for a training level's {@code mitreTechniques}: the import direction copies the id, the export
 * direction strips it.
 */
@Mapper(
    componentModel = "spring",
    uses = {},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MitreTechniqueMapper {

  /**
   * Maps a MITRE technique into a new entity, copying id and technique key.
   *
   * @param dto the technique to map
   * @return the mapped technique entity
   */
  MitreTechnique mapToEntity(MitreTechniqueDTO dto);

  /**
   * Maps a MITRE technique to its DTO, copying id and technique key.
   *
   * @param dto the technique to map
   * @return the mapped DTO
   */
  MitreTechniqueDTO mapToDTO(MitreTechnique dto);

  /**
   * Maps a MITRE technique to its DTO, copying the technique key and leaving the id unset.
   *
   * @param dto the technique to map
   * @return the mapped DTO, with no id
   */
  @Named("ignoreId")
  @Mapping(target = "id", ignore = true)
  MitreTechniqueDTO mapToDTOIgnoreId(MitreTechnique dto);

  /**
   * Maps each MITRE technique into a new entity, as {@link #mapToEntity(MitreTechniqueDTO)}.
   *
   * @param dtos the techniques to map
   * @return the mapped technique entities
   */
  List<MitreTechnique> mapDTOsToList(Collection<MitreTechniqueDTO> dtos);

  /**
   * Maps each MITRE technique to its DTO, as {@link #mapToDTO(MitreTechnique)}.
   *
   * @param entities the techniques to map
   * @return the mapped DTOs
   */
  List<MitreTechniqueDTO> mapToListDTO(Collection<MitreTechnique> entities);

  /**
   * Maps each MITRE technique to its DTO, as {@link #mapToDTOIgnoreId(MitreTechnique)}.
   *
   * @param entities the techniques to map
   * @return the mapped DTOs, with no id
   */
  @Named("ignoreIds")
  @IterableMapping(qualifiedByName = "ignoreId")
  Set<MitreTechniqueDTO> mapToListDTOIgnoreIds(Collection<MitreTechnique> entities);
}
