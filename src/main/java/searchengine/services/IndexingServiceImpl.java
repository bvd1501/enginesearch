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
public class IndexingServiceImpl implements IndexingService {
    private final ApplicationContext context;
    private final RepoService repoService;
    private final SitesList sites;
    private static boolean stopFlag;
    private ExecutorService siteExecutor;
    @Autowired
    public IndexingServiceImpl(ApplicationContext context) {
        this.context = context;
        this.sites = context.getBean(SitesList.class);
        this.repoService = context.getBean(RepoService.class);
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
        Site site = sites.getSites().stream().filter(s->url.startsWith(s.getUrl())).findAny().orElse(null);
        if (site == null) {
            log.error("Страница " + url + " находится за пределами сайтов, указанных в конфигурационном файле");
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        SiteEntity siteEntity = repoService.findSite(site.getUrl(), site.getName());
        PageEntity currentPage = new PageEntity(siteEntity, url);
        repoService.cleanPage(currentPage);
        var pageIndexService = context.getBean(PageIndexService.class, context, currentPage);
        Thread thread = new Thread(()->{
            pageIndexService.singePageHandler();
            repoService.endSiteIndex(siteEntity);
            log.info("End index page: " + url);
        });
        thread.start();
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
        SiteEntity currentSite = repoService.initSite(urlSite, nameSite);
        PageEntity firstPage = new PageEntity(currentSite, urlSite);
        ForkJoinPool sitePool = new ForkJoinPool();
        var pageIndexService = context.getBean(PageIndexService.class, context, firstPage);
        sitePool.invoke(pageIndexService);
        sitePool.shutdown();
        if (isStopFlag()) {currentSite.setLast_error("Принудительная остановка индексации пользователем");}
        long countPages = repoService.endSiteIndex(currentSite);
        long resultTime = (System.currentTimeMillis() - startTime) / 60000;
        log.info(nameSite + " - " + resultTime + " min / " + countPages + " pages");
    }
}
