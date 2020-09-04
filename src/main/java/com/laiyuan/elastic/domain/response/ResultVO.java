package com.laiyuan.elastic.domain.response;

import com.laiyuan.elastic.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author laiyuan
 * @date 2020/9/2
 */
@Data
@AllArgsConstructor
public class ResultVO {
    private Map<String, List<FacetData>> facets;
    private Page page;
    private List<Result> results;
    private Boolean wasSearched;
}
