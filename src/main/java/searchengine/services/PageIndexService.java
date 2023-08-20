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
            Connection.Response response = connector(pageURL);
            synchronized (pageRepo) {
                if (pageRepo.findBySiteAndPath(site, pageURL.getPath()).isPresent()) return;
                pageRepo.save(new PageEntity(site, pageURL.getPath(), response.statusCode(), response.body()));
                siteIndexingService.saveSite(site, StatusType.INDEXING, "");
            }
            if (response.statusCode() != 200) return;
            //TODO запуск лемантизатора для индексации содержимого страницы (в отдельном потоке???)
            HashSet<URL> links = linkExtractor(response.parse());
            for (URL itemURL : links) {
                siteFJPool.execute(new PageIndexService(context, site, itemURL, siteFJPool));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Internal error on timeout");
        }
    }


    private Connection.Response connector(URL pageAddress) throws IOException, InterruptedException {
        int timeout = jsoupCfg.getTimeoutMin() + (int) (Math.random()*(jsoupCfg.getTimeoutMax()-jsoupCfg.getTimeoutMin()));
        Thread.sleep(timeout);
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



    private HashSet<URL> linkExtractor(Document inputDoc) {
        HashSet<URL> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkString = element.absUrl("href").toLowerCase().replaceAll(" ", "%20");
            if (!linkChecker(linkString)) {continue;}
            try {
                URL childLinkURL = URI.create(linkString).toURL();
                childLinkURL = URI.create(childLinkURL.getProtocol() + "://" + childLinkURL.getHost() + childLinkURL.getPath()).toURL();

                if (pageRepo.findBySiteAndPath(site, childLinkURL.getPath()).isEmpty())  {resultLinks.add(childLinkURL);}
            } catch (MalformedURLException | IllegalArgumentException e) {
                log.error("extractorLinks: " + e.getMessage());
            }
        }
        return resultLinks;
    }

    private boolean linkChecker(String link) {
        String finalLink = link;
        if (EnumSet.allOf(BadLinks.class).stream().anyMatch(enumElement ->
                finalLink.contains(enumElement.toString()))) {return false;}
        if (!link.startsWith(pageURL.getProtocol())) {return false;}
        String site = pageURL.getHost();
        String siteHost = site.startsWith("www.") ? site.substring(4) : site;
        link = link.substring(pageURL.getProtocol().length()+3);
        String linkHost = link.startsWith("www.") ? link.substring(4) : link;
        if (!siteHost.equals(linkHost)) {return false;}
        return true;
    }


}
