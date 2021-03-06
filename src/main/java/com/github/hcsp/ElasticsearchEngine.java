package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ElasticsearchEngine {

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("Please input a search keyword:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String keyword = reader.readLine();
            System.out.println(keyword);

            search(keyword);
        }
    }

    private static void search(String keyword) throws IOException {
        // 和Elasticsearch的服务器交互必须有一个client客户端
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest request = new SearchRequest("news");
            // request.source(new SearchSourceBuilder().query(new MatchQueryBuilder("title",keyword)));
            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));
            SearchResponse result = client.search(request, RequestOptions.DEFAULT);
            // result.getHits 有多少搜索命中
            result.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        }
    }
}
