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
import java.util.concurrent.ForkJoinTask;
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

    //private final ForkJoinPool siteFJPool;


    @Autowired
    public PageIndexService(ApplicationContext context, SiteEntity site, URL pageURL) {
        this.context = context;
        this.pageRepo = context.getBean(PageRepo.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.siteIndexingService = context.getBean(SiteIndexingService.class);
        this.site = site;
        this.pageURL = pageURL;
        //this.siteFJPool = siteFJPool;
    }


    @Override
    protected void compute() {
        if (siteIndexingService.isStopFlag()) {return;}
        if (pageRepo.findBySiteAndPath(site, pageURL.getPath()).isPresent()) return;
        try {
            Connection.Response response = connector(pageURL);
            synchronized (pageRepo) {
                if (pageRepo.findBySiteAndPath(site, pageURL.getPath()).isPresent()) return;
                pageRepo.save(new PageEntity(site, pageURL.getPath(), response.statusCode(), response.body()));
                siteIndexingService.saveSite(site, StatusType.INDEXING, "");
            }
            if (response.statusCode() != 200) return;
            //TODO запуск лемантизатора для индексации содержимого страницы (в отдельном потоке???)
            ForkJoinTask.invokeAll(pageExtractor(response.parse()));
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



    private HashSet<PageIndexService> pageExtractor(Document inputDoc) {
        HashSet<PageIndexService> resultPageServices = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkS = element.absUrl("href").toLowerCase().replaceAll(" ", "%20");
            if (!linkS.startsWith(site.getUrl())) {continue;}
            try {
                URL fullURL = URI.create(linkS).toURL();
                URL linkURL = URI.create(fullURL.getProtocol() + "://" + fullURL.getHost() + fullURL.getPath()).toURL();
                if (isLinkValid(linkURL) && pageRepo.findBySiteAndPath(site, linkURL.getPath()).isEmpty())  {
                    resultPageServices.add(context.getBean(PageIndexService.class, context, site, linkURL));
                    }
            } catch (MalformedURLException | IllegalArgumentException e) {
                log.error(linkS + " :: " + e.getMessage());
            }
        }
        return resultPageServices;
    }

    private boolean isLinkValid(URL link) {
        URL finalLink = link;
        //if (!link.getProtocol().equals(pageURL.getProtocol())) {return false;}
        String parentHost = pageURL.getHost().startsWith("www.") ? pageURL.getHost().substring(4) : pageURL.getHost();
        String childHost = finalLink.getHost().startsWith("www.") ? finalLink.getHost().substring(4) : finalLink.getHost();
        if (!childHost.equals(parentHost)) {return false;}
        return EnumSet.allOf(BadLinks.class).stream().noneMatch(enumElement ->
                finalLink.getPath().contains(enumElement.toString()));
    }


}
