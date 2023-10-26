package searchengine.services;

import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Map;

public interface RepoService {


    SiteEntity initSite(String urlSite, String nameSite);
    boolean saveAllDataPage(PageEntity page, Map<String, Integer> lemmaMap, String error);

    void endSiteIndex(SiteEntity site);



    boolean existPage(PageEntity pageEntity);

    SiteEntity findSite(String url, String name);

    void cleanPage(PageEntity pageEntity);


    int countPagesOnSite(SiteEntity siteEntity);

    int countLemmasOnSite(SiteEntity siteEntity);


}
