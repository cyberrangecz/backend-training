package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link ParentMapper}.
 *
 * <p>Tests the {@code createPagination(Page<?>)} default method which extracts pagination metadata
 * from a Spring Data {@link Page} object into a {@link PageResultResource.Pagination} DTO.
 */
@DisplayName("ParentMapper")
class ParentMapperTest {

  private ParentMapper sut;

  @BeforeEach
  void setUp() {
    sut = new ParentMapper() {};
  }

  @Nested
  @DisplayName("createPagination(Page<?>)")
  class CreatePagination {

    @Test
    @DisplayName("should extract all pagination fields from Page")
    void shouldExtractAllPaginationFieldsFromPage() {
      Pageable pageable = PageRequest.of(2, 10);
      Page<Object> page = new PageImpl<>(Collections.emptyList(), pageable, 55L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(2, result.getNumber());
      assertEquals(0, result.getNumberOfElements());
      assertEquals(10, result.getSize());
      assertEquals(55L, result.getTotalElements());
      assertEquals(6, result.getTotalPages());
    }

    @Test
    @DisplayName("should handle first page with full elements")
    void shouldHandleFirstPageWithFullElements() {
      Pageable pageable = PageRequest.of(0, 20);
      Page<Object> page = new PageImpl<>(Collections.emptyList(), pageable, 100L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(0, result.getNumber());
      assertEquals(0, result.getNumberOfElements());
      assertEquals(20, result.getSize());
      assertEquals(100L, result.getTotalElements());
      assertEquals(5, result.getTotalPages());
    }

    @Test
    @DisplayName("should handle empty page")
    void shouldHandleEmptyPage() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Object> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(0, result.getNumber());
      assertEquals(0, result.getNumberOfElements());
      assertEquals(10, result.getSize());
      assertEquals(0L, result.getTotalElements());
      assertEquals(0, result.getTotalPages());
    }

    @Test
    @DisplayName("should handle single element page")
    void shouldHandleSingleElementPage() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Object> page = new PageImpl<>(Collections.singletonList(new Object()), pageable, 1L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(0, result.getNumber());
      assertEquals(1, result.getNumberOfElements());
      assertEquals(10, result.getSize());
      assertEquals(1L, result.getTotalElements());
      assertEquals(1, result.getTotalPages());
    }

    @Test
    @DisplayName("should handle last page with partial elements")
    void shouldHandleLastPageWithPartialElements() {
      Pageable pageable = PageRequest.of(4, 10);
      Page<Object> page = new PageImpl<>(Collections.emptyList(), pageable, 45L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(4, result.getNumber());
      assertEquals(0, result.getNumberOfElements());
      assertEquals(10, result.getSize());
      assertEquals(45L, result.getTotalElements());
      assertEquals(5, result.getTotalPages());
    }

    @Test
    @DisplayName("should handle large totalElements value")
    void shouldHandleLargeTotalElementsValue() {
      Pageable pageable = PageRequest.of(0, 100);
      Page<Object> page = new PageImpl<>(Collections.emptyList(), pageable, 1_000_000L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(0, result.getNumber());
      assertEquals(100, result.getSize());
      assertEquals(1_000_000L, result.getTotalElements());
      assertEquals(10000, result.getTotalPages());
    }
  }
}
