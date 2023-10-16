package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repo.IndexRepo;
import searchengine.repo.LemmaRepo;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseServiceImpl implements DatabaseService{
    @Autowired
    private final SiteRepo siteRepo;
    @Autowired
    private final PageRepo pageRepo;
    @Autowired
    private final LemmaRepo lemmaRepo;
    @Autowired
    private final IndexRepo indexRepo;


    /**
     * Создание записи сайта. Если сайт уже был в базе, то вся информация, связанныая с ним,
     * предварительно удаляется
     * @param urlSite - адрес главной страницы сайта
     * @param nameSite - имя сайта
     * @return site - экземпляр сущности SiteEntity, сохраненный в БД
     */
    @Override
    @Transactional
    public SiteEntity initSite(String urlSite, String nameSite) {
        log.info("Clean data on site " + nameSite);
        siteRepo.deleteByUrlAndName(urlSite, nameSite);
        return siteRepo.save(new SiteEntity(urlSite, nameSite));
    }

    /**
     * Запись информации о завершении обхода страниц сайта
     * @param site - сайт, данные которого необходимо обновить     *
     * @return - количество страниц сайта в БД
     */
    @Override
    @Transactional
    public long endSiteIndex(SiteEntity site) {
        Optional<SiteEntity> existSite = siteRepo.findById(site.getId());
        if (existSite.isEmpty()) {
            log.error(site.getName() + " is missing in base. Create it");
            siteRepo.save(site);
            return 0;
        }
        long result = pageRepo.countBySite_Id(site.getId());
        if (site.getLast_error() == null) {
            site.setLast_error(existSite.get().getLast_error());
        }
        site.setStatus(site.getLast_error()!=null ? StatusType.FAILED : StatusType.INDEXED);
        site.setLast_error("Found " + result + " pages. Error: " + site.getLast_error());
        site.setStatusTime(new java.util.Date());
        siteRepo.save(site);
        return result;
    }

    /**
     * Запись информации о произошедшей ошибке при индексации сайта
     * @param site - индексируемый сайт
     * @param error - описание произошедшей ошибки
     */
    @Override
    @Transactional
    public void updateLastErrorOnSite(SiteEntity site, String error) {
        siteRepo.updateStatusTimeAndLast_errorById(new java.util.Date(), error, site.getId());
    }

    /**
     * Поиск страницы в БД
     * @param page - страница, которая ищется в базе     *
     * @return - true если страница найдена, иначе false
     */
    @Override
    @Transactional
    public boolean existPage(PageEntity page) {
        return  pageRepo.existsBySite_IdAndPath(page.getSite().getId(), page.getPath());
    }

    /**
     * Поиск сайта в БД
     * @param url - адрес главной страницы
     * @param name - имя (описание) сайта
     * @return - новый (если ранее не существовал), либо существующий объект SiteEntity из БДыы
     */
    @Override
    @Transactional
    public SiteEntity findSite(String url, String name) {
        Optional<SiteEntity> siteEntityOptional = siteRepo.findByUrlAndName(url, name);
        if (!siteEntityOptional.isPresent()) {
            return siteRepo.save(new SiteEntity(url, name));
        }
        siteEntityOptional.get().setLast_error(null);
        return siteRepo.save(siteEntityOptional.get());
    }

    /**
     * Очистка БД от информации по странице
     * @param pageEntity - страница сайта, которая обрабатывается
     */
    @Override
    @Transactional
    public void cleanPage(PageEntity pageEntity) {
        if (!existPage(pageEntity)) {return;}
        PageEntity pageInBase = pageRepo.findBySite_IdAndPath(pageEntity.getSite().getId()
                , pageEntity.getPath()).get();
        Set<IndexEntity> indexInBase = indexRepo.findByPage_Id(pageInBase.getId());
        Set<LemmaEntity> lemmaInBase = lemmaRepo.findByIdIn(
            indexInBase.stream()
                    .map(idx ->idx.getLemma().getId())
                    .collect(Collectors.toSet()));
        lemmaInBase.forEach(l->{
            l.setFrequency(l.getFrequency()-1);
            if (l.getFrequency()<1) {
                lemmaRepo.deleteById(l.getId());
            } else {
                lemmaRepo.save(l);
            }});
        pageRepo.deleteById(pageInBase.getId());
    }

    /**
     * Внесение результатов индексации страницы в БД
     * @param page - проиндексированная страница
     * @param lemmaMap - леммы, найденные на странице
     * @return - false, если страница уже есть в базе, иначе true
     * ключ synchronized - обязателен! Без недо возможны deadlock
     */
    @Override
    @Transactional
    public synchronized boolean saveUsedPage(PageEntity page, Map<String, Integer> lemmaMap) {
        if (page==null) {return false;}
        if (pageRepo.insertNewPage(page) !=1)  {return false;}
        page = pageRepo.findBySite_IdAndPath(page.getSite().getId(), page.getPath()).get();
        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
            LemmaEntity lemmaEntity = new LemmaEntity(page.getSite(), entry.getKey());
            lemmaRepo.insertOrUpdate(lemmaEntity);
            Optional<LemmaEntity> lemmaEntityOptional =
                    lemmaRepo.findBySite_IdAndLemma(page.getSite().getId()
                            , entry.getKey());
            if (lemmaEntityOptional.isPresent()) {
                Integer rank = entry.getValue();
                indexRepo.save(new IndexEntity(page, lemmaEntityOptional.get(), rank));
            }
        }
        siteRepo.updateStatusTimeById(new java.util.Date(), page.getSite().getId());
        return true;
    }
}
