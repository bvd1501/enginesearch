package searchengine.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface SiteRepo extends JpaRepository<SiteEntity, Integer> {
    @Query("select s from SiteEntity s where s.status = ?1 and s.url in ?2")
    List<SiteEntity> findByStatusAndUrlIn(StatusType status, Collection<String> urls);



    //@Transactional
    @Modifying
    @Async
    @Query("update SiteEntity s set s.statusTime = :statusTime, s.last_error = :last_error where s.id = :id")
    void updateStatusTimeAndLast_errorById(@Param("statusTime") Date statusTime, @Param("last_error") String last_error, @Param("id") Integer id);

    @Query("select s from SiteEntity s where s.url = ?1 and s.name = ?2")
    Optional<SiteEntity> findByUrlAndName(String url, String name);

    @Modifying
    @Async
    @Query("update SiteEntity s set s.statusTime = :statusTime where s.id = :id")
    //@Transactional
    void updateStatusTimeById(@Param("statusTime") Date statusTime, @Param("id") Integer id);


    @Modifying
    //@Transactional
    void deleteByUrlAndName(String url, String name);



}
