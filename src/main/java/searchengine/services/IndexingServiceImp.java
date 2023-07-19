package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.*;


@Service
@Slf4j
public class IndexingServiceImp implements IndexingService {
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    //private final JsoupCfg jsoupCfg;
    private final ApplicationContext applicationContext;
 
    //public static volatile boolean stopFlag = false;
    public static boolean stopFlag;// = false;
    private ExecutorService siteExecutor;

    @Autowired
    public IndexingServiceImp(ApplicationContext applicationContext, SitesList sites, SiteRepo siteRepo, PageRepo pageRepo) {
        this.applicationContext = applicationContext;
        this.sites = sites;
        this.siteRepo = siteRepo;
        this.pageRepo = pageRepo;
        //this.jsoupCfg = jsoupCfg;
        this.stopFlag = false;
    }

    @Override
    public IndexingResponse getStartIndexing() {
        if (isRunning()) {
            log.info("Индексация уже запущена. Повторный запуск невозможен");
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        siteExecutor = Executors.newFixedThreadPool(sites.getSites().size());
        stopFlag = false;
        for (Site s : sites.getSites()) {
            siteExecutor.execute(()->{siteIndexing(s.getUrl(), s.getName());});
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
        stopFlag = true;
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


    private void siteIndexing(String urlSiteString, String nameSite) {
        long startTime = System.currentTimeMillis();
        //Thread.currentThread().setName("thread-" + nameSite);
        log.info("Start indexing: " + nameSite);
        SiteEntity currentSite = new SiteEntity(urlSiteString, nameSite);
        siteRepo.deleteByUrlAndName(urlSiteString, nameSite);
        siteRepo.save(currentSite);
        ForkJoinPool pageFJP = new ForkJoinPool(); // каждый сайт в своем FJP.
        try {
            URL urlSite = URI.create(urlSiteString).toURL();
            URL urlPage = URI.create(urlSite.getProtocol() + "://" + urlSite.getHost() + "/").toURL();
//            var pageIndexService = new PageIndexService(siteRepo,
//                    pageRepo, jsoupCfg, currentSite, urlPage);
            var pageIndexService = applicationContext.getBean(PageIndexService.class);
            pageFJP.invoke(pageIndexService);
            pageFJP.shutdown();
            long result = pageRepo.countBySite(currentSite);
            long resultTime = (System.currentTimeMillis() - startTime) / 60000;
            saveSite(currentSite, StatusType.INDEXED, "OK. Found " + result + " pages in " + resultTime + "min");
            log.info("Сайт " + nameSite +
                    ", найдено страниц " + result +
                    ", затрачено " + resultTime + " мин");
        }catch (Exception e) {
            pageFJP.shutdownNow();
            log.error("Ошибка при обработке сайта: " + e.getMessage());
            saveSite(currentSite, StatusType.FAILED, e.getMessage());
        }
    }

    private void saveSite(SiteEntity siteEntity, StatusType statusType, String last_error) {
        siteEntity.setStatus(statusType);
        siteEntity.setLast_error(last_error);
        siteEntity.setStatusTime(new java.util.Date());
        siteRepo.save(siteEntity);
    }

}
