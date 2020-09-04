package com.laiyuan.elastic.controller;

import com.laiyuan.elastic.domain.request.SearchSource;
import com.laiyuan.elastic.domain.request.Sort;
import com.laiyuan.elastic.domain.response.FacetData;
import com.laiyuan.elastic.domain.Page;
import com.laiyuan.elastic.domain.response.Highlight;
import com.laiyuan.elastic.domain.response.Result;
import com.laiyuan.elastic.domain.response.ResultVO;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author laiyuan
 * @date 2020/9/1
 */
@RestController
@RequestMapping("/elastic")
@CrossOrigin()
public class SearchController {

    private static final RestHighLevelClient client = new SearchController().connect();

    @GetMapping("/search")
    public ResultVO search(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "current", defaultValue = "1") int current,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "applicant", required = false) String applicant,
            @RequestParam(name = "unitType", required = false) String unitType,
            @RequestParam(name = "patentType", required = false) String patentType
    ) throws IOException {
        Page page = new Page(current, size);

        SearchSource searchSource = new SearchSource();
        Sort sort = sortBy == null ? new Sort() : new Sort(sortBy, "desc");
        searchSource.setSort(sort);
        searchSource.setPage(page);
        searchSource.setQuery(query);
        Map<String, String> queryParams = new HashMap<>(3);
        queryParams.put("applicant", applicant);
        queryParams.put("unit_type", unitType);
        queryParams.put("patent_type", patentType);
        searchSource.setQueryParams(queryParams);
        SearchSourceBuilder sourceBuilder = searchSource.createSourceBuilder();

        SearchRequest searchRequest = new SearchRequest("patent");
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return result(page, searchResponse);
    }

    @GetMapping("/getMoreAgg")
    public List<FacetData> search(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "aggField") String aggField,
            @RequestParam(name = "size") int size
    ) throws IOException {
        List<FacetData> facetDataList = new ArrayList<>();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /**
         * 构建查询条件
         */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.existsQuery("applicant"))
                .mustNot(QueryBuilders.termQuery("applicant", "null"))
                .should(QueryBuilders.matchQuery("name", query).boost(5))
                .must(QueryBuilders.matchQuery("abstract", query));
        sourceBuilder.query(boolQueryBuilder).size(0);
        sourceBuilder.aggregation(AggregationBuilders.terms(aggField.concat("s"))
                .field(aggField).size(size));

        SearchRequest searchRequest = new SearchRequest("patent");
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        Aggregation agg = aggregations.get(aggField.concat("s"));
        List<Terms.Bucket> buckets = (List<Terms.Bucket>) ((Terms) agg).getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String value = bucket.getKey().toString();
            long count = bucket.getDocCount();
            facetDataList.add(new FacetData(value, count));
        }

        return facetDataList;
    }

    private ResultVO result(Page page, SearchResponse searchResponse) {
        //facets
        Map<String, List<FacetData>> facets = facets(searchResponse);

        //metaPage
        long totalHits = searchResponse.getHits().getTotalHits().value;
        page.setTotalResults(totalHits);

        //result
        List<Result> results = results(searchResponse);

        if (results.size() > 0) {
            return new ResultVO(facets, page, results, true);
        }
        return new ResultVO(facets, page, results, false);
    }

    private List<Result> results(SearchResponse searchResponse) {
        List<Result> results = new ArrayList<>();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> objectMap = searchHit.getSourceAsMap();
            String name = objectMap.get("name") == null ? null : objectMap.get("name").toString();
            String applicant = objectMap.get("applicant") == null ? null : objectMap.get("applicant").toString();
            String abstractStr = objectMap.get("abstract") == null ? null : objectMap.get("abstract").toString();
            String patentId = objectMap.get("PATENT_ID") == null ? null : objectMap.get("PATENT_ID").toString();
            String publicationDate = objectMap.get("publication_date") == null ? null : objectMap.get("publication_date").toString();
            String inventor = objectMap.get("inventor") == null ? null : objectMap.get("inventor").toString();
            Highlight highlight = highlighterResults(searchHit.getHighlightFields());
            Result result = new Result(patentId, name, applicant, abstractStr, publicationDate, inventor, highlight);

            results.add(result);
        }
        return results;
    }

    private Highlight highlighterResults(Map<String, HighlightField> highlightFields) {
        Highlight highlight = new Highlight();
        HighlightField highlightName = highlightFields.get("name");
        HighlightField highlightAbstract = highlightFields.get("abstract");

        if (highlightName != null) {
            String highlightNameStr = highlightName.fragments()[0].toString();
            highlight.setName(highlightNameStr);
        }
        if (highlightAbstract != null) {
            String highlightAbstractStr = highlightAbstract.fragments()[0].toString();
            highlight.setAbstractStr(highlightAbstractStr);
        }

        return highlight;
    }

    private Map<String, List<FacetData>> facets(SearchResponse searchResponse) {
        Map<String, List<FacetData>> facets = new HashMap<>(4);
        List<FacetData> facetDataList;
        Aggregations aggregations = searchResponse.getAggregations();

        for (Aggregation agg : aggregations) {
            facetDataList = new ArrayList<>(5);
            List<Terms.Bucket> buckets = (List<Terms.Bucket>) ((Terms) agg).getBuckets();
            String field = agg.getName();
            for (Terms.Bucket bucket : buckets) {
                String value = bucket.getKey().toString();
                long count = bucket.getDocCount();
                facetDataList.add(new FacetData(value, count));
            }
            facets.put(field, facetDataList);
        }

        return facets;
    }

    private RestHighLevelClient connect() {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("10.1.13.143", 9200, "http"),
                new HttpHost("10.1.13.143", 9201, "http"),
                new HttpHost("10.1.13.143", 9202, "http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }


    private void close(RestHighLevelClient client) {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @GetMapping("/suggest")
    public String suggest() {
        return "just suggest";
    }
}
