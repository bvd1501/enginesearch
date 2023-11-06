package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.*;

@Service
@Slf4j
//@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    //private final ApplicationContext context;
    private final RepoService repoService;
    private final LemmaService lemmaService;

    private final JsoupCfg jsoupCfg;

    private final SitesList siteList;

     @Autowired
    public SearchServiceImpl(ApplicationContext context) {
        //this.context = context;
        this.repoService = context.getBean(RepoService.class);
        this.lemmaService = context.getBean(LemmaService.class);
        this.siteList = context.getBean(SitesList.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
    }


    @Override
    public SearchResponse getSearch(String query, String site, int offset, int limit) {
        //Определение списка сайтов, по которым производится поиск
        List<SiteEntity> siteEntities = siteIndexedList(site);
        if (siteEntities.isEmpty()) {return new SearchResponse("Нет готового индекса");}

        //Получаем все леммы запроса
        Map<String, Integer> lemmasSearched = lemmaService.lemmaCount(query);
        if (lemmasSearched.size()<1) {return new SearchResponse("Пустой запрос");}

        //Ищем леммы в базе
        Set<LemmaEntity> lemmaEntitySet = new TreeSet<>(new LemmaComparator());
        lemmaEntitySet.addAll(getLemmaEntity(siteEntities, lemmasSearched.keySet()));

        log.info("Леммы в базе: " + lemmaEntitySet.stream().map(l->{
            return ("\n" + l.getLemma()+" " + l.getSite().getName() + " " + l.getFrequency());
        }).toList());

        return new SearchResponse("Ошибочка. Поиск не настроен");
    }

    private Set<LemmaEntity> getLemmaEntity(List<SiteEntity> siteEntities, Set<String> lemmas) {
        TreeSet<LemmaEntity> resultLemmaSet = new TreeSet<>(new LemmaComparator());
        resultLemmaSet.addAll(repoService.findQueryLemmas(siteEntities, lemmas));
        if (resultLemmaSet.size()<1) {return resultLemmaSet;}
        LemmaEntity lemmaEntityMaxFreq = new LemmaEntity();
        lemmaEntityMaxFreq.setFrequency(maxFreq(siteEntities));
        return  resultLemmaSet.headSet(lemmaEntityMaxFreq);
    }

    private Integer maxFreq(List<SiteEntity> siteEntities) {
        int max=Integer.MIN_VALUE;
        for (SiteEntity s : siteEntities) {
            int count = repoService.countPagesOnSite(s);
            max = Math.max(max, count);
        }
        max = 1 + max * jsoupCfg.getMaxFreqPercent() / 100;
        return max;
    }


    /**
     * @param site - URL сайта, на котором выполняется поиск, если NULL, то о всем ПРОИНДЕКСИРОВАННЫМ сайтам
     * @return - список принденксированных сайтов, на которых будет осуществляться поиск. Может состоять из одного
     * элемента (например если задан site и он проиндексирован), группы элементов (не более общего списка сайтов,
     * заданных в конфигурации и проиндексированных) или NULL, если нет проиндесированных сайтов, по которым можно
     * осуществлять поиск
     */
    private List<SiteEntity> siteIndexedList(String site) {
        List<String> sitesURL = new ArrayList<>();
        if (site!=null) {
            sitesURL.add(site);
        } else {
            sitesURL.addAll(siteList.getSites().stream().map(Site::getUrl).toList());
        }
        return new ArrayList<>(repoService.findIndexedSite(sitesURL));
    }
}
