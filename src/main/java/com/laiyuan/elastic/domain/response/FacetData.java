package com.laiyuan.elastic.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author laiyuan
 * @date 2020/9/2
 */
@Data
@AllArgsConstructor
public class FacetData {
    private String value;
    private Long count;
}
