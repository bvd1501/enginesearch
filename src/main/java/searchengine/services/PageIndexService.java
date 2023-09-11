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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.BadLinks;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

@Component
@Scope("prototype")
@Slf4j
public class PageIndexService extends RecursiveAction {
    private final ApplicationContext context;
    private final SiteIndexingService siteIndexingService;
    private final PageRepo pageRepo;
    private final SiteRepo siteRepo;
    private final JsoupCfg jsoupCfg;

    private final SiteEntity site;
    private final String pageAddress;


    @Autowired
    public PageIndexService(ApplicationContext context, SiteEntity site, String pageAddress) {
        this.context = context;
        this.pageRepo = context.getBean(PageRepo.class);
        this.siteRepo = this.context.getBean(SiteRepo.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.siteIndexingService = context.getBean(SiteIndexingService.class);
        this.site = site;
        this.pageAddress = pageAddress;
    }

    @Override
    public void compute() {
        if (siteIndexingService.isStopFlag() || ForkJoinTask.getPool().isShutdown()) {
            return;
        }
        try {
            Connection.Response response = connector();
            boolean isSaved = savePage(response);
            if (!isSaved || response.statusCode() != 200) return;
            //TODO запуск лемантизатора для индексации содержимого страницы (в отдельном потоке???)
            ForkJoinTask.invokeAll(pageHandler(response.parse()));
        } catch (UnsupportedMimeTypeException e) {
        }  catch (IOException e) {
            siteRepo.updateStatusAndLast_errorAndStatusTimeById(StatusType.INDEXING,
                    "Connection error", new java.util.Date(), site.getId());
            log.error(e.getMessage() + " " + pageAddress);
        } catch (Exception e) {
            log.error(e.getMessage() + " " + pageAddress);
            siteRepo.updateStatusAndLast_errorAndStatusTimeById(StatusType.FAILED,
                    e.getMessage() + " " + pageAddress, new java.util.Date(), site.getId());
            ForkJoinTask.getPool().shutdown();
        }
    }


    @Transactional
    private boolean savePage(Connection.Response response) {
        String pathPage = "/" + URI.create(site.getUrl()).relativize(URI.create(pageAddress));
        synchronized (pageRepo) {
            if (pageRepo.findBySiteAndPath(site, pathPage).isPresent()) return false;
            pageRepo.save(new PageEntity(site, pathPage, response.statusCode(), response.body()));
            siteRepo.updateStatusTimeById(new java.util.Date(), site.getId());
        }
        return true;
    }

    private Connection.Response connector() throws IOException, InterruptedException {
        int retriveCount=1;
        while (retriveCount<5) {
            try {
                return Jsoup.connect(pageAddress)
                        .userAgent(jsoupCfg.getUserAgent())
                        .referrer(jsoupCfg.getReferrer())
                        .timeout(jsoupCfg.getTimeout())
                        .followRedirects(jsoupCfg.isFollowRedirects())
                        .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                        .ignoreContentType(jsoupCfg.isIgnoreContentType())
                        .execute();
            }
            catch (IOException e) { log.error(e.getMessage() + " " + pageAddress);
                Thread.sleep(retriveCount*1000);
            }
            retriveCount++;
        }
        throw new IOException("Too many retries");
    }


    private HashSet<PageIndexService> pageHandler(Document inputDoc) {
        HashSet<PageIndexService> resultPageServices = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String link = element.absUrl("href").toLowerCase();
            try {
                link = URLDecoder.decode(link, "UTF-8");
                if (!link.startsWith(site.getUrl()) || !isLinkValid(link)) {continue;}
                URI fullLinkURI = URI.create(link);
                String cleanLink = fullLinkURI.getScheme() + "://" + fullLinkURI.getRawAuthority() + fullLinkURI.getPath();
                String pathLink = "/" + URI.create(site.getUrl()).relativize(URI.create(cleanLink));
                if (pageRepo.findBySiteAndPath(site, pathLink).isEmpty()) {
                    resultPageServices.add(context.getBean(PageIndexService.class, context, site, cleanLink));
                }
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                log.error(e.getMessage() + " " + link);
            }
        }
        return resultPageServices;
    }

    private boolean isLinkValid(String link) {
        return EnumSet.allOf(BadLinks.class).stream().noneMatch(enumElement ->
                link.contains(enumElement.toString()));
    }


}
