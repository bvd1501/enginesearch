package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

public interface SiteIndexingService {
    IndexingResponse getStartIndexing();
    IndexingResponse getStopIndexing();
    boolean isStopFlag();
    void resetStopFlag();
    void setStopFlag();
    void saveSite(SiteEntity siteEntity, StatusType statusType, String last_error);

}
