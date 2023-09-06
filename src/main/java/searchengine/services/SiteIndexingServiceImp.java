package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.util.Optional;
import java.util.concurrent.*;


@Service
@Slf4j
public class SiteIndexingServiceImp implements SiteIndexingService {
    private final ApplicationContext context;
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;

    private static boolean stopFlag;

    private ExecutorService siteExecutor;

    @Autowired
    public SiteIndexingServiceImp(ApplicationContext context) {
        this.context = context;     
        this.sites = this.context.getBean(SitesList.class);
        this.siteRepo = this.context.getBean(SiteRepo.class);
        this.pageRepo = this.context.getBean(PageRepo.class);
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

    private boolean isRunning() {
        // так же тестировался вариант:
        // !(siteExecutor == null || (siteExecutor.isShutdown() && siteExecutor.isTerminated()))
        return siteExecutor != null && !siteExecutor.isTerminated();
    }
    @Override
    public boolean isStopFlag() {
        return stopFlag;
    }

    private void startIndex(String urlSite, String nameSite) {
        long startTime = System.currentTimeMillis();
        Thread.currentThread().setName("th." + nameSite);
        SiteEntity currentSite = createSiteForIndexing(urlSite, nameSite);
        ForkJoinPool sitePool = new ForkJoinPool();
        var pageIndexService = context.getBean(PageIndexService.class, context, currentSite, urlSite);
        sitePool.invoke(pageIndexService);
        sitePool.shutdownNow();
        long countPages = refreshSite(currentSite);
        long resultTime = (System.currentTimeMillis() - startTime) / 60000;
        log.info(nameSite + " - " + resultTime + " min / " + countPages + " pages");
    }



    private long refreshSite(SiteEntity site) {
        Optional<SiteEntity> updateSite = siteRepo.findById(site.getId());
        if (!updateSite.isPresent()) {
            log.error(site.getName() + " is missing in base!!!");
            return 0;}
        long result = pageRepo.countBySite(site);
        if (isStopFlag()) {
            siteRepo.updateStatusAndLast_errorAndStatusTimeById(StatusType.FAILED,
                    ("Stop by user. Found " + result + " pages"), new java.util.Date(), site.getId());
            return result;
        }
        if (!updateSite.get().getStatus().equals(StatusType.FAILED)) {
            siteRepo.updateStatusAndLast_errorAndStatusTimeById(StatusType.INDEXED,
                    (result + " pages"), new java.util.Date(), site.getId());
        }
        return result;
    }


    @Transactional
    private SiteEntity createSiteForIndexing(String urlSite, String nameSite) {
        log.info("Start indexing site: " + nameSite);
        siteRepo.deleteByUrlAndName(urlSite, nameSite);
        SiteEntity currentSite = new SiteEntity(urlSite, nameSite);
        siteRepo.save(currentSite);
        return currentSite;
    }


}
