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
import java.net.URI;
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
    private Set<URL> childUrl;

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
            Thread.sleep(210);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Error on sleep thread");
        }

        childUrl = readPage(page);

        if (childUrl == null) {
            log.debug("Not found valid links on " + page.getSite().getName()
                    + ": " + page.getPath());
            return;
        }
        log.debug("Found " + childUrl.size() + " valid links on " +
                page.getSite().getName() + ": " + page.getPath());
        ArrayList<PageIndexUtil> subTaskList = new ArrayList<>();
        for (URL itemURL : childUrl) {
            PageEntity childPage;
            synchronized (this) {
                if (pageRepo.countBySiteAndPath(page.getSite(), itemURL.getPath()) > 0) {
                    log.error("Already in base: " + page.getSite().getName() + " - " + page.getPath());
                    continue;
                }
                childPage = pageRepo.save(new PageEntity(page.getSite(), itemURL.getPath()));
            }
            subTaskList.add(new PageIndexUtil(pageRepo, siteRepo, jsoupCfg, childPage));
        }
        invokeAll(subTaskList);
    }

    @Nullable
    private HashSet<URL> readPage(PageEntity readingPage) {
        HashSet<URL> urlSet = new HashSet<>();
        try {
            URL urlSite = URI.create(readingPage.getSite().getUrl()).toURL();
            Document doc = Jsoup
                    .connect(urlSite.getProtocol() + "://" +
                            urlSite.getHost() + readingPage.getPath())
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    .get();
            int statusCode = doc.connection().response().statusCode();
            if ((statusCode != 200)) {
                pageRepo.updateCodeAndContentById(statusCode, "", readingPage.getId());
                siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(),
                        "Error code " + statusCode,
                        readingPage.getSite().getId());
                return null;
            }
            pageRepo.updateCodeAndContentById(statusCode, doc.html(), readingPage.getId());
            siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(),
                    null, readingPage.getSite().getId());

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
                        linkURL.getPath().equals(readingPage.getPath()) ||
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
            log.error("MalformedURLException link on site: " + readingPage.getSite().getName());
            return null;
        } catch (IOException e) {
            log.error("Exception on execute connection to: " + readingPage.getSite().getName());
            return null;
        }
        return urlSet;
    }

}
