package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.responses.PageResultResource;
import org.springframework.data.domain.Page;

/**
 * ParentMapper is parent class for mappers which contains only one method for creating pagination.
 * Enum conversions are handled by {@link EnumMapper}.
 */
public interface ParentMapper {

  /**
   * Builds pagination metadata from a page's number, size, element counts and total page count.
   *
   * @param objects the page to read the metadata from
   * @return the resulting pagination metadata
   */
  default PageResultResource.Pagination createPagination(Page<?> objects) {
    PageResultResource.Pagination pageMetadata = new PageResultResource.Pagination();
    pageMetadata.setNumber(objects.getNumber());
    pageMetadata.setNumberOfElements(objects.getNumberOfElements());
    pageMetadata.setSize(objects.getSize());
    pageMetadata.setTotalElements(objects.getTotalElements());
    pageMetadata.setTotalPages(objects.getTotalPages());
    return pageMetadata;
  }
}
