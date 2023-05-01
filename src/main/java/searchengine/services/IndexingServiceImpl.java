package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;
    public static volatile boolean stopFlag = false;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Override
    public IndexingResponse getStartIndexing() {
        if (!forkJoinPool.isQuiescent() && forkJoinPool!=null) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        stopFlag = false;
        sites.getSites().forEach(s -> {
            forkJoinPool.execute(()->siteIndexing(s.getUrl(), s.getName()));
        });
        log.info("Start indexing threads for all sites");
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
        if (forkJoinPool.isTerminating() || forkJoinPool == null) {
            return new IndexingResponse(false, "Индексация не запущена");
        }
        stopFlag = true;
        forkJoinPool.shutdown();
        try {
            forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("Error while waiting for siteExecutor termination", e);
        }
        log.info("Stop site indexing");
        return new IndexingResponse(true);
    }


    private void siteIndexing(String urlSite, String nameSite) {
        long startTime = System.currentTimeMillis();
        log.info("Start " + Thread.currentThread().getName() + " : " + nameSite);
        SiteEntity currentSite = new SiteEntity(urlSite, nameSite);
        try {
            siteRepo.deleteByUrlAndName(urlSite, nameSite);
            siteRepo.save(currentSite);
            pageIndexing(urlSite, "/");
            currentSite.setStatus(StatusType.INDEXED);
            currentSite.setStatusTime(new java.util.Date());
            siteRepo.save(currentSite);
            long result = pageRepo.countBySite(currentSite);
            log.info("Сайт " + nameSite +
                    ", найдено страниц " + result +
                    ", затрачено " + (System.currentTimeMillis() - startTime) + " мс");
        } catch (Exception e) {
            currentSite = siteRepo.findByUrl(urlSite);
            currentSite.setStatus(StatusType.FAILED);
            currentSite.setStatusTime(new java.util.Date());
            currentSite.setLast_error(e.getMessage());
            siteRepo.save(currentSite);
        }
    }

    private void pageIndexing(String site, String path) {
        try {
            if (stopFlag) {
                log.error("Stop read on: " + site + " - " + path);
                return;
            }
            URL siteAddress = URI.create(site).toURL();
            String pageAddress = siteAddress.getProtocol()
                    + "://"
                    + siteAddress.getHost()
                    + path;
            Document doc = Jsoup
                    .connect(pageAddress)
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    .get();
            SiteEntity siteEntity = siteRepo.findByUrl(site);
            PageEntity pageEntity = new PageEntity(siteEntity,path);
            pageEntity.setCode(doc.connection().response().statusCode());
            pageEntity.setContent("");
            if ((pageEntity.getCode() == 200)) {
                pageEntity.setContent(doc.html());
            }
            siteEntity.setStatusTime(new java.util.Date());
            siteRepo.save(siteEntity);
            pageRepo.save(pageEntity);
            HashSet<String> links = linksParse(doc, site);
            for (String itemLink : links) {
                if (!itemLink.startsWith(pageAddress) ||itemLink.equals(pageAddress)
                ) {continue;}
                URI itemURI = URI.create(itemLink);
                if(!pageRepo.findBySite_UrlAndPath(site, itemURI.getPath()).isPresent()) {
                    forkJoinPool.execute(()->pageIndexing(site, itemURI.getPath()));
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            log.error("Uncorrected site URL " + site);
            siteRepo.updateStatusAndStatusTimeAndLast_errorByUrl(
                    StatusType.FAILED,
                    new java.util.Date(),
                    "Uncorrected site URL",
                    site);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error read page " + site + " " + path);
            siteRepo.updateStatusTimeAndLast_errorByUrl(
                    new java.util.Date(),
                    "Error read page",
                    site);
        }
    }


    private HashSet<String> linksParse(Document currentDoc, String currentSite) {
        HashSet<String> linksSet = new HashSet<>();
        Elements elements = currentDoc.select("a");
        for (Element e : elements) {
            String linkString = e.absUrl("href");
            if (linkString.contains("#") || linkString.contains("download")) {continue;}
            linksSet.add(linkString);
            log.debug("add " + linkString);
        }
        return linksSet;
    }
}
