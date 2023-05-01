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
    @Query("update SiteEntity s set s.statusTime = ?1, s.last_error = ?2 where s.url = ?3")
    void updateStatusTimeAndLast_errorByUrl(Date statusTime, String last_error, String url);

    @Transactional
    @Modifying
    @Query("update SiteEntity s set s.status = ?1, s.statusTime = ?2, s.last_error = ?3 where s.url = ?4")
    void updateStatusAndStatusTimeAndLast_errorByUrl(StatusType status, Date statusTime, String last_error, String url);
    @Transactional
    @Modifying
    @Query("update SiteEntity s set s.statusTime = ?1 where s.id = ?2")
    int updateStatusTimeById(Date statusTime, Integer id);
    SiteEntity findByUrl(String url);

    @Transactional
    @Modifying
    @Query("update SiteEntity s set s.statusTime = ?1, s.last_error = ?2 where s.id = ?3")
    void updateStatusTimeAndLast_errorById(Date statusTime, String last_error, Integer id);

    @Modifying
    @Transactional
    long deleteByUrlAndName(String url, String name);

}
