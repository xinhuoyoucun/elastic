package com.laiyuan.elastic.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author laiyuan
 * @date 2020/9/1
 */
@Data
@AllArgsConstructor
public class Result {
    private String patentId;
    private String name;
    private String applicant;
    private String abstractStr;
    private String publicationDate;
    private String inventor;
    private Highlight highlight;


    public Result() {
    }
}
