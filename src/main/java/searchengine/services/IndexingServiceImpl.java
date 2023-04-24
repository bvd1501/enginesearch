package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private static volatile boolean stopFlag = false;
    private static volatile boolean runFlag = false;

    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;



    @Override
    public IndexingResponse getStartIndexing() {
        if (isRunFlag()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        setRunFlag(true);
        setStopFlag(false);
        sites.getSites().forEach(s->{
            Thread siteTask = new Thread(()->{
                siteIndexing(s.getUrl(), s.getName());
            });
            siteTask.start();
            log.info("Запуск индексации: " + s.getName());
        });
        log.info("Start indexing threads for all sites");
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
        if (!isRunFlag()) {
            return new IndexingResponse(false, "Индексация не запущена");
        }
        //taskSitesIndexing.shutdownNow();
        setStopFlag(true);
        log.info("Остановка индексации сайтов");
        return new IndexingResponse(true);
    }

    private void siteIndexing(String url, String name) {
        long startTime = System.currentTimeMillis();
        log.info("Start " + Thread.currentThread().getName() + " :" + name);
        siteRepo.deleteByUrlAndName(url, name);
        SiteEntity currentSite = siteRepo.save(new SiteEntity(url, name));
        URI uri = URI.create(url);
        if (uri.getPath().isEmpty()) { uri = URI.create(url + "/");}
        PageIndexUtil pageIndexUtilTask = new PageIndexUtil(pageRepo, siteRepo, jsoupCfg,
                currentSite, uri);
        new ForkJoinPool().invoke(pageIndexUtilTask);
        currentSite.setStatus(StatusType.INDEXED);
        if (isStopFlagSet()) {
            currentSite.setStatus(StatusType.FAILED);
            setStopFlag(false);
        }
        currentSite.setStatusTime(new java.util.Date());
        siteRepo.save(currentSite);
        setRunFlag(false);
        long result = pageRepo.countBySite(currentSite);
        log.info("Сайт " + name +
                ", найдено страниц " + result +
                ", затрачено " + (System.currentTimeMillis() - startTime) + " мс");
    }


    public static void setStopFlag(boolean stopFlagStatus) {
        stopFlag = stopFlagStatus;
    }

    public static boolean isStopFlagSet() {
        return stopFlag;
    }
    public static void setRunFlag (boolean runFlagStatus) {runFlag = runFlagStatus;}
    public static boolean isRunFlag() {return runFlag;}

}
