package searchengine.services;

import searchengine.model.SiteEntity;

import java.net.URL;

public interface PageIndexServiceFactory {
    PageIndexService create(SiteEntity site, URL page);
}
