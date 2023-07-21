package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.SiteRepo;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.*;


@Service
@Slf4j
public class SiteIndexingServiceImp implements SiteIndexingService {
    private final ApplicationContext context;
    private final SitesList sites;
    private final SiteRepo siteRepo;
    public final PageIndexServiceFactory pageIndexServiceFactory;

    private static boolean stopFlag;// volatile, = false;

    private ExecutorService siteExecutor;

    @Autowired
    public SiteIndexingServiceImp(ApplicationContext context) {
        this.context = context;
        this.pageIndexServiceFactory = this.context.getBean(PageIndexServiceFactory.class);
        this.sites = this.context.getBean(SitesList.class);
        this.siteRepo = this.context.getBean(SiteRepo.class);
        this.stopFlag = false;
    }

    @Override
    public IndexingResponse getStartIndexing() {
        if (isRunning()) {
            log.info("Индексация уже запущена. Повторный запуск невозможен");
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        siteExecutor = Executors.newFixedThreadPool(sites.getSites().size());
        resetStopFlag();
        for (Site s : sites.getSites()) {
            siteExecutor.execute(()-> siteIndexing(s.getUrl(), s.getName()));
        }
        siteExecutor.shutdown();
        log.info("Все сайты отправлены на индексацию");
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
        if (!isRunning()) {
            log.info("Индексация не запущена - остановка не возможна");
            return new IndexingResponse(false, "Индексация не запущена");
        }
        log.info("Принудительная остановка индексации пользователем");
        setStopFlag();
        return new IndexingResponse(true);
    }

    private boolean isRunning() {
        // так же тестировался вариант:
        // !(siteExecutor == null || (siteExecutor.isShutdown() && siteExecutor.isTerminated()))
        return siteExecutor != null && !siteExecutor.isTerminated();
    }

    @Override
    public boolean isStopFlag() {
        return stopFlag;
    }

    @Override
    public void resetStopFlag() {
        stopFlag = false;
    }

    @Override
    public void setStopFlag() {
        stopFlag = true;
    }

    @Override
    public void saveSite(SiteEntity siteEntity, StatusType statusType, String last_error) {
        if (isStopFlag()) {
            statusType = StatusType.FAILED;
            last_error = "Stop by user";
            log.error(siteEntity.getName() + " - stop by user");
        }
        siteEntity.setStatus(statusType);
        siteEntity.setLast_error(last_error);
        siteEntity.setStatusTime(new java.util.Date());
        siteRepo.save(siteEntity);
    }


    private void siteIndexing(String urlSiteString, String nameSite) {
        long startTime = System.currentTimeMillis();
        SiteEntity currentSite = preStartSiteIndexing(urlSiteString, nameSite);
        ForkJoinPool pageFJP = new ForkJoinPool(); // каждый сайт в своем FJP.
        try {
            URL urlSite = URI.create(urlSiteString).toURL();
            URL urlPage = URI.create(urlSite.getProtocol() + "://" + urlSite.getHost() + "/").toURL();
            var pageIndexService = pageIndexServiceFactory.create(currentSite, urlPage);
            pageFJP.invoke(pageIndexService);
            pageFJP.shutdown();
            long resultTime = (System.currentTimeMillis() - startTime) / 60000;
            log.info(nameSite + " - elapsed time=" + resultTime + "min");
            saveSite(currentSite, StatusType.INDEXED, null);
        }catch (Exception e) {
            pageFJP.shutdownNow();
            log.error("Error in site indexing: " + e.getMessage());
            saveSite(currentSite, StatusType.FAILED, e.getMessage());
        }
    }


    private SiteEntity preStartSiteIndexing(String urlSiteString, String nameSite) {
        //Thread.currentThread().setName("thread-" + nameSite);
        log.info("Start indexing site: " + nameSite);
        siteRepo.deleteByUrlAndName(urlSiteString, nameSite);
        SiteEntity currentSite = new SiteEntity(urlSiteString, nameSite);
        saveSite(currentSite, StatusType.INDEXING, null);
        return currentSite;
    }




}
