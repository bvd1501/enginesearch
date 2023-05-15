package searchengine.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

@Repository
public interface SiteRepo extends CrudRepository<SiteEntity, Integer> {

  

    @Modifying
    @Transactional
    long deleteByUrlAndName(String url, String name);


}
