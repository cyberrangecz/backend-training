package cz.muni.ics.kypo.training.api.RestResponses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PageResultResourcePython<E> {
    private int page;
    @JsonProperty("page_size")
    private int pageSize;
    @JsonProperty("page_count")
    private int pageCount;
    private int count;
    @JsonProperty("total_count")
    private int totalCount;
    private List<E> results;

    public PageResultResourcePython() {
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<E> getResults() {
        return results;
    }

    public void setResults(List<E> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "PageResultResourcePython{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", pageCount=" + pageCount +
                ", count=" + count +
                ", totalCount=" + totalCount +
                ", results=" + results +
                '}';
    }
}
