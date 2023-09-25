package searchengine.services;

import searchengine.model.SiteEntity;

public interface DatabaseService {


    SiteEntity cleanSites(String urlSite, String nameSite);

    long updateSite(SiteEntity site, boolean isEndWork);


    SiteEntity createSiteForPage(String urlPage);


}
