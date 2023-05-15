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

import java.net.URI;
import java.net.URL;
import java.util.concurrent.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImp implements IndexingService {
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;
 
    public static volatile boolean stopFlag = false;

    //private static ForkJoinPool siteFJP = new ForkJoinPool();
    private ExecutorService siteExecutor;


    @Override
    public IndexingResponse getStartIndexing() {
//        if (!siteFJP.isQuiescent()) {
//            log.info("Индексация уже запущена. Повторный запуск невозможен");
//            return new IndexingResponse(false, "Индексация уже запущена");
//        }
        if (siteExecutor != null && !siteExecutor.isTerminated()) {
            log.info("Индексация уже запущена. Повторный запуск невозможен");
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        siteExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //siteExecutor = Executors.newFixedThreadPool(sites.getSites().size());

        log.info("Запускаем перебор сайтов из файла конфигурации");
        stopFlag = false;
        //siteFJP = new ForkJoinPool(sites.getSites().size());
        for (Site s : sites.getSites()) {
//            siteFJP.execute(() -> {
//                log.info("Начинаем обработку сайта " + s.getName());
//                siteIndexing(s.getUrl(), s.getName());
//                log.info("Закончили обрабатывать сайт " + s.getName());
//            });
            siteExecutor.execute(()->{
                //log.info("Начинаем обработку сайта " + s.getName());
                siteIndexing(s.getUrl(), s.getName());
                //log.info("Закончили обрабатывать сайт " + s.getName());
            });
        }
        //siteFJP.shutdown();
        siteExecutor.shutdown();
        log.info("Все сайты отправлены на индексацию");
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
//        if (siteFJP.isQuiescent()) {
//            log.info("Индексация не запущена - остановка не возможна");
//            return new IndexingResponse(false, "Индексация не запущена");
//        }
        if (siteExecutor == null || (siteExecutor.isShutdown() && siteExecutor.isTerminated())) {
            log.info("Индексация не запущена - остановка не возможна");
            return new IndexingResponse(false, "Индексация не запущена");
        }
        log.info("Принудительная остановка индексации пользователем");
        stopFlag = true;
        return new IndexingResponse(true);
    }

    private void siteIndexing(String urlSiteString, String nameSite) {
        long startTime = System.currentTimeMillis();
        Thread.currentThread().setName("thread-"+nameSite);
        log.info("Start " + Thread.currentThread().getName() + " : " + nameSite);
        SiteEntity currentSite = new SiteEntity(urlSiteString, nameSite);
        siteRepo.deleteByUrlAndName(urlSiteString, nameSite);
        siteRepo.save(currentSite);
        try {
            URL urlSite = URI.create(urlSiteString).toURL();
            URL urlPage = URI.create(urlSite.getProtocol() + "://" +
                    urlSite.getHost() + "/").toURL();
//            URI uriSite = URI.create(urlSiteString);
//            URI uriPage = URI.create(uriSite.getScheme() + "://" + uriSite.getHost() + "/");
            var pageIndexService = new PageIndexService(siteRepo,
                    pageRepo, jsoupCfg, currentSite.getId(), urlPage);
            //pageIndexService.invoke(); //все сайты в общем FJP - CommonPool
            ForkJoinPool pageFJP = new ForkJoinPool(); // каждый сайт в своем FJP.
            pageFJP.invoke(pageIndexService);
            currentSite.setStatus(StatusType.INDEXED);
            long result = pageRepo.countBySite(currentSite);
            long resultTime = (System.currentTimeMillis() - startTime)/60000;
            currentSite.setLast_error("OK. Found " + result + " pages in " + resultTime + "min");
            log.info("Сайт " + nameSite +
                    ", найдено страниц " + result +
                    ", затрачено " + resultTime + " мин");
            currentSite.setStatusTime(new java.util.Date());
            siteRepo.save(currentSite);
        } catch (Exception e) {
            //если произошла ошибка и обход завершить не удалось, изменять
            //статус на FAILED и вносить в поле last_error понятную
            //информацию о произошедшей ошибке.
            //currentSite = siteRepo.findByUrl(urlSiteString);
            log.error("Exception при обработке сайта: " + e.getMessage());
            //stopFlag = true;
            currentSite.setStatus(StatusType.FAILED);
            currentSite.setLast_error(e.getMessage());
            currentSite.setStatusTime(new java.util.Date());
            siteRepo.save(currentSite);
        }
    }

}
