package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseServiceImpl implements DatabaseService{
    @Autowired
    private final SiteRepo siteRepo;
    @Autowired
    private final PageRepo pageRepo;


    /**
     * Создание записи сайта. Если сайт уже был в базе, то вся информация, связанныая с ним,
     * предварительно удаляется
     * @param urlSite - адрес главной страницы сайта
     * @param nameSite - имя сайта
     * @return site - экземпляр сущности SiteEntity, сохраненный в БД
     */
    @Override
    //@Transactional
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
    //@Transactional
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
    //@Transactional (timeout = 3000, isolation = Isolation.READ_COMMITTED)
    public void updateLastErrorOnSite(SiteEntity site, String error) {
        site.setLast_error(error);
        site.setStatusTime(new java.util.Date());
        siteRepo.save(site);
    }

    /**
     * Поиск страницы в бД
     * @param page - страница, которая ищется в базе     *
     * @return - true если страница найдена, иначе false
     */
    @Override
    public boolean existPage(PageEntity page) {
        return  pageRepo.existsBySite_IdAndPath(page.getSite().getId(), page.getPath());
    }

    /**
     *
     *
     * @param url
     * @return
     */
    @Override
    public SiteEntity findSiteByPageUrl(String url) {
        Optional site = siteRepo.findByUrl(url);
        return null;
    }

    /**
     * Внесение результатов индексации страницы в БД
     * @param page - проиндексированная страница
     * @param lemmaMap - леммы, найденные на странице
     * @return - false, если страница уже есть в базе, иначе true
     */
    @Override
    @Transactional(timeout = 5000)
    public synchronized boolean savePage(PageEntity page, Map<String, Integer> lemmaMap) {
        //if (existPage(site, page.getPath())) {return false;}
        if(pageRepo.existsBySite_IdAndPath(page.getSite().getId(), page.getPath())) {return false;}
        pageRepo.save(page);
        //TODO обновление данных о леммах в таблицах lemma и index
        siteRepo.updateStatusTimeById(new java.util.Date(), page.getSite().getId());
        return true;
    }



}
