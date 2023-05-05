package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.util.concurrent.ForkJoinPool;


@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImp implements IndexingService {
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;
 
    public static volatile boolean stopFlag = false;

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(8);
    //private ObjectFactory<PageIndexService> pageIndexServiceObjectFactory;

    @Override
    public IndexingResponse getStartIndexing() {
        if (!forkJoinPool.isQuiescent()) {
            log.info("Индексация уже запущена. Повторный запуск невозможен");
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        log.info("Запускаем перебор сайтов из файла конфигурации");
        stopFlag = false;
        forkJoinPool = new ForkJoinPool();
        for (Site s : sites.getSites()) {
            forkJoinPool.execute(() -> {
                log.info("Начинаем обработку сайта " + s.getName());
                siteIndexing(s.getUrl(), s.getName());
                log.info("Закончили обрабатывать сайт " + s.getName());
            });
        }
        forkJoinPool.shutdown();
        log.info("Все сайты отправлены на индексацию");
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
        if (forkJoinPool.isQuiescent()) {
            log.info("Индексация не запущена - остановка не возможна");
            return new IndexingResponse(false, "Индексация не запущена");
        }
        log.info("Принудительная остановка индексации пользователем");
        stopFlag = true;
        return new IndexingResponse(true);
    }


    private void siteIndexing(String urlSite, String nameSite) {
        long startTime = System.currentTimeMillis();
        log.info("Start " + Thread.currentThread().getName() + " : " + nameSite);
        SiteEntity currentSite = new SiteEntity(urlSite, nameSite);
        try {
            siteRepo.deleteByUrlAndName(urlSite, nameSite);
            siteRepo.save(currentSite);
            PageIndexService pageIndexService = new PageIndexService(siteRepo,
                    pageRepo, jsoupCfg, urlSite, "/");
            pageIndexService.invoke();
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

}
