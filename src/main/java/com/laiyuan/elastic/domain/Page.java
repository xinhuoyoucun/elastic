package com.laiyuan.elastic.domain;

import lombok.Data;

/**
 * @author laiyuan
 * @date 2020/9/1
 */
@Data
public class Page {
    private Integer totalPages;
    private Long totalResults;
    private Integer current;
    private Integer size;

    public Page(Integer current, Integer size) {
        this.current = current;
        this.size = size;
    }

    public void setSize(Integer size) {
        this.size = size > 100 ? 100 : size;
    }

    public Page() {
    }

    public void setTotalResults(Long totalResults) {
        this.totalResults = totalResults;
        int total = totalResults.intValue() / size;
        this.totalPages = totalResults.intValue() % size == 0 ? total : total + 1;
    }

    public int getFrom(){
        return size * (current - 1);
    }
}
