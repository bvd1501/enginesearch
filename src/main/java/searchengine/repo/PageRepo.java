package searchengine.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface PageRepo extends JpaRepository<PageEntity, Integer> {
    long countBySite(SiteEntity site);
    @Query("select p from PageEntity p where p.site.id = ?1 and p.path = ?2")
    Optional<PageEntity> findBySite_IdAndPath(Integer id, String path);
    //@Transactional
    boolean existsBySite_IdAndPath(Integer id, String path);

    //@Transactional
    long countBySite_Id(Integer id);


    @Modifying
    @Query(value = "INSERT IGNORE INTO page (site_id, path, code, content) " +
            "values (:#{#page.site.id}, :#{#page.path}, :#{#page.code}, :#{#page.content})",
            nativeQuery = true)
    int insertNewPage(
            @Param("page") PageEntity pageEntity);


}
