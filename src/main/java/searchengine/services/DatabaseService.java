package searchengine.services;

import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Map;

public interface DatabaseService {


    SiteEntity createSite(String urlSite, String nameSite);
    boolean saveIndexPage(SiteEntity site, PageEntity page, Map<String, Integer> lemmaMap);

    long endSiteIndex(SiteEntity site);


    void updateLastErrorOnSite(SiteEntity site, String errorStr);

    boolean existPage(SiteEntity site, String pathPage);

}
