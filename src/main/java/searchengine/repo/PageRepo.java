package searchengine.repo;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

public interface PageRepo extends CrudRepository<PageEntity, Integer> {

    @Transactional
    Optional<PageEntity> findBySiteAndPath(SiteEntity site, String path);


    @Transactional
    long countBySite(SiteEntity site);


}
