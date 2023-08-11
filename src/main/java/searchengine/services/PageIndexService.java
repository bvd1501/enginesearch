package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
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
import java.util.concurrent.ForkJoinPool;
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

    private final ForkJoinPool siteFJPool;


    @Autowired
    public PageIndexService(ApplicationContext context, SiteEntity site, URL pageURL, ForkJoinPool siteFJPool) {
        this.context = context;
        this.pageRepo = context.getBean(PageRepo.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.siteIndexingService = context.getBean(SiteIndexingService.class);
        this.site = site;
        this.pageURL = pageURL;
        this.siteFJPool = siteFJPool;
    }


    @Override
    protected void compute() {
        if (siteIndexingService.isStopFlag()) {return;}
        try {
            Thread.sleep((int) (Math.random() * jsoupCfg.getTimeoutMax()) + 1);
            //if (siteIndexingService.isStopFlag()) {return;}
            Connection.Response response = connector(pageURL);
            synchronized (pageRepo) {
                if (pageRepo.findBySiteAndPath(site, pageURL.getPath()).isPresent()) return;
                pageRepo.save(new PageEntity(site, pageURL.getPath(), response.statusCode(), response.body()));
                siteIndexingService.saveSite(site, StatusType.INDEXING, "");
            }
            if (response.statusCode() != 200) return;
            //TODO запуск лемантизатора для индексации содержимого страницы (в отдельном потоке???)
            HashSet<URL> links = getValidLinks(response.parse());
            for (URL itemURL : links) {
                siteFJPool.execute(new PageIndexService(context, site, itemURL, siteFJPool));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Internal error on timeout");
        }
    }



    private Connection.Response connector(URL pageAddress) throws IOException {
        return Jsoup
                .connect(pageAddress.toString())
                .userAgent(jsoupCfg.getUserAgent())
                .referrer(jsoupCfg.getReferrer())
                .timeout(jsoupCfg.getTimeout())
                .followRedirects(jsoupCfg.isFollowRedirects())
                .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                //.ignoreContentType(true)
                .execute();
    }



    private HashSet<URL> getValidLinks(Document inputDoc) {
        HashSet<URL> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String finalLinkString = element.absUrl("href").toLowerCase().replaceAll(" ", "%20");
            //String finalLinkString = (linkString.indexOf('{') > 0) ? linkString.substring(0, linkString.indexOf('{')) : linkString;
            if (EnumSet.allOf(BadLinks.class).stream().anyMatch(enumElement -> finalLinkString.contains(enumElement.toString())) ||
                    !finalLinkString.startsWith("http") || finalLinkString.isEmpty()) continue;
            try {
                URL childLinkURL = URI.create(finalLinkString).toURL();
                childLinkURL = URI.create(childLinkURL.getProtocol() + "://" + childLinkURL.getHost() + childLinkURL.getPath()).toURL();
                String siteHost = pageURL.getHost().startsWith("www.") ? pageURL.getHost().substring(4) : pageURL.getHost();
                String pageHost = childLinkURL.getHost().startsWith("www.") ? childLinkURL.getHost().substring(4) : childLinkURL.getHost();
                if ((!siteHost.equals(pageHost)) || (pageRepo.findBySiteAndPath(site, childLinkURL.getPath()).isPresent()))
                    continue;
                resultLinks.add(childLinkURL);
            } catch (MalformedURLException | IllegalArgumentException e) {
                log.error("ValidationError: " + e.getMessage() + " :: " + pageURL.toString());
            }
        }
        return resultLinks;
    }


}
