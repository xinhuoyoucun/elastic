package com.laiyuan.elastic.domain.request;

import lombok.Data;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * @author laiyuan
 * @date 2020/8/31
 */
public class Sort {
    public static final String ASC = "asc";

    private String fieldName;
    private String order;

    public SortBuilder<?> sortBuild(){
        SortBuilder<?> sortBuilder;
        if(fieldName==null|| "_score".equals(fieldName)){
           sortBuilder = new ScoreSortBuilder();
        }else{
            sortBuilder = new FieldSortBuilder(fieldName);
        }

        if(ASC.equals(order)){
            sortBuilder.order(SortOrder.ASC);
        }else{
            sortBuilder.order(SortOrder.DESC);
        }

        return sortBuilder;
    }


    public Sort(String fieldName, String order) {
        this.fieldName = fieldName;
        this.order = order;
    }

    public Sort(String fieldName) {
        this.fieldName = fieldName;
    }

    public Sort() {
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
