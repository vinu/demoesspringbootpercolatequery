package rocks.vinu.demo.es.controller.dto;


import java.util.ArrayList;
import java.util.List;

public class ListQueryResponse {
    public long count;
    public List<Query> queries = new ArrayList<>();
}
