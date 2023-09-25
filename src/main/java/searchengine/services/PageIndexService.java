package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.config.BadLinks;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;


import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


@Service
@Scope("prototype")
@Slf4j
public class PageIndexService extends RecursiveAction {
    private final ApplicationContext context;
    private final IndexingService indexingService;
    private final LemmaService lemmaService;
    private final DatabaseService databaseService;
    private final JsoupCfg jsoupCfg;
    private final SiteEntity site;
    private final String pageAddress;

    @Autowired
    public PageIndexService(ApplicationContext context, SiteEntity site, String pageAddress) {
        this.context = context;
        this.indexingService = context.getBean(IndexingService.class);
        this.lemmaService = context.getBean(LemmaService.class);
        this.databaseService = context.getBean(DatabaseService.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.site = site;
        this.pageAddress = pageAddress;
    }



    @Override
    public void compute() {
        if (indexingService.isStopFlag() || ForkJoinTask.getPool().isShutdown()) {
            return;
        }
        Document docPage = singlePageIndexing();
        if (docPage == null) {
            return;
        }
        ForkJoinTask.invokeAll(pageHandler(docPage));
    }


    /**
     * 1) Соединиться со страницей, 2) получить ответ,
     * 3) записать страницу(ответ) в БД, 4) провести лемантизацию и 5) обновить данные сайта в БД
     *
     * @return Document содержащий прочитанную и проиндексированную страницу или null при ошибке
     **/
    public Document singlePageIndexing() {
        String pathPage = "/" + URI.create(site.getUrl()).relativize(URI.create(pageAddress));
        PageEntity page = new PageEntity(site, pathPage);
        Map<String, Integer> lemmaMap = new HashMap<>();
        try {
            //long sleepTime = 500L + (long) (Math.random() * 5000);
            long sleepTime = 500L;
            Thread.sleep(sleepTime);
            Connection.Response responsePage = connector();

            if (responsePage.statusCode() == HttpStatus.OK.value()) {
                lemmaMap = lemmaService.lemmaCount(responsePage.body());
            }
            page.setCode(responsePage.statusCode());
            page.setContent(responsePage.body());
            if (!databaseService.saveIndexPage(page, lemmaMap)) {return null;}
            return responsePage.parse();
        } catch (Exception e) {
            if (!(e instanceof UnsupportedMimeTypeException)) {
                log.error(e + " on " + pageAddress);
                handlerConnectException(e);
            }
        }
        return null;
    }


    private void handlerConnectException(Exception e) {
        databaseService.updateLastErrorOnSite(site, e.getMessage() + " on " + pageAddress);
        if (!(e instanceof  IOException)) {
            log.error("shutdown pool on " + pageAddress);
            ForkJoinTask.getPool().shutdown();
        }
    }

    private Connection.Response connector() throws IOException {
        return Jsoup.connect(pageAddress)
                .userAgent(jsoupCfg.getUserAgent())
                .referrer(jsoupCfg.getReferrer())
                .timeout(jsoupCfg.getTimeout())
                .followRedirects(jsoupCfg.isFollowRedirects())
                .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                .ignoreContentType(jsoupCfg.isIgnoreContentType())
                .execute();
    }


    private HashSet<PageIndexService> pageHandler(Document inputDoc) {
        HashSet<PageIndexService> resultPageServices = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String link = element.absUrl("href").toLowerCase();
            String newPageAddress = linkValidator(link);
            if (newPageAddress == null) {continue;}
            String newPath = "/" + URI.create(site.getUrl()).relativize(URI.create(newPageAddress));
            if (databaseService.existPage(site, newPath)) {continue;}
            resultPageServices.add(context.getBean(PageIndexService.class, context, site, newPageAddress));
        }
        return resultPageServices;
    }

    private String linkValidator(String link) {
        if (!link.startsWith(site.getUrl())) {
            return null;}
        if (EnumSet.allOf(BadLinks.class).stream().anyMatch(enumElement ->
                link.contains(enumElement.toString()))) {
            return null;}
        try {
            URI linkUri = URI.create(link);
            return linkUri.getScheme()
                    + "://"
                    + linkUri.getRawAuthority()
                    + linkUri.getRawPath();
        } catch (IllegalArgumentException e) {
            log.error("Bad link: " + e.getMessage());
            return null;
        }
    }


}
