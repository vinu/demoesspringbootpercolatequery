package rocks.vinu.demo.es.controller.dto;


import java.util.ArrayList;
import java.util.List;

public class SearchResponse {
    public long tookInMillis;
    public List<SearchHit> searchHits = new ArrayList<>();
}
