package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

//    private final Random random = new Random();
    private final SitesList sites;
    private final RepoService repoService;

    @Override
    public StatisticsResponse getStatistics() {
//        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
//        String[] errors = {
//                "Ошибка индексации: главная страница сайта не доступна",
//                "Ошибка индексации: сайт не доступен",
//                "Нет ошибок"
//        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            SiteEntity siteEntity = repoService.findSite(site.getUrl(), site.getName());

            item.setName(site.getName());
            item.setUrl(site.getUrl());

            //int pages = random.nextInt(1_000);
            int pages = repoService.countPagesOnSite(siteEntity);
            //int lemmas = pages * random.nextInt(1_000);
            int lemmas = repoService.countLemmasOnSite(siteEntity);

            item.setPages(pages);
            item.setLemmas(lemmas);

            //item.setStatus(statuses[i % 3]);
            item.setStatus(siteEntity.getStatus().toString());

            //item.setError(errors[1 % 3]);
            item.setError(siteEntity.getLast_error());

            //item.setStatusTime(System.currentTimeMillis() -
            //        (random.nextInt(10_000)));
            item.setStatusTime(siteEntity.getStatusTime().getTime());

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
