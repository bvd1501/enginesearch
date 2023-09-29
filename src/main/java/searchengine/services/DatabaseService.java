package searchengine.services;

import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Map;

public interface DatabaseService {


    SiteEntity initSite(String urlSite, String nameSite);
    boolean savePage(PageEntity page, Map<String, Integer> lemmaMap);

    long endSiteIndex(SiteEntity site);


    void updateLastErrorOnSite(SiteEntity site, String errorStr);

    boolean existPage(PageEntity pageEntity);

    SiteEntity findSiteByPageUrl(String url);
}
