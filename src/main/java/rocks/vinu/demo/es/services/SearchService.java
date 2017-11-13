package rocks.vinu.demo.es.services;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocks.vinu.demo.es.controller.dto.*;

import java.util.Map;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ElasticSearchFacade elasticSearchFacade;

    /**
     * Creates query
     *
     * @param createQueryRequest
     * @return
     */
    public CreateQueryResponse createQuery(CreateQueryRequest createQueryRequest) {
        IndexResponse indexResponse = elasticSearchFacade.addQuery(createQueryRequest.query);
        CreateQueryResponse createQueryResponse = new CreateQueryResponse();
        createQueryResponse.id = indexResponse.getId();
        return createQueryResponse;
    }

    /**
     * Deletes the query
     *
     * @param deleteQueryRequest
     * @return
     */
    public DeleteQueryResponse deleteQuery(DeleteQueryRequest deleteQueryRequest) {
        DeleteResponse deleteResponse = elasticSearchFacade.deleteQuery(deleteQueryRequest.id);

        DeleteQueryResponse deleteQueryResponse = new DeleteQueryResponse();
        switch (deleteResponse.getResult().getLowercase()) {
            case "deleted":
                deleteQueryResponse.status = "OK";
                break;
            case "not_found":
                deleteQueryResponse.status = "Not found";
                break;
            default:
                deleteQueryResponse.status = deleteResponse.getResult().getLowercase();
        }

        return deleteQueryResponse;
    }

    /**
     * List the queries
     *
     * @return
     */
    public ListQueryResponse listQueries() {
        ListQueryResponse listQueryResponse = new ListQueryResponse();
        org.elasticsearch.action.search.SearchResponse esSearchResponse = elasticSearchFacade.listQueries();
        listQueryResponse.count = esSearchResponse.getHits().getTotalHits();
        Gson gson = new Gson();
        for (SearchHit esSearchHit : esSearchResponse.getHits()) {
            Query query = new Query();
            query.id = esSearchHit.getId();
            JsonObject jsonElement = gson.fromJson(esSearchHit.getSourceAsString(), JsonObject.class);
            query.query = jsonElement.getAsJsonObject("query").getAsJsonObject("query_string").get("query").getAsString();
            listQueryResponse.queries.add(query);
        }
        return listQueryResponse;
    }

    /**
     * Does the search
     *
     * @param searchRequest
     * @return
     */
    public SearchResponse search(SearchRequest searchRequest) {
        org.elasticsearch.action.search.SearchResponse esSearchResponse = elasticSearchFacade.search(searchRequest.text);
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.tookInMillis = esSearchResponse.getTookInMillis();
        Gson gson = new Gson();
        for (SearchHit searchHit : esSearchResponse.getHits()) {
            Map<String, HighlightField> highlightFieldMap = searchHit.getHighlightFields();
            rocks.vinu.demo.es.controller.dto.SearchHit hit = new rocks.vinu.demo.es.controller.dto.SearchHit();
            hit.id = searchHit.getId();
            JsonObject jsonElement = gson.fromJson(searchHit.getSourceAsString(), JsonObject.class);
            hit.query = jsonElement.getAsJsonObject("query").getAsJsonObject("query_string").get("query").getAsString();
            hit.score = searchHit.getScore();
            hit.hightLight = highlightFieldMap.get("content").getFragments()[0].toString();
            searchResponse.searchHits.add(hit);
        }
        return searchResponse;
    }
}