package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.util.concurrent.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;

    public static volatile boolean stopFlag = false;

    private ExecutorService siteExecutor;

    @Override
    public IndexingResponse getStartIndexing() {
        if ((siteExecutor != null) && (!siteExecutor.isShutdown())) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        stopFlag = false;
        //siteExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        siteExecutor = Executors.newCachedThreadPool();
        sites.getSites().forEach(s -> {
            Runnable siteTask = () -> siteIndexing(s.getUrl(), s.getName());
            siteExecutor.execute(siteTask);
        });
        log.info("Start indexing threads for all sites");
        siteExecutor.shutdown();
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
        if (siteExecutor == null) {
            return new IndexingResponse(false, "Индексация не запущена");
        }
        stopFlag = true;
        siteExecutor.shutdown();
        try {
            siteExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("Error while waiting for siteExecutor termination", e);
        }
        //siteExecutor.shutdownNow();
        log.info("Stop site indexing");
        //stopFlag = false;
        return new IndexingResponse(true);
    }


    private void siteIndexing(String urlSite, String nameSite) {
        long startTime = System.currentTimeMillis();
        log.info("Start " + Thread.currentThread().getName() + " : " + nameSite);
        siteRepo.deleteByUrlAndName(urlSite, nameSite);
        SiteEntity currentSite = siteRepo.save(new SiteEntity(urlSite, nameSite));
        PageEntity rootPage = pageRepo.save(new PageEntity(currentSite, "/"));

        PageIndexUtil pageIndexUtilTask = new PageIndexUtil(pageRepo, siteRepo, jsoupCfg,
                rootPage);
        ForkJoinPool fjp;
        fjp = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        fjp.invoke(pageIndexUtilTask);
        //pageIndexUtilTask.invoke();

        currentSite.setStatus(StatusType.INDEXED);
        if (stopFlag) {
            currentSite.setStatus(StatusType.FAILED);
        }
        currentSite.setStatusTime(new java.util.Date());
        siteRepo.save(currentSite);
        long result = pageRepo.countBySite(currentSite);
        log.info("Сайт " + nameSite +
                ", найдено страниц " + result +
                ", затрачено " + (System.currentTimeMillis() - startTime) + " мс");
    }
}
