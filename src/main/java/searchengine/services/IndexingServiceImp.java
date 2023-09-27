package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.concurrent.*;


@Service
@Slf4j
public class IndexingServiceImp implements IndexingService {
    private final ApplicationContext context;
    private final DatabaseService databaseService;
    private final SitesList sites;
    private static boolean stopFlag;
    private ExecutorService siteExecutor;
    @Autowired
    public IndexingServiceImp(ApplicationContext context) {
        this.context = context;
        this.sites = context.getBean(SitesList.class);
        this.databaseService = context.getBean(DatabaseService.class);
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
            siteExecutor.execute(()-> startIndex(s.getUrl(), s.getName()));
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

    @Override
    public IndexingResponse getPageIndexing(String url) {
        boolean isUrlCorrect = sites.getSites().stream().anyMatch(s -> url.startsWith(s.getUrl()));
        if (!isUrlCorrect) {
            log.info("Страница " + url + " находится за пределами сайтов, указанных в конфигурационном файле");
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
//        Site simpleSite = findSiteForPage(url);
//        if (simpleSite == null) {
//            log.info("Страница " + url + " находится за пределами сайтов, указанных в конфигурационном файле");
//            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
//        }
        return new IndexingResponse(true);
    }

    private boolean isRunning() {
        return siteExecutor != null && !siteExecutor.isTerminated();
    }
    @Override
    public boolean isStopFlag() {
        return stopFlag;
    }

    private void startIndex(String urlSite, String nameSite) {
        long startTime = System.currentTimeMillis();
        Thread.currentThread().setName("th." + nameSite);
        SiteEntity currentSite = databaseService.createSite(urlSite, nameSite);
        PageEntity firstPage = new PageEntity(currentSite, "/");
        ForkJoinPool sitePool = new ForkJoinPool();
        var pageIndexService = context.getBean(PageIndexService.class, context, firstPage);
        //var pageIndexService = context.getBean(PageIndexService.class, context, currentSite, urlSite);
        sitePool.invoke(pageIndexService);
        sitePool.shutdown();
        //sitePool.execute(pageIndexService);
//        try {
//            sitePool.awaitTermination(60, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        currentSite.setLast_error(isStopFlag() ? "Индексация остановлена пользователем" : null);
        long countPages = databaseService.endSiteIndex(currentSite);
        long resultTime = (System.currentTimeMillis() - startTime) / 60000;
        log.info(nameSite + " - " + resultTime + " min / " + countPages + " pages");
    }
}