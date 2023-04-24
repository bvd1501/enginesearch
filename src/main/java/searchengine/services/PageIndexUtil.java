package searchengine.services;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.lang.Nullable;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;

@Getter
@Setter
@Slf4j
public class PageIndexUtil extends RecursiveAction {
    private final PageRepo pageRepo;
    private final SiteRepo siteRepo;
    private final JsoupCfg jsoupCfg;
    private URI pageUri;
    private SiteEntity site;
    private Set<URI> childrenUri;

    public PageIndexUtil(PageRepo pageRepo, SiteRepo siteRepo, JsoupCfg jsoupCfg, SiteEntity site, URI uri) {
        this.pageRepo = pageRepo;
        this.siteRepo = siteRepo;
        this.jsoupCfg = jsoupCfg;
        this.site = site;
        this.pageUri = uri;
    }

    /**
     * Основные вычисления, выполняемые этой задачей
     */
    @Override
    protected void compute() throws RuntimeException {
        /* Таймаут между запросами к страницам сайта */
        try {
            Thread.sleep(110);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Error on sleep thread");
            Thread.currentThread().interrupt();
        }
        if (IndexingServiceImpl.isStopFlagSet()) {
            return;
        }

        childrenUri = readPage(pageUri);

        if (childrenUri == null) {
            log.debug("Not found valid links on: " + pageUri);
            return;
        }
        log.debug("Found " + childrenUri.isEmpty() + " valid links on: " + pageUri);
        ArrayList<PageIndexUtil> subTaskList = new ArrayList<>();
        for (URI itemURI : childrenUri) {
            if (pageRepo.countBySiteAndPath(site, itemURI.getPath())>0) {
                continue;
            }
            subTaskList.add(new PageIndexUtil(pageRepo, siteRepo, jsoupCfg, site, itemURI));
        }
        try {
            invokeAll(subTaskList);
        } catch (CancellationException c) {
            log.info("User interrupt indexing: " + pageUri);
        }
    }

    @Nullable
    private HashSet<URI> readPage(URI uri) {
        HashSet<URI> uriSet = new HashSet<>();
        PageEntity pageEntity = new PageEntity(site, pageUri.getPath());
        if(pageRepo.countBySiteAndPath(pageEntity.getSite(), pageEntity.getPath())>0) {
            return null;
        }
        pageRepo.save(pageEntity);
        Elements elements;
        Document doc;
        try {
            doc = Jsoup
                    .connect(uri.toString())
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    .get();
            int statusCode = doc.connection().response().statusCode();
            if ((statusCode != 200)) {
                pageRepo.updateCodeAndContentById(statusCode, "", pageEntity.getId());
                siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(),
                        "Read page code = " + statusCode,
                        site.getId());
                return null;
            }
            pageRepo.updateCodeAndContentById(statusCode,doc.body().html(), pageEntity.getId());
            siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(),
                    null, site.getId());

            elements = doc.select("a");
            elements.stream().map(e -> e.absUrl("href")).forEach(link -> {
                if (link.contains("#")) {
                    log.debug("# on link: " + link);
                    return;
                }
                URI linkURI = URI.create(link);
                if (!linkURI.getScheme().startsWith("http")) {
                    return;
                }
                if (!linkURI.getHost().equals(uri.getHost())) {
                    return;
                }
                if (linkURI.getPath().equals(uri.getPath())) {
                    return;
                }
                if(pageRepo.countBySiteAndPath(site, linkURI.getPath())>0) {
                    return;
                }
                uriSet.add(linkURI);
                log.debug("add " + link);
            });
        } catch (IOException e) {
            log.error("Exception on execute connection to: " + uri);
            return uriSet;
        }
        return uriSet;
    }

}
