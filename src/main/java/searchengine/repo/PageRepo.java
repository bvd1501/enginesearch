package searchengine.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Transactional
    long countBySite(SiteEntity site);


}
