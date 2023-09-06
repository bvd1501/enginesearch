package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

public interface SiteIndexingService {
    IndexingResponse getStartIndexing();
    IndexingResponse getStopIndexing();
    boolean isStopFlag();
}
