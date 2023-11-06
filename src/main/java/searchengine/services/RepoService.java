package searchengine.services;

import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RepoService {


    SiteEntity initSite(String urlSite, String nameSite);
    boolean saveAllDataPage(PageEntity page, Map<String, Integer> lemmaMap, String error);

    void endSiteIndex(SiteEntity site);



    boolean existPage(PageEntity pageEntity);

    SiteEntity findSite(String url, String name);

    void cleanPage(PageEntity pageEntity);


    int countPagesOnSite(SiteEntity siteEntity);

    int countLemmasOnSite(SiteEntity siteEntity);


    List<SiteEntity> findIndexedSite(List<String> siteURL);


    Set<LemmaEntity> findQueryLemmas(List<SiteEntity> siteEntities, Set<String> lemmas);
}
