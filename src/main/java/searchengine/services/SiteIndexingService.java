package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

public interface SiteIndexingService {
    IndexingResponse getStartIndexing();
    IndexingResponse getStopIndexing();
    boolean isStopFlag();
    void resetStopFlag();
    void setStopFlag();

}
