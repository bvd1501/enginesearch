package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final ExecutorService taskSitesIndexing = Executors.newCachedThreadPool();
    private final List<Future<?>> futureList = new ArrayList<>();

    @Override
    public IndexingResponse getStartIndexing() {
        if (isRunning()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        sites.getSites().forEach(s->{
            Future<?> future = taskSitesIndexing.submit(()->
                    siteIndexing(s.getUrl(), s.getName()));
            futureList.add(future);
        });
        log.info("Start indexing threads for all sites");
        taskSitesIndexing.shutdown();
        return new IndexingResponse(true);
    }

    @Override
    public IndexingResponse getStopIndexing() {
        if (!isRunning()) {
            return new IndexingResponse(false, "Индексация не запущена");
        }
        //TODO остановка потоков индексации
        taskSitesIndexing.shutdownNow();
        log.info("Остановка индексации сайтов");
        return new IndexingResponse(true);
    }

    private boolean isRunning() {
        //Возвращаем true, если хоть один поток обработки сайтов еще работает
        for (Future<?> futureItem : futureList) {
            if (!futureItem.isDone()) {return true;}
        }
        return false;
    }


    private void siteIndexing(String url, String name) {
        long startTime = System.currentTimeMillis();
        log.info("Start " + Thread.currentThread().getName() + " :" + name);

        siteRepo.deleteByUrlAndName(url, name);

        SiteEntity currentSite = siteRepo.save(new SiteEntity(url, name));
        URI uri = URI.create(url);
        if (uri.getPath().isEmpty()) { uri = URI.create(url + "/");}
//TODO Поиск страниц сайта
        ////        PageIndex pageIndex = new PageIndex()
//        //PageIndex.onePageIndex(uri);
//
        currentSite.setStatus(StatusType.INDEXED);
        currentSite.setStatusTime(new java.util.Date());
        siteRepo.save(currentSite);
        long result = pageRepo.countBySite(currentSite);
        log.info("Сайт " + name +
                ", найдено страниц " + result +
                ", затрачено " + (System.currentTimeMillis() - startTime) + " мс");
    }

}
