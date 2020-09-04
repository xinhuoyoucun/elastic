package com.laiyuan.elastic.domain.request;

import com.laiyuan.elastic.domain.Page;
import lombok.Data;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;


import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author laiyuan
 * @date 2020/9/2
 */
@Data
public class SearchSource {
    private String query;
    private Sort sort;
    private Facets facets;
    private Page page;
    private Map<String, String> queryParams;

    public SearchSourceBuilder createSourceBuilder() {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /**
         * 构建查询条件
         */
        sourceBuilder.query(createQueryBuilder(query,queryParams));
        /**
         * 分页
         */
        sourceBuilder.from(page.getFrom());
        sourceBuilder.size(page.getSize());
        /**
         * 排序
         */
        sourceBuilder.sort(sort.sortBuild());
        /**
         * 超时
         */
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        /**
         * 只返回包含的内容
         * 不返回包含的内容
         */
        String[] includeFields = new String[]{"PATENT_ID","name", "abstract", "applicant", "patent_type", "unit_type","publication_date"};
        String[] excludeFields = new String[]{"agent"};
        sourceBuilder.fetchSource(includeFields, excludeFields);
        /**
         * 高亮
         */
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        String[] highlightFields = {"name", "abstract"};
        for (String str : highlightFields) {
            highlightBuilder.field(str);
        }
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        /**
         * 聚合
         */
        String[] aggFields = {"applicant", "patent_type", "unit_type"};
        createAggs(sourceBuilder, aggFields);
        /**
         * 过滤结果
         */
//        sourceBuilder.postFilter(QueryBuilders.boolQuery()
//                .filter(QueryBuilders.termQuery("unit_type","03"))
//                .filter(QueryBuilders.termQuery("applicant","桐乡市绿康菊业有限公司"))
//        );

        return sourceBuilder;
    }

    /**
     * query,根据实际情况调整配置
     *
     * @param query
     * @return
     */
    private BoolQueryBuilder createQueryBuilder(String query, Map<String, String> queryParams) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.existsQuery("applicant"))
                .mustNot(QueryBuilders.termQuery("applicant","null"))
                .should(QueryBuilders.matchQuery("name", query).boost(5))
                .must(QueryBuilders.matchQuery("abstract", query));

        if (queryParams==null){
            return boolQueryBuilder;
        }

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if(entry.getValue()!=null){
                boolQueryBuilder.filter(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));
            }
        }
        return boolQueryBuilder;
    }


    /**
     * 聚合
     *
     * @return
     */
    private SearchSourceBuilder createAggs(SearchSourceBuilder sourceBuilder, String[] aggFields) {
        for (String str : aggFields) {
            sourceBuilder.aggregation(AggregationBuilders.terms(str.concat("s"))
                    .field(str).size(5));
        }
//        sourceBuilder.aggregation(AggregationBuilders.filter("my_agg",QueryBuilders.termQuery("unit_type","03"))
//                .subAggregation(AggregationBuilders.terms("my_applicant").field("applicant").size(3))
//                .subAggregation(AggregationBuilders.terms("my_patent_type").field("patent_type").size(3))
//        );

        return sourceBuilder;
    }
}
