package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    /**
     * @param fragment 
     * @return
     */
    @Override
    public SearchResponse getSearch(String fragment, String site) {
        return new SearchResponse("Ошибочка. Поиск не настроен");
    }
}
