package searchengine.services;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.lang.Nullable;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

@Getter
@Setter
@Slf4j
public class PageIndexUtil extends RecursiveAction {
    private final PageRepo pageRepo;
    private final SiteRepo siteRepo;
    private final JsoupCfg jsoupCfg;
    private PageEntity page;
    private Set<URL> childrenUrl;

    public PageIndexUtil(PageRepo pageRepo, SiteRepo siteRepo, JsoupCfg jsoupCfg, PageEntity page) {
        this.pageRepo = pageRepo;
        this.siteRepo = siteRepo;
        this.jsoupCfg = jsoupCfg;
        this.page = page;
    }

    /**
     * Основные вычисления, выполняемые этой задачей
     */
    @Override
    protected void compute() {
        if (IndexingServiceImpl.stopFlag) {
            return;
        }
        /* Таймаут между запросами к страницам сайта */
        try {
            Thread.sleep(110);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Error on sleep thread");
        }

        childrenUrl = readPage(page);

        if (childrenUrl == null) {
            log.debug("Not found valid links on " + page.getSite().getName()
                    + ": " + page.getPath());
            return;
        }
        log.debug("Found " + childrenUrl.size() + " valid links on " +
                page.getSite().getName() + ": " + page.getPath());
        ArrayList<PageIndexUtil> subTaskList = new ArrayList<>();
        for (URL itemURL : childrenUrl) {
            if (pageRepo.countBySiteAndPath(page.getSite(), itemURL.getPath()) > 0) {
                continue;
            }
            subTaskList.add(new PageIndexUtil(pageRepo, siteRepo, jsoupCfg,
                    new PageEntity(page.getSite(), itemURL.getPath())));
        }
        invokeAll(subTaskList);
    }

    @Nullable
    private HashSet<URL> readPage(PageEntity page) {
        HashSet<URL> urlSet = new HashSet<>();
        //synchronized (this) {
            if (pageRepo.countBySiteAndPath(page.getSite(), page.getPath()) > 0) {
                log.error("Already in base: " + page.getSite().getName() + " " + page.getPath());
                return null;
            }
            pageRepo.save(page);
       log.info("Save " + page.getSite().getName() + " " + page.getPath() + " id = " + page.getId());
        //}
        try {
            URL urlSite = new URL(page.getSite().getUrl());
            Document doc = Jsoup
                    .connect(urlSite.getProtocol() + "://" +
                            urlSite.getHost() + page.getPath())
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    .get();
            int statusCode = doc.connection().response().statusCode();
            if ((statusCode != 200)) {
                pageRepo.updateCodeAndContentById(statusCode, "", page.getId());
                siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(),
                        "Read page code = " + statusCode,
                        page.getSite().getId());
                return null;
            }
            pageRepo.updateCodeAndContentById(statusCode, doc.html(), page.getId());
            siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(),
                    null, page.getSite().getId());

            Elements elements = doc.select("a");

            elements.stream().map(e -> e.absUrl("href")).forEach(link -> {
                if (link.contains("#")) {
                    log.debug("# on link: " + link);
                    return;
                }
                URL linkURL;
                try {
                    linkURL = new URL(link);
                } catch (MalformedURLException e) {
                    log.error("MalformedURLException on page: " + link);
                    return;
                }
                if (linkURL.getPath().isEmpty() ||
                        !linkURL.getProtocol().startsWith("http") ||
                        !linkURL.getHost().equals(urlSite.getHost()) ||
                        linkURL.getPath().equals(page.getPath()) ||
                        linkURL.getPath().endsWith(".doc") ||
                        linkURL.getPath().endsWith(".docx") ||
                        linkURL.getPath().endsWith(".rtf") ||
                        linkURL.getPath().endsWith(".pptx") ||
                        linkURL.getPath().endsWith(".pdf") ||
                        linkURL.getPath().endsWith(".jpg") ||
                        linkURL.getPath().endsWith(".jpeg") ||
                        linkURL.getPath().endsWith(".png") ||
                        linkURL.getPath().endsWith(".tif") ||
                        linkURL.getPath().endsWith(".tiff") ||
                        linkURL.getPath().endsWith(".xls") ||
                        linkURL.getPath().endsWith(".xlsx") ||
                        linkURL.getPath().contains("http") ||
                        linkURL.getPath().contains("download")
                ) {return;}
                urlSet.add(linkURL);
                log.debug("add " + link);
            });
        } catch (MalformedURLException e) {
            log.error("MalformedURLException link on site: " + page.getSite().getName());
            return null;
        } catch (IOException e) {
            log.error("Exception on execute connection to: " + page.getSite().getName());
            return null;
        }
        return urlSet;
    }

}
