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
import searchengine.config.BadLinks;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;

import java.io.IOException;
import java.net.*;
import java.util.EnumSet;
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
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;

    private final SiteEntity site;
    private final URL pageURL;
    private final PageEntity page;




    @Autowired
    public PageIndexService(ApplicationContext context, SiteEntity site, URL pageURL) {
        this.context = context;
        this.pageRepo = context.getBean(PageRepo.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.siteIndexingService = context.getBean(SiteIndexingService.class);
        this.site = site;
        this.pageURL = pageURL;
        this.page = new PageEntity(site, pageURL.getPath());
    }


    @Override
    protected void compute() {
        if (siteIndexingService.isStopFlag()) return;
        synchronized (pageRepo) {
            if (pageRepo.findBySiteAndPath(site, pageURL.getPath()).isPresent()) return;
            pageRepo.save(page);
        }
        Document pageDoc = pageReader();
        if (pageDoc == null) return;
        pageRepo.save(page);
        siteIndexingService.saveSite(site, StatusType.INDEXING, null);
        //TODO запуск лемантизатора для индексации содержимого страницы (в отдельном потоке???)
        HashSet<URL> links = getValidLinks(pageDoc);
        Set<PageIndexService> subPageTasks = new HashSet<>();
        for (URL itemURL : links) {
            PageIndexService subPageIndexService = new PageIndexService(context, site, itemURL);
            subPageTasks.add(subPageIndexService);
        }
        invokeAll(subPageTasks);
    }



    private Document pageReader() {
        int timeout = jsoupCfg.getTimeoutBase();
        Document resultDoc = null;
        do {
            try {
                Thread.sleep(timeout);
                resultDoc = connectToPage();
                page.setCode(resultDoc.connection().response().statusCode());
                page.setContent(resultDoc.html());
                timeout = jsoupCfg.getTimeout();
            } catch (InterruptedException | IOException e) {
                log.error(e.getMessage());
                if (!(e instanceof HttpStatusException)) {
                    checkStopByUser();
                    page.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    timeout = jsoupCfg.getTimeout();
                } else {
                    page.setCode(((HttpStatusException) e).getStatusCode());
                    timeout = (timeout + jsoupCfg.getTimeoutDelta())*jsoupCfg.getTimeoutFactor();
                }
            }
        } while (timeout<jsoupCfg.getTimeout());
        return resultDoc;
    }

    private Document connectToPage() throws IOException {
        return Jsoup
                    .connect(pageURL.toString())
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    //.ignoreContentType(true)
                    .get();
    }

    private HashSet<URL> getValidLinks(Document inputDoc) {
        HashSet<URL> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkString = element.absUrl("href").toLowerCase();
            boolean badLinkFlag = EnumSet.allOf(BadLinks.class).stream().anyMatch(enumElement -> linkString.contains(enumElement.toString()));
            if (!linkString.startsWith("http") || badLinkFlag) continue;
            try {
                URL childLinkURL = URI.create(linkString).toURL();
                String siteHost = pageURL.getHost().startsWith("www.") ? pageURL.getHost().substring(4) : pageURL.getHost();
                String pageHost = childLinkURL.getHost().startsWith("www.") ? childLinkURL.getHost().substring(4) : childLinkURL.getHost();
                badLinkFlag = (!siteHost.equals(pageHost)) || (childLinkURL.getPath().contains("http")) || (childLinkURL.getPath().equals(pageURL.getPath()));
                if (!badLinkFlag) {
                    childLinkURL = URI.create(childLinkURL.getProtocol() + "://" + childLinkURL.getHost() + childLinkURL.getPath()).toURL();
                    resultLinks.add(childLinkURL);
                }
            } catch (MalformedURLException | IllegalArgumentException e) { log.error(e.getMessage());}
        }
        return resultLinks;
    }

    private void savePage(int statusCode, String content) {
        PageEntity pageEntity = new PageEntity(site, pageURL.getPath());
        pageEntity.setCode(statusCode);
       pageEntity.setContent(content);
        synchronized (pageRepo) {
            Optional<PageEntity> pageEntityOptional = pageRepo.findBySiteAndPath(site, pageURL.getPath());
            if (pageEntityOptional.isPresent()) return;
            pageRepo.save(pageEntity);
            siteIndexingService.saveSite(site, StatusType.INDEXING, null);
        }
    }

    private void checkStopByUser() {
        if (siteIndexingService.isStopFlag()) {
            throw new RuntimeException("Indexing stopped by user");
        }
    }

}
