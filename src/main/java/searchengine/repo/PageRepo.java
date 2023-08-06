package searchengine.repo;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Modifying
    @Query("update PageEntity p set p.code = ?1, p.content = ?2")
    void updateCodeAndContentBy(Integer code, String content);
//    @Transactional
//    Optional<PageEntity> findBySite_IdAndPath(Integer id, String path);


//    @Query("select p from PageEntity p where p.site.id = ?1 and p.path = ?2")
//    Optional<PageEntity> findBySite_IdAndPath(Integer id, String path);

    @Transactional
    Optional<PageEntity> findBySiteAndPath(SiteEntity site, String path);

    @Transactional
    long countBySite(SiteEntity site);


}
