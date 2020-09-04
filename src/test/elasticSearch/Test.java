package elasticSearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;

import java.io.IOException;

/**
 * @author laiyuan
 * @date 2020/8/31
 */
public class Test {

    public static void main(String[] args) throws IOException {
        Test test=new Test();
        RestHighLevelClient client = test.connect();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestionBuilder termSuggestionBuilder =
                SuggestBuilders.termSuggestion("name").text("数据挖局");
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("name", termSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);

        SearchResponse searchResponse = client.search(new SearchRequest("patent"), RequestOptions.DEFAULT);

        Suggest suggest = searchResponse.getSuggest();
        TermSuggestion termSuggestion = suggest.getSuggestion("name");
        for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
            for (TermSuggestion.Entry.Option option : entry) {
                String suggestText = option.getText().string();
                System.out.println(suggestText);
            }
        }

        test.close(client);
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
