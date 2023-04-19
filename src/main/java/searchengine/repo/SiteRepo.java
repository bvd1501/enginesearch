package searchengine.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

import java.util.Date;
import java.util.Optional;

@Repository
public interface SiteRepo extends CrudRepository<SiteEntity, Integer> {
    @Transactional
    @Modifying
    @Query("update SiteEntity s set s.statusTime = ?1, s.last_error = ?2 where s.id = ?3")
    void updateStatusTimeAndLast_errorById(Date statusTime, String last_error, Integer id);
    @Transactional
    Optional<SiteEntity> findByUrlContains(String url);

    @Modifying
    @Transactional
    long deleteByUrlAndName(String url, String name);

}
