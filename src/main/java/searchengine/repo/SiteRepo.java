package searchengine.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
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
    @Query("update SiteEntity s set s.statusTime = :statusTime where s.id = :id")
    void updateStatusTimeById(@Param("statusTime") Date statusTime, @Param("id") Integer id);
    @Transactional
    @Modifying
    @Query("""
            update SiteEntity s set s.status = :status, s.last_error = :last_error, s.statusTime = :statusTime
            where s.id = :id""")
    void updateStatusAndLast_errorAndStatusTimeById(@Param("status") StatusType status, @Param("last_error") String last_error, @Param("statusTime") Date statusTime, @Param("id") Integer id);


    @Modifying
    @Transactional
    long deleteByUrlAndName(String url, String name);


}
