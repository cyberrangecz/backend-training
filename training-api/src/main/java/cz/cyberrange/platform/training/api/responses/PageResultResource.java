package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collections;
import java.util.List;

/**
 * Carries one page of results as a content list alongside its pagination metadata.
 *
 * @param <E> the type of element held in the page.
 */
@ApiModel(
    value = "PageResultResouce",
    description =
        "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
public class PageResultResource<E> {

  @JsonProperty(required = true)
  @ApiModelProperty(value = "Content - (Retrieved data) from databases.")
  private List<E> content;

  @JsonProperty(required = true)
  @ApiModelProperty(
      value =
          "Pagination including: page number, number of elements in page, size, total elements and total pages.")
  private Pagination pagination;

  public PageResultResource() {}

  /**
   * Creates a page over the given content, leaving the pagination metadata unset.
   *
   * @param content the elements the page carries.
   */
  public PageResultResource(List<E> content) {
    super();
    this.content = content;
  }

  /** Creates a page over the given content, carrying the given pagination metadata */
  public PageResultResource(List<E> content, Pagination pageMetadata) {
    super();
    this.content = content;
    this.pagination = pageMetadata;
  }

  /**
   * Returns the page content as an unmodifiable view.
   *
   * @return the page content; changes to the underlying list are reflected, but not permitted
   *     through the returned view.
   */
  public List<E> getContent() {
    return Collections.unmodifiableList(content);
  }

  public void setContent(List<E> content) {
    this.content = content;
  }

  public Pagination getPagination() {
    return pagination;
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

  /**
   * Describes where the enclosing page sits within the whole result set, and how large both the
   * page and that set are
   */
  public static class Pagination {

    @ApiModelProperty(value = "Page number.", example = "1")
    @JsonProperty(required = true)
    private int number;

    @ApiModelProperty(value = "Number of elements in page.", example = "20")
    @JsonProperty(required = true, value = "number_of_elements")
    private int numberOfElements;

    @ApiModelProperty(value = "Page size.", example = "20")
    @JsonProperty(required = true)
    private int size;

    @ApiModelProperty(
        value = "Total number of elements in this resource (in all Pages).",
        example = "100")
    @JsonProperty(required = true, value = "total_elements")
    private long totalElements;

    @ApiModelProperty(value = "Total number of pages.", example = "5")
    @JsonProperty(required = true, value = "total_pages")
    private int totalPages;

    public Pagination() {}

    /** Creates the pagination metadata from the given page position and size values */
    public Pagination(
        int number, int numberOfElements, int size, long totalElements, int totalPages) {
      super();
      this.number = number;
      this.numberOfElements = numberOfElements;
      this.size = size;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

    public int getNumberOfElements() {
      return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
      this.numberOfElements = numberOfElements;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public long getTotalElements() {
      return totalElements;
    }

    public void setTotalElements(long totalElements) {
      this.totalElements = totalElements;
    }

    public int getTotalPages() {
      return totalPages;
    }

    public void setTotalPages(int totalPages) {
      this.totalPages = totalPages;
    }

    @Override
    public String toString() {
      return "PageMetadata [number="
          + number
          + ", numberOfElements="
          + numberOfElements
          + ", size="
          + size
          + ", totalElements="
          + totalElements
          + ", totalPages="
          + totalPages
          + "]";
    }
  }

  @Override
  public String toString() {
    return "PageResultDTO [content="
        + content
        + ", pageMetadata="
        + pagination
        + ", getContent()="
        + getContent()
        + ", getPageMetadata()="
        + getPagination()
        + "]";
  }
}
