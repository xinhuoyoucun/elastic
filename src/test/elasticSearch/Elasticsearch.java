package elasticSearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author laiyuan
 * @date 2020/8/28
 */
public class Elasticsearch {
    public static void main(String[] args) throws IOException {
        String query="清扫";


        Elasticsearch elasticsearch = new Elasticsearch();
        RestHighLevelClient client = elasticsearch.connect();

        SearchObject searchObject=new SearchObject();
        searchObject.setSort(new Sort("id","desc"));
        searchObject.setPage(new Page());
        searchObject.setQuery(query);
        SearchSourceBuilder  sourceBuilder = searchObject.createSourceBuilder();


        SearchRequest searchRequest = new SearchRequest("patent");
        searchRequest.source(sourceBuilder);

        /**
         * ========================执行同步搜索==============================
         */
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        /**
         * 搜索结果
         */
        elasticsearch.getHits(searchResponse);
//        elasticsearch.getSuggest(searchResponse);
        elasticsearch.getAggs(searchResponse);


        elasticsearch.close(client);

    }






    private void getSuggest(SearchResponse searchResponse) {
        Suggest suggest = searchResponse.getSuggest();
        TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user");
        for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
            for (TermSuggestion.Entry.Option option : entry) {
                String suggestText = option.getText().string();
                System.out.println(suggestText);
            }
        }
    }

    private void getHits(SearchResponse searchResponse) {
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : hits.getHits()) {
           String name = hit.getSourceAsMap().get("name").toString();

            System.out.println(name);
        }
    }


    private void getAggs(SearchResponse searchResponse) {
        Aggregations aggregations = searchResponse.getAggregations();
        for (Aggregation agg : aggregations) {
            List<Terms.Bucket> buckets = (List<Terms.Bucket>) ((Terms) agg).getBuckets();
            System.out.println(agg.getName());
            for (Terms.Bucket bucket : buckets) {
                System.out.println(bucket.getKey().toString());
                System.out.println(bucket.getDocCount());
            }
        }
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
}
