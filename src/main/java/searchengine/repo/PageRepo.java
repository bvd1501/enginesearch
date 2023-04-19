package searchengine.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Transactional
    long countBySite_NameContainsAndPath(String name, String path);

    @Transactional
    long countBySite(SiteEntity site);

    @Transactional
    long countBySiteAndPath(SiteEntity siteEntity, String path);
}
