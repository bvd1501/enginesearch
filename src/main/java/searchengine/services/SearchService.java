package searchengine.services;

import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse getSearch(String fragment, String site);
}
