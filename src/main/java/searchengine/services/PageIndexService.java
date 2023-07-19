package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

@Component
@Scope("prototype")
@Slf4j
public class PageIndexService extends RecursiveAction {
    private final ApplicationContext context;
    private final SiteIndexingService siteIndexingService;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;

    private final SiteEntity site;
    private final URL page;
    private static final int TIMEOUT_BASE = 510; // 510 ms
    private static final int TIMEOUT_MULTIPLIER = 2;

    @Autowired
    public PageIndexService(ApplicationContext context, SiteEntity site, URL page) {
        this.context = context;
        this.siteRepo = context.getBean(SiteRepo.class);
        this.pageRepo = context.getBean(PageRepo.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.siteIndexingService = context.getBean(SiteIndexingService.class);
        this.site = site;
        this.page = page;
    }



    @Override
    protected void compute() {
        checkStopByUser();
        if (pageRepo.findBySiteAndPath(site, page.getPath()).isPresent()) return;
        Document pageDoc = readPage(page);
        if (pageDoc == null) return;
        HashSet<URL> links = getValidLinks(pageDoc);
        Set<PageIndexService> subPageTasks = new HashSet<>();
        for (URL itemURL : links) {
            PageIndexService subPageIndexService = new PageIndexService(context, site, itemURL);
            subPageTasks.add(subPageIndexService);
        }
        invokeAll(subPageTasks);
    }

    private void checkStopByUser() {
        if (siteIndexingService.isStopFlag()) {
            throw new RuntimeException("Indexing stopped by user");
        }
    }

    private Document readPage(URL pageAddress) {
        int timeout = TIMEOUT_BASE;
        Document resultDoc = null;
        int responseCode = HttpURLConnection.HTTP_OK;
        String content = "";
        while (timeout < jsoupCfg.getTimeout()) {
            try {
                resultDoc = Jsoup
                        .connect(pageAddress.toString())
                        .userAgent(jsoupCfg.getUserAgent())
                        .referrer(jsoupCfg.getReferrer())
                        .timeout(jsoupCfg.getTimeout())
                        .followRedirects(jsoupCfg.isFollowRedirects())
                        .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                        //.ignoreContentType(true)
                        .get();
                responseCode = resultDoc.connection().response().statusCode();
                content = resultDoc.html();
                break;

            } catch (SocketTimeoutException e) {
                savePage(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, "");
                return null;
            } catch (HttpStatusException e) {
                responseCode = e.getStatusCode();
                if (responseCode != 429) break;
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ex) {
                    checkStopByUser();
                    savePage(HttpURLConnection.HTTP_INTERNAL_ERROR, "");
                    return null;
                }
                timeout *= TIMEOUT_MULTIPLIER;
            } catch (IOException e) {
                log.error(pageAddress + " - " + e.getMessage());
                savePage(HttpURLConnection.HTTP_INTERNAL_ERROR, "");
                return null;
                //throw new RuntimeException(e.getMessage());
            }
        }
        savePage(responseCode, content);
        return resultDoc;
    }


    private HashSet<URL> getValidLinks(Document inputDoc) {
        HashSet<URL> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkString = element.absUrl("href").toLowerCase();
            if (linkString.contains("#") ||
                    !linkString.startsWith("http") ||
                    linkString.contains("download") ||
                    linkString.contains(" ") ||
                    linkString.contains("%20") ||
                    linkString.contains(".pdf") ||
                    linkString.contains(".jpg") ||
                    linkString.contains(".jpeg") ||
                    linkString.contains(".png") ||
                    linkString.contains(".svg") ||
                    linkString.contains(".docx") ||
                    linkString.contains(".doc")) continue;
            try {
                URL newURL = URI.create(linkString).toURL();
                String siteHost = page.getHost().startsWith("www.")
                        ? page.getHost().substring(4) : page.getHost();
                String pageHost = newURL.getHost().startsWith("www.")
                        ? newURL.getHost().substring(4) : newURL.getHost();
                if (!siteHost.equals(pageHost)) continue;
                if (newURL.getPath().contains("http")) continue;
                if (newURL.getPath().equals(page.getPath())) continue;
                newURL = URI.create(newURL.getProtocol() + "://" + newURL.getHost() + newURL.getPath()).toURL();
                resultLinks.add(newURL);
            } catch (MalformedURLException | IllegalArgumentException e) {
                //log.error("Неверная ссылка: " + e.getMessage() + " - " + linkString);
            }
        }
        return resultLinks;
    }

    private void savePage(int statusCode, String content) throws RuntimeException {
        PageEntity pageEntity = new PageEntity(site, page.getPath());
        pageEntity.setCode(statusCode);
        pageEntity.setContent(content);
        synchronized (pageRepo) {
            try {
                Optional<PageEntity> pageEntityOptional = pageRepo.findBySiteAndPath(site, page.getPath());
                if (pageEntityOptional.isPresent()) return;
                pageRepo.save(pageEntity);
                site.setStatusTime(new java.util.Date());
                siteRepo.save(site);
            } catch (Exception e) {
                log.error("Error adding record: " + page + "\n" + e.getMessage());
                siteIndexingService.setStopFlag();
                throw new RuntimeException("Error adding record: " + page + "\n" + e.getMessage());
            }
        }
    }
}
