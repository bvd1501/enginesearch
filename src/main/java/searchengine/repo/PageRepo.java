package searchengine.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Transactional
    Optional<PageEntity> findBySite_UrlAndPath(String url, String path);

    @Modifying
    @Transactional
    @Query("update PageEntity p set p.code = ?1, p.content = ?2 where p.id = ?3")
    int updateCodeAndContentById(Integer code, String content, Integer id);

    @Transactional
    long countBySite(SiteEntity site);

    @Transactional
    long countBySiteAndPath(SiteEntity siteEntity, String path);
}
