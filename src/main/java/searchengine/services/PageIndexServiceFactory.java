package searchengine.services;

import searchengine.model.SiteEntity;

import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public interface PageIndexServiceFactory {
    PageIndexService create(SiteEntity site, URL page, ForkJoinPool pool);
}
