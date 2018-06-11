package cz.muni.ics.kypo.transfer;

import org.jsondoc.core.annotation.ApiObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiObject(name = "Result info (Page)",
    description = "Meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
public class ResultInfoDTO {

  @JsonProperty(required = true)
  private int number;
  @JsonProperty(required = true)
  private int numberOfElements;
  @JsonProperty(required = true)
  private int size;
  @JsonProperty(required = true)
  private long totalElements;
  @JsonProperty(required = true)
  private int totalPages;

  public ResultInfoDTO() {}

  public ResultInfoDTO(int number, int numberOfElements, int size, long totalElements, int totalPages) {
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
    return "ResultInfoDTO [number=" + number + ", numberOfElements=" + numberOfElements + ", size=" + size + ", totalElements=" + totalElements
        + ", totalPages=" + totalPages + "]";
  }

}
