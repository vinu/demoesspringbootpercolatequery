package rocks.vinu.demo.es.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.vinu.demo.es.controller.dto.*;
import rocks.vinu.demo.es.services.SearchService;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PutMapping("/query")
    public CreateQueryResponse createQuery(@RequestBody CreateQueryRequest createQueryRequest) {
        return searchService.createQuery(createQueryRequest);
    }

    @DeleteMapping("/query")
    public DeleteQueryResponse deleteQuery(@RequestBody DeleteQueryRequest deleteQueryRequest) {
        return searchService.deleteQuery(deleteQueryRequest);
    }

    @GetMapping("/query")
    public ListQueryResponse listQueries() {
        return searchService.listQueries();
    }

    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest searchRequest) {
        return searchService.search(searchRequest);
    }
}
