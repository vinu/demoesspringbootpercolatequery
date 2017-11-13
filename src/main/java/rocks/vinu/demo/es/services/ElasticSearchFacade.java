package rocks.vinu.demo.es.services;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.percolator.PercolateQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rocks.vinu.demo.es.exception.JsonBuilderException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Component
public class ElasticSearchFacade {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchFacade.class);

    @Value("${elastic.search.host}")
    private String esHost;

    @Value("${elastic.search.port}")
    private int esPort;


    @Value("${elastic.search.indexname:demoindex}")
    private String indexName;

    @Value("${elastic.search.clustername}")
    private String clusterName;

    @Value("${elastic.search.xpack.security.user}")
    private String xPackSecurityUser;


    private TransportClient client;

    /**
     * Adds query to the index
     *
     * @param query
     * @return the indexreponse
     */
    public IndexResponse addQuery(String query) {
        QueryBuilder qb = queryStringQuery(query).field("content");
        XContentBuilder docBuilder;
        try {
            docBuilder = jsonBuilder()
                    .startObject()
                    .field("query", qb) // Register the query
                    .endObject();
        } catch (IOException e) {
            throw new JsonBuilderException("Jsonbuilder exception in addQuery " + query, e);
        }
        IndexResponse indexResponse = client.prepareIndex(indexName, "query")
                .setSource(docBuilder)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE) // Needed when the query shall be available immediately
                .get();
        return indexResponse;

    }

    /**
     * Deletes the query with id
     *
     * @param id
     * @return
     */
    public DeleteResponse deleteQuery(String id) {
        return client.prepareDelete(indexName, "query", id)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();
    }


    /**
     * Does the search using the percolator
     *
     * @param content
     * @return
     */
    public SearchResponse search(String content) {
        XContentBuilder docBuilder;
        try {
            docBuilder = jsonBuilder().startObject();
            docBuilder.field("content", content);
            docBuilder.endObject();
        } catch (IOException e) {
            throw new JsonBuilderException("Jsonbuilder exception in search " + content, e);
        }

        PercolateQueryBuilder percolateQuery = new PercolateQueryBuilder("query", "docs", docBuilder.bytes(), XContentType.JSON);

        SearchResponse response = client.prepareSearch(indexName)
                .setQuery(percolateQuery)
                .highlighter(new HighlightBuilder().field("content"))
                .get();
        return response;
    }


    /**
     * Listing the queries attached with this index
     *
     * @return
     */
    public SearchResponse listQueries() {
        return client.prepareSearch(indexName)
                .get();
    }


    @PostConstruct
    protected void afterCreate() throws IOException {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .put("xpack.security.user", xPackSecurityUser)
                .build();
        try {
            client = new PreBuiltXPackTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        logger.info("Created es search client");

        AdminClient adminClient = client.admin();

        boolean indexExists = adminClient.indices().prepareExists(indexName).get().isExists();
        if (!indexExists) {
            CreateIndexResponse createIndexResponse = adminClient.indices().prepareCreate(indexName)
                    .addMapping("query", "query", "type=percolator")
                    .addMapping("docs", "content", "type=text").get();

            logger.info("Created es Index  " + createIndexResponse.index());
        }
    }

    @PreDestroy
    protected void preDestroy() {
        if (client != null) {
            client.close();
        }
    }
}
